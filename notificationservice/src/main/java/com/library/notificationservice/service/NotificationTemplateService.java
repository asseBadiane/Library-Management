package com.library.notificationservice.service;

import com.library.notificationservice.dto.BookDto;
import com.library.notificationservice.dto.UserDto;
import com.library.notificationservice.event.BorrowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {
    
    private final TemplateEngine templateEngine;
    
    public String generateEmailContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
    
    public String generateUserWelcomeEmail(UserDto user) {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("lastName", user.getLastName());
        context.setVariable("email", user.getEmail());
        return templateEngine.process("welcome-email", context);
    }
    
    public String generateBorrowApprovalEmail(UserDto user, BookDto book, BorrowEvent event) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName() + " " + user.getLastName());
        context.setVariable("bookTitle", book.getTitle());
        context.setVariable("bookAuthor", book.getAuthor());
        context.setVariable("eventDate", event.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return templateEngine.process("borrow-approval-email", context);
    }
    
    public String generateOverdueNotificationEmail(UserDto user, BookDto book, BorrowEvent event) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName() + " " + user.getLastName());
        context.setVariable("bookTitle", book.getTitle());
        context.setVariable("bookAuthor", book.getAuthor());
        context.setVariable("eventDate", event.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return templateEngine.process("overdue-notification-email", context);
    }
    
    public String generateDueSoonNotificationEmail(UserDto user, BookDto book, BorrowEvent event) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName() + " " + user.getLastName());
        context.setVariable("bookTitle", book.getTitle());
        context.setVariable("bookAuthor", book.getAuthor());
        context.setVariable("dueDate", event.getDetails());
        return templateEngine.process("due-soon-notification-email", context);
    }
}