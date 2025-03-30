package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.BranchRepository;
import org.aptech.backendmypham.repositories.RoleRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;

    //tạo thread pool với 2 thread
    ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void createUser(String password, String email, String phoneNumber, String address, Integer roleId, Integer branchId) {
        //tìm role và branch theo id
        //dùng thread để kiểm tra tồn tại của role và branch
        try {
            if (password == null || email == null || phoneNumber == null || address == null) {
                throw new RuntimeException("Thông tin không được để trống!");
            }
            Future<Optional<Role>> roleFuture;
            Future<Optional<Branch>> branchFuture = null;
            if (roleId != null) {
                // bất đồng bộ để kiểm tra sự tồn tại của role với id
                roleFuture = executor.submit(() ->
                        roleRepository.findById((long) roleId));
            } else {
                throw new RuntimeException("Role không được để trống!");
            }
            if (branchId != null) {
                // Bất đồng bộ để kiểm tra sự tồn tại của branch với id
                branchFuture = executor.submit(() ->
                        branchRepository.findById((long) branchId));
            }
            // Bất đồng bộ để kiểm tra sự tồn tại của Email nhập vào
            Future<Optional<User>> emailFuture = executor.submit(() ->
                    userRepository.findByEmail(email));

            // Bất đồng bộ để kiểm tra sự tồn tại của Phone nhập vào
            Future<Optional<User>> phoneFuture = executor.submit(() ->
                    userRepository.findByPhone(phoneNumber));


            Optional<Branch> branchOpt = Optional.empty();
            // Lấy kết quả với timeout 3 giây cho mỗi Future
            //truyền kiểu dữ liệu Optional<Role> vào hàm
            Optional<Role> roleOpt = getFutureResultWithTimeout(roleFuture, "role", 3);
            if (branchId != null) {
                //truyền kiểu dữ liệu Optional<Branch> vào hàm
                branchOpt = getFutureResultWithTimeout(branchFuture, "chi nhánh", 3);
            }
            //truyền kiểu dữ liệu Optional<User> vào hàm
            Optional<User> emailOpt = getFutureResultWithTimeout(emailFuture, "email", 3);
            //truyền kiểu dữ liệu Optional<User> vào hàm
            Optional<User> phoneOpt = getFutureResultWithTimeout(phoneFuture, "số điện thoại", 3);

            //Lấy dữ liệu thành công kiểm tra role có bằng null không
            if (roleOpt.isEmpty()) {
                throw new RuntimeException("Role không tồn tại!");
            }

            if (branchId != null) {
                //kiểm tra branch có null không
                if (branchOpt.isEmpty()) {
                    throw new RuntimeException("Chi nhánh không tồn tại!");
                }
            }
            //nếu email đã tồn tại thì trả về lỗi
            if (emailOpt.isPresent()) {
                throw new RuntimeException("Email đã tồn tại!");
            }
            //nếu số điện thoại đã tồn tại thì trả về lỗi
            if (phoneOpt.isPresent()) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }
            //nếu thỏa mãn điều kiện bên trên thì tạo mới user
            User user = new User();
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phoneNumber);
            user.setAddress(address);
            user.setRole(roleOpt.get());
            if (branchId != null) {
                //nếu branchId khác null thì set branch cho user
                user.setBranch(branchOpt.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại của role và branch: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    //hàm này kiểm tra giá trị so với list truyền vào
    //nếu có thì trả về true
    private boolean checkValueInList(String value, List<String> list) {
        for (String item : list) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    //hàm này dùng để lấy kết quả của future với timeout
    //sử dụng generic để có thể lấy được nhiều kiểu dữ liệu khác nhau
    // T thay cho kiểu dữ liệu được truyền vào
    private <T> T getFutureResultWithTimeout(Future<T> future, String entityName, int seconds)
            throws InterruptedException, ExecutionException {
        try {
            return future.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Tìm kiếm " + entityName + " quá thời gian. Vui lòng thử lại sau.");
        }
    }
}
