package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Product;
import org.aptech.backendmypham.repositories.ProductRepository;
import org.aptech.backendmypham.services.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product createProduct(Product product){
       productRepository.save(product);
       return product;
    }
    @Override
    public List<Product> getALlProduct(){
        return productRepository.findAll();

    }
    @Override
    public Product findById(Long PId){
        return productRepository.findByIdAndIsActiveTrue(PId).orElse(null);
    }

    @Override
    public  void updateProduct(Long PiD,String newProductName){
        Optional<Product> product = productRepository.findByIdAndIsActiveTrue(PiD);
        if(product.isPresent()){
            Product newProduct = product.get();
            newProduct.setName(newProductName);
            productRepository.save(newProduct);
        }else{
            throw new RuntimeException("Product not found");
        }
    }

}
