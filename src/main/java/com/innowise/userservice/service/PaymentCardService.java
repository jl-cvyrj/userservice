package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.BusinessLogicException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCard createPaymentCard(PaymentCardDto paymentCardDto) throws BusinessLogicException, ResourceNotFoundException;

    PaymentCard getPaymentCardById(Long id) throws ResourceNotFoundException;

    PaymentCard updatePaymentCard(Long id, PaymentCardDto updatedPaymentCardDto) throws ResourceNotFoundException;

    Page<PaymentCard> getAllPaymentCards(String holder, Pageable pageable);

    void setActiveStatus(Long id, boolean active) throws ResourceNotFoundException;

    List<PaymentCard> getCardsByUserId(Long userId);
}