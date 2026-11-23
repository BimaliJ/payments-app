package com.demo.app.payments.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class FraudStubController {

    @PostMapping(
            value = "/screening/v2/check",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> screen()
            throws IOException, InterruptedException {

        int outcome = ThreadLocalRandom.current().nextInt(10);

        // Simulate a slow provider (20% of requests)
        if (outcome < 2) {
            Thread.sleep(3000);
        }

        // 70% success, 30% error
        boolean success = outcome < 7;

        String file = success ? "fraud/success.xml" : "fraud/error.xml";

        ClassPathResource resource = new ClassPathResource(file);

        String xml;
        try (var input = resource.getInputStream()) {
            xml = new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        // Errors may have HTTP 200 or HTTP 500
        int status = success ? 200 : (outcome == 7 ? 200 : 500);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }
}

