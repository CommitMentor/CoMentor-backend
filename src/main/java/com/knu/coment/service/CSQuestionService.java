package com.knu.coment.service;

import com.knu.coment.dto.cs.CSDashboard;
import com.knu.coment.dto.cs.QuestionListDto;
import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.exception.QuestionException;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.repository.UserCSQuestionRepository;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.util.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CSQuestionService {
    private final UserService userService;
    private final QuestionRepository questionRepository;
    private final UserCSQuestionRepository userCSQuestionRepository;

    @Transactional(readOnly = true)
    public PageResponse<CSDashboard> getDashboard(String githubId, int page) {
        User user = userService.findByGithubId(githubId);
        Pageable pageable = PageRequest.of(page, 8, Sort.by(Sort.Direction.DESC, "date"));

        Page<UserCSQuestion> userCSPage = userCSQuestionRepository.findAllByUserId(user.getId(), pageable);
        if (userCSPage.isEmpty()) {
            return new PageResponse<>(Page.empty(pageable));
        }

        Map<LocalDate, List<UserCSQuestion>> grouped = userCSPage.stream()
                .collect(Collectors.groupingBy(UserCSQuestion::getDate, TreeMap::new, Collectors.toList()));

        List<CSDashboard> dashboards = grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<QuestionListDto> questions = entry.getValue().stream()
                            .map(dq -> {
                                Question question = questionRepository.findById(dq.getQuestionId())
                                        .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
                                return toQuestionListDto(question, dq);
                            })
                            .toList();
                    return CSDashboard.builder()
                            .date(date)
                            .questions(questions)
                            .build();
                })
                .sorted(Comparator.comparing(CSDashboard::getDate).reversed()) // 날짜 내림차순
                .toList();

        return new PageResponse<>(new PageImpl<>(dashboards, pageable, userCSPage.getTotalElements()));
    }

    private QuestionListDto toQuestionListDto(Question q, UserCSQuestion uq) {
        return new QuestionListDto(
                uq.getId(),
                q.getQuestion(),
                q.getStack(),
                q.getCsCategory(),
                uq.getQuestionStatus()
        );
    }

}
