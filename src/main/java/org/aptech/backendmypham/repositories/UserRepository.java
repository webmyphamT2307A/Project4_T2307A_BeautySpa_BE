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


}
