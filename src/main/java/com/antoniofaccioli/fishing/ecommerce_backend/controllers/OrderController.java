package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Order;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderRepository;
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
    private OrderRepository orderRepository;

    //CREATE
    @PostMapping("/{userId}")
    public ResponseEntity<HttpResponse> addProductToCart(@Valid @RequestBody Product product, @PathVariable("userId") String userId) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.addProductToCart(product, userId)))
                        .message("Prodotto aggiunto nel carrello.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @RequestMapping(value = "/checkout", method = RequestMethod.POST)
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
    public List<Order> getAllOrdersofUser(@RequestParam(name = "id") String userId) {
        return orderService.getAllOrdersOfUser(userId);
    }

    @GetMapping("/pending-cart")
    public Order getPendingCart(@RequestParam String userId) {
        return orderService.getPendingCart(userId);
    }

    @GetMapping("/{userId}/cart-items")
    public List<OrderProduct> getItemsInPendingCart(@PathVariable("userId") String userId) {
        Order cart = getPendingCart(userId);
        return orderProductService.getItemsInPendingCart(cart.getId());
    }

    //UPDATE
    @RequestMapping(value = "/order", method = RequestMethod.PUT)
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

    @RequestMapping(value = "/{userId}/incr-quantity/product", method = RequestMethod.PUT)
    public ResponseEntity<HttpResponse> increaseProductQtyInCart(@PathVariable("userId") String userId, @RequestParam(name = "id") Product product) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.updateQtyInCart(product, userId, true)))
                        .message("Quantità del prodotto aumentata.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @RequestMapping(value = "/{userId}/decr-quantity/product", method = RequestMethod.PUT)
    public ResponseEntity<HttpResponse> decreaseProductQtyInCart(@PathVariable("userId") String userId, @RequestParam(name = "id") Product product) {
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
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<HttpResponse> removeProductFromCart(@PathVariable("userId") String userId, @RequestParam(name = "id") Product product) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("item", orderService.removeProductFromCart(product, userId)))
                        .message("Prodotto rimosso dal carrello.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @RequestMapping(value = "/{userId}/reset", method = RequestMethod.DELETE)
    public ResponseEntity<HttpResponse> resetCart(@PathVariable("userId") String userId) {
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
