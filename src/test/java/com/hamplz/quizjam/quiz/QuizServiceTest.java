package com.hamplz.quizjam.quiz;

import com.hamplz.quizjam.openai.OpenAiService;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.dto.QuizResponse;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.quiz.service.QuizQueryService;
import com.hamplz.quizjam.quiz.service.QuizService;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import com.hamplz.quizjam.util.PdfUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Create and save quiz from PDF text")
    void generateQuizFromPdf_success() throws Exception {
        Long userId = 1L;
        User user = User.create("host", 12345L);
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Unit Test Quiz", "multiple-choice", "easy", 3, 60
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz quiz = invocation.getArgument(0);
            ReflectionTestUtils.setField(quiz, "id", 1L);
            return quiz;
        });

        MockMultipartFile pdfFile = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "fake-content".getBytes(StandardCharsets.UTF_8)
        );

        OpenAiResponse openAiResponse = new OpenAiResponse(
            List.of(
                new OpenAiResponse.QuestionForm("Question 1", Map.of("A", "Option 1", "B", "Option 2"), "Hint 1"),
                new OpenAiResponse.QuestionForm("Question 2", Map.of("A", "Option 1", "B", "Option 2"), "Hint 2"),
                new OpenAiResponse.QuestionForm("Question 3", Map.of("A", "Option 1", "B", "Option 2"), "Hint 3")
            ),
            List.of(
                new OpenAiResponse.AnswerForm("A", "Explanation 1"),
                new OpenAiResponse.AnswerForm("B", "Explanation 2"),
                new OpenAiResponse.AnswerForm("A", "Explanation 3")
            )
        );

        when(openAiService.generateQuizFromPdf(eq(userId), anyString(), eq(quizCreateFormat)))
            .thenReturn(openAiResponse);

        try (MockedStatic<PdfUtil> mockedPdfUtil = mockStatic(PdfUtil.class)) {
            mockedPdfUtil.when(() -> PdfUtil.extractText(any(File.class)))
                .thenReturn("PDF text for test.");

            QuizService quizService = new QuizService(openAiService, quizQueryService, quizRepository, userRepository);

            QuizResponse result = quizService.createQuiz(userId, quizCreateFormat, pdfFile);

            assertThat(result).isNotNull();
            assertThat(result.quizId()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("Unit Test Quiz");
            assertThat(result.questionCount()).isEqualTo(3);
            verify(openAiService, times(1)).generateQuizFromPdf(eq(userId), anyString(), eq(quizCreateFormat));
            verify(quizRepository, times(1)).save(any(Quiz.class));
        }
    }
}
