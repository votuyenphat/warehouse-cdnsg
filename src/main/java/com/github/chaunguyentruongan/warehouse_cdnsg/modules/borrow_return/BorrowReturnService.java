package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BorrowReturnService {

    private final BorrowTicketRepository ticketRepository;
    private final JavaMailSender mailSender;
    private final BorrowItemService borrowItemService;

    @Transactional
    public BorrowTicket createTicket(BorrowRequestDTO request) {
        BorrowTicket ticket = BorrowTicket.builder()
                .borrowCode("M-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .items(request.getItems())
                .borrowerName(request.getBorrowerName())
                .department(request.getDepartment())
                .email(request.getEmail())
                .expectedReturnDate(request.getExpectedReturnDate())
                .status(TicketStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        BorrowTicket savedTicket = ticketRepository.save(ticket);
        
        for (BorrowTicketItem item : request.getItems()) {
            if (item.getItemId() != null) {
                borrowItemService.updateStockOnCreateTicket(item.getItemId(), item.getQuantity());
            }
        }
        
        sendHtmlEmail(savedTicket, "CREATE", true);
        return savedTicket;
    }

    @Transactional
    public BorrowTicket approveTicket(Long id) {
        BorrowTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Phiếu không ở trạng thái chờ duyệt");
        }

        for (BorrowTicketItem item : ticket.getItems()) {
            if (item.getItemId() != null) {
                borrowItemService.updateStockOnApproveTicket(item.getItemId(), item.getQuantity());
            }
        }

        ticket.setStatus(TicketStatus.BORROWED);
        ticket.setBorrowTime(LocalDateTime.now());
        ticket.setReturnCode("T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        BorrowTicket savedTicket = ticketRepository.save(ticket);
        sendHtmlEmail(savedTicket, "APPROVE", true);

        return savedTicket;
    }

    @Transactional
    public BorrowTicket rejectTicket(Long id, String reason) {
        BorrowTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new RuntimeException("Phiếu không ở trạng thái chờ duyệt");
        }

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setNote(reason);

        for (BorrowTicketItem item : ticket.getItems()) {
            if (item.getItemId() != null) {
                borrowItemService.updateStockOnRejectTicket(item.getItemId(), item.getQuantity());
            }
        }

        BorrowTicket savedTicket = ticketRepository.save(ticket);
        sendHtmlEmail(savedTicket, "REJECT", true);

        return savedTicket;
    }

    public BorrowTicket getTicketByReturnCode(String returnCode) {
        return ticketRepository.findByReturnCode(returnCode)
                .orElseThrow(() -> new RuntimeException("Mã phiếu trả không tồn tại hoặc đã bị xóa"));
    }

    @Transactional
    public BorrowTicket confirmReturn(ConfirmReturnDTO request) {
        BorrowTicket ticket;
        if (request.getReturnCode() != null && !request.getReturnCode().isEmpty()) {
            ticket = ticketRepository.findByReturnCode(request.getReturnCode())
                    .orElseThrow(() -> new RuntimeException("Mã phiếu trả không hợp lệ"));
        } else if (request.getTicketId() != null) {
            ticket = ticketRepository.findById(request.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        } else {
            throw new RuntimeException("Vui lòng cung cấp mã QR hoặc ID phiếu");
        }

        if (ticket.getStatus() != TicketStatus.BORROWED && ticket.getStatus() != TicketStatus.OVERDUE) {
            throw new RuntimeException("Trạng thái phiếu không hợp lệ để trả");
        }

        ticket.setReturnTime(LocalDateTime.now());
        ticket.setNote(request.getGeneralNote());

        boolean isEnough = true;

        if (request.getItems() != null) {
            for (BorrowTicketItem ticketItem : ticket.getItems()) {
                ItemReturnRequest reqItem = request.getItems().stream()
                        .filter(i -> i.getItemId().equals(ticketItem.getItemId()))
                        .findFirst()
                        .orElse(null);

                if (reqItem != null) {
                    ticketItem.setReturnedQuantity(reqItem.getReturnedQuantity());
                    ticketItem.setBrokenQuantity(reqItem.getBrokenQuantity());
                    ticketItem.setConditionNote(reqItem.getConditionNote());

                    int missing = ticketItem.getQuantity() - (reqItem.getReturnedQuantity() + reqItem.getBrokenQuantity());
                    if (reqItem.getBrokenQuantity() > 0 || missing > 0) {
                        isEnough = false;
                    }

                    borrowItemService.updateStockOnReturnTicketDetailed(
                            ticketItem.getItemId(),
                            reqItem.getReturnedQuantity(),
                            reqItem.getBrokenQuantity()
                    );
                }
            }
        }

        if (isEnough) {
            ticket.setStatus(TicketStatus.RETURNED);
            sendHtmlEmail(ticket, "RETURN", true);
        } else {
            ticket.setStatus(TicketStatus.INCOMPLETE);
            sendHtmlEmail(ticket, "RETURN", false);
        }

        return ticketRepository.save(ticket);
    }

    @Transactional
    public BorrowTicket resolveIncomplete(Long ticketId) {
        BorrowTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (ticket.getStatus() != TicketStatus.INCOMPLETE) {
            throw new RuntimeException("Phiếu này không ở trạng thái báo thiếu");
        }

        ticket.setStatus(TicketStatus.RETURNED);
        ticket.setNote((ticket.getNote() != null ? ticket.getNote() : "") + " | [Hệ thống]: Đã thu hồi đủ đồ bổ sung.");

        for (BorrowTicketItem item : ticket.getItems()) {
            if (item.getItemId() != null) {
                int returned = item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0;
                int broken = item.getBrokenQuantity() != null ? item.getBrokenQuantity() : 0;
                int missing = Math.max(0, item.getQuantity() - (returned + broken));
                borrowItemService.updateStockOnResolveIncomplete(item.getItemId(), missing, broken);
                item.setReturnedQuantity(item.getQuantity());
                item.setBrokenQuantity(0);
            }
        }

        sendHtmlEmail(ticket, "RETURN", true);
        return ticketRepository.save(ticket);
    }

    public Page<BorrowTicket> findAll(String keyword, TicketStatus status, Pageable pageable) {
        ticketRepository.updateOverdueTickets(java.time.LocalDate.now());
        return ticketRepository.searchWithFilter(keyword, status, pageable);
    }

    private void sendHtmlEmail(BorrowTicket ticket, String type, boolean isEnough) {
        try {
            if (ticket.getEmail() == null || ticket.getEmail().isEmpty())
                return;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(ticket.getEmail());

            String subject = "";
            String title = "";
            String statusText = "";
            String qrContent = ticket.getReturnCode() != null ? ticket.getReturnCode() : "";
            String extraNote = "";
            boolean showQr = false;

            if (type.equals("CREATE")) {
                subject = "Xác nhận tạo phiếu mượn vật tư - " + ticket.getBorrowCode();
                title = "TẠO PHIẾU MƯỢN THÀNH CÔNG";
                statusText = "Yêu cầu mượn vật phẩm của bạn đã được ghi nhận và đang chờ quản lý duyệt. Chúng tôi sẽ thông báo lại khi đơn được duyệt.";
                showQr = false;
            } else if (type.equals("APPROVE")) {
                subject = "Đơn mượn vật tư đã được duyệt - " + ticket.getBorrowCode();
                title = "ĐƠN ĐÃ ĐƯỢC DUYỆT";
                statusText = "Đơn mượn vật phẩm của bạn đã được duyệt thành công. Vui lòng giữ Mã Trả Đồ (QR) bên dưới để hoàn trả sau khi sử dụng xong.";
                showQr = true;
            } else if (type.equals("REJECT")) {
                subject = "Đơn mượn vật tư bị từ chối - " + ticket.getBorrowCode();
                title = "ĐƠN BỊ TỪ CHỐI";
                statusText = "Rất tiếc, yêu cầu mượn vật phẩm của bạn đã bị từ chối.";
                extraNote = "<div style='background-color: #fee2e2; padding: 12px; border-left: 4px solid #ef4444; margin-bottom: 15px;'><strong style='color: #b91c1c;'>Lý do từ chối:</strong> <span style='color: #7f1d1d;'>"
                        + ticket.getNote() + "</span></div>";
                showQr = false;
            } else if (type.equals("RETURN")) {
                if (isEnough) {
                    subject = "Hoàn tất trả vật tư - " + ticket.getReturnCode();
                    title = "HOÀN TẤT TRẢ VẬT TƯ";
                    statusText = "Cảm ơn bạn, hệ thống ghi nhận bạn đã hoàn tất việc trả vật phẩm đầy đủ và nguyên vẹn.";
                    showQr = true;
                } else {
                    subject = "Cảnh báo: Trả thiếu/Hư hỏng vật tư - " + ticket.getReturnCode();
                    title = "CẢNH BÁO: TRẢ THIẾU VẬT TƯ";
                    statusText = "Hệ thống ghi nhận bạn đã trả vật phẩm nhưng bị THIẾU hoặc HƯ HỎNG. Vui lòng liên hệ thủ kho để bổ sung/xử lý sớm nhất.";
                    extraNote = "<div style='background-color: #fee2e2; padding: 12px; border-left: 4px solid #ef4444; margin-bottom: 15px;'><strong style='color: #b91c1c;'>Ghi chú từ thủ kho:</strong> <span style='color: #7f1d1d;'>"
                            + ticket.getNote() + "</span></div>";
                    showQr = true;
                }
            }

            helper.setSubject(subject);
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + qrContent;
            String itemsStr = ticket.getItems().stream()
                    .map(i -> i.getItemName() + " (x" + i.getQuantity() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));

            String qrSection = showQr ? "    <div style='text-align: center; margin-top: 30px;'>"
                    + "      <p style='font-size: 13px; color: #94a3b8; margin-bottom: 12px; text-transform: uppercase; font-weight: bold; letter-spacing: 0.5px;'>Mã QR xác thực của bạn</p>"
                    + "      <img src='" + qrUrl
                    + "' alt='QR Code' style='border: 1px solid #e2e8f0; border-radius: 12px; padding: 10px; background: white;'/>"
                    + "    </div>" : "";

            String htmlContent = String.format(
                    "<div style='font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"
                            + "  <div style='background-color: #1a237e; padding: 25px; text-align: center; color: white;'>"
                            + "    <h2 style='margin: 0; font-size: 20px; letter-spacing: 1px;'>%s</h2>"
                            + "  </div>"
                            + "  <div style='padding: 30px; line-height: 1.6; color: #334155;'>"
                            + "    <p style='font-size: 16px;'>Xin chào <strong>%s</strong>,</p>"
                            + "    <p style='font-size: 15px;'>%s</p>"
                            + "    %s"
                            + "    <div style='background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-top: 20px;'>"
                            + "      <table style='width: 100%%; border-collapse: collapse;'>"
                            + "        <tr><td style='padding: 10px 0; border-bottom: 1px dashed #cbd5e1; color: #64748b;'>Vật phẩm mượn:</td><td style='text-align: right; font-weight: bold; font-size: 15px;'>%s</td></tr>"
                            + "        <tr><td style='padding: 10px 0; border-bottom: 1px dashed #cbd5e1; color: #64748b;'>Số lượng:</td><td style='text-align: right; font-weight: bold; font-size: 15px; color: #4338ca;'>%d</td></tr>"
                            + "        <tr><td style='padding: 10px 0; color: #64748b;'>Mã giao dịch:</td><td style='text-align: right; font-weight: 900; font-size: 16px; color: #1a237e;'>%s</td></tr>"
                            + "      </table>"
                            + "    </div>"
                            + "%s"
                            + "  </div>"
                            + "</div>",
                    title, ticket.getBorrowerName(), statusText, extraNote,
                    itemsStr,
                    ticket.getItems().size(), ticket.getBorrowCode(), qrSection);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    public void sendManualEmail(ManualEmailRequest request) {
        try {
            for (String email : request.getToEmails()) {
                if (email == null || email.isEmpty()) continue;
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(request.getSubject());
                
                String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>"
                    + "  <div style='background-color: #f43f5e; color: white; padding: 15px; text-align: center; border-radius: 8px 8px 0 0;'>"
                    + "    <h2 style='margin: 0;'>THÔNG BÁO TỪ QUẢN TRỊ VIÊN</h2>"
                    + "  </div>"
                    + "  <div style='padding: 20px; color: #333; line-height: 1.6;'>"
                    + "    <p>%s</p>"
                    + "  </div>"
                    + "</div>", request.getContent().replace("\n", "<br/>"));
                
                helper.setText(htmlContent, true);
                mailSender.send(message);
            }
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail thủ công: " + e.getMessage());
        }
    }

    @Transactional
    public BorrowTicket updateTicket(Long id, UpdateBorrowTicketDTO request) {
        BorrowTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        if (request.getBorrowerName() != null)
            ticket.setBorrowerName(request.getBorrowerName());
        if (request.getDepartment() != null)
            ticket.setDepartment(request.getDepartment());
        if (request.getNote() != null)
            ticket.setNote(request.getNote());
        if (request.getStatus() != null) {
            // Logic cập nhật tồn kho nếu status thay đổi cũng nên được xem xét ở đây,
            // nhưng để đơn giản ta chỉ cập nhật status.
            ticket.setStatus(request.getStatus());
        }

        switch (ticket.getStatus()) {
            case RETURNED:
                sendHtmlEmail(ticket, "RETURN", true);
                break;
            case INCOMPLETE:
                sendHtmlEmail(ticket, "RETURN", false);
                break;
            default:
                break;
        }

        return ticketRepository.save(ticket);
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}