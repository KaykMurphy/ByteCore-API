package com.byteCore.demo;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.LoginRequestDTO;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.LoginResponseDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.exceptions.DuplicateEmailException;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.security.JwtUtils;
import com.byteCore.demo.service.AuthService;
import com.mercadopago.resources.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenUserIsValid() {

       RegisterRequestDTO dto = new RegisterRequestDTO(
               "user", "email@gmail.com", "123456", "123456"
       );

       when(userRepository.findByEmail(dto.email()))
               .thenReturn(Optional.empty());

       UserEntity mappedUser = new  UserEntity();
       mappedUser.setEmail(dto.email());
       mappedUser.setName(dto.name());

       when(userMapper.toEntity(dto))
               .thenReturn(mappedUser);


       when(passwordEncoder.encode(dto.password()))
               .thenReturn("senha-criptografada-hash");

       UserResponseDTO expectedResponse = new UserResponseDTO(
               dto.name(), dto.email(), null
       );

       when(userMapper.toResponseDTO(any(UserEntity.class)))
               .thenReturn(expectedResponse);

       UserResponseDTO result = authService.register(dto);

       Assertions.assertEquals(expectedResponse, result);
       Assertions.assertNotNull(result);

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();

        Assertions.assertEquals("senha-criptografada-hash", savedUser.getPassword());
        Assertions.assertEquals(BigDecimal.ZERO, savedUser.getAvailableBalance());
        Assertions.assertEquals(BigDecimal.ZERO, savedUser.getPendingBalance());

        verify(userRepository, times(1))
                .findByEmail(dto.email());

        verify(userMapper, times(1))
                .toEntity(dto);

        verify(passwordEncoder, times(1))
                .encode(dto.password());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequestDTO dto = new  RegisterRequestDTO(
                "user", "email@gmail.com", "123456", "123456"
        );

        UserEntity user = new UserEntity();
        user.setEmail(dto.email());

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.of(user));

        DuplicateEmailException exception = Assertions.assertThrows(
                DuplicateEmailException.class,
                () -> authService.register(dto)
        );

        Assertions.assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verify(userRepository, times(1))
                .findByEmail(dto.email());
    }

    @Test
    void register_shouldSetAvailableBalanceToZero_whenNull() {

        RegisterRequestDTO dto = new RegisterRequestDTO(
                "user", "email@gmail.com",  "123456", "123456"
        );

        UserEntity user = new UserEntity();
        user.setAvailableBalance(null);

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.empty());


        when(userMapper.toEntity(dto))
                .thenReturn(user);

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encoded");

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(user);

        authService.register(dto);

        Assertions.assertEquals(BigDecimal.ZERO, user.getAvailableBalance());

    }


    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        LoginRequestDTO request = new   LoginRequestDTO(
                "email@gmail.com", "123456"
        );

        UserEntity user = new UserEntity();
        user.setRole(Role.USER);
        user.setEmail(request.email());

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(jwtUtils.generateToken(user.getEmail(), user.getRole().name()))
                .thenReturn("token-jwt");

        LoginResponseDTO result = authService.login(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("token-jwt", result.token());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository, times(1))
                .findByEmail(request.email());

        verify(jwtUtils, times(1))
                .generateToken(user.getEmail(), user.getRole().name());
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        LoginRequestDTO request = new LoginRequestDTO("email@gmail.com", "senha-errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        Assertions.assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtils, never()).generateToken(anyString(), anyString());
    }


    @Test
    void login_shouldThrowException_whenUserNotFound() {

        LoginRequestDTO request = new LoginRequestDTO("email@gmail.com", "123456");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        Assertions.assertEquals("Usuário não encontrado", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(jwtUtils, never()).generateToken(anyString(), anyString());
    }




































}
