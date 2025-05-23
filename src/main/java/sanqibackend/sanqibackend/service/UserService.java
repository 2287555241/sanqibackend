package sanqibackend.sanqibackend.service;

import sanqibackend.sanqibackend.entity.User;

public interface UserService {
    User loginService(String username, String password);
    User registService(User user);
}