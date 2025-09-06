package com.instagram.businessdiscovery.repository;

import com.instagram.businessdiscovery.domain.BusinessDiscoverySearch;
import com.instagram.businessdiscovery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessDiscoverySearchRepository extends JpaRepository<BusinessDiscoverySearch, Long> {
    
    List<BusinessDiscoverySearch> findBySearcherUserOrderByCreatedAtDesc(User searcherUser);
    
    Optional<BusinessDiscoverySearch> findBySearcherUserAndTargetUsername(User searcherUser, String targetUsername);
    
    @Query("SELECT bds FROM BusinessDiscoverySearch bds WHERE bds.searcherUser = :user AND bds.createdAt >= :since")
    List<BusinessDiscoverySearch> findRecentSearchesByUser(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(bds) FROM BusinessDiscoverySearch bds WHERE bds.searcherUser = :user AND bds.createdAt >= :since")
    Long countSearchesByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    List<BusinessDiscoverySearch> findByTargetUsernameOrderByCreatedAtDesc(String targetUsername);
}