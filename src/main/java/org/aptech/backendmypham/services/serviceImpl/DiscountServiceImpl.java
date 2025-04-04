package org.aptech.backendmypham.services.serviceImpl;

import org.aptech.backendmypham.models.Discount;
import org.aptech.backendmypham.repositories.DiscountRepository;
import org.aptech.backendmypham.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public Discount createDiscount(Discount discount) {
        discount.setCreatedAt(Instant.now()); // Set thời gian tạo
        discount.setIsActive(true); // Mặc định là active
        return discountRepository.save(discount);
    }

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public Optional<Discount> getDiscountById(Integer id) {
        return discountRepository.findById(id);
    }

    @Override
    public Discount updateDiscount(Integer id, Discount updatedDiscount) {
        return discountRepository.findById(id)
                .map(existingDiscount -> {
                    existingDiscount.setCode(updatedDiscount.getCode());
                    existingDiscount.setDiscountPercent(updatedDiscount.getDiscountPercent());
                    existingDiscount.setStartDate(updatedDiscount.getStartDate());
                    existingDiscount.setEndDate(updatedDiscount.getEndDate());
                    existingDiscount.setMinOrderValue(updatedDiscount.getMinOrderValue());
                    existingDiscount.setApplicableTo(updatedDiscount.getApplicableTo());
                    existingDiscount.setIsActive(updatedDiscount.getIsActive());

                    return discountRepository.save(existingDiscount);
                })
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));
    }
    @Override
    public Optional<Discount> findDiscountById(Integer id) {
        return discountRepository.findById(id);
    }
    @Override
    public void softDeleteDiscount(Integer id) {
        try {
            Optional<Discount> discountOpt = discountRepository.findByIdAndIsActiveTrue(id);
            if (discountOpt.isPresent()) {
                Discount discount = discountOpt.get();
                discount.setIsActive(false);
                discountRepository.save(discount); // Cập nhật trạng thái xóa mềm
            } else {
                System.out.println("Discount không tồn tại hoặc đã bị vô hiệu hóa."); // hoặc log.warn(...)
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thực hiện soft delete discount: " + e.getMessage());
            e.printStackTrace();
        }
    }


}

