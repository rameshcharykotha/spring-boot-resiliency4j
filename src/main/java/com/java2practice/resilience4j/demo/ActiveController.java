package com.java2practice.resilience4j.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/active")
public class ActiveController {

    Logger logger = LoggerFactory.getLogger(ActiveController.class);
    private final RestTemplate restTemplate;

    @Autowired
    public ActiveController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    @CircuitBreaker(name = "dummyjson-window", fallbackMethod = "getActiveFallback")
    public ResponseEntity<String> getActive() {
        logger.info("Invoking upstream service from /active endpoint (annotation-based)");
        String url = "https://dummyjson.com/test";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        logger.info("Calling upstream URL: {}", url);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        logger.info("Upstream returned status={}, body={}", resp.getStatusCode(), resp.getBody());
        return resp;
    }

    // New endpoint: uses a COUNT_BASED circuit breaker configured via application.yaml
    @GetMapping("/count")
    @CircuitBreaker(name = "dummyjson-count", fallbackMethod = "getActiveFallback")
    public ResponseEntity<String> getActiveCountBased() {
        logger.info("Invoking count-based upstream service from /active/count endpoint (annotation-based)");
        String url = "https://dummyjson.com/test";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        logger.info("[count-based] Calling upstream URL: {}", url);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        logger.info("[count-based] Upstream returned status={}, body={}", resp.getStatusCode(), resp.getBody());
        return resp;
    }

    // Fallback method used by annotated circuit breakers. Signature: same return type + Throwable as last param
    public ResponseEntity<String> getActiveFallback(Throwable t) {
        logger.error("Annotated circuit breaker fallback invoked. Reason:", t);
        return getLocal();
    }

    // Local endpoint that returns an 'up' response when upstream is unavailable
    @GetMapping("/local")
    public ResponseEntity<String> getLocal() {
        logger.info("Invoking local fallback endpoint /active/local");
        String body = "{ \"status\": \"up\", \"source\": \"local-fallback\" }";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        logger.info("Returning local fallback response: {}", body);
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
