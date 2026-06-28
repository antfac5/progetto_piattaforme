package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProductPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductPK> {

    @Query("SELECT op FROM OrderProduct op WHERE op.pk.product.id = :productId AND op.pk.order.id = :orderId") //restituisce un prodotto associato ad un ordine specifico
    OrderProduct findByProductIdAndOrderId(
            @Param("productId") Long productId,
            @Param("orderId") Long orderId);

    // Rimuovi l'EntityGraph e usa direttamente il JOIN FETCH nella query per eliminare il Proxy Lazy
    @Query("SELECT op FROM OrderProduct op JOIN FETCH op.pk.product WHERE op.pk.order.id = :cartId") // restituisce tutti i prodotti associati ad un ordine specifico
    List<OrderProduct> findAllByOrderId(
            @Param("cartId") Long orderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderProduct op WHERE op.pk.order.id = :orderId")
    void deleteAllByOrderId(@Param("orderId") Long orderId);
}
