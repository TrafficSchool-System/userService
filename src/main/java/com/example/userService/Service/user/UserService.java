package com.example.userService.Service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.userService.Dto.RegisterRequestDTO;
import com.example.userService.Dto.UpdateUserRequestDTO;
import com.example.userService.Dto.UserResponseDTO;
import com.example.userService.Entity.User;
import com.example.userService.Entity.LoginToken;
import com.example.userService.Entity.Subscription;
import com.example.userService.Exception.EmailAllreadyExistsException;
import com.example.userService.Exception.PersonalNumberAlreadyExistsException;
import com.example.userService.Exception.UserNotFoundException;
import com.example.userService.Repository.UserRepository;
import com.example.userService.Service.email.EmailServiceInterface;
import com.example.userService.Repository.LoginTokenRepository;
import com.example.userService.Repository.SubscriptionRepository;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    private EmailServiceInterface emailService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private WebClient paymentServiceWebClient;

    // ==========================================
    // REGISTRERINGS-METODER
    // ==========================================

    /**
     * GRUNDLÄGGANDE REGISTRERING
     * Skapar ny användare utan att skicka välkomstmail.
     * Används internt av registerUserWithWelcomeEmail().
     */
    @Override
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        // VALIDERING: Kontrollera om email redan finns
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAllreadyExistsException("Email finns redan registrerad: " + request.getEmail());
        }

        // VALIDERING: Kontrollera om personnummer redan finns
        if (userRepository.existsByPersonalNumber(request.getPersonalNumber())) {
            throw new PersonalNumberAlreadyExistsException(
                    "Personnummer finns redan registrerat: " + request.getPersonalNumber());
        }

        // SKAPA: Ny användare från request
        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPersonalNumber(),
                request.getPhoneNumber());

        // SPARA: Användare i databasen
        User savedUser = userRepository.save(user);

        // KONVERTERA: Till DTO och sätt hasActiveSubscription
        UserResponseDTO dto = new UserResponseDTO(savedUser);
        dto.setHasActiveSubscription(calculateHasActiveSubscription(savedUser.getId()));

        return dto;
    }

    /**
     * REGISTRERING MED VÄLKOMSTMAIL
     * Public endpoint för user registration.
     * Registrerar användare OCH skickar välkomstmail med login-länk.
     */
    @Override
    public UserResponseDTO registerUserWithWelcomeEmail(RegisterRequestDTO request) {
        // STEG 1: Registrera användaren först
        UserResponseDTO user = registerUser(request);

        // STEG 2: Skapa välkomsttoken (30 minuter giltighetstid)
        String token = createWelcomeToken(user.getEmail());

        // STEG 3: Skicka välkomstbrev med magic link
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), token);

        return user;
    }

    /**
     * PRIVAT HJÄLPMETOD: Skapar welcome token
     * Genererar en magic link token som är giltig i 30 minuter.
     * Används vid registrering för att skicka välkomstmail.
     */
    private String createWelcomeToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        LocalDateTime createdAt = LocalDateTime.now();

        LoginToken loginToken = new LoginToken(token, email, expiresAt, createdAt);
        loginTokenRepository.save(loginToken);

        return token;
    }

    // ==========================================
    // HÄMTA ANVÄNDARE - METODER
    // ==========================================

    /**
     * HÄMTA ANVÄNDARE VIA EMAIL
     * Används vid inloggning och validering.
     */
    @Override
    public UserResponseDTO findByEmail(String email) {
        // HITTA: Användare i databasen
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Användare hittades inte med email: " + email));

        // KONVERTERA: Till DTO och sätt hasActiveSubscription
        UserResponseDTO dto = new UserResponseDTO(user);
        dto.setHasActiveSubscription(calculateHasActiveSubscription(user.getId()));

        return dto;
    }

    /**
     * KOLLA OM EMAIL FINNS
     * Snabb validering utan att hämta hela användaren.
     */
    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * HÄMTA ANVÄNDARE VIA ID
     * Används av AdminService och interna anrop.
     */
    @Override
    public UserResponseDTO findById(Long id) {
        // HITTA: Användare med ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Användare hittades inte med ID: " + id));

        // KONVERTERA: Till DTO och sätt hasActiveSubscription
        UserResponseDTO dto = new UserResponseDTO(user);
        dto.setHasActiveSubscription(calculateHasActiveSubscription(id));

        return dto;
    }

    /**
     * HÄMTA AKTUELL INLOGGAD ANVÄNDARE
     * Används av GET /api/users/me endpoint.
     * Returnerar användarens profil baserat på email från JWT token.
     */
    @Override
    public UserResponseDTO getCurrentUser(String email) {
        // VALIDERING: Email får inte vara null eller tomt
        if (email == null || email.trim().isEmpty()) {
            throw new UserNotFoundException("Email kan inte vara null eller tomt");
        }

        // ÅTERANVÄND: findByEmail() som redan sätter hasActiveSubscription
        return findByEmail(email);
    }

    /**
     * HÄMTA ALLA ANVÄNDARE
     * Används av AdminService för att visa samtliga användare.
     * OBS: Kan bli prestandakrävande vid många användare.
     */
    @Override
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    // Konvertera till DTO
                    UserResponseDTO dto = new UserResponseDTO(user);
                    // Beräkna hasActiveSubscription för varje användare
                    dto.setHasActiveSubscription(calculateHasActiveSubscription(user.getId()));
                    return dto;
                })
                .toList();
    }

    // ==========================================
    // UPPDATERA ANVÄNDARE - METODER
    // ==========================================

    /**
     * UPPDATERA ANVÄNDARE (Legacy metod)
     * Använder RegisterRequestDTO.
     * OBS: Överväg att använda updateUser() med UpdateUserRequestDTO istället.
     */
    @Override
    public UserResponseDTO updateUserById(Long id, RegisterRequestDTO request) {
        // HITTA: Användare att uppdatera
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Användare hittades inte med ID: " + id));

        // VALIDERING: Kontrollera att email är unikt (om den ändras)
        if (userRepository.existsByEmail(request.getEmail()) && !user.getEmail().equals(request.getEmail())) {
            throw new EmailAllreadyExistsException("Email existerar redan");
        }

        // UPPDATERA: Användarens fält
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // SPARA: Ändringar i databasen
        User updatedUser = userRepository.save(user);

        // KONVERTERA: Till DTO och sätt hasActiveSubscription
        UserResponseDTO dto = new UserResponseDTO(updatedUser);
        dto.setHasActiveSubscription(calculateHasActiveSubscription(updatedUser.getId()));

        return dto;
    }

    /**
     * UPPDATERA ANVÄNDARE (Modern metod)
     * Använder UpdateUserRequestDTO med optional fields.
     * Rekommenderad metod för uppdateringar.
     */
    @Override
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        // HITTA: Användare att uppdatera
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Användare med ID " + id + " hittades inte"));

        // UPPDATERA: Användarens fält
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // OPTIONAL: Uppdatera endast om värde finns
        if (request.getPersonalNumber() != null && !request.getPersonalNumber().isEmpty()) {
            user.setPersonalNumber(request.getPersonalNumber());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // SPARA: Ändringar
        User updatedUser = userRepository.save(user);

        // KONVERTERA: Till DTO och sätt hasActiveSubscription
        UserResponseDTO dto = new UserResponseDTO(updatedUser);
        dto.setHasActiveSubscription(calculateHasActiveSubscription(updatedUser.getId()));

        return dto;
    }
