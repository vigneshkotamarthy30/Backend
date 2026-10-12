package com.hotel.service;

import com.hotel.dto.UserDto;
import com.hotel.entity.Role;
import com.hotel.entity.User;
import com.hotel.exception.NotFoundException;
import com.hotel.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String firstName, String lastName, String email, String password, String phone) {
        return registerUser(firstName, lastName, email, password, phone, Role.CUSTOMER);
    }

    @Transactional
    public User registerHotelPartner(String firstName, String lastName, String email, String password, String phone) {
        return registerUser(firstName, lastName, email, password, phone, Role.HOTEL_PARTNER);
    }

    private User registerUser(String firstName, String lastName, String email, String password, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists.");
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            .accountLocked(!user.isEnabled())
            .disabled(!user.isEnabled())
            .build();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Authenticated user not found."));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}
