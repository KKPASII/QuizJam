package com.hamplz.quizjam.auth.service;

import com.hamplz.quizjam.auth.RefreshTokenRepository;
import com.hamplz.quizjam.auth.dto.AuthToken;
import com.hamplz.quizjam.auth.entity.RefreshToken;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.UnAuthorizedException;
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
        // 1️⃣ JWT 유효성 검사
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnAuthorizedException(ErrorCode.INVALID_TOKEN);
        }

        // 2️⃣ 토큰 ID 추출
        Claims claims = jwtUtil.getClaims(refreshToken);
        String tokenId = claims.getId();

        // 3️⃣ DB에서 해당 RefreshToken 조회
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new UnAuthorizedException(ErrorCode.INVALID_TOKEN));

        // 4️⃣ 만료 또는 폐기 여부 확인
        if (storedRefreshToken.isExpired() || storedRefreshToken.isRevoked()) {
            refreshTokenRepository.deleteById(tokenId);
            throw new UnAuthorizedException(ErrorCode.EXPIRED_TOKEN);
        }

        // 5️⃣ 기존 리프레시 토큰 폐기
        storedRefreshToken.revoke();
        refreshTokenRepository.save(storedRefreshToken); // ✅ DB 반영 (revoked=true)

        // 6️⃣ 새 토큰 발급
        AuthToken newTokenSet = tokenIssuer.issue(
            storedRefreshToken.getUser(),
            storedRefreshToken.getDeviceType(),
            storedRefreshToken.getUserAgent()
        );

        // 7️⃣ 새 리프레시 토큰을 DB에 저장
        Claims newClaims = jwtUtil.getClaims(newTokenSet.refreshToken());
        String newTokenId = newClaims.getId();

        RefreshToken newRefreshToken = RefreshToken.create(
            newTokenId,
            storedRefreshToken.getUser(),
            storedRefreshToken.getDeviceType(),
            storedRefreshToken.getUserAgent()
        );
        refreshTokenRepository.save(newRefreshToken);

        return newTokenSet;
    }
}
