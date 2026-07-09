package com.ltqtest.springbootquickstart.user.service;

import com.ltqtest.springbootquickstart.support.TestDataFactory;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldRejectDuplicateUsername() {
        User user = TestDataFactory.newFarmer("farmer_dup");
        when(userRepository.existsByUsername("farmer_dup")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(user));
    }

    @Test
    void register_shouldApplyDefaultRoleAndStatus() {
        User user = TestDataFactory.newFarmer("farmer_new");
        user.setRoleType(null);
        user.setStatus(null);

        when(userRepository.existsByUsername("farmer_new")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(user);

        assertEquals(1, saved.getRoleType());
        assertEquals(1, saved.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void findByUserId_shouldDelegateToRepository() {
        User user = TestDataFactory.newBuyer("buyer_1");
        user.setUserId(9);
        when(userRepository.findByUserId(9)).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByUserId(9);

        assertEquals(9, found.orElseThrow().getUserId());
    }

    @Test
    void findByRoleType_shouldFilterUsers() {
        User farmer = TestDataFactory.newFarmer("farmer_a");
        User buyer = TestDataFactory.newBuyer("buyer_a");
        when(userRepository.findAll()).thenReturn(List.of(farmer, buyer));

        List<User> farmers = userService.findByRoleType(1);

        assertEquals(1, farmers.size());
        assertEquals("farmer_a", farmers.get(0).getUsername());
    }
}
