package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Discount;

import java.util.List;
import java.util.Optional;

public interface DiscountService {
    Discount createDiscount(Discount discount);
    List<Discount> getAllDiscounts();
    Optional<Discount> getDiscountById(Integer id);
    Discount updateDiscount(Integer id, Discount updatedDiscount);
    Optional<Discount> findDiscountById(Integer id);
    void softDeleteDiscount(Integer id);
}
