package com.innowise.userservice.service;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCard createPaymentCard(PaymentCard paymentCard) throws ServiceException;

    PaymentCard getPaymentCardById(Long id) throws ServiceException;

    PaymentCard updatePaymentCard(Long id, PaymentCard updatedCard) throws ServiceException;

    Page<PaymentCard> getAllPaymentCards(String holder, Pageable pageable);

    void setActiveStatus(Long id, boolean active) throws ServiceException;

    List<PaymentCard> getCardsByUserId(Long userId);
}