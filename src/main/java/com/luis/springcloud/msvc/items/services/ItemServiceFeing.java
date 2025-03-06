package com.luis.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.luis.springcloud.msvc.items.clients.ProductFeignClient;
import com.luis.springcloud.msvc.items.models.Item;
import com.luis.springcloud.msvc.items.models.Product;

@Service
public class ItemServiceFeing implements ItemService {

    private final ProductFeignClient client;

    public ItemServiceFeing(ProductFeignClient client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return client.findAll().stream()
        .map(product -> new Item(product, new Random().nextInt(1, 10)))
        .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Product product = client.details(id);
        if(product == null){
            return Optional.empty();
        }
        return Optional.ofNullable(new Item(product, new Random().nextInt(1, 10)));
    }
}
