package com.github.chaunguyentruongan.warehouse_cdnsg.modules.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
