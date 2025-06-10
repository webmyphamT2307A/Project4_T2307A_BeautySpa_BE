package org.aptech.backendmypham.configs; // Hoặc package security của bạn

import org.aptech.backendmypham.models.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// Bỏ các annotation constructor không cần thiết để code rõ ràng hơn
public class CustomUserDetails implements UserDetails {

    private final Customer customer;

    // Thêm constructor tường minh để khởi tạo customer
    public CustomUserDetails(Customer customer) {
        this.customer = customer;
    }

    // --- CÁC PHƯƠG THỨC TIỆN ÍCH ---

    // Sửa lại kiểu dữ liệu trả về là Long cho nhất quán
    public Integer getId() {
        return customer.getId();
    }

    public Customer getCustomer() {
        return customer;
    }

    // --- CÁC PHƯƠNG THỨC CỦA UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Gán một vai trò logic mặc định cho tất cả khách hàng
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public String getUsername() {
        // Username dùng để đăng nhập là email
        return customer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Trạng thái kích hoạt của tài khoản phụ thuộc vào customer
        return customer.getIsActive();
    }
}
