package ru.maverick.cardmanagementsystem.user.service;

import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.maverick.cardmanagementsystem.user.request.UpdateUserRequest;

public interface UserService extends UserDetailsService {

    boolean existsByEmail(String email);

    User createUser(User user);

    User updateUser(Integer userId, UpdateUserRequest request);

    User getUserById(Integer userId);
    User getUserByEmail(String email);
    User getCurrentUser();
    Page<User> getAllUsers(Pageable pageable, Role role);

    void deleteUser(Integer userId);
}
