package com.flashsale.booking.domain.user.repository;

import com.flashsale.booking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}