package com.shopsphere.Service;

import com.shopsphere.DTO.LoginRequestDTO;
import com.shopsphere.DTO.RegisterRequestDTO;
import com.shopsphere.DTO.UserResponseDTO;
import com.shopsphere.Entity.Order;
import com.shopsphere.Entity.User;
import com.shopsphere.Repository.OrderRepository;
import com.shopsphere.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OrderRepository orderRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.orderRepository = orderRepository;
    }

    public UserResponseDTO register(
            RegisterRequestDTO request) {

        if (userRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encrypt password
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        // Every newly registered account is a USER
        user.setRole("USER");

        User savedUser =
                userRepository.save(user);

        return convertToDTO(savedUser);
    }

    public UserResponseDTO login(
            LoginRequestDTO request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password");
        }

        String token =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                );

        UserResponseDTO response =
                convertToDTO(user);

        response.setToken(token);

        return response;
    }

    // Get all normal customers
    public List<UserResponseDTO> getAllCustomers() {

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        "USER".equals(user.getRole()))
                .map(this::convertToDTO)
                .toList();
    }

    // Get orders belonging to a specific customer
    public List<Order> getCustomerOrders(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found"));

        // Make sure the selected account is a customer
        if (!"USER".equals(user.getRole())) {
            throw new RuntimeException(
                    "Selected account is not a customer");
        }

        return orderRepository.findByUser(user);
    }

    private UserResponseDTO convertToDTO(
            User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}