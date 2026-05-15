package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Producer;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProducerRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProducerService {

    @Autowired
    private ProducerRepository producerRepository;

    public Producer addNewProducer(String producer, String imageUrl) {
        Producer newProducer = new Producer();
        newProducer.setName(producer);
        if(imageUrl != null)
            newProducer.setImageUrl(imageUrl);
        return producerRepository.save(newProducer);
    }

    public List<Producer> getAllProducers() {
        return producerRepository.findAll();
    }

    public Producer updateProducer(Long id, Producer producer){
        Producer exist = producerRepository.findById(id).orElse( new Producer() );
        return producerRepository.save(producer);
    }

    public String deleteProducer(Long producerId) {
        try{
            producerRepository.deleteById(producerId);
            return "Produttore eliminato.";
        }catch (CustomException e){
            throw new CustomException("Cancellazione del produttore nel database non riuscita.");
        }
    }

    public Producer getProducerById(Long producerId) {
        return producerRepository.findById(producerId).orElseThrow(
                () -> new CustomException("Il produttore con ID "+ producerId + " non è stato trovato.")
        );
    }



}