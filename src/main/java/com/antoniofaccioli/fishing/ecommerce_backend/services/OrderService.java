package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Order;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.UserRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.enums.OrderStatus;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakService keycloakService;

    public Order getPendingCart(String userId){
        Optional<UserRepresentation> uro = keycloakService.getUserById(userId);
        if(uro.isEmpty()) throw new CustomException("Utente non trovato.");
        UserRepresentation userRepresentation = uro.get();
        User user = userRepository.findById(userRepresentation.getId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(userRepresentation.getId());
                    newUser.setFirstName(userRepresentation.getFirstName());
                    newUser.setLastName(userRepresentation.getLastName());
                    return userRepository.save(newUser);
                });

        Order pendingCart = orderRepository.findByUserIdAndOrderStatus(user.getId(), "PENDING");
        if (pendingCart == null) {
            pendingCart = new Order();
            pendingCart.setUser(user);
            pendingCart.setOrderStatus(OrderStatus.PENDING);
            pendingCart = orderRepository.save(pendingCart);
        }
        return pendingCart;
    }
}
