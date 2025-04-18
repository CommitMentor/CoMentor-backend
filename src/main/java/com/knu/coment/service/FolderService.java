package com.knu.coment.service;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.entity.ProjectCsQuestion;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.repository.ProjectCsQuestionRepository;
import com.knu.coment.repository.FolderRepository;
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
    private final ProjectCsQuestionRepository projectCsQuestionRepository;
    private final CsQuestionService csQuestionService;
    private final FolderRepository folderRepository;

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

        List<ProjectCsQuestion> questions = projectCsQuestionRepository.findAllByFolderId(folderId);

        return questions.stream()
                .map(q -> new FolderCsQuestionListDto(
                        folder.getFileName(),
                        q.getId(),
                        q.getQuestion(),
                        q.getQuestionStatus()
                ))
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

        ProjectCsQuestion question = csQuestionService.findById(dto.getCsQuestionId());
        question.bookMark(folder.getId());
        projectCsQuestionRepository.save(question);
    }

    @Transactional
    public void cancelBookmark(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        String cleanName = sanitize(dto.getFileName());
        Folder folder = folderRepository.findByUserIdAndFileName(user.getId(), cleanName)
                .orElseThrow(() -> new FolderException(FolderErrorCode.NOT_FOUND_FOLDER));

        ProjectCsQuestion projectCsQuestion = csQuestionService.findById(dto.getCsQuestionId());

        if (projectCsQuestion.getFolderId() == null || !projectCsQuestion.getFolderId().equals(folder.getId())) {
            throw new FolderException(FolderErrorCode.BAD_REQUEST);
        }
        projectCsQuestion.unBookMark();
        projectCsQuestionRepository.save(projectCsQuestion);
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
        List<ProjectCsQuestion> questions = projectCsQuestionRepository.findAllByFolderId(folderId);
        for (ProjectCsQuestion question : questions) {
            question.unBookMark();
            projectCsQuestionRepository.save(question);
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
