package formulAI.project.bank.banqueCredit.dto;

import java.time.Instant;

/** Reponse d'authentification. */
public class AuthResponseDto {

    private String token;
    private String username;
    private String role;
    private Instant expiresAt;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, String username, String role, Instant expiresAt) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}

