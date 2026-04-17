package com.orderprocessing.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items", schema = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String productId;

    private String productName;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    // Total stock = availableQuantity + reservedQuantity
    public boolean hasEnoughStock(int requestedQuantity) {
        return this.availableQuantity >= requestedQuantity;
    }

    public void reserve(int quantity) {
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}