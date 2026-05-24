package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Order;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderProductRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.OrderRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.UserRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.enums.OrderStatus;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

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

    // reset del carrello robusto alla race condition
    @Transactional
    public Order resetCart(String userId, Long orderId) {
        try {
            // Carico l'ordine con OPTIMISTIC_FORCE_INCREMENT per bloccare la riga e far scattare l'eccezione in caso di modifica concorrente
            Order order = orderRepository.findByIdOptimisticForceIncrement(orderId)
                    .orElseThrow(() -> new CustomException("Errore nel ripristino del carrello: ordine non trovato"));

            // Verifico che l'ordine appartenga all'utente
            if (!order.getUser().getId().toString().equals(userId)) throw new CustomException("Non autorizzato");

            // Verifica che il carrello non sia in stato Pending
            if (order.getOrderStatus() != OrderStatus.PENDING) throw new CustomException("Ordine non resettabile");

            orderProductRepository.deleteAllByOrderId(orderId); //Svuoto le righe in modo atomico

            // Reset campi dell'Order (come richiesto)
            order.setTotalAmount(0.0);
            order.setShippingAddress(null);
            order.setPhoneNumber(null);
            order.setRecipientName(null);
            order.setOrderStatus(OrderStatus.PENDING);

            orderRepository.flush(); // Flush per forzare l'aggiornamento e far scattare l'eccezione in caso di modifica concorrente

            return order;

        } catch (OptimisticLockingFailureException ex) {
            throw new CustomException("Conflitto: il carrello è stato modificato da un'altra operazione. Riprova.");
        }
    }

    // aggiunta di un prodotto al carrello robusta alla race condition
    @Transactional
    public Order addProductToCart(Product product, String userId) {
        try {
            if (product == null || product.getId() == null) {
                throw new CustomException("Prodotto non valido.");
            }

            // Prendo il carrello con OPTIMISTIC_FORCE_INCREMENT (incrementa version)
            Order cart = orderRepository.findPendingCartByUserIdForceIncrement(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException("Utente non trovato."));

                        Order o = new Order();
                        o.setUser(user);
                        o.setOrderStatus(OrderStatus.PENDING);
                        o.setDateCreated(LocalDateTime.now());
                        o.setTotalAmount(0.0);
                        return orderRepository.save(o);
                    });

            // Aggiungo il prodotto al carrello (incremento quantità se già presente)
            // Cerco se il prodotto è già presente nel carrello
            OrderProduct existing = cart.getOrderProducts().stream()
                    .filter(op -> op.getPk() != null
                            && op.getPk().getProduct() != null
                            && op.getPk().getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + 1); // aggiunta quantità
            } else {
                cart.getOrderProducts().add(new OrderProduct(cart, product, 1)); // nuova riga per il prodotto
            }

            cart.setTotalAmount(cart.getTotalPrice()); // Reset/aggiornamento campi Order

            orderRepository.flush(); // Flush per forzare l'aggiornamento e far scattare l'eccezione in caso di modifica concorrente

            return cart;

        } catch (OptimisticLockingFailureException ex) {
            throw new CustomException("Conflitto: il carrello è stato modificato da un'altra operazione. Riprova.");
        }
    }



}
