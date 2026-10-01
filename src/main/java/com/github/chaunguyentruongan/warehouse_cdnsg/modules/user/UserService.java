package com.github.chaunguyentruongan.warehouse_cdnsg.modules.user;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceExistsException;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.dto.UserCreateDTO;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.dto.UserUpdatedDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepo roleRepo;
    private final PermissionRepo permissionRepo;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("not found user by email: " + email));

        user.setPassword("");

        return user;
    }

    public User findBySchoolID(String schoolID) {
        return userRepository.findBySchoolID(schoolID)
                .orElseThrow(() -> new ResourceNotFoundException("not found user by schoolID: " + schoolID));
    }

    public User createUser(UserCreateDTO request) {
        // 1. Kiểm tra Email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceExistsException("Email này đã được sử dụng!");
        }

        // 2. Kiểm tra Username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceExistsException("Tên đăng nhập (Username) này đã tồn tại!");
        }

        // (Tùy chọn) 3. Kiểm tra Mã NV / Mã Trường
        if (userRepository.findBySchoolID(request.getSchoolID()).isPresent()) {
            throw new ResourceExistsException("Mã nhân viên/trường này đã tồn tại!");
        }

        Role role = roleRepo.findById(request.getRoleID()).orElse(null);

        if (role == null) {
            throw new ResourceNotFoundException("not found role by id: " + request.getRoleID());
        }

        Set<Permission> permissions = getPermissionFromRequest(request.getPermissionIDs());

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .isActive(true)
                .schoolID(request.getSchoolID())
                .role(role)
                .permissions(permissions)
                .build();

        return userRepository.save(user);
    }

    private Set<Permission> getPermissionFromRequest(List<Long> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(permissionID -> permissionRepo.findById(permissionID).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }

    public Role createRole(String name) {
        Role role = roleRepo.findByName(name).orElse(null);

        if (role != null) {
            throw new ResourceExistsException("role was existed: " + name);
        }

        role = new Role();
        role.setName(name);

        return roleRepo.save(role);
    }

    public Permission createPermission(String name) {
        Permission permission = permissionRepo.findByName(name).orElse(null);

        if (permission != null) {
            throw new ResourceExistsException("permission was existed: " + name);
        }

        permission = new Permission();
        permission.setName(name);

        return permissionRepo.save(permission);
    }

    public User updatedUser(UserUpdatedDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("not found user by email: " + request.getEmail()));

        Role role = roleRepo.findById(request.getRoleID()).orElse(null);

        if (role == null) {
            throw new ResourceNotFoundException("not found role by id: " + request.getRoleID());
        }

        user.setEmail(request.getEmail());
        user.setSchoolID(request.getSchoolID());
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setPermissions(getPermissionFromRequest(request.getPermissionIDs()));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    public boolean disabledUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("not found user by email: " + email));

        user.setActive(false);
        userRepository.save(user);

        return true;
    }

    public boolean activeUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("not found user by email: " + email));

        user.setActive(true);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }

    public User findByEmailOrUsername(String request) {
        return userRepository.findByUsername(request)
                .or(() -> userRepository.findByEmail(request))
                .orElseThrow(() -> new ResourceNotFoundException("not found user by email or username: " + request));
    }

    public Page<User> getAllUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return userRepository.searchUsers(keyword, pageable);
    }

    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    public List<Permission> getAllPermissions() {
        return permissionRepo.findAll();
    }
}
