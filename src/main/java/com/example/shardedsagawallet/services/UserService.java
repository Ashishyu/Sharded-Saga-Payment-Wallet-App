package com.example.shardedsagawallet.services;

import com.example.shardedsagawallet.entities.User;
import com.example.shardedsagawallet.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        log.info("Creating user");
        User newUser = userRepository.save(user);
        log.info("User created with id " + newUser.getId() + " in shard db : " + (newUser.getId() % 2 + 1));
        return newUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
    }

    public List<User> getUserByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

}
