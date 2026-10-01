package com.github.chaunguyentruongan.warehouse_cdnsg.modules.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaDocumentRepository extends JpaRepository<MediaDocument, Long> {

    @Query("SELECT m FROM MediaDocument m WHERE " +
            "(:category IS NULL OR :category = '' OR m.category = :category) AND " +
            "(:kw IS NULL OR :kw = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<MediaDocument> searchWithFilter(@Param("kw") String kw, @Param("category") String category, Pageable pageable);
}