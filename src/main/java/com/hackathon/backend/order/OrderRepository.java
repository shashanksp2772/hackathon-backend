package com.hackathon.backend.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByAssignedAgent_IdAndStatus(String assignedAgentId, OrderStatus status);
}
