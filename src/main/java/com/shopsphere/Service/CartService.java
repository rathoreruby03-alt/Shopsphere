package com.shopsphere.Service;

import com.shopsphere.Entity.Cart;
import com.shopsphere.Entity.Product;
import com.shopsphere.Entity.User;
import com.shopsphere.Repository.CartRepository;
import com.shopsphere.Repository.ProductRepository;
import com.shopsphere.Repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository){
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(()->new RuntimeException("Product not found"));
        if(quantity<=0){
            throw new RuntimeException("Quantity must be greater that 0");
        }
        if(quantity>product.getStock()){
            throw new RuntimeException("Not enough stock available");
        }
        Cart existingCart = cartRepository
                .findByUserAndProduct(user, product)
                .orElse(null);
        if(existingCart != null){
            int newQuantity = existingCart.getQuantity()+quantity;
            if(newQuantity > product.getStock()){
                throw new RuntimeException("Not enough stock available");
            }
            existingCart.setQuantity(newQuantity);
            return cartRepository.save(existingCart);
        }
        Cart cart = new Cart();

        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(quantity);

        return cartRepository.save(cart);
    }
    public List<Cart> getUserCart(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        return cartRepository.findByUser(user);
    }
    public Cart updateQuantity(Long cartId, Integer quantity){
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new RuntimeException("Cart item not found"));
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if(quantity>cart.getProduct().getStock()){
            throw new RuntimeException("Not enough stock available");
        }
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }
    public void removeFromCart(Long cartId){
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new RuntimeException("Cart item not found"));
        cartRepository.delete(cart);
    }

}
