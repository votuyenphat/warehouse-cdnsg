package com.github.chaunguyentruongan.warehouse_cdnsg.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.chaunguyentruongan.warehouse_cdnsg.auth.dto.LoginRequest;
import com.github.chaunguyentruongan.warehouse_cdnsg.auth.dto.LoginResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws Exception {
        LoginResponse resp = jwtService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", resp.getRefreshToken())
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                // .sameSite("Lax")
                .sameSite("None")
                .secure(true)
                .build();

        Map<String, String> token = new HashMap<>();
        token.put("access_token", resp.getToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request)
            throws Exception {

        String token = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        LoginResponse resp = jwtService.refresh(token);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", resp.getRefreshToken())
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                // .sameSite("Lax")
                .sameSite("None")
                .secure(true)
                .build();

        Map<String, String> accessToken = new HashMap<>();
        accessToken.put("access_token", resp.getToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(accessToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .path("/")
                .maxAge(0)
                // .sameSite("Lax")
                .sameSite("None")
                .secure(true)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out");
    }

}