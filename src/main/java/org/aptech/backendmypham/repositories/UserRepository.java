package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phoneNumber);

    @Modifying

    @Query(nativeQuery = true, value = "SELECT * FROM users")

    List<User> findAllIsActive();

    @Query("select u from User u join fetch u.role where u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE users SET is_active = :isActive WHERE id = :id")
    void updateIsActiveById(@Param("id") Long id, @Param("isActive") int isActive);
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    boolean existsByEmail(String email);
    @Query("SELECT DISTINCT u FROM User u JOIN u.skills s WHERE (u.role.name = :staffRoleName OR u.role.id = :specificRoleId) AND s.id = :skillId")
    List<User> findUsersByRoleStaffOrRoleIdAndSkillId(
            @Param("staffRoleName") String staffRoleName,
            @Param("specificRoleId") Integer specificRoleId,
            @Param("skillId") Long skillId
    );
    @Query("SELECT u FROM User u WHERE u.isActive IN (0, 1)")
    List<User> findAllActiveAndInactive();
    @Query("SELECT u FROM User u WHERE u.isActive = -1")
    List<User> findAllDeleted();
    @Query("SELECT COALESCE(AVG(u.averageRating), 0.0) FROM User u WHERE u.isActive = 1 AND u.averageRating > 0")
    double getOverallAverageRating();

    // Lấy tất cả user active có role để tính toán ở Service
    @Query("SELECT u FROM User u WHERE u.isActive = 1 AND u.role IS NOT NULL")
    List<User> findAllActiveWithRoles();
    @Query("SELECT FUNCTION('MONTH', r.createdAt) as month, AVG(r.rating) as avgRating " +
            "FROM Review r " +
            "JOIN Appointment a ON r.relatedId = a.service.id " +
            "WHERE a.user.id = :userId AND r.rating IS NOT NULL AND FUNCTION('YEAR', r.createdAt) = :year " +
            "GROUP BY FUNCTION('MONTH', r.createdAt)")
    List<Object[]> getMonthlyRatingsForUser(@Param("year") int year, @Param("userId") Long userId);

}
