package com.example.userService.features.subscription.dto;

import com.example.userService.features.subscription.entity.Subscription;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime purchaseDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime endDate;

    private boolean active;
    private long hoursRemaining; // ✅ Beräknat fält

    // Frontend-kompatibla fält
    private boolean valid; // Alias för active - används av frontend
    private boolean isExpired; // JavaScript naming convention
    private long daysRemaining; // Beräknat från hoursRemaining
    private boolean isExpiringSoon; // Beräknat: active && daysRemaining <= 7

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
        this.valid = subscription.isActive(); // Frontend använder 'valid' property
        this.hoursRemaining = subscription.getHoursRemaining();
        this.isExpired = subscription.isExpired();

        // Avrunda dagar uppåt för bättre UX: 10 timmar = 1 dag (inte 0 dagar)
        // Detta förhindrar förvirrande "0 dagar kvar" meddelanden för aktiva
        // subscriptions
        this.daysRemaining = (long) Math.ceil(this.hoursRemaining / 24.0);

        // Varning när mindre än 7 dagar kvar (endast för aktiva subscriptions)
        this.isExpiringSoon = this.active && this.daysRemaining <= 7 && this.daysRemaining > 0;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getHoursRemaining() {
        return hoursRemaining;
    }

    public void setHoursRemaining(long hoursRemaining) {
        this.hoursRemaining = hoursRemaining;
    }

    public boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
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