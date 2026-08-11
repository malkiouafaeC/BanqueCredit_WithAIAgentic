package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.AuthResponseDto;
import formulAI.project.bank.banqueCredit.model.Role;
import formulAI.project.bank.banqueCredit.model.User;
import formulAI.project.bank.banqueCredit.repository.UserRepository;
import formulAI.project.bank.banqueCredit.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * QA-TASK-007 - Unitaire pur (Mockito) de AuthServiceImpl.
 * Business rule : US-001, AC-001-3 (message d'erreur generique, pas de fuite d'info sur la cause).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("conseiller1");
        user.setPassword("hashed");
        user.setRole(Role.CONSEILLER);
        user.setActif(true);
    }

    @Test
    void login_shouldReturnTokenAndRole_whenCredentialsValid() {
        when(userRepository.findByUsername("conseiller1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Conseiller123!", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken("conseiller1", "CONSEILLER")).thenReturn("token123");
        when(jwtTokenProvider.getExpiration("token123")).thenReturn(Instant.parse("2030-01-01T00:00:00Z"));

        AuthResponseDto result = authService.login(new AuthRequestDto("conseiller1", "Conseiller123!"));

        assertThat(result.getToken()).isEqualTo("token123");
        assertThat(result.getUsername()).isEqualTo("conseiller1");
        assertThat(result.getRole()).isEqualTo("CONSEILLER");
    }

    @Test
    void login_shouldThrowGenericBadCredentialsException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequestDto("inconnu", "whatever")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Identifiants invalides");
    }

    @Test
    void login_shouldThrowGenericBadCredentialsException_whenUserInactive() {
        user.setActif(false);
        when(userRepository.findByUsername("conseiller1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new AuthRequestDto("conseiller1", "Conseiller123!")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Identifiants invalides");
    }

    @Test
    void login_shouldThrowGenericBadCredentialsException_whenPasswordIncorrect() {
        when(userRepository.findByUsername("conseiller1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequestDto("conseiller1", "wrong")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Identifiants invalides");
    }
}

