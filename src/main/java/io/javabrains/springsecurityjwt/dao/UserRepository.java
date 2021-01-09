package io.javabrains.springsecurityjwt.dao;

import io.javabrains.springsecurityjwt.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    public UserEntity findByUsername(String username);
    public UserEntity findByEmail(String email);

}
