package com.ltqtest.springbootquickstart.user.repository;

import com.ltqtest.springbootquickstart.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    
    List<UserAddress> findByUserId(Integer userId);
    
    Optional<UserAddress> findByAddressIdAndUserId(Integer addressId, Integer userId);
}
