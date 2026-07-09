package com.ltqtest.springbootquickstart.user.repository;

import com.ltqtest.springbootquickstart.support.TestDataFactory;
import com.ltqtest.springbootquickstart.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndFindByUsername() {
        String username = TestDataFactory.uniqueUsername("repo_user_");
        User user = TestDataFactory.newFarmer(username);

        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByUsername(username);
        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUsername());
        assertEquals(1, found.get().getRoleType());
    }
}
