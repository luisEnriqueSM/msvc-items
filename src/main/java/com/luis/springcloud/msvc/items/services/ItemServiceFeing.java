package com.luis.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.luis.springcloud.msvc.items.clients.ProductFeignClient;
import com.luis.springcloud.msvc.items.models.Item;
import com.luis.springcloud.msvc.items.models.Product;

import feign.FeignException.FeignClientException;

@Service
public class ItemServiceFeing implements ItemService {

    private final ProductFeignClient client;

    public ItemServiceFeing(ProductFeignClient client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return this.client.findAll().stream()
        .map(product -> new Item(product, new Random().nextInt(1, 10)))
        .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            Product product = this.client.details(id);   
            return Optional.ofNullable(new Item(product, new Random().nextInt(1, 10))); 
        } catch (FeignClientException e) {
           return Optional.empty();
        }
    }

    @Override
    public Product save(Product product) {
        return this.client.create(product);
    }

    @Override
    public Product update(Product product, Long id) {
        return this.client.update(product, id);
    }

    @Override
    public void delete(Long id) {
        this.client.delete(id);
    }
}
