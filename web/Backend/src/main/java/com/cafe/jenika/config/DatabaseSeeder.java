package com.cafe.jenika.config;

import com.cafe.jenika.model.NhanVien;
import com.cafe.jenika.model.Quyen;
import com.cafe.jenika.model.TaiKhoan;
import com.cafe.jenika.model.VaiTro;
import com.cafe.jenika.repository.NhanVienRepository;
import com.cafe.jenika.repository.QuyenRepository;
import com.cafe.jenika.repository.TaiKhoanRepository;
import com.cafe.jenika.repository.VaiTroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;
    private final QuyenRepository quyenRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(VaiTroRepository vaiTroRepository,
                          QuyenRepository quyenRepository,
                          TaiKhoanRepository taiKhoanRepository,
                          NhanVienRepository nhanVienRepository,
                          PasswordEncoder passwordEncoder) {
        this.vaiTroRepository = vaiTroRepository;
        this.quyenRepository = quyenRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Permissions
        String[] defaultPermissions = {
            "DASHBOARD_VIEW",
            
            "INVENTORY_VIEW", "INVENTORY_CREATE", "INVENTORY_EDIT", "INVENTORY_DELETE",
            "SALES_VIEW", "SALES_CREATE", "SALES_EDIT", "SALES_DELETE",
            "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_EDIT", "PURCHASE_DELETE",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_DELETE",
            "PARTNERS_VIEW", "PARTNERS_CREATE", "PARTNERS_EDIT", "PARTNERS_DELETE",
            
            "ACTIVITY_LOGS_VIEW",
            "EXCEL_CONFIG",
            "ACCOUNTS_MANAGE"
        };

        Set<String> defaultPermNames = new HashSet<>(Arrays.asList(defaultPermissions));

        // Clean up legacy/old permissions from DB that are no longer in defaultPermissions
        List<Quyen> allDbPerms = quyenRepository.findAll();
        List<VaiTro> allDbRoles = vaiTroRepository.findAll();

        for (Quyen dbPerm : allDbPerms) {
            if (!defaultPermNames.contains(dbPerm.getTenQuyen())) {
                // Remove this permission from all roles first to avoid foreign key constraint issues
                for (VaiTro role : allDbRoles) {
                    if (role.getQuyens() != null && role.getQuyens().contains(dbPerm)) {
                        role.getQuyens().remove(dbPerm);
                        vaiTroRepository.save(role);
                    }
                }
                // Delete the permission
                quyenRepository.delete(dbPerm);
            }
        }

        Map<String, Quyen> quyenMap = new HashMap<>();
        Set<Quyen> allQuyens = new HashSet<>();

        Map<String, String> permDescriptions = new HashMap<>();
        permDescriptions.put("DASHBOARD_VIEW", "Xem trang tổng quan, thống kê doanh thu");
        permDescriptions.put("INVENTORY_VIEW", "Xem trang quản lý hàng hóa");
        permDescriptions.put("INVENTORY_CREATE", "Thêm hàng hóa mới");
        permDescriptions.put("INVENTORY_EDIT", "Sửa hàng hóa");
        permDescriptions.put("INVENTORY_DELETE", "Xóa hàng hóa");
        permDescriptions.put("SALES_VIEW", "Xem lịch sử đơn bán hàng");
        permDescriptions.put("SALES_CREATE", "Tạo đơn bán hàng mới");
        permDescriptions.put("SALES_EDIT", "Sửa đơn bán hàng");
        permDescriptions.put("SALES_DELETE", "Hủy/Xóa đơn bán hàng");
        permDescriptions.put("PURCHASE_VIEW", "Xem lịch sử nhập hàng");
        permDescriptions.put("PURCHASE_CREATE", "Tạo đơn nhập hàng mới");
        permDescriptions.put("PURCHASE_EDIT", "Sửa đơn nhập hàng");
        permDescriptions.put("PURCHASE_DELETE", "Hủy/Xóa đơn nhập hàng");
        permDescriptions.put("EXPENSE_VIEW", "Xem sổ thu chi cửa hàng");
        permDescriptions.put("EXPENSE_CREATE", "Tạo phiếu thu chi mới");
        permDescriptions.put("EXPENSE_EDIT", "Sửa phiếu thu chi");
        permDescriptions.put("EXPENSE_DELETE", "Hủy/Xóa phiếu thu chi");
        permDescriptions.put("PARTNERS_VIEW", "Xem đối tác & nhân sự");
        permDescriptions.put("PARTNERS_CREATE", "Thêm đối tác & nhân sự mới");
        permDescriptions.put("PARTNERS_EDIT", "Sửa thông tin đối tác & nhân sự");
        permDescriptions.put("PARTNERS_DELETE", "Xóa đối tác & nhân sự");
        permDescriptions.put("ACTIVITY_LOGS_VIEW", "Xem nhật ký hoạt động hệ thống");
        permDescriptions.put("EXCEL_CONFIG", "Cấu hình cấu trúc tệp Excel");
        permDescriptions.put("ACCOUNTS_MANAGE", "Cấp tài khoản, đổi mật khẩu và phân quyền");

        for (String permName : defaultPermissions) {
            String moTa = permDescriptions.getOrDefault(permName, permName);
            Quyen quyen = quyenRepository.findByTenQuyen(permName)
                    .map(existing -> {
                        if (existing.getMoTa() == null || existing.getMoTa().startsWith("Quyền hạn")) {
                            existing.setMoTa(moTa);
                            return quyenRepository.save(existing);
                        }
                        return existing;
                    })
                    .orElseGet(() -> quyenRepository.save(Quyen.builder()
                            .tenQuyen(permName)
                            .moTa(moTa)
                            .build()));
            quyenMap.put(permName, quyen);
            allQuyens.add(quyen);
        }

        // 2. Seed & Link Roles
        String[] defaultRoles = {"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CASHIER", "ROLE_BARISTA", "ROLE_WAITER"};
        for (String roleName : defaultRoles) {
            VaiTro role = vaiTroRepository.findByTenVaiTro(roleName)
                    .orElseGet(() -> VaiTro.builder()
                            .tenVaiTro(roleName)
                            .moTa("Vai trò " + roleName.substring(5))
                            .build());

            Set<Quyen> roleQuyens = new HashSet<>();
            if ("ROLE_ADMIN".equals(roleName)) {
                roleQuyens.addAll(allQuyens);
            } else if ("ROLE_MANAGER".equals(roleName)) {
                roleQuyens.addAll(allQuyens);
            } else if ("ROLE_CASHIER".equals(roleName)) {
                roleQuyens.add(quyenMap.get("DASHBOARD_VIEW"));
                roleQuyens.add(quyenMap.get("INVENTORY_VIEW"));
                roleQuyens.add(quyenMap.get("SALES_VIEW"));
                roleQuyens.add(quyenMap.get("SALES_CREATE"));
                roleQuyens.add(quyenMap.get("EXPENSE_VIEW"));
                roleQuyens.add(quyenMap.get("PARTNERS_VIEW"));
            } else if ("ROLE_BARISTA".equals(roleName)) {
                roleQuyens.add(quyenMap.get("INVENTORY_VIEW"));
                roleQuyens.add(quyenMap.get("SALES_VIEW"));
            } else if ("ROLE_WAITER".equals(roleName)) {
                roleQuyens.add(quyenMap.get("SALES_VIEW"));
                roleQuyens.add(quyenMap.get("SALES_CREATE"));
            }

            role.setQuyens(roleQuyens);
            vaiTroRepository.save(role);
        }

        // 3. Seed Default Admin Account
        Optional<TaiKhoan> adminOpt = taiKhoanRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            NhanVien adminEmployee = nhanVienRepository.findAll().stream()
                    .filter(nv -> "Quản trị viên".equalsIgnoreCase(nv.getTenNhanVien()))
                    .findFirst()
                    .orElseGet(() -> nhanVienRepository.save(NhanVien.builder()
                            .tenNhanVien("Quản trị viên")
                            .sdt("0123456789")
                            .email("admin@cafe.com")
                            .vaiTro("Quản lý")
                            .build()));

            VaiTro adminRole = vaiTroRepository.findByTenVaiTro("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Default ROLE_ADMIN not found"));

            Set<VaiTro> roles = new HashSet<>();
            roles.add(adminRole);

            taiKhoanRepository.save(TaiKhoan.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("JenkaM@2026"))
                    .trangThai("ACTIVE")
                    .nhanVien(adminEmployee)
                    .vaiTros(roles)
                    .build());

            System.out.println(">>> SEED DATA: Created default admin account with all roles and permissions!");
        } else {
            // Update admin password to JenkaM@2026 to avoid security alerts
            TaiKhoan admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode("JenkaM@2026"));
            admin.setTrangThai("ACTIVE");
            taiKhoanRepository.save(admin);
            System.out.println(">>> SEED DATA: Updated admin password to JenkaM@2026 and set status to ACTIVE successfully!");
        }
    }
}
