package com.yatrify.user.controller;

import com.yatrify.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@Tag(name = "User Documents", description = "User document APIs")
public class UserDocumentController {

    @GetMapping("/my")
    @Operation(summary = "Get current user documents", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyDocuments() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload user document", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        Map<String, Object> payload = Map.of(
                "id", "pending",
                "documentType", documentType,
                "status", "PENDING",
                "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );
        return ResponseEntity.ok(ApiResponse.success("Document accepted", payload));
    }
}
