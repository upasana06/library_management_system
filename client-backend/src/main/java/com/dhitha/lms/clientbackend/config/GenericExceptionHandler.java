package com.dhitha.lms.clientbackend.config;

import com.dhitha.lms.clientbackend.dto.ErrorDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Common Exception Handler
 *
 * @author Dhiraj
 */
@ControllerAdvice
@Log4j2
@RequiredArgsConstructor
public class GenericExceptionHandler extends ResponseEntityExceptionHandler {

  private final ObjectMapper objectMapper;

  @ExceptionHandler({FeignException.class})
  private ResponseEntity<ErrorDTO> handleFeignException(FeignException e) {
    log.error("FeignError: {}, {}", e.contentUTF8(), e);
    ErrorDTO errorDTO;
    try {
      errorDTO = objectMapper.readValue(e.contentUTF8(), ErrorDTO.class);
      log.debug("Mapped Error DTO {}", errorDTO);
    } catch (JsonProcessingException jsonProcessingException) {
      errorDTO =
          ErrorDTO.builder()
              .error("invalid_request")
              .error_description(e.contentUTF8())
              .status(e.status())
              .timestamp(LocalDateTime.now())
              .build();
    }
    return ResponseEntity.status(e.status()).body(errorDTO);
  }

  @NonNull
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      @NonNull Exception ex,
      Object body,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatus status,
      @NonNull WebRequest request) {
    log.error("handleInvalidRequest():{} -> {}", ex.getCause(), ex.getMessage());
    ErrorDTO err =
        ErrorDTO.builder()
            .error("invalid_request")
            .error_description(ex.getLocalizedMessage())
            .status(status.value())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.status(status).body(err);
  }

  @ExceptionHandler({IllegalArgumentException.class})
  private ResponseEntity<ErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErrorDTO err =
        ErrorDTO.builder()
            .error("invalid_request")
            .error_description(ex.getLocalizedMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.badRequest().body(err);
  }

  @ExceptionHandler({Exception.class})
  private ResponseEntity<ErrorDTO> handleException(Exception ex) {
    log.error("handleException():", ex);
    ErrorDTO err =
        ErrorDTO.builder()
            .error("server_error")
            .error_description("Something went wrong")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
  }

  @ExceptionHandler(AccessDeniedException.class)
  private ResponseEntity<ErrorDTO> handleAccessDeniedException(AccessDeniedException ex) {
    ErrorDTO err =
        ErrorDTO.builder()
            .error("access_denied")
            .error_description(ex.getMessage())
            .status(HttpStatus.FORBIDDEN.value())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
  }
}
