package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE roles SET is_active = 0 WHERE role_id = :roleId")
    void disableRoleById(@Param("roleId") Long roleId);

    @Modifying
    @Query(nativeQuery = true,
            value = "SELECT * FROM roles WHERE role_id = :id and is_active = 1")
    Optional<Role> findById(Long id);

}
