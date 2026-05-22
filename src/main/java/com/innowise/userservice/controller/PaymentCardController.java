package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.ServiceException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardServiceImpl paymentCardService;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @PostMapping
    public PaymentCardDto createCard(@Valid @RequestBody PaymentCardDto cardDto) throws ServiceException {
        PaymentCard card = paymentCardMapper.toEntity(cardDto);
        PaymentCard savedCard = paymentCardService.createPaymentCard(card);
        return paymentCardMapper.toDto(savedCard);
    }

    @GetMapping("/{id}")
    public PaymentCardDto getCardById(@PathVariable Long id) throws ServiceException {
        PaymentCard card = paymentCardService.getPaymentCardById(id);
        return paymentCardMapper.toDto(card);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentCardDto> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCard> cards = paymentCardService.getCardsByUserId(userId);
        return cards.stream()
                .map(paymentCardMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public PaymentCardDto updateCard(@PathVariable Long id, @Valid @RequestBody PaymentCardDto cardDto) throws ServiceException {
        PaymentCard card = paymentCardMapper.toEntity(cardDto);
        PaymentCard updatedCard = paymentCardService.updatePaymentCard(id, card);
        return paymentCardMapper.toDto(updatedCard);
    }

    @PatchMapping("/{id}/active")
    public void setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ServiceException {
        paymentCardService.setActiveStatus(id, active);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleServiceException(ServiceException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}