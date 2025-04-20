package com.knu.coment.service;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.entity.*;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.repository.ProjectRepository;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.repository.FolderRepository;
import com.knu.coment.repository.RepoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FolderService {
    private final UserService userService;
    private final QuestionRepository questionRepository;
    private final ProjectQuestionService projectQuestionService;
    private final FolderRepository folderRepository;
    private final RepoRepository repoRepository;
    private ProjectRepository pro;

    public List<FolderListDto> getFolderList(String githubId) {
        User user = userService.findByGithubId(githubId);
        List<Folder> folders = folderRepository.findAllByUserId(user.getId());
        return folders.stream()
                .map(folder -> new FolderListDto(folder.getId(), folder.getFileName()))
                .sorted(Comparator.comparing(FolderListDto::getFolderId))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<FolderCsQuestionListDto> getFolderQuestions(String githubId, Long folderId) {
        User user = userService.findByGithubId(githubId);
        Folder folder = folderRepository.findByUserIdAndId(user.getId(), folderId)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        List<Question> questions = questionRepository.findAllByFolderId(folderId);

        return questions.stream()
                .map(q -> {
                    Project project = pro.findById(q.getProjectId())
                            .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_PROJECT));
                    Repo repo = repoRepository.findById(project.getRepoId())
                            .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_REPO));
                    return new FolderCsQuestionListDto(
                            q.getId(),
                            q.getQuestion(),
                            repo.getName(),
                            folder.getFileName(),
                            q.getCsCategory(),
                            q.getQuestionStatus()
                    );
                })
                .collect(Collectors.toList());
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

        Question question = projectQuestionService.findById(dto.getCsQuestionId());
        question.bookMark(folder.getId());
        questionRepository.save(question);
    }

    @Transactional
    public void cancelBookmark(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        String cleanName = sanitize(dto.getFileName());
        Folder folder = folderRepository.findByUserIdAndFileName(user.getId(), cleanName)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        Question projectCsQuestion = projectQuestionService.findById(dto.getCsQuestionId());

        if (projectCsQuestion.getFolderId() == null || !projectCsQuestion.getFolderId().equals(folder.getId())) {
            throw new FolderException(FolderErrorCode.BAD_REQUEST);
        }
        projectCsQuestion.unBookMark();
        questionRepository.save(projectCsQuestion);
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
