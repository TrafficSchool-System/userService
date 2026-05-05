package com.example.userService.features.subscription.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    /**
     * CANCELLED FLAG
     * 
     * OBS: Inverterad logik jämfört med gamla 'active'!
     * - cancelled = false → Subscription är aktiv ✅
     * - cancelled = true → Subscription är avbruten av användare/admin ❌
     * 
     * I DAGSLÄGET: Används EJ (subscriptions är tidsbaserade, löper ut
     * automatiskt).
     * FRAMTIDA: Kan användas för manuell avbrytning eller återbetalning.
     */
    @Column(nullable = false)
    private boolean cancelled = false;

    // Tom konstruktor
    public Subscription() {
    }

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

        // ✅ VIKTIGT: Använd UTC tid för konsistens med databas
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.purchaseDate = now;
        this.startDate = now;
        this.endDate = now.plusDays(validityDays);
        this.cancelled = false; // Ny subscription är alltid aktiv

        // DEBUG LOGGING
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Subscription.class);
        log.info("🔍 DEBUG Subscription created (UTC time):");
        log.info("   - Current time (UTC): {}", now);
        log.info("   - validityDays: {}", validityDays);
        log.info("   - startDate: {}", this.startDate);
        log.info("   - endDate: {}", this.endDate);
        log.info("   - isExpired(): {}", this.isExpired());
    }

    // ✅ BERÄKNA TIMMAR KVAR (viktigt för frontend!)
    public long getHoursRemaining() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (cancelled || now.isAfter(endDate)) {
            return 0;
        }
        Duration duration = Duration.between(now, endDate);
        return duration.toHours();
    }

    // ✅ KOLLA OM SUBSCRIPTION HAR GÅTT UT
    // Jämför exakt tid - subscription går ut vid endDate (inte end of day)
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        boolean expired = now.isAfter(endDate);

        // DEBUG LOGGING för felsökning - ALLTID logga för att se vad som händer
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Subscription.class);
        log.info("🔍 DEBUG isExpired() check:");
        log.info("   - Subscription ID: {}", id);
        log.info("   - userId: {}", userId);
        log.info("   - Current time (UTC): {}", now);
        log.info("   - endDate: {}", endDate);
        log.info("   - cancelled: {}", cancelled);
        log.info("   - now.isAfter(endDate): {}", expired);
        log.info("   - RETURNING: {}", expired);

        return expired;
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

    /**
     * KOLLA OM SUBSCRIPTION ÄR AKTIV
     * 
     * En subscription är aktiv om:
     * 1. Den inte är manuellt avbruten (cancelled = false)
     * 2. Den inte har gått ut (isExpired() = false)
     * 
     * @return true om subscription är aktiv OCH inte utgången
     */
    public boolean isActive() {
        return !cancelled && !isExpired();
    }

    /**
     * MANUELLT AVBRYT SUBSCRIPTION
     * 
     * FRAMTIDA FUNKTION: Används när användare/admin vill avbryta subscription
     * för återbetalning eller liknande.
     * 
     * I DAGSLÄGET: Används EJ (subscriptions löper ut automatiskt).
     */
    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", userId=" + userId +
                ", packageName='" + packageName + '\'' +
                ", validityDays=" + validityDays +
                ", endDate=" + endDate +
                ", cancelled=" + cancelled +
                ", isActive=" + isActive() +
                ", hoursRemaining=" + getHoursRemaining() +
                '}';
    }
}