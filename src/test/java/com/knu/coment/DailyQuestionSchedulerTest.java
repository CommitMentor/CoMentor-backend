package com.knu.coment;

import com.knu.coment.entity.User;
import com.knu.coment.entity.Question;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.repository.UserCSQuestionRepository;
import com.knu.coment.repository.UserStackRepository;
import com.knu.coment.service.DailyQuestionScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyQuestionSchedulerTest {

    @Mock private UserRepository userRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserCSQuestionRepository userCSQuestionRepository;
    @Mock private UserStackRepository userStackRepository;

    @InjectMocks private DailyQuestionScheduler dailyQuestionScheduler;

    private User user;
    private Question question;

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
    }
    @Test
    @DisplayName("generateDailyQuestions: 스택이 없으면 추천 안함")
    void generateDailyQuestions_NoStacks() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userStackRepository.findStacksByUserId(user.getId())).thenReturn(List.of());
        when(userCSQuestionRepository.existsByUserIdAndDate(eq(user.getId()), any(LocalDate.class))).thenReturn(false);

        // when
        dailyQuestionScheduler.generateDailyQuestions();

        // then
        verify(userCSQuestionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("generateDailyQuestions: 추천 문제 정상 생성")
    void generateDailyQuestions_Success() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userStackRepository.findStacksByUserId(user.getId())).thenReturn(List.of(Stack.BACKEND));
        when(userCSQuestionRepository.existsByUserIdAndDate(eq(user.getId()), any(LocalDate.class))).thenReturn(false);
        when(userCSQuestionRepository.findSolvedQuestionIdsByUserId(user.getId())).thenReturn(List.of());
        when(questionRepository.findBalancedUnreceivedWithoutExclude(any(), anyInt(), anyInt()))
                .thenReturn(List.of(question));

        // when
        dailyQuestionScheduler.generateDailyQuestions();

        // then
        verify(userCSQuestionRepository, times(1)).saveAll(any());
    }
    @Test
    @DisplayName("generateDailyQuestions: 추천할 문제가 없으면 저장 안함")
    void generateDailyQuestions_NoQuestions() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userStackRepository.findStacksByUserId(user.getId())).thenReturn(List.of(Stack.BACKEND));
        when(userCSQuestionRepository.existsByUserIdAndDate(eq(user.getId()), any(LocalDate.class))).thenReturn(false);
        when(userCSQuestionRepository.findSolvedQuestionIdsByUserId(user.getId())).thenReturn(List.of());
        when(questionRepository.findBalancedUnreceivedWithoutExclude(any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        // when
        dailyQuestionScheduler.generateDailyQuestions();

        // then
        verify(userCSQuestionRepository, never()).saveAll(any());
    }
}
