package com.donut.donutpdf.Controller;

import com.donut.donutpdf.Dto.DocumentDto;
import com.donut.donutpdf.Dto.QuestionDto;
import com.donut.donutpdf.Entity.Document;
import com.donut.donutpdf.Entity.Question;
import com.donut.donutpdf.Entity.User;
import com.donut.donutpdf.Service.GeminiService;
import com.donut.donutpdf.Service.UserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeminiController {
    private final GeminiService geminiService;
    private final UserService userService;

    public GeminiController(GeminiService geminiService, UserService userService) {
        this.geminiService = geminiService;
        this.userService = userService;
    }

    @GetMapping("/download-summary/{id}")
    public ResponseEntity<Resource> downloadSummary(@PathVariable Long id, Principal principal) throws IOException {
        Document doc = geminiService.getDocumentById(id);
        if (!doc.getOwner().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        File summaryPdf = geminiService.saveSummaryToPdf(doc.getSummary(), doc.getFileName());
        Resource resource = new FileSystemResource(summaryPdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + summaryPdf.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("/documents/{id}/ask")
    public ResponseEntity<String> askQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        String question = body.get("question");
        String username = principal.getName();

        String answer = geminiService.answerQuestion(id, question, username);
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/users/me/questions")
    public ResponseEntity<List<QuestionDto>> getUserQuestions(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        List<Question> questions = geminiService.getQuestions(user);

        List<QuestionDto> result = questions.stream()
                .map(QuestionDto::new)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/summarizeFile")
    public ResponseEntity<String> handlePdfUpload(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String extractedText = stripper.getText(document);
        document.close();
        String prompt = "다음 문서를 요약해줘:\n" + extractedText;
        String summary = geminiService.summarize(prompt); // 직접 구현한 GeminiService 사용
        DocumentDto documentDto = new DocumentDto(file.getOriginalFilename(), extractedText, summary);
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Document doc = documentDto.toEntity(user);
        geminiService.save(doc);

        return ResponseEntity.ok(summary);
    }
}