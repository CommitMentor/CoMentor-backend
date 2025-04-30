package com.knu.coment;

import com.knu.coment.dto.cs.CSDashboard;
import com.knu.coment.dto.cs.CSQuestionInfoResponse;
import com.knu.coment.entity.Answer;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.global.*;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.repository.UserCSQuestionRepository;
import com.knu.coment.service.CSQuestionService;
import com.knu.coment.service.UserService;
import com.knu.coment.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSQuestionServiceTest {

    @Mock private UserService userService;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserCSQuestionRepository userCSQuestionRepository;
    @Mock private AnswerRepository answerRepository;

    @InjectMocks private CSQuestionService csQuestionService;

    private User user;
    private Question question;
    private UserCSQuestion userCSQuestion;
    private Answer answer;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .githubId("testUser")
                .email("test@example.com")
                .build();

        question = new Question(
                CSCategory.DATABASES,
                QuestionType.CS,
                "트랜잭션이란?",
                QuestionStatus.TODO,
                Stack.BACKEND
        );

        userCSQuestion = new UserCSQuestion(
                user.getId(),
                1L,
                LocalDate.now(),
                QuestionStatus.TODO
        );

        answer = new Answer(
                "트랜잭션은 작업 단위입니다.",
                LocalDateTime.now(),
                Author.USER,
                1L,
                user.getId()
        );
    }

    @Test
    @DisplayName("getDashboard: 대시보드 정상 조회")
    void getDashboard_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "date"));
        Page<UserCSQuestion> userCSPage = new PageImpl<>(List.of(userCSQuestion), pageable, 1);

        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(userCSQuestionRepository.findByUserIdAndCategory(user.getId(), CSCategory.DATABASES, pageable))
                .thenReturn(userCSPage);
        when(questionRepository.findById(userCSQuestion.getQuestionId())).thenReturn(Optional.of(question));


        // when
        PageResponse<CSDashboard> response = csQuestionService.getDashboard("testUser", 0, CSCategory.DATABASES);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getQuestions()).hasSize(1);
    }


    @Test
    @DisplayName("getCSQuestionDetail: 문제 상세 조회 성공")
    void getCSQuestionDetail_Success() {
        // given
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(userCSQuestionRepository.findByIdAndUserId(userCSQuestion.getId(), user.getId()))
                .thenReturn(Optional.of(userCSQuestion));
        when(questionRepository.findById(userCSQuestion.getQuestionId()))
                .thenReturn(Optional.of(question));
        when(answerRepository.findAllByUserIdAndQuestionId(user.getId(), question.getId()))
                .thenReturn(List.of(answer));

        // when
        CSQuestionInfoResponse response = csQuestionService.getCSQuestionDetail("testUser", userCSQuestion.getId());

        // then
        assertThat(response.getQuestion()).isEqualTo("트랜잭션이란?");
        assertThat(response.getAnswers()).hasSize(1);
    }
    @Test
    @DisplayName("getDashboard: 추천 문제가 없을 때 빈 페이지 반환")
    void getDashboard_NoUserCSQuestions() {
        // given
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "date"));
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(userCSQuestionRepository.findByUserIdAndCategory(user.getId(), CSCategory.DATABASES, pageable))
                .thenReturn(Page.empty(pageable));

        // when
        PageResponse<CSDashboard> response = csQuestionService.getDashboard("testUser", 0, CSCategory.DATABASES);

        // then
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("getCSQuestionDetail: 추천받지 않은 문제 요청 시 예외")
    void getCSQuestionDetail_NotRecommendedQuestion() {
        // given
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(userCSQuestionRepository.findByIdAndUserId(anyLong(), eq(user.getId())))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csQuestionService.getCSQuestionDetail("testUser", 999L))
                .isInstanceOf(com.knu.coment.exception.QuestionException.class)
                .hasMessageContaining("질문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("getCSQuestionDetail: 문제 자체가 없는 경우 예외")
    void getCSQuestionDetail_QuestionNotFound() {
        // given
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(userCSQuestionRepository.findByIdAndUserId(eq(userCSQuestion.getId()), eq(user.getId())))
                .thenReturn(Optional.of(userCSQuestion));
        when(questionRepository.findById(eq(userCSQuestion.getQuestionId())))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csQuestionService.getCSQuestionDetail("testUser", userCSQuestion.getId()))
                .isInstanceOf(com.knu.coment.exception.QuestionException.class)
                .hasMessageContaining("질문을 찾을 수 없습니다");
    }

}
