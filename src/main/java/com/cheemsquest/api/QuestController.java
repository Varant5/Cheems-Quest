package com.cheemsquest.api;

import com.cheemsquest.domain.QuestService;
import com.cheemsquest.domain.QuestView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/characters/{id}/quests")
@RequiredArgsConstructor
@Tag(name = "Quests", description = "Much task. Very reward.")
class QuestController {

    private final QuestService questService;

    @GetMapping
    @Operation(summary = "List quests", description = "Every quest with this character's progress: AVAILABLE, ACTIVE or COMPLETED.")
    @ApiResponse(responseCode = "200", description = "Quest log")
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    List<QuestView> list(@PathVariable UUID id) {
        return questService.list(id);
    }

    @PostMapping("/{code}/accept")
    @Operation(summary = "Accept a quest", description = "Progress is counted from now on, rewards are granted on completion.")
    @ApiResponse(responseCode = "200", description = "Quest accepted")
    @ApiResponse(responseCode = "404", description = "No such character or quest",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Quest already accepted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    QuestView accept(@PathVariable UUID id, @PathVariable String code) {
        return questService.accept(id, code);
    }
}
