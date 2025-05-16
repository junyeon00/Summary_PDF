package com.donut.donutpdf.Repository;

import com.donut.donutpdf.Entity.Document;
import com.donut.donutpdf.Entity.Question;
import com.donut.donutpdf.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByUser(User user);
    List<Question> findByDocument(Document document);
}
