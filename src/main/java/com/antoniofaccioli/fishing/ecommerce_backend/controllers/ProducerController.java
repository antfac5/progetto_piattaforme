package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Producer;
import com.antoniofaccioli.fishing.ecommerce_backend.services.ProducerService;
import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/producers")
public class ProducerController {

    @Autowired
    private ProducerService producerService;

    //CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> createNewProducer(String producerName, String imageUrl) {
        if(producerName == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(String.valueOf(System.currentTimeMillis()))
                        .data(Map.of("producer", producerService.addNewProducer(producerName, imageUrl)))
                        .message("Un nuovo produttore è stato aggiunto nel database.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    //READ
    @GetMapping
    public List<Producer> getAllProducers() {
        return producerService.getAllProducers();
    }

    //UPDATE
    @RequestMapping(value = "/{producerId}", method= RequestMethod.PUT) //serve a
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> updateProducer(@PathVariable("producerId") Long producerId, @RequestBody Producer producer) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(String.valueOf(System.currentTimeMillis()))
                        .data(Map.of("producer", producerService.updateProducer(producerId, producer)))
                        .message("Il produttore con ID " + producerId + " è stato aggiornato")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/{producerId}")
    public Producer getProducerById(@PathVariable("producerId") Long producerId) {
        return producerService.getProducerById(producerId);
    }

    //DELETE
    @RequestMapping(value = "/{producerId}", method= RequestMethod.DELETE) //serve a
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpResponse> deleteProducer(@PathVariable("producerId") Long producerId) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(String.valueOf(System.currentTimeMillis()))
                        .data(Map.of("message", producerService.deleteProducer(producerId)))
                        .message("Il produttore con ID " + producerId + " è stato eliminato")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
