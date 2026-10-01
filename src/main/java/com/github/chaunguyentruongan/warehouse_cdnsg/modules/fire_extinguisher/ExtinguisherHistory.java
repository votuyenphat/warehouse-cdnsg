package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "fire_extinguisher_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtinguisherHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "extinguisher_id")
    @JsonBackReference
    private FireExtinguisher extinguisher;

    private LocalDate rechargeDate; // Ngày thực hiện nạp

    private LocalDate nextRechargeDate; // Hạn nạp lần tới

    private String note; // Ghi chú (VD: Đơn vị nạp ABC, Thay vòi phun...)
}