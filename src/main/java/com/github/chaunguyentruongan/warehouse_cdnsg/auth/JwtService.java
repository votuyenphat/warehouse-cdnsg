package com.github.chaunguyentruongan.warehouse_cdnsg.auth;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.chaunguyentruongan.warehouse_cdnsg.auth.dto.LoginRequest;
import com.github.chaunguyentruongan.warehouse_cdnsg.auth.dto.LoginResponse;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.TokenException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.User;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.expire}")
    private String expiredTime;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) throws JOSEException {
        User user = null;
        try {
            user = userService.findByEmailOrUsername(request.getRequest());
        } catch (Exception e) {
            throw new org.springframework.security.authentication.BadCredentialsException("Sai tài khoản hoặc mật khẩu!");
        }

        if (user == null || !user.isActive()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa!");
        }

        String dbPassword = user.getPassword();

        if (!passwordEncoder.matches(request.getPassword(), dbPassword)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Sai tài khoản hoặc mật khẩu!");
        }

        return genToken(user);
    }

    private LoginResponse genToken(User user) throws JOSEException {
        JWSSigner signer = new MACSigner(secretKey.getBytes());
        List<String> permissionNames = user.getPermissions() != null
                ? user.getPermissions().stream()
                        .map(com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.Permission::getName)
                        .toList()
                : List.of();

        JWTClaimsSet accessTokenClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("cdnsg-qlk")
                .expirationTime(new Date(System.currentTimeMillis() + Long.parseLong(expiredTime) * 1000))
                .claim("role", user.getRole().getName().toUpperCase())
                .claim("permission", permissionNames)
                .build();

        JWTClaimsSet refreshTokenClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("cdnsg-qlk")
                .expirationTime(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .build();

        SignedJWT accessToken = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), accessTokenClaimsSet);
        SignedJWT refreshToken = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), refreshTokenClaimsSet);

        accessToken.sign(signer);
        refreshToken.sign(signer);

        String accessTokenString = accessToken.serialize();
        String refreshTokenString = refreshToken.serialize();

        LoginResponse response = new LoginResponse();
        response.setToken(accessTokenString);
        response.setRefreshToken(refreshTokenString);

        return response;
    }

    public LoginResponse refresh(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

        if (!signedJWT.verify(verifier)) {
            throw new TokenException("Invalid signature");
        }

        if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new TokenException("Token expired");
        }

        var claimsSet = signedJWT.getJWTClaimsSet();
        String email = claimsSet.getSubject();

        User user = userService.findByEmail(email);

        return genToken(user);
    }

    public JWTClaimsSet validateToken(String token) throws TokenException, ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

        if (!signedJWT.verify(verifier)) {
            throw new TokenException("Invalid signature");
        }

        if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new TokenException("Token expired");
        }

        return signedJWT.getJWTClaimsSet();
    }
}
