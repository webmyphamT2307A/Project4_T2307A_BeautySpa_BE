package org.aptech.backendmypham.configs;

import org.aptech.backendmypham.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetailsForUser implements UserDetails {

    private final User user;

    // Thêm constructor tường minh để khởi tạo customer
    public CustomUserDetailsForUser(User user) {
        this.user = user;
    }

    // --- CÁC PHƯƠG THỨC TIỆN ÍCH ---

    // Sửa lại kiểu dữ liệu trả về là Long cho nhất quán
    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    // --- CÁC PHƯƠNG THỨC CỦA UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Gán một vai trò logic mặc định cho tất cả khách hàng
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Username dùng để đăng nhập là email
        return user.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true; // Bạn có thể sửa theo logic riêng nếu muốn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Bạn có thể sửa theo logic riêng nếu muốn
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Bạn có thể sửa theo logic riêng nếu muốn
    }
    @Override
    public boolean isEnabled() {
        return user.getIsActive() != null && user.getIsActive() == 1;
    }
}
