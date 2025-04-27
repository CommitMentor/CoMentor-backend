package com.knu.coment.service;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.entity.*;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.QuestionException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class FolderService {
    private final UserService userService;
    private final QuestionRepository questionRepository;
    private final ProjectQuestionService projectQuestionService;
    private final FolderRepository folderRepository;
    private final RepoRepository repoRepository;
    private final UserCSQuestionRepository userCSQuestionRepository;
    private ProjectRepository projectRepository;

    public List<FolderListDto> getFolderList(String githubId) {
        User user = userService.findByGithubId(githubId);
        List<Folder> folders = folderRepository.findAllByUserId(user.getId());
        return folders.stream()
                .map(folder -> new FolderListDto(folder.getId(), folder.getFileName()))
                .sorted(Comparator.comparing(FolderListDto::getFolderId))
                .collect(toList());
    }
    @Transactional(readOnly = true)
    public List<FolderCsQuestionListDto> getFolderQuestions(String githubId, Long folderId) {
        User user = userService.findByGithubId(githubId);

        folderRepository.findByUserIdAndId(user.getId(), folderId)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        List<FolderCsQuestionListDto> result = new ArrayList<>();

        List<Question> projectQuestions = questionRepository.findAllByFolderId(folderId);
        result.addAll(
                projectQuestions.stream()
                        .map(q -> {
                            Project project = projectRepository.findById(q.getProjectId())
                                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_PROJECT));
                            Repo repo = repoRepository.findById(project.getRepoId())
                                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_REPO));

                            return new FolderCsQuestionListDto(
                                    null,
                                    q.getId(),
                                    q.getQuestion(),
                                    repo.getName(),
                                    q.getFolderName(),
                                    q.getCsCategory(),
                                    q.getQuestionStatus()
                            );
                        })
                        .toList()
        );

        List<UserCSQuestion> userCSQuestions = userCSQuestionRepository.findAllByUserIdAndFolderId(user.getId(), folderId);

        if (!userCSQuestions.isEmpty()) {
            List<Long> csQuestionIds = userCSQuestions.stream()
                    .map(UserCSQuestion::getQuestionId)
                    .toList();
            List<Question> csQuestions = questionRepository.findAllById(csQuestionIds);

            result.addAll(
                    userCSQuestions.stream()
                            .map(uq -> {
                                Question question = csQuestions.stream()
                                        .filter(q -> q.getId().equals(uq.getQuestionId()))
                                        .findFirst()
                                        .orElseThrow(() -> new FolderException(QuestionErrorCode.NOT_FOUND_QUESTION));

                                return new FolderCsQuestionListDto(
                                        uq.getId(),
                                        null,
                                        question.getQuestion(),
                                        null,
                                        null,
                                        question.getCsCategory(),
                                        uq.getQuestionStatus()
                                );
                            })
                            .toList()
            );
        }

        return result;
    }


    @Transactional
    public void bookmarkQuestion(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        String rawName = sanitize(dto.getFileName());
        String folderName = (rawName == null || rawName.isEmpty())
                ? generateDefaultName(user.getId())
                : rawName;

        Folder folder = folderRepository.findByUserIdAndFileName(user.getId(), folderName)
                .orElseGet(() -> folderRepository.save(new Folder(folderName, user.getId())));

        if (dto.getCsQuestionId() != null) {
            UserCSQuestion userCSQuestion = userCSQuestionRepository.findById(dto.getCsQuestionId())
                    .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
            userCSQuestion.bookMark(folder.getId());
            userCSQuestionRepository.save(userCSQuestion);
        } else if (dto.getQuestionId() != null) {
            Question question = projectQuestionService.findById(dto.getQuestionId());
            question.bookMark(folder.getId());
            questionRepository.save(question);
        } else {
            throw new FolderException(FolderErrorCode.MISSING_REQUIRED_FIELD);
        }
    }


    @Transactional
    public void cancelBookmark(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        String cleanName = sanitize(dto.getFileName());
        Folder folder = folderRepository.findByUserIdAndFileName(user.getId(), cleanName)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        if (dto.getCsQuestionId() != null) { // UserCSQuestion 북마크 취소
            UserCSQuestion userCSQuestion = userCSQuestionRepository.findById(dto.getCsQuestionId())
                    .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
            if (!folder.getId().equals(userCSQuestion.getFolderId())) {
                throw new FolderException(FolderErrorCode.BAD_REQUEST);
            }
            userCSQuestion.unBookMark();
            userCSQuestionRepository.save(userCSQuestion);
        } else if (dto.getQuestionId() != null) { // 일반 Question 북마크 취소
            Question question = projectQuestionService.findById(dto.getQuestionId());
            if (!folder.getId().equals(question.getFolderId())) {
                throw new FolderException(FolderErrorCode.BAD_REQUEST);
            }
            question.unBookMark();
            questionRepository.save(question);
        } else {
            throw new FolderException(FolderErrorCode.MISSING_REQUIRED_FIELD);
        }
    }
    @Transactional
    public void updateFolderName(String githubId, Long folderId, String newFileName) {
        User user = userService.findByGithubId(githubId);
        Folder folder = folderRepository.findByUserIdAndId(user.getId(), folderId)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));
        String cleanName = sanitize(newFileName);
        if (cleanName == null || cleanName.isEmpty()) {
            throw new FolderException(FolderErrorCode.MISSING_REQUIRED_FIELD);
        }

        boolean duplicated = folderRepository.existsByUserIdAndFileName(user.getId(), cleanName);
        if (duplicated) {
            throw new FolderException(FolderErrorCode.DUPLICATE_FILE_NAME);
        }

        folder.setFileName(cleanName);
        folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(String githubId, Long folderId) {
        User user = userService.findByGithubId(githubId);

        Folder folder = folderRepository.findByUserIdAndId(user.getId(), folderId)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        List<Question> questions = questionRepository.findAllByFolderId(folderId);
        for (Question question : questions) {
            question.unBookMark();
            questionRepository.save(question);
        }

        List<UserCSQuestion> userCSQuestions = userCSQuestionRepository.findAllByUserIdAndFolderId(user.getId(), folderId);
        for (UserCSQuestion userCSQuestion : userCSQuestions) {
            userCSQuestion.unBookMark();
            userCSQuestionRepository.save(userCSQuestion);
        }

        folderRepository.delete(folder);
    }
    private String sanitize(String raw) {
        return raw == null ? null : raw.replaceAll("\\s+", "");
    }

    private String generateDefaultName(Long userId) {
        long existing = folderRepository.countByUserIdAndFileNameStartingWith(userId, "default");
        return existing == 0 ? "default" : "default" + (existing + 1);
    }

}
