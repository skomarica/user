package io.github.skomarica.practice.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Sinisa Komarica
 */
@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User with id %d can not be found";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return Optional.ofNullable(userRepository.findOne(id)).orElseThrow(
                () -> new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, id)));
    }

    @Transactional
    public Long createUser(User user) {
        // ensure that the id is not set so the creation will occur (the entity is used as a DTO for this sample w/o JSON serializer customizations)
        user.setId(null);
        final User createdUser = userRepository.save(user);
        return createdUser.getId();
    }

    @Transactional
    public void updateUser(Long id, User user) {
        if (!userRepository.exists(id)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, id));
        }

        user.setId(id); // ensure that the id passed via path param is set so the update will occur (the entity is used as a DTO for this sample w/o JSON serializer customizations)

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.exists(id)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, id));
        }

        userRepository.delete(id);
    }
}
