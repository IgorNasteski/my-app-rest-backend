package io.javabrains.springsecurityjwt.dao;

import io.javabrains.springsecurityjwt.model.TokenUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenUserRepository extends JpaRepository<TokenUser, Integer>{



}
