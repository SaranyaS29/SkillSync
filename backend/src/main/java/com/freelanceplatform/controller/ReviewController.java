package com.freelanceplatform.controller;

import com.freelanceplatform.dto.review.*;
import com.freelanceplatform.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service){this.service=service;}
    @PostMapping("/assignments/{assignmentId}/reviews") @ResponseStatus(HttpStatus.CREATED) @Operation(summary="Review the other participant after completion")
    public ReviewResponse create(@PathVariable Long assignmentId,@Valid @RequestBody ReviewRequest request,Authentication auth){return service.create(assignmentId,auth.getName(),request);}
    @GetMapping("/users/{userId}/reviews") public ReviewSummary byUser(@PathVariable Long userId){return service.byUser(userId);}
    @GetMapping("/reviews/my") public List<ReviewResponse> mine(Authentication auth){return service.mine(auth.getName());}
    @GetMapping("/reviews/written") public List<ReviewResponse> written(Authentication auth){return service.written(auth.getName());}
}
