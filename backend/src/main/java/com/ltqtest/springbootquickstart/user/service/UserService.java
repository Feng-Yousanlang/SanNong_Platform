package com.ltqtest.springbootquickstart.user.service;

import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (user.getRoleType() == null) {
            user.setRoleType(1);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUserId(Integer userId) {
        return userRepository.findByUserId(userId);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRoleType(Integer roleType) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoleType().equals(roleType))
                .toList();
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }
}
