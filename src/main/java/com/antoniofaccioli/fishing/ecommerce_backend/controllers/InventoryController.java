package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.antoniofaccioli.fishing.ecommerce_backend.services.InventoryService;

@RestController

public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = ("inventory-quantity"), method = RequestMethod.PUT)
    public ResponseEntity<HttpResponse> updateProducyQuantityInIntentory(@RequestParam Long productId, @RequestParam Integer quantity){
        if ( quantity == null || quantity < 0 ) throw  new CustomException(("Quantita' non valida!"));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(java.time.LocalDateTime.now().toString())
                        .data(java.util.Map.of("product", inventoryService.updateProducyQuantityInIntentory(productId, quantity)))
                        .message("Quantita' del prodotto aggiornata.")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build());
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = ("num-purchases"), method = RequestMethod.PUT)
    public ResponseEntity<HttpResponse> incrementNumPurchases(@RequestParam Long productId){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(java.time.LocalDateTime.now().toString())
                        .data(java.util.Map.of("product",  inventoryService.incrementNumPurchases(productId)))
                        .message("Un nuovo acquisto è stato effettuato.")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build());
    }
}
