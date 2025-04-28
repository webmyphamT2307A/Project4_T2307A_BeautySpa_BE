package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;

public interface userDetailService {
    ResponseObject login(LoginRequestDto dto);
    ResponseObject logout();
    ResponseObject getUserDetail(Long id);
    ResponseObject updateInfo(Long id, UserInfoUpdateDto dto);
    ResponseObject changePassword(Long id, UserPasswordChangeDto dto);
    ResponseObject registerUser(UserRegisterDto userRegisterDto);
}
