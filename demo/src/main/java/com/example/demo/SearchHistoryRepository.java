package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;



import java.time.LocalDateTime;


public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // Find all search history for a specific user
    List<SearchHistory> findByUser(User user);

    // Find search history for a user within a date range
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user AND sh.searchDate BETWEEN :startDate AND :endDate")
    List<SearchHistory> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Count total searches by a user
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.user = :user")
    Long countByUser(@Param("user") User user);

    // Sum total credits spent by a user
    @Query("SELECT SUM(sh.creditsDeducted) FROM SearchHistory sh WHERE sh.user = :user")
    Integer sumCreditsDeductedByUser(@Param("user") User user);



    // Basic queries
    List<SearchHistory> findByUserId(Long userId);
    List<SearchHistory> findByLinkedinUrl(String url);

    // Advanced queries
    List<SearchHistory> findBySearchDateBetween(LocalDateTime start, LocalDateTime end);
    List<SearchHistory> findByCreditsDeductedGreaterThan(Integer credits);

    // Pagination
    Page<SearchHistory> findByUserId(Long userId, Pageable pageable);

    // Custom JPQL
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user.id = :userId ORDER BY sh.searchDate DESC")
    List<SearchHistory> findRecentSearchesByUser(@Param("userId") Long userId);

    @Query("SELECT NEW map(sh.linkedinUrl as url, COUNT(sh) as count) " +
            "FROM SearchHistory sh " +
            "GROUP BY sh.linkedinUrl " +
            "ORDER BY count DESC")
    List<Object[]> getSearchCountByUrl();

    // Native query example
    @Query(value = "SELECT * FROM search_history WHERE user_id = :userId AND credits_deducted > :minCredits",
            nativeQuery = true)
    List<SearchHistory> findHighCreditSearches(@Param("userId") Long userId, @Param("minCredits") Integer minCredits);
}