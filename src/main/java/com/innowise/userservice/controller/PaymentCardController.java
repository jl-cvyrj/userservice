package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @PostMapping
    public ResponseEntity<PaymentCardDto> createCard(@Valid @RequestBody PaymentCardDto paymentCardDto) throws BusinessLogicException, ResourceNotFoundException {
        PaymentCard savedCard = paymentCardService.createPaymentCard(paymentCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardMapper.toDto(savedCard));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getCardById(@PathVariable Long id) throws ResourceNotFoundException {
        PaymentCard card = paymentCardService.getPaymentCardById(id);
        return ResponseEntity.ok(paymentCardMapper.toDto(card));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> updateCard(@PathVariable Long id, @Valid @RequestBody PaymentCardDto cardDto) throws ResourceNotFoundException {
        PaymentCard updatedCard = paymentCardService.updatePaymentCard(id, cardDto);
        return ResponseEntity.ok(paymentCardMapper.toDto(updatedCard));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActiveStatus(@PathVariable Long id, @RequestParam boolean active) throws ResourceNotFoundException {
        paymentCardService.setActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAllPaymentCards(@RequestParam (required = false) String holder, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentCard> allPaymentCards = paymentCardService.getAllPaymentCards(holder, pageable);
        Page<PaymentCardDto> allPaymentCardDtos = allPaymentCards.map(paymentCardMapper::toDto);
        return ResponseEntity.ok(allPaymentCardDtos);
    }
}