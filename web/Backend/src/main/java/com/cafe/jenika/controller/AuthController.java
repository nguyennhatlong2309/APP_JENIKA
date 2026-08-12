package com.cafe.jenika.controller;

import com.cafe.jenika.dto.AuthDto.*;
import com.cafe.jenika.model.NhanVien;
import com.cafe.jenika.model.Quyen;
import com.cafe.jenika.model.TaiKhoan;
import com.cafe.jenika.model.VaiTro;
import com.cafe.jenika.repository.NhanVienRepository;
import com.cafe.jenika.repository.QuyenRepository;
import com.cafe.jenika.repository.TaiKhoanRepository;
import com.cafe.jenika.repository.VaiTroRepository;
import com.cafe.jenika.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final QuyenRepository quyenRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          TaiKhoanRepository taiKhoanRepository,
                          VaiTroRepository vaiTroRepository,
                          QuyenRepository quyenRepository,
                          NhanVienRepository nhanVienRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.quyenRepository = quyenRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

            String tenNhanVien = taiKhoan.getNhanVien() != null ? taiKhoan.getNhanVien().getTenNhanVien() : "Guest";

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    "Bearer",
                    userDetails.getUsername(),
                    roles,
                    tenNhanVien
            ));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Tài khoản đã bị khóa hoặc ngừng hoạt động.");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (taiKhoanRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
        }

        NhanVien nhanVien = null;
        if (signUpRequest.getNhanVienId() != null) {
            nhanVien = nhanVienRepository.findById(signUpRequest.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy nhân viên với ID " + signUpRequest.getNhanVienId()));
        }

        Set<VaiTro> roles = new HashSet<>();
        if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) {
            VaiTro defaultRole = vaiTroRepository.findByTenVaiTro("ROLE_STAFF")
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò ROLE_STAFF mặc định."));
            roles.add(defaultRole);
        } else {
            signUpRequest.getRoles().forEach(roleStr -> {
                String fullRoleName = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr.toUpperCase();
                VaiTro foundRole = vaiTroRepository.findByTenVaiTro(fullRoleName)
                        .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò " + fullRoleName));
                roles.add(foundRole);
            });
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .trangThai("ACTIVE")
                .nhanVien(nhanVien)
                .vaiTros(roles)
                .build();

        taiKhoanRepository.save(taiKhoan);

        return ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts() {
        List<TaiKhoan> accounts = taiKhoanRepository.findAll();
        List<AccountResponse> response = accounts.stream().map(acc -> AccountResponse.builder()
                .id(acc.getId())
                .username(acc.getUsername())
                .trangThai(acc.getTrangThai())
                .ngayTao(acc.getNgayTao())
                .nhanVienId(acc.getNhanVien() != null ? acc.getNhanVien().getId() : null)
                .tenNhanVien(acc.getNhanVien() != null ? acc.getNhanVien().getTenNhanVien() : "Chưa liên kết")
                .roles(acc.getVaiTros().stream().map(VaiTro::getTenVaiTro).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(vaiTroRepository.findAll().stream()
                .map(VaiTro::getTenVaiTro)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @GetMapping("/unlinked-employees")
    public ResponseEntity<?> getUnlinkedEmployees() {
        List<NhanVien> allEmployees = nhanVienRepository.findAll();
        List<TaiKhoan> allAccounts = taiKhoanRepository.findAll();
        Set<Integer> linkedEmployeeIds = allAccounts.stream()
                .filter(acc -> acc.getNhanVien() != null)
                .map(acc -> acc.getNhanVien().getId())
                .collect(Collectors.toSet());

        List<NhanVien> unlinked = allEmployees.stream()
                .filter(nv -> !linkedEmployeeIds.contains(nv.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(unlinked);
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @PutMapping("/accounts/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Integer id, @RequestBody UpdateAccountRequest req) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        if (req.getTrangThai() != null) {
            taiKhoan.setTrangThai(req.getTrangThai());
        }

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            taiKhoan.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        }

        if (req.getRoles() != null) {
            Set<VaiTro> roles = new HashSet<>();
            req.getRoles().forEach(roleStr -> {
                String fullRoleName = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr.toUpperCase();
                VaiTro foundRole = vaiTroRepository.findByTenVaiTro(fullRoleName)
                        .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò " + fullRoleName));
                roles.add(foundRole);
            });
            taiKhoan.setVaiTros(roles);
        }

        taiKhoanRepository.save(taiKhoan);
        return ResponseEntity.ok("Cập nhật tài khoản thành công!");
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        if ("admin".equals(taiKhoan.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Không thể xóa tài khoản admin hệ thống!");
        }

        taiKhoanRepository.delete(taiKhoan);
        return ResponseEntity.ok("Xóa tài khoản thành công!");
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions() {
        return ResponseEntity.ok(quyenRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @GetMapping("/roles/details")
    public ResponseEntity<?> getRolesDetails() {
        List<VaiTro> roles = vaiTroRepository.findAll();
        List<RoleDetailResponse> response = roles.stream().map(role -> RoleDetailResponse.builder()
                .id(role.getId())
                .tenVaiTro(role.getTenVaiTro())
                .moTa(role.getMoTa())
                .quyens(role.getQuyens() != null ? role.getQuyens().stream().map(Quyen::getTenQuyen).collect(Collectors.toList()) : List.of())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody CreateRoleRequest req) {
        if (req.getTenVaiTro() == null || req.getTenVaiTro().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Lỗi: Tên vai trò không được để trống!");
        }

        String fullRoleName = req.getTenVaiTro().trim();
        if (!fullRoleName.startsWith("ROLE_")) {
            fullRoleName = "ROLE_" + fullRoleName.toUpperCase();
        }

        if (vaiTroRepository.findByTenVaiTro(fullRoleName).isPresent()) {
            return ResponseEntity.badRequest().body("Lỗi: Vai trò " + fullRoleName + " đã tồn tại!");
        }

        Set<Quyen> quyens = new HashSet<>();
        if (req.getQuyens() != null) {
            for (String permName : req.getQuyens()) {
                Quyen quyen = quyenRepository.findByTenQuyen(permName)
                        .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy quyền " + permName));
                quyens.add(quyen);
            }
        }

        VaiTro newRole = VaiTro.builder()
                .tenVaiTro(fullRoleName)
                .moTa(req.getMoTa())
                .quyens(quyens)
                .build();

        vaiTroRepository.save(newRole);
        return ResponseEntity.ok("Tạo vai trò mới thành công!");
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Integer id, @RequestBody CreateRoleRequest req) {
        VaiTro vaiTro = vaiTroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại."));

        List<String> defaultRoles = List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CASHIER", "ROLE_BARISTA", "ROLE_WAITER");
        if (defaultRoles.contains(vaiTro.getTenVaiTro())) {
            // Protect default role names
        } else {
            if (req.getTenVaiTro() != null && !req.getTenVaiTro().trim().isEmpty()) {
                String fullRoleName = req.getTenVaiTro().trim();
                if (!fullRoleName.startsWith("ROLE_")) {
                    fullRoleName = "ROLE_" + fullRoleName.toUpperCase();
                }

                if (!fullRoleName.equals(vaiTro.getTenVaiTro()) && vaiTroRepository.findByTenVaiTro(fullRoleName).isPresent()) {
                    return ResponseEntity.badRequest().body("Lỗi: Tên vai trò " + fullRoleName + " đã tồn tại!");
                }
                vaiTro.setTenVaiTro(fullRoleName);
            }
        }

        if (req.getMoTa() != null) {
            vaiTro.setMoTa(req.getMoTa());
        }

        if (req.getQuyens() != null) {
            Set<Quyen> quyens = new HashSet<>();
            for (String permName : req.getQuyens()) {
                Quyen quyen = quyenRepository.findByTenQuyen(permName)
                        .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy quyền " + permName));
                quyens.add(quyen);
            }

            if ("ROLE_ADMIN".equals(vaiTro.getTenVaiTro())) {
                boolean hasAccountsManage = req.getQuyens().contains("ACCOUNTS_MANAGE");
                if (!hasAccountsManage) {
                    return ResponseEntity.badRequest().body("Lỗi: Không thể xóa quyền quản trị tài khoản (ACCOUNTS_MANAGE) của vai trò Admin!");
                }
            }

            vaiTro.setQuyens(quyens);
        }

        vaiTroRepository.save(vaiTro);
        return ResponseEntity.ok("Cập nhật vai trò thành công!");
    }

    @PreAuthorize("hasAuthority('ACCOUNTS_MANAGE')")
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer id) {
        VaiTro vaiTro = vaiTroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại."));

        List<String> defaultRoles = List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CASHIER", "ROLE_BARISTA", "ROLE_WAITER");
        if (defaultRoles.contains(vaiTro.getTenVaiTro())) {
            return ResponseEntity.badRequest().body("Lỗi: Không thể xóa vai trò mặc định của hệ thống!");
        }

        List<TaiKhoan> accounts = taiKhoanRepository.findAll();
        boolean isUsed = accounts.stream().anyMatch(acc -> acc.getVaiTros().contains(vaiTro));
        if (isUsed) {
            return ResponseEntity.badRequest().body("Lỗi: Không thể xóa vai trò này vì đang được sử dụng bởi một hoặc nhiều tài khoản!");
        }

        vaiTroRepository.delete(vaiTro);
        return ResponseEntity.ok("Xóa vai trò thành công!");
    }
}
