package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ... các import khác
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Table(name = "material")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Tự động chuyển lệnh delete thành update deleted = true
@SQLDelete(sql = "UPDATE material SET deleted = true WHERE id=?")
// Mặc định chỉ lấy các bản ghi chưa xóa
@Where(clause = "deleted = false")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.EAGER)
    private Unit unit;

    private int inventory;

    // Thêm trường này để đánh dấu xóa mềm
    @Column(nullable = false)
    private boolean deleted = false;
}