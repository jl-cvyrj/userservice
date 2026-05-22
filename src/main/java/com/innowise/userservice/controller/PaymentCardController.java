package com.innowise.userservice.controller;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardServiceImpl paymentCardService;

    @PostMapping
    public PaymentCard createCard(@RequestBody PaymentCard card) throws ServiceException {
        return paymentCardService.createPaymentCard(card);
    }

    @GetMapping("/{id}")
    public PaymentCard getCardById(@PathVariable Long id) throws ServiceException {
        return paymentCardService.getPaymentCardById(id);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentCard> getCardsByUserId(@PathVariable Long userId) {
        return paymentCardService.getCardsByUserId(userId);
    }

    @PutMapping("/{id}")
    public PaymentCard updateCard(@PathVariable Long id, @RequestBody PaymentCard card) throws ServiceException {
        return paymentCardService.updatePaymentCard(id, card);
    }

    @PatchMapping("/{id}/active")
    public void setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ServiceException {
        paymentCardService.setActiveStatus(id, active);
    }
}