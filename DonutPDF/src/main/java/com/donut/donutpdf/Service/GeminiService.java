package com.donut.donutpdf.Service;

import com.donut.donutpdf.Entity.Document;
import com.donut.donutpdf.Entity.Question;
import com.donut.donutpdf.Entity.User;
import com.donut.donutpdf.Repository.DocumentRepository;
import com.donut.donutpdf.Repository.QuestionRepository;
import com.donut.donutpdf.Repository.UserRepository;
import com.donut.donutpdf.Util.GeminiApiClient;
import jakarta.persistence.EntityNotFoundException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class GeminiService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final GeminiApiClient geminiApiClient;

    public GeminiService(DocumentRepository documentRepository, UserRepository userRepository, GeminiApiClient geminiApiClient, QuestionRepository questionRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.geminiApiClient = geminiApiClient;
        this.questionRepository = questionRepository;
    }

    public String summarize(String extractedText) {
        String prompt = """
                다음은 사용자가 업로드한 문서의 본문입니다. 이 문서를 한글로 핵심 내용만 간결하게 요약해 주세요. 
                
                문서:
                %s
                """.formatted(extractedText);

        return geminiApiClient.sendPrompt(prompt); // 실제 Gemini API 호출
    }

    // 글자 추출
    public String extractText(MultipartFile file) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/share/tessdata"); // 실제 경로 확인
        tesseract.setLanguage("kor+eng");

        String tempDir = System.getProperty("java.io.tmpdir");
        File convPdf = new File(tempDir, file.getOriginalFilename());
        file.transferTo(convPdf);

        File imageFile = null;
        try {
            imageFile = convertPdfToImage(convPdf); // PDF → PNG 변환
            String extractedText = tesseract.doOCR(imageFile).trim();
            return extractedText;
        } catch (Exception e) {
            throw new Exception("OCR 처리 중 오류 발생", e);
        } finally {
            if (convPdf.exists()) convPdf.delete();
            if (imageFile != null && imageFile.exists()) imageFile.delete();
        }
    }

    public File saveSummaryToPdf(String summary, String fileName) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(50, 750);

        // 줄바꿈 처리
        for (String line : summary.split("\n")) {
            contentStream.showText(line);
            contentStream.newLine();
        }

        contentStream.endText();
        contentStream.close();

        Path outputPath = Paths.get("summaries", fileName + "_summary.pdf");
        Files.createDirectories(outputPath.getParent());
        document.save(outputPath.toFile());
        document.close();

        return outputPath.toFile();
    }

    public void save(Document document){
        documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));
    }

    public String answerQuestion(Long documentId, String questionText, String username) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("문서 없음"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String summary = doc.getSummary();

        String prompt = """
            아래는 문서 요약입니다:
            ---
            %s
            ---
            위 문서 요약을 참고하여 다음 질문에 답해주세요.
            질문: %s
            답변:
            """.formatted(summary, questionText);

        String answer = geminiApiClient.sendPrompt(prompt);

        Question question = new Question();
        question.setQuestionText(questionText);
        question.setAnswerText(answer);
        question.setDocument(doc);
        question.setUser(user);

        questionRepository.save(question);

        return answer;
    }

    public List<Question> getQuestions(User user){
        return questionRepository.findByUser(user);
    }

    public File convertPdfToImage(File pdfFile) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        // 첫 페이지만 변환 (여러 페이지 변환하려면 반복문 사용)
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300); // 300 DPI 권장

        String imagePath = pdfFile.getParent() + "/" + pdfFile.getName() + ".png";
        File imageFile = new File(imagePath);
        ImageIO.write(bim, "png", imageFile);

        document.close();
        return imageFile;
    }
}
