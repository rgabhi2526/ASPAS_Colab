package com.aspas.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stops default browser requests for icons from polluting logs as 404s.
 */
@RestController
@CrossOrigin(origins = "*")
public class FaviconController {

    @GetMapping({"/favicon.ico", "/apple-touch-icon.png"})
    public ResponseEntity<Void> noopIcon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
