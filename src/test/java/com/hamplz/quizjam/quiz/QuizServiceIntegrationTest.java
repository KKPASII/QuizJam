package com.hamplz.quizjam.quiz;

import com.hamplz.quizjam.openai.OpenAiService;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.AnswerRepository;
import com.hamplz.quizjam.quiz.repository.QuestionRepository;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.quiz.service.QuizService;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
@SpringBootTest
@ActiveProfiles("test")
public class QuizServiceIntegrationTest {

    @Autowired private QuizService quizService;
    @Autowired private QuizRepository quizRepository;
    @Autowired private OpenAiService openAiService;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerRepository answerRepository;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 (순서 중요: 자식 → 부모)
        answerRepository.deleteAllInBatch();
        questionRepository.deleteAllInBatch();
        quizRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        user = userRepository.save(createUser());
        userRepository.flush();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("퀴즈 생성 후 Quiz, Question, Answer가 모두 영속화된다")
    void checkAllEntitiesPersisted(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Spring Framework Quiz", "객관식", "보통", "3", "120"
        );

        File pdfFile = createTestPdf(tempDir, "spring-quiz.pdf", """
        Spring is a Java framework providing dependency injection and AOP.
        """);

        MockMultipartFile multipartFile = new MockMultipartFile(
            "spring_file", "spring-quiz.pdf", "application/pdf", new FileInputStream(pdfFile)
        );

        // when
        quizService.createQuiz(user.getId(), quizCreateFormat, multipartFile);

        // ✅ 비동기 저장이 완료될 때까지 대기
        await()
            .atMost(Duration.ofSeconds(10)) // 최대 5초까지 기다림
            .pollInterval(Duration.ofMillis(200)) // 0.2초마다 체크
            .untilAsserted(() -> {
                assertThat(quizRepository.findAll()).hasSize(1);
                assertThat(questionRepository.findAll()).isNotEmpty();
                assertThat(answerRepository.findAll()).isNotEmpty();
            });

        // ✅ 검증
        List<Quiz> quizzes = quizRepository.findAll();
        List<Question> questions = questionRepository.findAll();
        List<Answer> answers = answerRepository.findAll();

        Quiz quiz = quizzes.get(0);
        Question firstQ = questions.get(0);

        // 연관관계 확인
        assertThat(firstQ.getQuiz().getId()).isEqualTo(quiz.getId());
        assertThat(firstQ.getAnswer()).isNotNull();

