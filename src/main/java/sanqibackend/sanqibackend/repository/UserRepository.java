package sanqibackend.sanqibackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sanqibackend.sanqibackend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByUsernameAndPassword(String username, String password);
}