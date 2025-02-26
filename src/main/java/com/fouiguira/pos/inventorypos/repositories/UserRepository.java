package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
