package com.synchrony.appdev.repo;

import java.util.Optional;

import com.synchrony.appdev.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByEmail(String email);

}
