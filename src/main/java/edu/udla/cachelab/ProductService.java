package edu.udla.cachelab;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository=repository; }
    public List<Product> uncached(int limit) { return repository.find(limit); }
    @Cacheable(value="products",key="#limit",sync=true)
    public List<Product> cached(int limit) { return repository.find(limit); }
}
