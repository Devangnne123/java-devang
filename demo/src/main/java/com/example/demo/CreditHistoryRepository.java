package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
    List<CreditHistory> findByUserIdOrderByTimestampDesc(Long userId);

    List<CreditHistory> findByUserIdAndAdminEmailOrderByTimestampDesc(Long userId, String adminEmail);

    // Find all credit adjustments made by a specific admin
    List<CreditHistory> findByAdminEmailOrderByTimestampDesc(String adminEmail);



    List<CreditHistory> findByUserOrderByTimestampDesc(User user);
    List<CreditHistory> findAllByOrderByTimestampDesc();







}