package com.hse.leihsy.service;

import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.exception.UnauthorizedException;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("keycloak-123", "Test User");
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setBudget(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("searchUsers Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Sollte alle User zurückgeben bei leerer Query")
        void shouldReturnAllUsersWhenQueryEmpty() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<User> result = userService.searchUsers(null);

            assertThat(result).hasSize(1);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Sollte User nach Name suchen")
        void shouldSearchUsersByName() {
            when(userRepository.findByNameContainingIgnoreCase("Test"))
                    .thenReturn(List.of(testUser));

            List<User> result = userService.searchUsers("Test");

            assertThat(result).hasSize(1);
            verify(userRepository).findByNameContainingIgnoreCase("Test");
        }
    }

    @Nested
    @DisplayName("getOrCreateUser Tests")
    class GetOrCreateUserTests {

        @Test
        @DisplayName("Sollte existierenden User zurückgeben")
        void shouldReturnExistingUser() {
            when(userRepository.findByUniqueId("keycloak-123"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getOrCreateUser("keycloak-123", "Test User", "test@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test User");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Sollte neuen User erstellen wenn nicht existiert")
        void shouldCreateNewUser() {
            when(userRepository.findByUniqueId("new-keycloak-id"))
                    .thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });

            User result = userService.getOrCreateUser("new-keycloak-id", "New User", "new@example.com");

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Sollte User anhand ID finden")
        void shouldFindUserById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            User result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("Sollte RuntimeException werfen wenn nicht gefunden")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getUserByUniqueId Tests")
    class GetUserByUniqueIdTests {

        @Test
        @DisplayName("Sollte User anhand UniqueId finden")
        void shouldFindUserByUniqueId() {
            when(userRepository.findByUniqueId("keycloak-123"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getUserByUniqueId("keycloak-123");

            assertThat(result).isNotNull();
            assertThat(result.getUniqueId()).isEqualTo("keycloak-123");
        }
    }

    @Nested
    @DisplayName("getUserByName Tests")
    class GetUserByNameTests {

        @Test
        @DisplayName("Sollte User anhand Name finden")
        void shouldFindUserByName() {
            when(userRepository.findByName("Test User"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getUserByName("Test User");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("Sollte ResourceNotFoundException werfen wenn nicht gefunden")
        void shouldThrowWhenNameNotFound() {
            when(userRepository.findByName("Unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByName("Unknown"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCurrentUser Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Sollte Fehler werfen wenn keine Authentication")
        void shouldThrowWhenNoAuthentication() {
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Sollte aktuellen User aus JWT holen")
        void shouldGetCurrentUserFromJwt() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("keycloak-123");

            Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(jwt);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUniqueId("keycloak-123"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test User");
        }
    }
}
