package com.example.userService.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long packageId; // Referens till package i paymentService

    @Column(nullable = false)
    private String packageName; // Snapshot - vad som köptes

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal packagePrice; // Snapshot - priset vid köptillfället

    @Column(nullable = false)
    private Integer validityDays; // Snapshot - antal dagar

    @Column(nullable = false)
    private Integer validityHours; // Snapshot - totalt antal timmar

    @Column(nullable = false)
    private String paymentId; // Koppla till betalningen i paymentService

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active = true;

    // Tom konstruktor
    public Subscription() {}

    // Konstruktor för att skapa subscription
    public Subscription(Long userId, Long packageId, String packageName, 
                       BigDecimal packagePrice, Integer validityDays, 
                       Integer validityHours, String paymentId) {
        this.userId = userId;
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.validityDays = validityDays;
        this.validityHours = validityHours;
        this.paymentId = paymentId;
        this.purchaseDate = LocalDateTime.now();
        this.startDate = LocalDateTime.now();
        this.endDate = LocalDateTime.now().plusDays(validityDays);
        this.active = true;
    }

    // ✅ BERÄKNA TIMMAR KVAR (viktigt för frontend!)
    public long getHoursRemaining() {
        if (!active || LocalDateTime.now().isAfter(endDate)) {
            return 0;
        }
        Duration duration = Duration.between(LocalDateTime.now(), endDate);
        return duration.toHours();
    }

    // ✅ KOLLA OM SUBSCRIPTION HAR GÅTT UT
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public BigDecimal getPackagePrice() { return packagePrice; }
    public void setPackagePrice(BigDecimal packagePrice) { this.packagePrice = packagePrice; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public Integer getValidityHours() { return validityHours; }
    public void setValidityHours(Integer validityHours) { this.validityHours = validityHours; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", userId=" + userId +
                ", packageName='" + packageName + '\'' +
                ", validityDays=" + validityDays +
                ", endDate=" + endDate +
                ", active=" + active +
                ", hoursRemaining=" + getHoursRemaining() +
                '}';
    }
}