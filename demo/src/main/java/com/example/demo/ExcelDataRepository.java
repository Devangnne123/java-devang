package com.example.demo;

import com.example.demo.ExcelData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ExcelDataRepository extends JpaRepository<ExcelData, Long> {
    boolean existsByLinkedinId(String linkedinId);
    ExcelData findByLinkedinUrl(String linkedinUrl);
}
