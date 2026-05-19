package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /*@Query("SELECT p FROM Product p WHERE p.id = :productId")
    Product findProductById(@Param("productId") long productId);*/

    @Query("SELECT p FROM Product p WHERE p.producer.id = :producerId")
    List<Product> findProductsByProducerId(@Param("producerId") long producerId);

    /*@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Product findProductsByCategoryId(@Param("categoryId") Long categoryId);*/

    Page<Product> findProductsByCategoryIdIn( List<Long> categoryIds, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.discount IS NOT NULL AND p.discount != 0")
    List<Product> findAllProducts();

    @Query("SELECT p FROM Product p WHERE p.numPurchases IS NOT NULL AND p.numPurchases != 0")
    List<Product> findAllProductsByNumPurchasesIsNotNull();


}
