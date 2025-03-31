package org.aptech.backendmypham.services.serviceImpl;

import org.aptech.backendmypham.models.Discount;
import org.aptech.backendmypham.repositories.DiscountRepository;
import org.aptech.backendmypham.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }
}