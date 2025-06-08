package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Integer> {
    Optional<Review> findByIdAndIsActiveTrue(Integer id);

    // Tìm tất cả review theo relatedId (ví dụ: ID dịch vụ), có phân trang và phải đang hoạt động
    Page<Review> findByRelatedIdAndIsActiveTrue(Integer relatedId, Pageable pageable);

    // Tìm tất cả review có phân trang và đang hoạt động
    Page<Review> findByIsActiveTrue(Pageable pageable);
}
