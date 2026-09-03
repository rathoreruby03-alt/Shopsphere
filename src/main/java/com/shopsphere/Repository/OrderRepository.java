package com.shopsphere.Repository;

import com.shopsphere.Entity.Order;
import com.shopsphere.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}