package org.nikolait.assignment.caloriex.repository;

import org.nikolait.assignment.caloriex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<Long> findIdByEmail(String email);

    boolean existsByEmail(String email);

}
