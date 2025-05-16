package com.donut.donutpdf.Dto;

import com.donut.donutpdf.Entity.Question;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class QuestionDto {
    private String questionText;
    private String answerText;
    private Long documentId;
    private String documentName;
    private String createdAt;

    public QuestionDto(Question question) {
        this.questionText = question.getQuestionText();
        this.answerText = question.getAnswerText();
        this.documentId = question.getDocument().getId();
        this.documentName = question.getDocument().getFileName();
        this.createdAt = formatDateTime(question.getCreatedAt());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}

