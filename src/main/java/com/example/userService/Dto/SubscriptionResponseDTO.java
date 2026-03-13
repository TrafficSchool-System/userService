package com.example.userService.Dto;

import com.example.userService.Entity.Subscription;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubscriptionResponseDTO {

    private Long id;
    private Long userId;
    private Long packageId;
    private String packageName;
    private BigDecimal packagePrice;
    private Integer validityDays;
    private Integer validityHours;
    private String paymentId;
    private LocalDateTime purchaseDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private long hoursRemaining; // ✅ Beräknat fält
    private boolean expired; // ✅ Beräknat fält

    // Frontend-kompatibla fält
    private BigDecimal price; // Alias för packagePrice
    private boolean isExpired; // Alias för expired
    private boolean valid; // Beräknat: active && !expired
    private long daysRemaining; // Beräknat från hoursRemaining
    private boolean isExpiringSoon; // Beräknat: valid && daysRemaining <= 3

    // Tom konstruktor
    public SubscriptionResponseDTO() {
    }

    // Konstruktor från Entity
    public SubscriptionResponseDTO(Subscription subscription) {
        this.id = subscription.getId();
        this.userId = subscription.getUserId();
        this.packageId = subscription.getPackageId();
        this.packageName = subscription.getPackageName();
        this.packagePrice = subscription.getPackagePrice();
        this.validityDays = subscription.getValidityDays();
        this.validityHours = subscription.getValidityHours();
        this.paymentId = subscription.getPaymentId();
        this.purchaseDate = subscription.getPurchaseDate();
        this.startDate = subscription.getStartDate();
        this.endDate = subscription.getEndDate();
        this.active = subscription.isActive();
        this.hoursRemaining = subscription.getHoursRemaining();
        this.expired = subscription.isExpired();

        // Beräkna frontend-kompatibla fält
        this.price = this.packagePrice; // Alias
        this.isExpired = this.expired; // Alias

        // VIKTIGT: active är redan korrekt beräknad i backend (!cancelled &&
        // !isExpired)
        // Ingen anledning att duplicera logiken här
        this.valid = this.active;

        // Avrunda dagar uppåt för bättre UX: 10 timmar = 1 dag (inte 0 dagar)
        // Detta förhindrar förvirrande "0 dagar kvar" meddelanden för aktiva
        // subscriptions
        this.daysRemaining = (long) Math.ceil(this.hoursRemaining / 24.0);

        // Varning när mindre än 7 dagar kvar (endast för aktiva subscriptions)
        this.isExpiringSoon = this.valid && this.daysRemaining <= 7 && this.daysRemaining > 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public BigDecimal getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(BigDecimal packagePrice) {
        this.packagePrice = packagePrice;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Integer getValidityHours() {
        return validityHours;
    }

    public void setValidityHours(Integer validityHours) {
        this.validityHours = validityHours;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getHoursRemaining() {
        return hoursRemaining;
    }

    public void setHoursRemaining(long hoursRemaining) {
        this.hoursRemaining = hoursRemaining;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    // Frontend-kompatibla getters/setters
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public boolean getIsExpiringSoon() {
        return isExpiringSoon;
    }

    public void setIsExpiringSoon(boolean isExpiringSoon) {
        this.isExpiringSoon = isExpiringSoon;
    }
}