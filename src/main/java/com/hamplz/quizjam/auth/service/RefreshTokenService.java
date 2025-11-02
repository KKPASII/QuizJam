package com.hamplz.quizjam.auth.service;

import com.hamplz.quizjam.auth.RefreshTokenRepository;
import com.hamplz.quizjam.auth.dto.AuthToken;
import com.hamplz.quizjam.auth.entity.RefreshToken;
import com.hamplz.quizjam.util.JwtUtil;
import com.hamplz.quizjam.util.TokenIssuer;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;

    public RefreshTokenService(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository, TokenIssuer tokenIssuer) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthToken newTokenSet(String refreshToken) {
        Claims claims = jwtUtil.getClaims(refreshToken);
        String tokenId = claims.getId();
        
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰"));

        if (storedRefreshToken.isExpired() || storedRefreshToken.isRevoked()) {
            refreshTokenRepository.deleteById(tokenId);
            throw new RuntimeException("만료된 토큰");
        }

        storedRefreshToken.revoke();

        return tokenIssuer.issue(storedRefreshToken.getUser(), storedRefreshToken.getDeviceType(), storedRefreshToken.getUserAgent());
    }
}
