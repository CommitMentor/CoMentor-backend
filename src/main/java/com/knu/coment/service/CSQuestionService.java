package com.knu.coment.service;

import com.knu.coment.dto.cs.CSDashboard;
import com.knu.coment.dto.cs.QuestionListDto;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.repository.UserStackRepository;
import com.knu.coment.util.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CSQuestionService {
    private final UserStackRepository userStackRepo;
    private final UserService userService;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<QuestionListDto> recommendFour(String githubId) {
        User user = userService.findByGithubId(githubId);
        List<Stack> userStacks = userStackRepo.findStacksByUserId(user.getId());
        if (userStacks.isEmpty()) return List.of();
        int totalRows = 4;
        int stackCount = userStacks.size();
        int slotPerStack = (int) Math.ceil((double) totalRows / stackCount);

        List<String> stackNames = userStacks.stream()
                .map(Enum::name)
                .toList();

        List<Question> all = questionRepository.findBalanced(stackNames, slotPerStack, totalRows);

        return all.stream()
                .map(q -> new QuestionListDto(
                        q.getId(),
                        q.getQuestion(),
                        q.getStack(),
                        q.getCsCategory()
                ))
                .toList();
    }

    public PageResponse<CSDashboard> getDashboard(String githubId, int page) {
        User user = userService.findByGithubId(githubId);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime cutOff = today.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(page, 8, Sort.by(Sort.Direction.DESC, "createAt"));

        Page<Question> csPage = questionRepository
                .findAllUpToToday(
                        QuestionType.CS, QuestionStatus.DONE, user.getId(), cutOff, pageable);

        Map<LocalDate, List<Question>> grouped = csPage.getContent().stream()
                .collect(Collectors.groupingBy(q -> q.getCreateAt().toLocalDate()));

        List<CSDashboard> dashboards = grouped.entrySet().stream()
                .map(e -> CSDashboard.builder()
                        .date(e.getKey())
                        .questions(
                                e.getValue().stream()
                                        .sorted(Comparator.comparing(Question::getId).reversed())
                                        .map(this::toQuestionListDto)
                                        .toList())
                        .build())
                .sorted(Comparator.comparing(CSDashboard::getDate).reversed())
                .toList();

        return new PageResponse<>(new PageImpl<>(dashboards, pageable, csPage.getTotalElements()));
    }

    private QuestionListDto toQuestionListDto(Question q) {
        return new QuestionListDto(
                q.getId(),
                q.getQuestion(),
                q.getStack(),
                q.getCsCategory()
        );
    }


}
