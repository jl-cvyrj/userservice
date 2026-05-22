package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.*;
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
    public ResponseEntity<PaymentCardDto> createCard(@Valid @RequestBody PaymentCardDto cardDto) throws BusinessLogicException, ResourceNotFoundException {
        PaymentCard card = paymentCardMapper.toEntity(cardDto);
        PaymentCard savedCard = paymentCardService.createPaymentCard(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardMapper.toDto(savedCard));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getCardById(@PathVariable Long id) throws ResourceNotFoundException {
        PaymentCard card = paymentCardService.getPaymentCardById(id);
        return ResponseEntity.ok(paymentCardMapper.toDto(card));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCard> cards = paymentCardService.getCardsByUserId(userId);
        List<PaymentCardDto> cardDtos = cards.stream()
                .map(paymentCardMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> updateCard(@PathVariable Long id, @Valid @RequestBody PaymentCardDto cardDto) throws ResourceNotFoundException {
        PaymentCard card = paymentCardMapper.toEntity(cardDto);
        PaymentCard updatedCard = paymentCardService.updatePaymentCard(id, card);
        return ResponseEntity.ok(paymentCardMapper.toDto(updatedCard));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ResourceNotFoundException {
        paymentCardService.setActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}