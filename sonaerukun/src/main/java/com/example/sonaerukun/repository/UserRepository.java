package com.example.sonaerukun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sonaerukun.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByHostName(String hostName);
}