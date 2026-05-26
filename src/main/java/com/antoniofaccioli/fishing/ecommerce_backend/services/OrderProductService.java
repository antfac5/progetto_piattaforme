package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderProductService {

    @Autowired
    private OrderProductRepository orderProductRepository;

    public List<OrderProduct> getItemsInPendingCart(Long cartId) {
        return orderProductRepository.findAllByOrderId(cartId);
    }
}
