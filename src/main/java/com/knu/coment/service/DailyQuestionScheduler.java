package com.knu.coment.service;

import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.*;
import jakarta.annotation.PostConstruct;
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

//    @PostConstruct
//    public void init() {
//        generateDailyQuestions(); // 애플리케이션 시작 시 실행
//    }

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    @Transactional
    public void generateDailyQuestions() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        for (User user : users) {
            LocalDate expiryDate = today.minusDays(3);
            userCSQuestionRepository.deleteOldUnsolvedQuestions(user.getId(), expiryDate);
            //log.info("유저 {} 의 만료된 문제를 삭제했습니다.", user.getId());
            boolean alreadyGenerated = userCSQuestionRepository.existsByUserIdAndDate(user.getId(), today);
            if (alreadyGenerated) continue;

            List<Stack> stacks = userStackRepository.findStacksByUserId(user.getId());
            if (stacks.isEmpty()) {
                //log.info("유저 {} 는 스택이 없어 추천할 수 없습니다.", user.getId());
                continue;
            }

            List<String> stackNames = stacks.stream()
                    .map(Enum::name)
                    .toList();

            int slotPerStack = (int) Math.ceil(4.0 / stackNames.size());

            List<Long> excludedQuestionIds = userCSQuestionRepository.findAllQuestionIdsByUserId(user.getId());
            List<Question> recommended;
            if (excludedQuestionIds.isEmpty()) {
                recommended = questionRepository.findBalancedUnreceivedWithoutExclude(
                        stackNames, slotPerStack, 4
                );
            } else {
                recommended = questionRepository.findBalancedUnreceived(
                        stackNames, slotPerStack, 4, excludedQuestionIds
                );
            }

            if (recommended.isEmpty()) {
                //log.info("유저 {} 에게 추천할 문제가 없습니다.", user.getId());
                continue;
            }

            List<UserCSQuestion> UserCSQuestions = recommended.stream()
                    .map(q -> new UserCSQuestion(user.getId(), q.getId(), today, QuestionStatus.TODO))
                    .toList();

            userCSQuestionRepository.saveAll(UserCSQuestions);
            //log.info("유저 {} 에게 {}개의 문제를 추천 완료했습니다.", user.getId(), UserCSQuestions.size());
        }
    }
}
