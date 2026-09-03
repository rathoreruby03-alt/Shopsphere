package com.shopsphere.Controller;

import com.shopsphere.DTO.LoginRequestDTO;
import com.shopsphere.DTO.RegisterRequestDTO;
import com.shopsphere.DTO.UserResponseDTO;
import com.shopsphere.Service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService) {

        this.userService = userService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequestDTO request) {

        try {

            UserResponseDTO response =
                    userService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO request) {

        try {

            UserResponseDTO response =
                    userService.login(request);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ADMIN - GET ALL CUSTOMERS
    @GetMapping("/admin/customers")
    public ResponseEntity<?> getAllCustomers() {

        try {

            List<UserResponseDTO> customers =
                    userService.getAllCustomers();

            return ResponseEntity.ok(customers);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ADMIN - GET CUSTOMER ORDERS
    @GetMapping("/admin/customers/{id}/orders")
    public ResponseEntity<?> getCustomerOrders(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    userService.getCustomerOrders(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}