package com.example.userService.features.subscription.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateSubscriptionRequestDTO {
    
    @NotNull(message = "UserId är obligatoriskt")
    private Long userId;
    
    @NotNull(message = "PackageId är obligatoriskt")
    private Long packageId;
    
    @NotNull(message = "PackageName är obligatoriskt")
    private String packageName;
    
    @NotNull(message = "PackagePrice är obligatoriskt")
    @Positive(message = "PackagePrice måste vara större än 0")
    private BigDecimal packagePrice;
    
    @NotNull(message = "ValidityDays är obligatoriskt")
    @Positive(message = "ValidityDays måste vara större än 0")
    private Integer validityDays;
    
    @NotNull(message = "ValidityHours är obligatoriskt")
    @Positive(message = "ValidityHours måste vara större än 0")
    private Integer validityHours;
    
    @NotNull(message = "PaymentId är obligatoriskt")
    private String paymentId;

    // Tom konstruktor
    public CreateSubscriptionRequestDTO() {}

    // Getters and Setters
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
}