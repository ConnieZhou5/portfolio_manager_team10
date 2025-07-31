package com.portfolio.backend.repository;

import com.portfolio.backend.model.PortfolioMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioMonthlySummaryRepository extends JpaRepository<PortfolioMonthlySummary, Long> {
    
    // Find monthly summary by year and month
    Optional<PortfolioMonthlySummary> findByYearAndMonth(Integer year, Integer month);
    
    // Find all summaries for a specific year
    List<PortfolioMonthlySummary> findByYearOrderByMonth(Integer year);
    
    // Find all summaries for the last 12 months
    @Query("SELECT p FROM PortfolioMonthlySummary p WHERE (p.year = :currentYear AND p.month >= :currentMonth) OR (p.year = :currentYear - 1 AND p.month < :currentMonth) ORDER BY p.year, p.month")
    List<PortfolioMonthlySummary> findLast12Months(@Param("currentYear") Integer currentYear, @Param("currentMonth") Integer currentMonth);
    
    // Find all summaries for the last year (12 months from current date)
    @Query("SELECT p FROM PortfolioMonthlySummary p WHERE p.year >= :startYear ORDER BY p.year, p.month")
    List<PortfolioMonthlySummary> findLastYear(@Param("startYear") Integer startYear);
    
    // Check if summary exists for a specific year and month
    boolean existsByYearAndMonth(Integer year, Integer month);
} 