/**
 * TA BORT ANVÄNDARE (MED CASCADE DELETE VIA REST API)
 * 
 * Permanent radering från databasen och relaterade services.
 * VARNING: Detta kan inte ångras!
 * 
 * CASCADE DELETE FLOW:
 * 1. Radera subscriptions (UserService DB)
 * 2. Radera login tokens (UserService DB)
 * 3. Anropa PaymentService för att radera payments (PaymentService DB)
 * 4. Radera användaren (UserService DB)
 * 
 * MICROSERVICE KOMMUNIKATION:
 * - WebClient används för att anropa PaymentService
 * - Eureka service discovery hanterar routing automatiskt
 * - Felanrop loggas men blockerar inte raderingen (fail-safe)
 * 
 * @param id - User ID att radera
 * @throws UserNotFoundException om användaren inte finns
 */
@Override
public void delteUserById(Long id) {
    try {
        // STEG 1: HITTA användare att radera
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Användaren hittades inte med ID: " + id));

        System.out.println("🗑️ Initierar cascade delete för user ID: " + id + " (" + user.getEmail() + ")");

        // DEBUG: Kolla om WebClient är injected
        if (paymentServiceWebClient == null) {
            System.err.println("❌ FATAL: paymentServiceWebClient är NULL! Har UserService startat om?");
            throw new RuntimeException("WebClient är inte konfigurerad. Starta om UserService.");
        }

        // STEG 2: RADERA alla subscriptions (UserService DB)
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(id);
        if (!subscriptions.isEmpty()) {
            subscriptionRepository.deleteAll(subscriptions);
            System.out.println("✅ Raderade " + subscriptions.size() + " subscriptions");
        }

        // STEG 3: RADERA alla login tokens (UserService DB)
        List<LoginToken> tokens = loginTokenRepository.findByEmail(user.getEmail());
        if (!tokens.isEmpty()) {
            loginTokenRepository.deleteAll(tokens);
            System.out.println("✅ Raderade " + tokens.size() + " login tokens");
        }

        // STEG 4: ANROPA PaymentService för att radera payments (PaymentService DB)
        try {
            System.out.println("📞 Anropar PaymentService för att radera payments...");
            
            String response = paymentServiceWebClient
                    .delete()
                    .uri("/api/admin/payments/users/" + id)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            System.out.println("✅ Payments raderade i PaymentService: " + response);
            
        } catch (Exception e) {
            // Logga fel men fortsätt (fail-safe approach)
            System.err.println("⚠️ Varning: Kunde inte radera payments i PaymentService");
            System.err.println("⚠️ Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.err.println("⚠️ Fortsätter med användarradering...");
        }

        // STEG 5: RADERA användaren (nu finns inga Foreign Key Constraints)
        userRepository.delete(user);
        System.out.println("✅ Användare raderad: " + user.getEmail() + " (ID: " + id + ")");
        System.out.println("✅ CASCADE DELETE GENOMFÖRD");
        
    } catch (Exception e) {
        System.err.println("❌ CASCADE DELETE FAILED för user ID: " + id);
        System.err.println("❌ Exception: " + e.getClass().getName());
        System.err.println("❌ Message: " + e.getMessage());
        e.printStackTrace();
        throw e; // Re-throw för att GlobalExceptionHandler ska fånga
    }
}

    // ==========================================
    // STATISTIK & ALIAS-METODER
    // ==========================================

    /**
     * RÄKNA TOTALT ANTAL ANVÄNDARE
     * Används för statistik i AdminService.
     */
    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    /**
     * ALIAS: getAllUsers() → findAllUsers()
     * Används av AdminService för konsekvent namngivning.
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        return findAllUsers();
    }

    /**
     * ALIAS: getUserById() → findById()
     * Används av AdminService för konsekvent namngivning.
     */
    @Override
    public UserResponseDTO getUserById(Long id) {
        return findById(id);
    }

    // ==========================================
    // PRIVAT HJÄLPMETOD - SUBSCRIPTION CHECK
    // ==========================================

    /**
     * BERÄKNA AKTIV PRENUMERATION
     * 
     * Kontrollerar om användaren har en aktiv, icke-utgången prenumeration.
     * Används av alla metoder som returnerar UserResponseDTO.
     * 
     * LOGIK:
     * 1. Hämta alla subscriptions där cancelled = false (inte avbrutna)
     * 2. Filtrera bort utgångna (isExpired() = true)
     * 3. Returnera true om minst EN icke-avbruten, icke-utgången subscription finns
     * 
     * EXEMPEL:
     * - Subscription cancelled=false, endDate=2026-12-31 (framtiden) → TRUE ✅
     * - Subscription cancelled=false, endDate=2025-01-01 (förflutet) → FALSE ❌
     * (utgången)
     * - Subscription cancelled=true → FALSE ❌ (ej inkluderad i sökning)
     * 
     * @param userId - User ID att kontrollera
     * @return true om användaren har minst EN giltig prenumeration
     */
    private boolean calculateHasActiveSubscription(Long userId) {
        // Hämta alla subscriptions som inte är avbrutna (cancelled = false)
        List<Subscription> nonCancelledSubscriptions = subscriptionRepository.findByUserIdAndCancelledFalse(userId);

        // Kolla om minst EN inte har gått ut
        return nonCancelledSubscriptions.stream()
                .anyMatch(sub -> !sub.isExpired());
    }

}
