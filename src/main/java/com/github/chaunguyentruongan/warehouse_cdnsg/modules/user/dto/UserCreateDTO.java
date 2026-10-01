package com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    private String schoolID;
    private String fullName;
    private String username;
    private String password;
    private String email;

    private Long roleID;
    private List<Long> permissionIDs;
}