        System.out.println("✅ Quiz: " + quiz.getTitle());
        System.out.println("✅ Question: " + firstQ.getQuestionText());
        System.out.println("✅ Answer: " + firstQ.getAnswer().getCorrectAnswer());
    }

    @Test
    @DisplayName("실제 OpenAI API로 PDF 기반 퀴즈 생성 통합 테스트")
    void generateQuizFromPdf_withRealOpenAI(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Java Programming Quiz", "객관식", "보통", "3", "120"
        );

        String pdfText = """
        Java Programming Language
        
        Java is an object-oriented programming language developed by Sun Microsystems.
        It runs on the Java Virtual Machine (JVM) which provides platform independence.
        Key features include: encapsulation, inheritance, polymorphism, and abstraction.
        
        Spring Framework is the most popular application framework for Java.
        It provides comprehensive infrastructure support for developing Java applications.
        Core features include Dependency Injection and Aspect-Oriented Programming.
        """;

        // when
        System.out.println("🚀 Sending raw text to OpenAI...");
        OpenAiResponse result = openAiService.generateQuizFromPdf(user.getId(), pdfText, quizCreateFormat);

        System.out.println("✅ OpenAI API response received successfully!");
        System.out.println("=".repeat(60));

        // then
        assertThat(result).isNotNull();
        assertThat(result.questions()).isNotEmpty();
        assertThat(result.answers()).isNotEmpty();
        assertThat(result.questions().size()).isEqualTo(result.answers().size());
        assertThat(result.questions().size()).isEqualTo(3);

        // Print generated quiz
        System.out.println("\n📋 Generated Quiz Questions:");
        System.out.println("-".repeat(60));

        for (int i = 0; i < result.questions().size(); i++) {
            OpenAiResponse.QuestionForm q = result.questions().get(i);
            OpenAiResponse.AnswerForm a = result.answers().get(i);

            System.out.println("\nQuestion " + (i + 1) + ": " + q.questionText());

            if (q.options() != null) {
                q.options().forEach((key, value) ->
                    System.out.println("  " + key + ") " + value));
            }

            if (q.hint() != null) {
                System.out.println("  💡 Hint: " + q.hint());
            }

            System.out.println("  ✅ Answer: " + a.correctAnswer());
            System.out.println("  📖 Explanation: " + a.explanation());
        }

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    @DisplayName("객관식 퀴즈 생성")
    void generateQuizFromPdf_databaseTopic(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Database Concepts Quiz", "객관식", "어려움", "5", "180"
        );

        String pdfText = """
        Database Normalization Fundamentals
        
        First Normal Form (1NF):
        - Eliminate repeating groups
        - All attributes must contain atomic values
        - Each record must be unique
        
        Second Normal Form (2NF):
        - Must be in 1NF
        - Remove partial dependencies
        - Non-key attributes fully dependent on primary key
        
        Third Normal Form (3NF):
        - Must be in 2NF
        - Remove transitive dependencies
        - Non-key attributes independent of other non-key attributes
        
        ACID Properties:
        - Atomicity: Transactions are all-or-nothing
        - Consistency: Database remains valid after transactions
        - Isolation: Concurrent transactions don't interfere
        - Durability: Completed transactions persist permanently
        
        SQL Join Operations:
        - INNER JOIN: Returns matching records from both tables
        - LEFT JOIN: All from left table, matched from right
        - RIGHT JOIN: All from right table, matched from left
        - FULL OUTER JOIN: All records with matches in either table
        """;

        // when
        System.out.println("\n🚀 Testing with Database content...");
        OpenAiResponse result = openAiService.generateQuizFromPdf(user.getId(), pdfText, quizCreateFormat);

        // then
        assertThat(result).isNotNull();
        assertThat(result.questions()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(result.questions().size()).isEqualTo(result.answers().size());

        System.out.println("✅ Generated " + result.questions().size() + " questions");
        System.out.println("📝 Sample question: " + result.questions().get(0).questionText());
        System.out.println("📝 Sample answer: " + result.answers().get(0).correctAnswer());
    }

    @Test
    @DisplayName("객관식 퀴즈 생성 테스트")
    void generateQuizFromPdf_objectiveType(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Spring Framework Quiz", "객관식", "보통", "4", "150"
        );

        String pdfText = """
        Spring Framework Overview
    
        The Spring Framework is a powerful, feature-rich framework for building Java applications.
        It provides features such as dependency injection, inversion of control, and aspect-oriented programming (AOP).
    
        Spring Boot is an extension of Spring that simplifies application setup with embedded servers and auto-configuration.
    
        The Spring Container manages the lifecycle of beans, injecting dependencies and controlling their scope.
        Spring MVC provides a powerful Model-View-Controller architecture for web development.
        """;

        // when
        System.out.println("\n🚀 Testing Objective (Multiple-Choice) Quiz Generation...");
        OpenAiResponse result = openAiService.generateQuizFromPdf(user.getId(), pdfText, quizCreateFormat);

        // then
        assertThat(result).isNotNull();
        assertThat(result.questions()).hasSize(4);
        assertThat(result.answers()).hasSize(4);

        // 객관식은 options가 반드시 4개(A~D)여야 함
        result.questions().forEach(q -> {
            assertThat(q.options()).isNotNull();
            assertThat(q.options()).containsKeys("A", "B", "C", "D");
            assertThat(q.questionText()).isNotBlank();
        });

        // 정답은 반드시 A, B, C, D 중 하나여야 함
        result.answers().forEach(a -> {
            assertThat(a.correctAnswer()).isIn("A", "B", "C", "D");
        });

        System.out.println("✅ Objective (Multiple-Choice) quiz generated successfully!");
        System.out.println("📝 Sample question: " + result.questions().get(0).questionText());
        System.out.println("📝 Sample answer: " + result.answers().get(0).correctAnswer());
    }


    @Test
    @DisplayName("단답식 퀴즈 생성 테스트")
    void generateQuizFromPdf_subjectiveType(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Java Subjective Quiz", "단답식", "쉬움", "3", "180"
        );

        String pdfText = """
        Object-Oriented Programming Concepts
        
        Encapsulation is the bundling of data and methods that operate on that data.
        It restricts direct access to object components.
        
        Inheritance allows a class to inherit properties and methods from another class.
        It promotes code reusability and establishes a parent-child relationship.
        
        Polymorphism means one interface with multiple implementations.
        It allows objects to be treated as instances of their parent class.
        """;

        // when
        System.out.println("\n🚀 Testing Subjective Quiz Generation...");
        OpenAiResponse result = openAiService.generateQuizFromPdf(user.getId(), pdfText, quizCreateFormat);

        // then
        assertThat(result).isNotNull();
        assertThat(result.questions()).hasSize(3);
        assertThat(result.answers()).hasSize(3);

        // 단답식은 options가 null이어야 함
        result.questions().forEach(q -> {
            assertThat(q.options()).isNull();
            assertThat(q.questionText()).isNotBlank();
        });

        System.out.println("✅ Subjective quiz generated successfully");
        System.out.println("📝 Sample question: " + result.questions().get(0).questionText());
        System.out.println("📝 Sample answer: " + result.answers().get(0).correctAnswer());
    }

    @Test
    @DisplayName("OX 퀴즈 생성 테스트")
    void generateQuizFromPdf_oxType(@TempDir Path tempDir) throws Exception {
        // given
        QuizCreateFormat quizCreateFormat = new QuizCreateFormat(
            "Java OX Quiz", "OX", "쉬움", "4", "120"
        );

        String pdfText = """
        Java Programming Facts
        
        Java is a compiled language that runs on the JVM.
        The JVM provides platform independence by interpreting bytecode.
        
        Java supports multiple inheritance through classes.
        However, Java supports multiple inheritance through interfaces.
        
        String objects in Java are immutable.
        Once created, their values cannot be changed.
        """;

        // when
        System.out.println("\n🚀 Testing OX Quiz Generation...");
        OpenAiResponse result = openAiService.generateQuizFromPdf(user.getId(), pdfText, quizCreateFormat);

        // then
        assertThat(result).isNotNull();
        assertThat(result.questions()).hasSize(4);
        assertThat(result.answers()).hasSize(4);

        // OX 퀴즈는 options가 {"A":"O", "B":"X"}여야 함
        result.questions().forEach(q -> {
            assertThat(q.options()).isNotNull();
            assertThat(q.options()).containsKeys("A", "B");
            assertThat(q.options().get("A")).isEqualTo("O");
            assertThat(q.options().get("B")).isEqualTo("X");
        });

        // 정답은 A 또는 B여야 함
        result.answers().forEach(a -> {
            assertThat(a.correctAnswer()).isIn("A", "B");
        });

        System.out.println("✅ OX quiz generated successfully");
        System.out.println("📝 Sample question: " + result.questions().get(0).questionText());
        System.out.println("📝 Sample answer: " + result.answers().get(0).correctAnswer());
    }

    /**
     * 테스트용 실제 PDF 파일 생성 헬퍼 메서드
     * PDFBox 내장 TrueType 폰트 사용 (Unicode 지원, Helvetica 문제 회피)
     */
    private File createTestPdf(Path tempDir, String filename, String content) throws Exception {
        File pdfFile = tempDir.resolve(filename).toFile();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // ✅ PDFBox 내장 TrueType 폰트 사용
            var fontStream = PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
            if (fontStream == null) {
                throw new IllegalStateException("❌ PDF font resource not found!");
            }
            PDType0Font font = PDType0Font.load(document, fontStream);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(16f);
                contentStream.newLineAtOffset(50, 750);

                // 줄바꿈 처리
                for (String line : content.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        contentStream.showText(trimmed);
                    }
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            document.save(pdfFile);
        }

        System.out.println("📄 Test PDF created: " + pdfFile.getName());
        return pdfFile;
    }

    public static User createUser() {
        long randomKakaoId = System.nanoTime();
        return User.create("ham", randomKakaoId);
    }
}