package OpenSourceSW.ArbeitMate.controller;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<Map<String, String>> handleFirebase(FirebaseAuthException ex) {
        var body = Map.of(
                "error", "FIREBASE_AUTH_ERROR",
                "message", ex.getAuthErrorCode() != null
                        ? ex.getAuthErrorCode().name()
                        : ex.getMessage()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","BAD_REQUEST","message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","BAD_REQUEST","message", ex.getMessage()));
    }
}
