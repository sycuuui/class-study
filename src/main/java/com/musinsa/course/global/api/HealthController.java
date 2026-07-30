package com.musinsa.course.global.api;

import com.musinsa.course.data.InMemoryStore;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
public class HealthController {
    private final InMemoryStore store;

    public HealthController(InMemoryStore store) {
        this.store = store;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        if (!store.isReady()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", Map.of(
                        "code", 700,
                        "message", "서비스 준비 중",
                        "details", Map.of()
                    )
                ));
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
