package org.nikolait.assignment.caloriex.service;

import org.nikolait.assignment.caloriex.model.User;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);
}
