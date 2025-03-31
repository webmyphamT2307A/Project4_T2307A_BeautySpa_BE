package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Product;
import org.aptech.backendmypham.repositories.ProductRepository;
import org.aptech.backendmypham.services.ProductService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product createProduct(Product product){
       productRepository.save(product);
       return product;
    }

}
