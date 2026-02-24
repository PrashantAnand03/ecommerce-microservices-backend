package com.cts.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();


    /** Merge or add new item */
    public void addOrIncrementItem(Long productId, int quantity) {
        for (CartItem ci : items) {
            if (ci.getProductId().equals(productId)) {
                ci.setQuantity(ci.getQuantity() + quantity);
                return;
            }
        }
        CartItem newItem = new CartItem();
        newItem.setCart(this);
        newItem.setProductId(productId);
        newItem.setQuantity(quantity);
        items.add(newItem);
    }

    /** Reduce by one; if zero, remove item */
    public boolean decrementItem(Long productId) {
        for (CartItem ci : items) {
            if (ci.getProductId().equals(productId)) {
                int q = ci.getQuantity() - 1;
                if (q <= 0) {
                    items.remove(ci);
                } else {
                    ci.setQuantity(q);
                }
                return true;
            }
        }
        return false;
    }

    /** Remove item completely */
    public boolean removeProduct(Long productId) {
        return items.removeIf(ci -> ci.getProductId().equals(productId));
    }
}

