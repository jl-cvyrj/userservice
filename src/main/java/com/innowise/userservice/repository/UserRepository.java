package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Transactional
    @Query ("update User u set u.active = :active where u.id = :id")
    void setActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @EntityGraph(attributePaths = "paymentCards")
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithCards(Long id);

    boolean existsByEmail(String email);
}