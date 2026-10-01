package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExtinguisherHistoryRepository extends JpaRepository<ExtinguisherHistory, Long> {
    // Lấy lịch sử nạp của 1 bình, sắp xếp theo ngày nạp mới nhất
    List<ExtinguisherHistory> findByExtinguisherIdOrderByRechargeDateDesc(Long extinguisherId);
}