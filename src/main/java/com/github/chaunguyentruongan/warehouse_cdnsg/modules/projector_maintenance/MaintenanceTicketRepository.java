package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {

        @Query("SELECT DISTINCT t FROM MaintenanceTicket t " +
                        "WHERE (:kw IS NULL OR :kw = '' OR " +
                        "LOWER(t.technician) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "LOWER(t.ticketCode) LIKE LOWER(CONCAT('%', :kw, '%'))) AND " +
                        "(:status IS NULL OR :status = '' OR " +
                        "(:status = 'IN_PROGRESS' AND t.completionDate IS NULL) OR " +
                        "(:status = 'COMPLETED' AND t.completionDate IS NOT NULL))")
        Page<MaintenanceTicket> searchWithFilter(
                        @Param("kw") String kw,
                        @Param("status") String status,
                        Pageable pageable);
}