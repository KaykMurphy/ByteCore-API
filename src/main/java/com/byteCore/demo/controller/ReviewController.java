package com.byteCore.demo.controller;

import com.byteCore.demo.dto.request.ReviewRequestDTO;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {


    private final ReviewService  reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.status(HttpStatus.CREATED).
                body(reviewService.createReview(userDetails.getUser(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.ok(reviewService.updateReview(id, userDetails.getUser(), dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(
            @PathVariable UUID userId){

        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

}
