package edu.handong.web11.service;

import edu.handong.web11.domain.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String path) throws IOException {
        uploadDir = Path.of(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    public Attachment store(MultipartFile file) {
        // 업로드 된 파일이 0바이트인지 검사하고 빈 파일이면 업로드 할 수 없음을 알린다.
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        // 원본 파일명을 가져와 nulldlrjsk / , \, .. 같은 문자가 있는지 확인한다.
        String original = file.getOriginalFilename();
        // 파일이름 길이 240자를 초과하는지 확인한다.
        if (original == null || original.isBlank() || original.contains("/") || original.contains("\\") || original.contains(".."))
            throw new IllegalArgumentException("잘못된 파일 이름입니다.");
        if (original.length() > 240) throw new IllegalArgumentException("파일 이름이 너무 깁니다.");
        // 마지막 .을 기준으로 확장자를 소문자로 추출하고 allowed에 있는지 확인한다.
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(ext)) throw new IllegalArgumentException("허용 형식: jpg, jpeg, png, gif, pdf, txt");
        // 업로드 파일의 최대 용량이 10MB를 넘는지 검사한다.
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("파일은 10MB 이하만 가능합니다.");
        // 파일명 중복 덮어쓰기를 방지하기 위해 랜덤 UUID와 확장자를 조합해 서버 저장용 파일이름을 만든다.
        String stored = UUID.randomUUID() + "." + ext;
       // 업로드 폴더 루트와 결합 후 normalize()로 경로를 정리하고, startsWith()로 지정된 업로드 폴더 바깥으로 넘어가지 않는지 검증한다.
        Path target = uploadDir.resolve(stored).normalize();
        if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        try (InputStream input = file.getInputStream()) {
            // 업로드된 파일의 입력 스트림을 읽어 실제 target에 파일로 복사 저장한다.
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            // Teaching sample: MIME type is inferred by extension, not trusted from request headers.

            //보안상 요청 헤더가 아닌 검증된 확장자를 기준으로 Content타입을 결정한다.
            String contentType = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "pdf" -> "application/pdf";
                default -> "text/plain";
            };
            //원본 파일명, UUID 저장 파일명, Content-Type, 파일 크기를 담은 Attachment 엔티티를 생성하고 반환한다.
            return new Attachment(original, stored, contentType, file.getSize());
        } catch (IOException e) {
            //파일 저장 중 오류가 발생하면 저장되고 있는 파일을 삭제하고 500 서버 에러를 발생시킨다.
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }
    }

    public Resource load(Attachment attachment) {
        Path target = uploadDir.resolve(attachment.getStoredFilename()).normalize();
        if (!target.startsWith(uploadDir) || !Files.isRegularFile(target))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        return new PathResource(target);
    }

    public void deleteIfExists(Attachment attachment) {
        try {
            Files.deleteIfExists(uploadDir.resolve(attachment.getStoredFilename()).normalize());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패", e);
        }
    }
}
