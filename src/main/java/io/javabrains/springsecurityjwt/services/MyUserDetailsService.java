package io.javabrains.springsecurityjwt.services;

import io.javabrains.springsecurityjwt.dao.UserRepository;
import io.javabrains.springsecurityjwt.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    //OVDE SAM ZAKUCAO JEDNOG USERA KOG CU DA TESTIRAM, AKO ZELIM DA UZIMAM USERE IZ BAZE, SAMO SE U TELU METODE KONEKTUJEM NA UserRepository
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);
        System.out.println("USERNAME IZ SERVICE " + username + " PASSWORD " + userEntity.getPassword());
        //return (UserDetails) user;
        //return new UserEntity("foo", "foo", new ArrayList<>());
        return new User(userEntity.getUsername(), userEntity.getPassword(), new ArrayList<>());
    }
}
