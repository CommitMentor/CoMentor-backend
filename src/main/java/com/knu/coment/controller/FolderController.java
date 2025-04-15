package com.knu.coment.controller;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.FolderService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "BOOKMARK 컨트롤러", description = "BOOKMARK API입니다.")
@Controller
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "폴더 목록 조회", description = "폴더 목록을 조회하는 API입니다.")
    @GetMapping("")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "폴더 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"), @ApiResponse(responseCode = "404", description = "폴더 목록 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getFolderList(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        List<FolderListDto> folderList = folderService.getFolderList(githubId);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                folderList);
    }

    @Operation(summary = "폴더 질문 목록 조회", description = "폴더 질문 목록을 조회하는 API입니다.")
    @GetMapping("/questions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "폴더 질문 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"), @ApiResponse(responseCode = "404", description = "폴더 질문 목록 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getFolderQuestions(@AuthenticationPrincipal UserDetails userDetails, Long folderId) {
        String githubId = userDetails.getUsername();
        List<FolderCsQuestionListDto> folderQuestions = folderService.getFolderQuestions(githubId, folderId);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                folderQuestions);
    }

    @Operation(summary = "폴더 질문 북마크", description = "폴더 질문을 북마크하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "폴더 질문 북마크 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"), @ApiResponse(responseCode = "404", description = "폴더 질문 북마크 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("")
    public ResponseEntity<?> bookmarkQuestion(@AuthenticationPrincipal UserDetails userDetails, BookMarkRequestDto dto) {
        String githubId = userDetails.getUsername();
        folderService.bookmarkQuestion(githubId, dto);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage());
    }
    @Operation(summary = "폴더 질문 북마크 해제", description = "폴더 질문 북마크를 해제하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "폴더 질문 북마크 해제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"), @ApiResponse(responseCode = "404", description = "폴더 질문 북마크 해제 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBookmark(@AuthenticationPrincipal UserDetails userDetails, BookMarkRequestDto dto) {
        String githubId = userDetails.getUsername();
        folderService.cancelBookmark(githubId, dto);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.DELETE_SUCCESS.getMessage());
    }
    @Operation(summary = "폴더 삭제", description = "폴더를 삭제하는 API입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "폴더 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"), @ApiResponse(responseCode = "404", description = "폴더 삭제 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam String folderName) {
        String githubId = userDetails.getUsername();
        folderService.deleteFolder(githubId, folderName);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.DELETE_SUCCESS.getMessage());
    }

}
