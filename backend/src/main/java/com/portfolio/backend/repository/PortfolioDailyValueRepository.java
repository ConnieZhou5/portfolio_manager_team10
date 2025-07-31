package com.portfolio.backend.repository;

import com.portfolio.backend.model.PortfolioDailyValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PortfolioDailyValueRepository extends JpaRepository<PortfolioDailyValue, Long> {
    
    /**
     * Find the most recent daily value before a given date
     * 
     * @param date The date to find the previous snapshot for
     * @return Optional containing the most recent daily value before the given date
     */
    Optional<PortfolioDailyValue> findTopBySnapshotDateBeforeOrderBySnapshotDateDesc(LocalDate date);
    
    /**
     * Find daily value for a specific date
     * 
     * @param date The specific date to find
     * @return Optional containing the daily value for the given date
     */
    Optional<PortfolioDailyValue> findBySnapshotDate(LocalDate date);
    
    /**
     * Check if a daily value exists for a specific date
     * 
     * @param date The date to check
     * @return true if a daily value exists for the given date
     */
    boolean existsBySnapshotDate(LocalDate date);
    
    /**
     * Delete all snapshots older than the specified date
     * 
     * @param cutoffDate The cutoff date (snapshots older than this will be deleted)
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM PortfolioDailyValue pdv WHERE pdv.snapshotDate < :cutoffDate")
    int deleteSnapshotsOlderThan(@Param("cutoffDate") LocalDate cutoffDate);
} 