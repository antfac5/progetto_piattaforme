package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProduct;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.OrderProductPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductPK> {

    @Query("SELECT op FROM OrderProduct op WHERE op.pk.product.id = :productId AND op.pk.order.id = :orderId") //restituisce un prodotto associato ad un ordine specifico
    OrderProduct findByProductIdAndOrderId(
            @Param("productId") Long productId,
            @Param("orderId") Long orderId);

    @Query("SELECT op FROM OrderProduct op WHERE op.pk.order.id = :cartId") //restiuisce tutti i prodotti associati ad un ordine
    List<OrderProduct> findAllByOrderId(
            @Param("cartId") Long orderId);
}
