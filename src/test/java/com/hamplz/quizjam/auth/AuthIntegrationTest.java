package com.hamplz.quizjam.auth;

import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import com.hamplz.quizjam.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

//    ✅ Bean 이름 지정은 불필요 — 타입으로 Mock 등록
//    @MockBean(name = "kakaoRestClient")
//    private RestClient kakaoRestClient;
//
//    @Test
//    @DisplayName("✅ 카카오 로그인 성공 시 쿠키 발급 및 사용자 저장이 이루어진다")
//    void kakaoLoginSuccess_setsCookiesAndSavesUser() throws Exception {
//        // ✅ 1️⃣ Mock Kakao AccessToken 응답
//        var fakeTokenResponse = new KakaoTokenResponse(
//            "bearer",
//            "access_token_dummy",
//            21599,
//            "refresh_token_dummy",
//            5184000,
//            "account_email profile_nickname"
//        );
//
//        // ✅ 2️⃣ Mock Kakao UserInfo 응답
//        var fakeProfile = new KakaoUserInfoResponse.KakaoProfile("햄테스트");
//        var fakeAccount = new KakaoUserInfoResponse.KakaoAccount(fakeProfile);
//        var fakeUserResponse = new KakaoUserInfoResponse(12345L, fakeAccount);
//
//        // ✅ 3️⃣ RestClient Mock 체인 설정
//        var postSpec = Mockito.mock(RestClient.RequestBodyUriSpec.class);
//        var retrieveSpec = Mockito.mock(RestClient.ResponseSpec.class);
//
//        Mockito.when(kakaoRestClient.post()).thenReturn(postSpec);
//        Mockito.when(postSpec.uri(Mockito.anyString())).thenReturn(postSpec);
//        Mockito.when(postSpec.body(Mockito.any())).thenReturn(postSpec);
//        Mockito.when(postSpec.retrieve()).thenReturn(retrieveSpec);
//
//        // 첫 번째 호출 → Token, 두 번째 호출 → UserInfo 응답
//        Mockito.when(retrieveSpec.body(Mockito.eq(KakaoTokenResponse.class)))
//            .thenReturn(fakeTokenResponse);
//        Mockito.when(retrieveSpec.body(Mockito.eq(KakaoUserInfoResponse.class)))
//            .thenReturn(fakeUserResponse);
//
//        // ✅ 4️⃣ 요청 실행
//        MvcResult result = mockMvc.perform(get("/api/kakao/callback")
//                .param("code", "dummyCode")
//                .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().is3xxRedirection())
//            .andReturn();
//
//        // ✅ 5️⃣ 쿠키 검증
//        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
//        assertThat(setCookieHeader).contains("accessToken");
//
//        // ✅ 6️⃣ DB 검증
//        var savedUser = userRepository.findByKakaoId(12345L);
//        assertThat(savedUser).isPresent();
//        assertThat(savedUser.get().getNickname()).isEqualTo("햄테스트");
//    }

    @Test
    @DisplayName("2️⃣ 인증된 쿠키로 사용자 정보 요청 성공")
    void access_withValidCookie() throws Exception {
        // ✅ 실제 유저 저장
        var user = userRepository.save(new User("테스트유저", 999L));

        // ✅ 실제 유효한 JWT 생성
        String token = jwtUtil.generateAccessToken(user);

        // ✅ HttpOnly 쿠키 추가
        MockCookie accessCookie = new MockCookie("accessToken", token);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");

        mockMvc.perform(get("/api/users/me")
                .cookie(accessCookie))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3️⃣ 쿠키가 없으면 401 Unauthorized 반환")
    void access_withoutCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }
}
