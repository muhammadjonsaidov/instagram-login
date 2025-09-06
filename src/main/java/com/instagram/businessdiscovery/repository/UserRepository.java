package com.instagram.businessdiscovery.repository;

import com.instagram.businessdiscovery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByInstagramId(String instagramId);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByInstagramId(String instagramId);
    
    boolean existsByUsername(String username);
    
    /*@Query("SELECT u FROM User u WHERE u.isBusinessAccount = true ORDER BY u.followersCount DESC")
    java.util.List<User> findBusinessAccountsOrderByFollowers();*/
    
    @Query("SELECT u FROM User u WHERE u.followersCount >= :minFollowers ORDER BY u.followersCount DESC")
    java.util.List<User> findUsersWithMinFollowers(@Param("minFollowers") Integer minFollowers);
}