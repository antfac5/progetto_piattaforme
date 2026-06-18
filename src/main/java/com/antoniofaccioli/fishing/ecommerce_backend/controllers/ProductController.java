package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Producer;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProductRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.services.ProductService;
import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;

@RestController
@RequestMapping("api/v1/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    //CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> createNewProduct(@RequestBody Product product) {
        if(product == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // restituisce un errore 400 Bad Request se il prodotto è null
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("product", productService.addNewProduct(product)))
                        .message("Un nuovo prodotto è stato aggiunto nel database.")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build()
        );
    }

    //READ
    @GetMapping
    public ResponseEntity<HttpResponse> getProducts(@RequestParam Optional<Integer> page,
                                                    @RequestParam Optional<Integer> size,
                                                    @RequestParam Optional<String> sortBy,
                                                    @RequestParam Optional<String> sortDirection) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("products", productService.getProducts(
                                page.orElse(0),
                                size.orElse(12),
                                sortBy.orElse("id"),
                                sortDirection.orElse("ASC"))))
                        .message("Prodotti caricati")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/producers")
    public ResponseEntity<List<Product>> getProductsOfProducer(@RequestParam Long producerId) {
        List<Product> productsOfProducer = productService.getAllProductsOfProducer(producerId);
        return new ResponseEntity<>( productsOfProducer, HttpStatus.OK);
    }

    @GetMapping("/product")
    public ResponseEntity<Product> getProduct(@RequestParam(name = "id") Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<HttpResponse> getAllProductsOfCategories(@RequestParam(name = "ids") Optional<List<Long>> categoryIds) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("products", productService.getAllProductsOfCategory(
                                categoryIds.orElse(null))))
                        .message("Prodotti caricati")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/discounted-products")
    public List<Product> getAllDiscountedProducts() { return productService.getAllDiscountedProducts(); }

    @GetMapping("/best-sellers")
    public List<Product> getBestSellingProducts() { return productService.getBestSellingProducts(); }

    //UPDATE
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> updateProduct(@RequestBody @Valid Product product) {
        if(product == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("product", productService.updateProduct(product)))
                        .message("Il prodotto è stato aggiornato.")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build()
        );
    }

    //DELETE
    @RequestMapping(value = "/product", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> deleteProduct(@RequestParam Long productId) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("page", productService.deleteProduct(productId)))
                        .message("Il prodotto è stato eliminato.")
                        .status(org.springframework.http.HttpStatus.OK)
                        .statusCode(org.springframework.http.HttpStatus.OK.value())
                        .build()
        );
    }
}