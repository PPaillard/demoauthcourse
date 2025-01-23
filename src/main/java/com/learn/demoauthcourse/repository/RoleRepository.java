package com.learn.demoauthcourse.repository;

import com.learn.demoauthcourse.models.ERole;
import com.learn.demoauthcourse.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
