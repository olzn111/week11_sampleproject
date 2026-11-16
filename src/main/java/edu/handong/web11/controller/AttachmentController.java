package edu.handong.web11.controller;

import edu.handong.web11.domain.Attachment;
import edu.handong.web11.repository.AttachmentRepository;
import edu.handong.web11.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments;
    private final FileStorageService storage;

    public AttachmentController(AttachmentRepository attachments, FileStorageService storage) {
        this.attachments = attachments;
        this.storage = storage;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        // 전달받은 첨부파일 id로 DB에서 파일을 조회하고, 없으면 404 Not Found 에러를 보낸다.
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));
        // FileStorageService로 디스크에 저장된 실제 파일을 읽고 Resource 객체로 가져온다.
        Resource resource = storage.load(file);
        // 브라우저가 화면에 안 열고 파일 다운로드로 Content-Disposition 헤더를 설정하고 UTF-8로 원본 파일명을 인코딩한다.
        String disposition = ContentDisposition.attachment()
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build().toString();
        // 200 OK 상태 코드, 다운로드용 바이너리 타입, Content-Disposition 헤더와 resource를 받아 HTTP 응답을 반환한다.
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    // STUDENT TODO: GET /attachments/{id}/view (image preview).
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 미리보기를 지원합니다.");
        }

        Resource resource = storage.load(file);
        String disposition = ContentDisposition.inline()
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }
}
