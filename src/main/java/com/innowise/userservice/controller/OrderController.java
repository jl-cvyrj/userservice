package com.innowise.userservice.controller;

import com.innowise.userservice.entity.Order;
import com.innowise.userservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<Order> getMyOrders() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return orderRepository.findByUserId(userId);
    }
}