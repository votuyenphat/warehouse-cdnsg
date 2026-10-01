package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowSchedulerService {

    private final BorrowTicketRepository ticketRepository;
    private final JavaMailSender mailSender;

    // Chạy mỗi ngày lúc 8:00 sáng
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendOverdueReminders() {
        // Tạm thời lấy các phiếu đang mượn có thời gian mượn quá 7 ngày
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);
        List<BorrowTicket> overdueTickets = ticketRepository.findOverdueTickets(limitDate);

        for (BorrowTicket ticket : overdueTickets) {
            // Đổi trạng thái sang OVERDUE
            ticket.setStatus(TicketStatus.OVERDUE);
            ticketRepository.save(ticket);

            // Gửi email
            sendReminderEmail(ticket, "Cảnh báo: Vật phẩm đã quá hạn trả");
        }
    }

    private void sendReminderEmail(BorrowTicket ticket, String title) {
        if (ticket.getEmail() == null || ticket.getEmail().isEmpty()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(ticket.getEmail());
            helper.setSubject(title + " - " + ticket.getBorrowCode());

            String itemsStr = ticket.getItems().stream()
                    .map(i -> i.getItemName() + " (x" + i.getQuantity() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));

            String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                            + "<h2 style='color: #ef4444; text-align: center;'>%s</h2>"
                            + "<p>Xin chào <strong>%s</strong>,</p>"
                            + "<p>Hệ thống ghi nhận bạn có các vật phẩm đã quá hạn hoàn trả. Vui lòng mang đồ đến trả lại hoặc liên hệ thủ kho sớm nhất có thể.</p>"
                            + "<ul><li><strong>Vật phẩm:</strong> %s</li></ul>"
                            + "</div>",
                    title, ticket.getBorrowerName(), itemsStr);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail nhắc nhở: " + e.getMessage());
        }
    }
}
