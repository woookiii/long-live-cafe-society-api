package com.jungwook.lit_api.image.controller;

import com.jungwook.lit_api.image.domain.AccessType;
import com.jungwook.lit_api.image.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.util.Map;

import static com.jungwook.lit_api.image.service.FileService.buildFilename;


@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<String> getUrl(@PathVariable String filename) {
        String url = fileService.generatePresignedUrl(filename, SdkHttpMethod.GET, null);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/pre-signed-url")
    public ResponseEntity<Map<String, Object>> generateUrl(
            @RequestParam(name = "filename", required = false, defaultValue = "") String filename,
            @RequestParam(name = "accessType", required = false, defaultValue = "PRIVATE") AccessType accessType) {
        filename = buildFilename(filename);
        String url = fileService.generatePresignedUrl(filename, SdkHttpMethod.PUT, accessType);
        return ResponseEntity.ok(Map.of("url", url, "file", filename));
    }



}