package ru.maverick.cardmanagementsystem.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.maverick.cardmanagementsystem.exception.DuplicateException;
import ru.maverick.cardmanagementsystem.exception.NotFoundException;
import ru.maverick.cardmanagementsystem.jwt.service.JWTService;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;
import ru.maverick.cardmanagementsystem.user.repository.UserRepository;
import ru.maverick.cardmanagementsystem.user.request.UpdateUserRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JWTService jwtService;

    private final UserRepository userRepository;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }


    @Override
    public User createUser(User user) {

        try {
            User createdUser = userRepository.save(user);
            log.info("Successfully createdUser with userId={}, email:{}",
                    createdUser.getId(), createdUser.getEmail()
            );
            return createdUser;
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate. Request to create with already used email address for another user {}", user.getEmail());
            throw new DuplicateException("This email is already in use.");
        }
    }

    @Override
    public User updateUser(Integer userId, UpdateUserRequest request) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                String.format("User not found with ID: %s", userId)));

        existingUser.setRole(request.getRole());
        existingUser.setFirstName(request.getFistName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPassword(request.getPassword());
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        log.info("Successfully loadUserByUsername with userId={}, email:{}",
                user.getId(), user.getEmail()
        );
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        log.info("Successfully getUserByEmail with userId={}, email:{}",
                user.getId(), user.getEmail()
        );
        return user;
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable, Role role) {
        log.info("Successfully getUserByEmail with pageable={}, role:{}",
                pageable, role
        );

        if (role != null) {
            return userRepository.findByRole(role, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
             new NotFoundException("User with id " + userId + " not found.")
        );

        jwtService.revokeAllUserRefreshTokens(user);
        userRepository.deleteById(userId);
        log.info("Successfully deleteUser with userId={}, email:{}",
                userId, user.getEmail()
        );
    }
}
