package com.gekko.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Very small AuthController that provides a token for client-credentials flow.
 * In real deployments Apigee would manage credentials and forward validated requests to Gekko.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@RequestParam String client_id, @RequestParam String client_secret) {
        // In this simplified implementation we accept any client and return a dummy token.
        // Replace this with JWT creation & proper credential storage or remove entirely if APIGEE does auth.
        Map<String, String> resp = new HashMap<>();
        resp.put("access_token", "dummy-token-for-" + client_id);
        resp.put("token_type", "bearer");
        resp.put("expires_in", "3600");
        return ResponseEntity.ok(resp);
    }
}
