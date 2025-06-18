package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Integer> {
    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.isActive = true")
    Optional<Review> findByIdAndIsActiveTrue(@Param("id") Integer id);


    // Tìm tất cả review theo relatedId (ví dụ: ID dịch vụ), có phân trang và phải đang hoạt động
    Page<Review> findByRelatedIdAndIsActiveTrue(Integer relatedId, Pageable pageable);

    // Tìm tất cả review có phân trang và đang hoạt động
    Page<Review> findByIsActiveTrue(Pageable pageable);
//    @Query("SELECT FUNCTION('MONTH', r.createdAt) as month, AVG(r.rating) as avgRating " +
//            "FROM Review r " +
//            "WHERE r.user.id = :userId AND r.rating IS NOT NULL AND FUNCTION('YEAR', r.createdAt) = :year " +
//            "GROUP BY FUNCTION('MONTH', r.createdAt)")
//    List<Object[]> getMonthlyRatingsForUser(@Param("year") int year, @Param("userId") Long userId);
}
