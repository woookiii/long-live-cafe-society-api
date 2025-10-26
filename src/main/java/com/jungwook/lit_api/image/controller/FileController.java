package com.jungwook.lit_api.image.controller;

import com.jungwook.lit_api.image.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.util.Map;
import java.util.UUID;

import static com.jungwook.lit_api.image.service.FileService.buildFilename;


@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<String> getUrl(@PathVariable UUID chatRoomId) {
        String url = fileService.generatePresignedUrl(null, SdkHttpMethod.GET, chatRoomId);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/pre-signed-url")
    public ResponseEntity<Map<String, Object>> generateUrl(
            @RequestParam(name = "filename", required = false, defaultValue = "") String filename,
            @RequestParam(name = "chatRoomId", required = false, defaultValue = "") UUID chatRoomId) {
        filename = buildFilename(filename);
        String url = fileService.generatePresignedUrl(filename, SdkHttpMethod.PUT, chatRoomId);
        return ResponseEntity.ok(Map.of("url", url, "file", filename));
    }



}