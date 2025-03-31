package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    List<Product> getALlProduct();
    Product findById(Long PId);
    void updateProduct(Long PiD,Product updatedProduct);
    void deleteProduct(Long PId);
}
