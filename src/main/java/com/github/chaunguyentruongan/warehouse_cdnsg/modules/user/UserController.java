package com.github.chaunguyentruongan.warehouse_cdnsg.modules.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.dto.UserCreateDTO;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.dto.UserUpdatedDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Quản lý Người dùng", description = "Các API quản lý người dùng, kích hoạt, vô hiệu hóa và xóa tài khoản")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Tạo user mới", description = "Tạo tài khoản người dùng mới với role và quyền cụ thể.")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateDTO request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "Cập nhật user", description = "Cập nhật thông tin người dùng theo email.")
    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody UserUpdatedDTO request) {
        return ResponseEntity.ok(userService.updatedUser(request));
    }

    @Operation(summary = "Lấy thông tin user theo email", description = "Trả về thông tin user đã đăng ký.")
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @Operation(summary = "Tìm user theo email hoặc username", description = "Tìm tài khoản người dùng bằng email hoặc username.")
    @GetMapping("/search")
    public ResponseEntity<User> searchUser(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(userService.findByEmailOrUsername(keyword));
    }

    @Operation(summary = "Vô hiệu hóa user", description = "Đặt trạng thái tài khoản là không hoạt động.")
    @PatchMapping("/{email}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable String email) {
        userService.disabledUser(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Kích hoạt user", description = "Đặt trạng thái tài khoản là hoạt động.")
    @PatchMapping("/{email}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable String email) {
        userService.activeUser(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xóa user", description = "Xóa tài khoản theo email.")
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy danh sách user", description = "Lấy danh sách người dùng có phân trang và tìm kiếm theo tên, email, mã NV")
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.getAllUsers(keyword, page, size));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(userService.getAllPermissions());
    }
}
