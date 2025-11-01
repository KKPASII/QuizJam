package com.hamplz.quizjam.auth;

import com.hamplz.quizjam.auth.dto.KakaoTokenCreationRequest;
import com.hamplz.quizjam.auth.dto.KakaoTokenResponse;
import com.hamplz.quizjam.auth.dto.KakaoUserInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoApiClient {
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Logger log = LoggerFactory.getLogger(KakaoApiClient.class);

    //private static final String SEND_MESSAGE_TO_ME_PATH = "/v2/api/talk/memo/default/send";
    private static final String TOKEN_CREATION_PATH = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_PATH = "https://kapi.kakao.com/v2/user/me";

    private final RestClient kakaoRestClient;

    public KakaoApiClient(RestClient kakaoRestClient) {
        this.kakaoRestClient = kakaoRestClient;
    }

    // 토큰 발급 요청
    public KakaoTokenResponse createToken(KakaoTokenCreationRequest request) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", request.grantType());
        requestBody.add("client_id", request.clientId());
        requestBody.add("redirect_uri", request.redirectUri());
        requestBody.add("code", request.code());

        return kakaoRestClient.post()
                .uri(TOKEN_CREATION_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    // 사용자 정보 요청
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        return kakaoRestClient.get()
                .uri(USER_INFO_PATH)
                .header("Authorization", BEARER_PREFIX + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }

//    /**
//     * '나에게 보내기' API를 호출하여 카카오톡 메시지를 전송합니다.
//     *
//     * @param accessToken        사용자의 카카오 액세스 토큰
//     * @param templateObjectJson 메시지 템플릿 내용을 담은 JSON 형식의 문자열
//     */
//    public void sendMessageToMe(String accessToken, String templateObjectJson) {
//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.add("template_object", templateObjectJson);
//
//        // API용 클라이언트를 사용하여 메시지 전송 API 호출
//        // RestClient는 기본적으로 UTF-8을 사용합니다.
//        String response = kakaoRestClient.post()
//                .uri("https://kapi.kakao.com" + SEND_MESSAGE_TO_ME_PATH) // 전체 경로 사용
//                .header("Authorization", BEARER_PREFIX + accessToken)
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body(formData)
//                .retrieve()
//                .body(String.class);
//
//        // 성공 시 {"result_code":0} 응답이 오지만, 여기서는 간단히 로그로 출력
//        log.info("Kakao Message API Response: {}", response);
//    }
}
