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
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

@RestController
@RequestMapping("/active")
public class ActiveController {

    Logger logger = LoggerFactory.getLogger(ActiveController.class);
    private final RestTemplate restTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    public ActiveController(RestTemplate restTemplate, CircuitBreakerFactory circuitBreakerFactory) {
        this.restTemplate = restTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping
    public ResponseEntity<String> getActive() {
        logger.info("Invoking upstream service from /active endpoint");
        String url = "https://dummyjson.com/test";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Use programmatic circuit breaker to avoid relying on AOP/annotation proxies
        ResponseEntity<String> response = circuitBreakerFactory.create("dummyjson").run(
                () -> {
                    logger.info("Calling upstream URL: {}", url);
                    ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
                    logger.info("Upstream returned status={}, body={}", resp.getStatusCode(), resp.getBody());
                    return resp;
                },
                throwable -> {
                    logger.error("Upstream call failed inside circuit breaker. Reason: {}", throwable.toString());
                    return getLocal();
                }
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
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
