package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Category;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.CategoryRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() { return categoryRepository.findAll(); }

    public Category addNewCategory(@NotNull String categoryName){
        Category category = new Category();
        category.setName(categoryName);
        return categoryRepository.save(category);
    }

    public String deleteCategoryById(@NotNull Long id) {
        if( categoryRepository.findById(id).isEmpty() )
            throw new CustomException("La categoria con ID "+ id + " non è stata trovata.");
        try{
            categoryRepository.deleteById(id);
            return "Categoria eliminata";
        }catch (CustomException e){
            throw new CustomException("La categoria non può essere eliminata.");
        }
    }

    public Category updateCategory(@NotNull Long id, @NotNull String name){
        Category category = categoryRepository.findById(id).orElse( new Category() );
        category.setName(name);
        return categoryRepository.save(category);
    }



}
