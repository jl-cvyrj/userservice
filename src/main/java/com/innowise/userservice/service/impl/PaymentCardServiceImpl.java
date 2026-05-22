package com.innowise.userservice.service.impl;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.specification.PaymentCardSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    @Autowired
    public PaymentCardRepository paymentCardRepository;

    @Autowired
    public UserRepository userRepository;

    public PaymentCard createPaymentCard(PaymentCard paymentCard) throws ServiceException {
        Long userId = paymentCard.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("User not found with id: " + userId));

        int activeCards = paymentCardRepository.countActiveCardsByUserId(userId);
        if (activeCards >= 5) {
            throw new ServiceException("User cannot have more than 5 active cards");
        }

        paymentCard.setUser(user);
        return paymentCardRepository.save(paymentCard);
    }

    public PaymentCard getPaymentCardById(Long id) throws ServiceException {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new ServiceException("PaymentCard not found with id: " + id));
    }

    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUser_Id(userId);
    }

    public Page<PaymentCard> getAllPaymentCards(String holder, Pageable pageable) {

        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecifications.hasHolder(holder));

        return paymentCardRepository.findAll(spec, pageable);
    }

    public PaymentCard updatePaymentCard(Long id, PaymentCard updatedPaymentCard) throws ServiceException {
        PaymentCard existingCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ServiceException("PaymentCard not found with id: " + id));

        if (updatedPaymentCard.getNumber() != null) {
            existingCard.setNumber(updatedPaymentCard.getNumber());
        }
        if (updatedPaymentCard.getHolder() != null) {
            existingCard.setHolder(updatedPaymentCard.getHolder());
        }
        if (updatedPaymentCard.getExpirationDate() != null) {
            existingCard.setExpirationDate(updatedPaymentCard.getExpirationDate());
        }
        existingCard.setActive(updatedPaymentCard.isActive());

        return paymentCardRepository.save(existingCard);
    }

    @Transactional
    public void setActiveStatus(Long id, boolean active) throws ServiceException {
        if (!paymentCardRepository.existsById(id)) {
            throw new ServiceException("PaymentCard not found with id: " + id);
        }
        paymentCardRepository.setActiveStatus(id, active);
    }
}
