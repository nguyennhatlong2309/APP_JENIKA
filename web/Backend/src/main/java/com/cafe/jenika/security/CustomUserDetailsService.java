package com.cafe.jenika.security;

import com.cafe.jenika.model.TaiKhoan;
import com.cafe.jenika.repository.TaiKhoanRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    public CustomUserDetailsService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        if (!"ACTIVE".equalsIgnoreCase(taiKhoan.getTrangThai())) {
            throw new LockedException("Tài khoản đã bị khóa hoặc ngừng hoạt động.");
        }

        java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        taiKhoan.getVaiTros().forEach(vaiTro -> {
            authorities.add(new SimpleGrantedAuthority(vaiTro.getTenVaiTro()));
            if (vaiTro.getQuyens() != null) {
                vaiTro.getQuyens().forEach(quyen -> {
                    authorities.add(new SimpleGrantedAuthority(quyen.getTenQuyen()));
                });
            }
        });

        return new User(
                taiKhoan.getUsername(),
                taiKhoan.getPassword(),
                authorities
        );
    }
}
