package io.javabrains.springsecurityjwt.services;

import io.javabrains.springsecurityjwt.dao.TokenUserRepository;
import io.javabrains.springsecurityjwt.dao.UserRepository;
import io.javabrains.springsecurityjwt.model.TokenUser;
import io.javabrains.springsecurityjwt.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenUserRepository tokenUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional  //dodao, za svaki slucaj
    public boolean isUserWithThisUsernameExistsInDb(String username){
        Optional<UserEntity> isUserWithThisUsernameExistsInDb = Optional.ofNullable(userRepository.findByUsername(username));
        return isUserWithThisUsernameExistsInDb.isPresent();
    }

    @Transactional  //dodao, za svaki slucaj
    public boolean isUserWithThisEmailExistsInDb(String email){
        Optional<UserEntity> isUserWithThisEmailExistsInDb = Optional.ofNullable(userRepository.findByEmail(email));
        return isUserWithThisEmailExistsInDb.isPresent();
    }

    /*@Transactional  //dodao, za svaki slucaj
    public UserEntity saveUserAfterSignUp(String username, String password, String email, TokenUser tokenUser){
        UserEntity userEntity = new UserEntity();
            userEntity.setUsername(username);

            //userEntity.setPassword(userSignUpRequest.getPassword());
            userEntity.setPassword(passwordEncoder.encode(password));
            System.out.println("ENKODIRAN PASSWORD   " + passwordEncoder.encode(password));
            userEntity.setEmail(email);
            userEntity.setTokenUser(tokenUser);

        userRepository.save(userEntity);
        return userEntity;
    }

    @Transactional  //dodao, za svaki slucaj
    public TokenUser saveEmptyTokenAfterSignUp(UserEntity userEntity){
        TokenUser tokenUser = new TokenUser();
            tokenUser.setJwt("");
            tokenUser.setUserEntity(userEntity);
        tokenUserRepository.save(tokenUser);
        return tokenUser;
    }*/

    @Transactional
    public void saveUserAndSetEmptyTokenAfterSignUp(String username, String password, String email){
        TokenUser tokenUser = new TokenUser();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);

        //userEntity.setPassword(userSignUpRequest.getPassword());
        userEntity.setPassword(passwordEncoder.encode(password));
        System.out.println("ENKODIRAN PASSWORD   " + passwordEncoder.encode(password));

        userEntity.setEmail(email);
        tokenUser.setJwt("");
        tokenUser.setUserEntity(userEntity);
        userEntity.setTokenUser(tokenUser);

        userRepository.save(userEntity);
        tokenUserRepository.save(tokenUser);
    }



}
