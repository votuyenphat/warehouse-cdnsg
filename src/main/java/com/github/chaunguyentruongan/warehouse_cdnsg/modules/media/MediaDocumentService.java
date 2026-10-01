package com.github.chaunguyentruongan.warehouse_cdnsg.modules.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaDocumentService {

    private final MediaDocumentRepository repository;
    private final Cloudinary cloudinary;

    @Transactional
    public MediaDocument uploadFile(MultipartFile file, String name, String category, String description)
            throws IOException {
        // 1. Upload lên Cloudinary
        // Tự động nhận diện loại file (auto) để hỗ trợ cả ảnh và pdf
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", "warehouse_documents" // Lưu vào một thư mục riêng trên Cloudinary cho gọn
        ));

        // 2. Lưu thông tin vào Database
        MediaDocument document = MediaDocument.builder()
                .name(name != null ? name : file.getOriginalFilename())
                .fileUrl(uploadResult.get("secure_url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .fileType(uploadResult.get("resource_type").toString() + "/" + uploadResult.get("format").toString())
                .fileSize(file.getSize())
                .category(category)
                .description(description)
                .uploadAt(LocalDateTime.now())
                .build();

        return repository.save(document);
    }

    @Transactional
    public void deleteFile(Long id) throws IOException {
        MediaDocument document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file!"));

        // 1. Xóa file trên Cloudinary trước
        cloudinary.uploader().destroy(document.getPublicId(), ObjectUtils.emptyMap());

        // 2. Xóa trong Database
        repository.delete(document);
    }

    public Page<MediaDocument> searchAndFilter(String keyword, String category, Pageable pageable) {
        return repository.searchWithFilter(keyword, category, pageable);
    }

    @Transactional
    public MediaDocument updateInfo(Long id, String name, String category, String description) {
        MediaDocument doc = repository.findById(id).orElseThrow();
        if (name != null)
            doc.setName(name);
        if (category != null)
            doc.setCategory(category);
        if (description != null)
            doc.setDescription(description);
        return repository.save(doc);
    }
}