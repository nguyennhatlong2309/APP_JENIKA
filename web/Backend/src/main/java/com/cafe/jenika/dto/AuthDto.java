package com.cafe.jenika.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        private String username;
        private String password;
        private Integer nhanVienId;
        private List<String> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        @Builder.Default
        private String type = "Bearer";
        private String username;
        private List<String> roles;
        private String tenNhanVien;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountResponse {
        private Integer id;
        private String username;
        private String trangThai;
        private java.time.LocalDateTime ngayTao;
        private Integer nhanVienId;
        private String tenNhanVien;
        private List<String> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAccountRequest {
        private String trangThai;
        private List<String> roles;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleDetailResponse {
        private Integer id;
        private String tenVaiTro;
        private String moTa;
        private List<String> quyens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoleRequest {
        private String tenVaiTro;
        private String moTa;
        private List<String> quyens;
    }
}
