package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Order;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProductRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.UserRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.services.KeycloakService;
import com.antoniofaccioli.fishing.ecommerce_backend.services.OrderProductService;
import com.antoniofaccioli.fishing.ecommerce_backend.services.OrderService;
import com.antoniofaccioli.fishing.ecommerce_backend.support.common.OrderForm;
import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;
import com.antoniofaccioli.fishing.ecommerce_backend.support.enums.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalTime.now;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private KeycloakService keycloackService;

    @Autowired
    private OrderProductService orderProductService;

    @Autowired
    private ProductRepository productRepository;

    //CREATE
    @PostMapping("/cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> addProductToCart(@Valid @RequestBody Long productId) {
        String userId = keycloackService.getCurrentUserId();
        Product product = productRepository.findProductById(productId); //se il prodotto non viene trovato, viene gestito nel OrderService
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.addProductToCart(userId, product)))
                        .message("Prodotto aggiunto nel carrello.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> checkout(@RequestBody OrderForm orderForm) {
        String userId = keycloackService.getCurrentUserId();
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("order", orderService.checkout(userId, orderForm)))
                        .message("Checkout andato a buon fine.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    //READ
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/user")
    public List<Order> getAllOrdersofUser() {
        String userId = keycloackService.getCurrentUserId();
        return orderService.getAllOrdersOfUser(userId);
    }

    @GetMapping("/pending-cart")
    @PreAuthorize("hasRole('USER')")
    public Order getPendingCart() {
        String userId = keycloackService.getCurrentUserId();
        return orderService.getPendingCart(userId);
    }

    @GetMapping("/cart-items")
    @PreAuthorize("hasRole('USER')")
    public List<OrderProduct> getItemsInPendingCart() {
        String userId = keycloackService.getCurrentUserId();
        Order cart = orderService.getPendingCart(userId);
        return orderProductService.getItemsInPendingCart(cart.getId());
    }

    //UPDATE
    @PutMapping(value = "/updateStatus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> updateOrderStatus(@RequestParam(name = "id") Long orderId, @Valid @RequestBody OrderStatus orderStatus) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("order", orderService.updateOrderStatus(orderId, orderStatus)))
                        .message("Stato dell'ordine aggiornato.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PutMapping(value = "/incr-quantity/product")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> increaseProductQtyInCart(@RequestParam(name = "id") Product product) {
        String userId = keycloackService.getCurrentUserId();
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.updateQtyInCart(product, userId, true)))
                        .message("Quantità del prodotto aumentata.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PutMapping(value = "/decr-quantity/product")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> decreaseProductQtyInCart(@RequestParam(name = "id") Product product) {
        String userId = keycloackService.getCurrentUserId();
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.updateQtyInCart(product, userId, false)))
                        .message("Quantità del prodotto diminuita.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    //DELETE
    @DeleteMapping(value = "/cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> removeProductFromCart(@RequestParam(name = "id") Product product) {
        String userId = keycloackService.getCurrentUserId();
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.removeProductFromCart(product, userId)))
                        .message("Prodotto rimosso dal carrello.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @DeleteMapping(value = "/reset")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HttpResponse> resetCart() {
        String userId = keycloackService.getCurrentUserId();
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("carrello", orderService.resetCart(userId)))
                        .message("Carrello ripristinato.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
