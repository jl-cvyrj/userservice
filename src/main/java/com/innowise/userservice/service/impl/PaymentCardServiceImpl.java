package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.BusinessLogicException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.specification.PaymentCardSpecifications;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    public final PaymentCardRepository paymentCardRepository;
    public final UserRepository userRepository;

    public static final int MAX_PAYMENT_CARDS = 5;

    public PaymentCardServiceImpl(PaymentCardRepository paymentCardRepository, UserRepository userRepository) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "user", key = "#paymentCardDto.userId")
    public PaymentCard createPaymentCard(PaymentCardDto paymentCardDto) throws BusinessLogicException, ResourceNotFoundException {

        Long userId = paymentCardDto.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        int paymentCards = paymentCardRepository.countPaymentCardsByUserId(userId);
        if (paymentCards >= MAX_PAYMENT_CARDS) {
            throw new BusinessLogicException("User cannot have more than 5 cards");
        }

        PaymentCard paymentCard = new PaymentCard(user,
                paymentCardDto.getNumber(),
                paymentCardDto.getHolder(),
                paymentCardDto.getExpirationDate(),
                paymentCardDto.isActive());

        return paymentCardRepository.save(paymentCard);
    }

    public PaymentCard getPaymentCardById(Long id) throws ResourceNotFoundException {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentCard not found with id: " + id));
    }

    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUser_Id(userId);
    }

    public Page<PaymentCard> getAllPaymentCards(String holder, Pageable pageable) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecifications.hasHolder(holder));
        return paymentCardRepository.findAll(spec, pageable);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#result.user.id")
    public PaymentCard updatePaymentCard(Long id, PaymentCardDto updatedPaymentCardDto) throws ResourceNotFoundException {
        PaymentCard existingCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentCard not found with id: " + id));

        if (updatedPaymentCardDto.getNumber() != null) {
            existingCard.setNumber(updatedPaymentCardDto.getNumber());
        }
        if (updatedPaymentCardDto.getHolder() != null) {
            existingCard.setHolder(updatedPaymentCardDto.getHolder());
        }
        if (updatedPaymentCardDto.getExpirationDate() != null) {
            existingCard.setExpirationDate(updatedPaymentCardDto.getExpirationDate());
        }
        existingCard.setActive(updatedPaymentCardDto.isActive());

        return paymentCardRepository.save(existingCard);
    }

    @Transactional
    @CacheEvict(value = "user", key = "#id")
    public void setActiveStatus(Long id, boolean active) throws ResourceNotFoundException {
        if (!paymentCardRepository.existsById(id)) {
            throw new ResourceNotFoundException("PaymentCard not found with id: " + id);
        }
        paymentCardRepository.setActiveStatus(id, active);
    }
}