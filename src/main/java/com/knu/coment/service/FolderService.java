package com.knu.coment.service;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.entity.CsQuestion;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import com.knu.coment.exception.FolderExceptionHandler;
import com.knu.coment.exception.code.FolderErrorCoe;
import com.knu.coment.repository.CsQuestionRepository;
import com.knu.coment.repository.FolderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FolderService {
    private final UserService userService;
    private final CsQuestionRepository csQuestionRepository;
    private final CsQuestionService csQuestionService;
    private final FolderRepository folderRepository;

    public List<FolderListDto> getFolderList(String githubId) {
        User user = userService.findByGithubId(githubId);
        List<Folder> folders = folderRepository.findAllByUser(user);
        return folders.stream()
                .map(folder -> new FolderListDto(folder.getId(), folder.getFolderName()))
                .sorted(Comparator.comparing(FolderListDto::getFolderId))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<FolderCsQuestionListDto> getFolderQuestions(String githubId, Long folderId) {
        userService.findByGithubId(githubId);
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCoe.NOT_FOUND_FOLDER));
        return folder.getQuestions().stream()
                .map(q -> new FolderCsQuestionListDto(
                        folder.getFolderName(),
                        q.getId(),
                        q.getQuestion(),
                        q.getQuestionStatus()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void bookmarkQuestion(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        if (dto.getFolderName() == null || dto.getFolderName().trim().isEmpty()) {
            throw new FolderExceptionHandler(FolderErrorCoe.MISSING_REQUIRED_FIELD);
        }
        Folder folder = folderRepository.findByUserAndFolderName(user, dto.getFolderName())
                .orElseGet(() -> {
                    Folder newFolder = new Folder(dto.getFolderName(), user);
                    return folderRepository.save(newFolder);
                });
        CsQuestion csQuestion = csQuestionService.findById(dto.getCsQuestionId());
        csQuestion.assignFolder(folder);

        csQuestionRepository.save(csQuestion);
    }

    @Transactional
    public void cancelBookmark(String githubId, BookMarkRequestDto dto) {
        User user = userService.findByGithubId(githubId);

        Folder folder = folderRepository.findByUserAndFolderName(user, dto.getFolderName())
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCoe.NOT_FOUND_FOLDER));

        CsQuestion csQuestion = csQuestionService.findById(dto.getCsQuestionId());

        if (csQuestion.getFolder() == null || !csQuestion.getFolder().equals(folder)) {
            throw new FolderExceptionHandler(FolderErrorCoe.BAD_REQUEST);
        }

        csQuestion.removeFolder();

        csQuestionRepository.save(csQuestion);
    }
    @Transactional
    public void deleteFolder(String githubId, String folderName) {
        User user = userService.findByGithubId(githubId);

        Folder folder = folderRepository.findByUserAndFolderName(user, folderName)
                .orElseThrow(() -> new FolderExceptionHandler(FolderErrorCoe.NOT_FOUND_FOLDER));

        List<CsQuestion> questions = new ArrayList<>(folder.getQuestions());
        for (CsQuestion question : questions) {
            question.removeFolder();
            csQuestionRepository.save(question);
        }

        folderRepository.delete(folder);
    }
}
