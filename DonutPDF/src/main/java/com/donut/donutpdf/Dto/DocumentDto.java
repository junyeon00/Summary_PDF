package com.donut.donutpdf.Dto;

import com.donut.donutpdf.Entity.Document;
import com.donut.donutpdf.Entity.User;
import lombok.Getter;
import lombok.Setter;

// 파일 정보를 레포지토리에 저장할때 쓰이는 DTO
@Getter
@Setter
public class DocumentDto {
    private String fileName;
    private String extractedText;
    private String summary;

    // owner는 Controller 또는 Service에서 주입할 예정
    public Document toEntity(User owner) {
        Document doc = new Document();
        doc.setFileName(this.fileName);
        doc.setExtractedText(this.extractedText);
        doc.setSummary(this.summary);
        doc.setOwner(owner);  // 외부에서 받은 User 객체
        return doc;
    }

    public DocumentDto(String fileName, String extractedText, String summary) {
        this.fileName = fileName;
        this.extractedText = extractedText;
        this.summary = summary;
    }
}
