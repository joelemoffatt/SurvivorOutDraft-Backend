package com.vivida.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Integer> {
    Optional<UserAvatar> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
}