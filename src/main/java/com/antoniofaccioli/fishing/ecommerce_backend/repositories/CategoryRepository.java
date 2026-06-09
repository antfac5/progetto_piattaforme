package com.antoniofaccioli.fishing.ecommerce_backend.repositories;


import com.antoniofaccioli.fishing.ecommerce_backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Transactional
    @Modifying
    @Query("delete from Category c where c.id = ?1") //Elimina una categoria in base al suo ID
    void deleteCategoryById(Long id);
}
