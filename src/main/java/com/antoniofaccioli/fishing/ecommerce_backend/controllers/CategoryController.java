package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Category;
import com.antoniofaccioli.fishing.ecommerce_backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> createNewCategory(@RequestBody String categoryName) {
        if (categoryName == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // restituisce un errore 400 Bad Request se il nome della categoria è null o vuoto
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(java.time.LocalDateTime.now().toString())
                        .data(java.util.Map.of("category", categoryService.addNewCategory(categoryName)))
                        .message("Una nuova categoria è stata aggiunta nel database.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    //READ
    @GetMapping
    public List<Category> getAllCategories() {return categoryService.getAllCategories();}

    //UPDATE
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> updateCategory(@PathVariable("id") Long id , @RequestBody String name) {
        if (name == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // restituisce un errore 400 Bad Request se il nome della categoria è null o vuoto
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(java.time.LocalDateTime.now().toString())
                        .data(java.util.Map.of("category", categoryService.updateCategory(id, name)))
                        .message("Categoria aggiornata.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    //DELETE
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> deleteCategoryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(java.time.LocalDateTime.now().toString())
                        .data(java.util.Map.of("category", categoryService.deleteCategoryById(id)))
                        .message("Categoria con id " + id + " eliminata.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
