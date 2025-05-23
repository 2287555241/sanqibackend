package sanqibackend.sanqibackend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sanqibackend.sanqibackend.entity.User;
import sanqibackend.sanqibackend.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User loginService(String username, String password) {
        User user = userRepository.findByUsernameAndPassword(username, password);
        if (user != null) {
            user.setPassword(""); // 返回时不包含密码
        }
        return user;
    }

    @Override
    public User registService(User user) {
        if (!user.getUsername().matches("^[a-zA-Z0-9]+$")) {
            return null; // 用户名格式不正确
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return null; // 用户名已存在
        }
        User newUser = userRepository.save(user);
        if (newUser != null) {
            newUser.setPassword(""); // 返回时不包含密码
        }
        return newUser;
    }
}