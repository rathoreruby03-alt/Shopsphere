package com.shopsphere.Controller;

import com.shopsphere.Entity.Cart;
import com.shopsphere.Service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add product to cart
    @PostMapping
    public Cart addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        return cartService.addToCart(
                userId,
                productId,
                quantity
        );
    }

    // Get user's cart
    @GetMapping("/user/{userId}")
    public List<Cart> getUserCart(
            @PathVariable Long userId) {

        return cartService.getUserCart(userId);
    }

    // Update quantity
    @PutMapping("/{cartId}")
    public Cart updateQuantity(
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {

        return cartService.updateQuantity(
                cartId,
                quantity
        );
    }

    // Remove product from cart
    @DeleteMapping("/{cartId}")
    public String removeFromCart(
            @PathVariable Long cartId) {

        cartService.removeFromCart(cartId);

        return "Product removed from cart";
    }
}