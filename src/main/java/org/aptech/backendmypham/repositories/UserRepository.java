package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phoneNumber);

    @Modifying
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE is_active = 1")
    List<User> findAllIsActive();

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE users SET is_active = :isActive WHERE id = :id")
    void updateIsActiveById(@Param("id") Long id, @Param("isActive") int isActive);

}
