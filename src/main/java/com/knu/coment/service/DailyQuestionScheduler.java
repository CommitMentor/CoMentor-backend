package com.knu.coment.service;

import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DailyQuestionScheduler {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final UserCSQuestionRepository userCSQuestionRepository;
    private final UserStackRepository userStackRepository;
    private final AnswerRepository answerRepository;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    @Transactional
    public void generateDailyQuestions() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        for (User user : users) {
            boolean alreadyGenerated = userCSQuestionRepository.existsByUserIdAndDate(user.getId(), today);
            if (alreadyGenerated) continue;

            List<Stack> stacks = userStackRepository.findStacksByUserId(user.getId());
            if (stacks.isEmpty()) {
                log.info("유저 {} 는 스택이 없어 추천할 수 없습니다.", user.getId());
                continue;
            }

            List<String> stackNames = stacks.stream()
                    .map(Enum::name)
                    .toList();

            int slotPerStack = (int) Math.ceil(4.0 / stackNames.size());
            List<Long> alreadySolvedQuestionIds = userCSQuestionRepository.findSolvedQuestionIdsByUserId(user.getId());
            List<Question> recommended;
            if (alreadySolvedQuestionIds.isEmpty()) {
                recommended = questionRepository.findBalancedUnansweredWithoutExclude(
                        stackNames, slotPerStack, 4, user.getId()
                );
            } else {
                recommended = questionRepository.findBalancedUnanswered(
                        stackNames, slotPerStack, 4, alreadySolvedQuestionIds, user.getId()
                );
            }


            if (recommended.isEmpty()) {
                log.info("유저 {} 에게 추천할 문제가 없습니다.", user.getId());
                continue;
            }

            List<UserCSQuestion> UserCSQuestions = recommended.stream()
                    .map(q -> new UserCSQuestion(user.getId(), q.getId(), today, QuestionStatus.TODO))
                    .toList();

            userCSQuestionRepository.saveAll(UserCSQuestions);
            log.info("유저 {} 에게 {}개의 문제를 추천 완료했습니다.", user.getId(), UserCSQuestions.size());
        }
    }
}
