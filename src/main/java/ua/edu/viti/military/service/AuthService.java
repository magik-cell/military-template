package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.LoginRequestDTO;
import ua.edu.viti.military.dto.request.RegisterRequestDTO;
import ua.edu.viti.military.dto.response.JwtResponseDTO;
import ua.edu.viti.military.entity.Role;
import ua.edu.viti.military.entity.RoleName;
import ua.edu.viti.military.entity.User;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.RoleRepository;
import ua.edu.viti.military.repository.UserRepository;
import ua.edu.viti.military.security.JwtUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    /**
     * Login користувача
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequestDTO dto) {
        log.info("User login attempt: {}", dto.getUsername());
        
        // 1. Автентифікація
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                dto.getUsername(),
                dto.getPassword()
            )
        );
        
        // 2. Встановити в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 3. Генерувати JWT token
        String jwt = jwtUtils.generateToken(authentication);
        
        // 4. Отримати UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        
        // 5. Отримати email
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
        
        log.info("User logged in successfully: {}", dto.getUsername());
        
        return new JwtResponseDTO(
            jwt,
            userDetails.getUsername(),
            user.getEmail(),
            roles
        );
    }
    
    /**
     * Реєстрація нового користувача
     */
    @Transactional
    public String register(RegisterRequestDTO dto) {
        log.info("User registration attempt: {}", dto.getUsername());
        
        // 1. Перевірити чи не існує
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username вже зайнятий");
        }
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email вже зареєстрований");
        }
        
        // 2. Створити User
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // Hash password
        user.setFullName(dto.getFullName());
        user.setMilitaryRank(dto.getMilitaryRank());
        user.setEnabled(true);
        
        // 3. Призначити ролі
        Set<Role> roles = new HashSet<>();
        
        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            // За замовчуванням - VIEWER
            Role viewerRole = roleRepository.findByName(RoleName.ROLE_VIEWER)
                .orElseThrow(() -> new ResourceNotFoundException("Роль VIEWER не знайдено"));
            roles.add(viewerRole);
        } else {
            // Призначити вказані ролі
            for (String roleName : dto.getRoles()) {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new ResourceNotFoundException("Роль не знайдено: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role name: {}", roleName);
                }
            }
        }
        
        user.setRoles(roles);
        
        // 4. Зберегти
        userRepository.save(user);
        
        log.info("User registered successfully: {}", dto.getUsername());
        
        return "Користувача зареєстровано успішно";
    }
}
