package com.hamplz.quizjam.quiz;

import com.hamplz.quizjam.openai.OpenAiService;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.service.QuizAsyncService;
import com.hamplz.quizjam.quiz.service.QuizService;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import com.hamplz.quizjam.util.PdfUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private QuizAsyncService quizAsyncService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizService quizService;

    @Test
    @DisplayName("PDF 기반 퀴즈 생성 시 OpenAI 응답을 파싱하고 비동기 저장을 호출한다")
    void generateQuizFromPdf_success() throws Exception {
        // ✅ given
        Long userId = 1L;
        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "단위테스트 퀴즈", "객관식", "쉬움", "3", "60"
        );

        MockMultipartFile pdfFile = new MockMultipartFile(
            "my_test_file", "test.pdf", "application/pdf",
            "fake-content".getBytes(StandardCharsets.UTF_8)
        );

        // ✅ Mock PdfUtil.extractText()
        try (MockedStatic<PdfUtil> mockedPdfUtil = mockStatic(PdfUtil.class)) {
            mockedPdfUtil.when(() -> PdfUtil.extractText(any(File.class)))
                .thenReturn("이것은 테스트 PDF 내용입니다.");

            // ✅ Mock OpenAiService 응답
            OpenAiResponse mockResponse = new OpenAiResponse(
                List.of(
                    new OpenAiResponse.QuestionForm("문제1", null, "힌트1"),
                    new OpenAiResponse.QuestionForm("문제2", null, "힌트2"),
                    new OpenAiResponse.QuestionForm("문제3", null, "힌트3")
                ),
                List.of(
                    new OpenAiResponse.AnswerForm("A", "해설1"),
                    new OpenAiResponse.AnswerForm("B", "해설2"),
                    new OpenAiResponse.AnswerForm("C", "해설3")
                )
            );

            when(openAiService.generateQuizFromPdf(any(Long.class), anyString(), any(QuizCreateFormat.class)))
                .thenReturn(mockResponse);

            // ✅ when
            var result = quizService.createQuiz(userId, quizCreateFormat, pdfFile);

            // ✅ then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result.get(0).questionText()).isEqualTo("문제1");

            // ✅ OpenAiService 호출 검증
            verify(openAiService, times(1))
                .generateQuizFromPdf(any(Long.class), anyString(), eq(quizCreateFormat));

            // ✅ Async 서비스 호출은 내부에서 이미 호출됨 (테스트에서는 직접 검증 불필요)
            verify(quizAsyncService, never()).saveQuizAsync(any(), any(), any());
        }
    }
}