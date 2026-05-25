package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus!= 'PENDING'")
    Order findAllByUserId(@Param("userId") String userId);

    @Query("SELECT o FROM Order  o WHERE o.user.id = :userId AND o.id = :orderId")
    List<Order> findAllByUserIdAndOrderId(
            @Param("userId") String userId,
            @Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    List<Order> findAllByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :orderStatus")
    Order findByUserIdAndOrderStatus(
            @Param("userId") String userId,
            @Param("orderStatus") String orderStatus);

    @Query("SELECT o FROM Order o WHERE o.orderStatus!= 'PENDING' ORDER BY o.dateCreated DESC") // query che
    List<Order> findAllByOrderStatus();

    void deleteByUserId(String userId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdOptimisticForceIncrement(@Param("id") Long orderId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("""
    SELECT o
    FROM Order o
    WHERE o.user.id = :userId
      AND o.orderStatus = OrderStatus.PENDING
""")
    Optional<Order> findPendingCartByUserIdForceIncrement(@Param("userId") String userId);
}
