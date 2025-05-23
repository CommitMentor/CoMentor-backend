package com.knu.coment.controller;

import com.knu.coment.dto.gpt.FeedBackRequestDto;
import com.knu.coment.entity.Answer;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.AnswerService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "피드백 컨트롤러", description = "피드백 API입니다.")
@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @Operation(summary = "프로젝트 CS 피드백 생성", description = "프로젝트 피드백을 생성합니다.")
    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "피드백 생성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
   public ResponseEntity<?> createFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody FeedBackRequestDto feedbackDto){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.createAnswer(
                githubId,
                feedbackDto.getCsQuestionId(),
                feedbackDto.getAnswer()
        );

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }
    @Operation(summary = "프로젝트 CS 질문 해설 생성 ", description = "프로젝트 질문 해설 생성합니다.")
    @PostMapping("/commentary" )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해설 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "해설 생성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> createCommentary(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam  Long csQuestionId){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.createAnswer2(
                githubId,
                csQuestionId
        );

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }

    @Operation(summary = "CS 피드백 생성", description = "CS 피드백을 생성합니다.")
    @PostMapping("/CS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 피드백 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 피드백 생성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> createCSFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody FeedBackRequestDto feedbackDto){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.createCSAnswer(
                githubId,
                feedbackDto.getCsQuestionId(),
                feedbackDto.getAnswer()
        );

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }
    @Operation(summary = "CS 해설 생성", description = "CS 피드백을 생성합니다.")
    @PostMapping("/CS/commentary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 피드백 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 피드백 생성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> createCSCommentary(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam  Long csQuestionId){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.createCSAnswer2(
                githubId,
                csQuestionId
        );

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }
    @Operation(summary = "CS 질문 다시 풀기", description = "CS 질문 다시 풀기 API입니다.")
    @PostMapping("/CS/retry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 다시 풀기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 다시 풀기 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> retryCSQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody FeedBackRequestDto feedbackDto){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.retryCsAnswer(
                githubId,
                feedbackDto.getCsQuestionId(),
                feedbackDto.getAnswer()
        );
        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }
    @Operation(summary = "프로젝트 질문 다시 풀기", description = "프로젝트 질문 다시 풀기 API입니다.")
    @PostMapping("/retry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로젝트 질문 다시 풀기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "프로젝트 질문 다시 풀기 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> retryProjectQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody FeedBackRequestDto feedbackDto){
        String githubId = userDetails.getUsername();
        Answer newFeedback = answerService.retryProjectAnswer(
                githubId,
                feedbackDto.getCsQuestionId(),
                feedbackDto.getAnswer()
        );
        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                newFeedback.getContent()
        );
    }


}
