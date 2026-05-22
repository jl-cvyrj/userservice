package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    List<PaymentCard> findByUser_Id(Long userId);
    @Modifying
    @Transactional
    @Query ("update PaymentCard u set u.active = :active where u.id = :id")
    void setActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Query(value = "SELECT COUNT(*) FROM payment_cards WHERE user_id = :userId AND active = true", nativeQuery = true)
    int countActiveCardsByUserId(@Param("userId") Long userId);
}
