package com.team9.virtualwallet.services.contracts;

import com.team9.virtualwallet.models.Pages;
import com.team9.virtualwallet.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getById(User user, int id);

    Pages<User> getAll(User user, Pageable pageable);

    Pages<User> getAllUnverified(User user, Pageable pageable);

    User getByUsername(String username);

    User getByField(User user, String fieldName, String searchTerm);

    void create(User user, Optional<String> invitationTokenUUID);

    void update(User userExecuting, User user, int id);

    void updateProfilePhoto(User user, MultipartFile multipartFile);

    void updateIdAndSelfiePhoto(User user, MultipartFile idPhoto, MultipartFile selfiePhoto);

    void removeProfilePhoto(User user);

    void delete(User user);

    void verifyUser(User userExecuting, int userId);

    void confirmUser(String confirmationTokenUUID, Optional<String> invitationTokenUUID);

    void addContact(User userExecuting, int contactId);

    void removeContact(User userExecuting, int contactId);

    List<User> getContacts(User user);

    void makeEmployee(User userExecuting, int userId);

    void removeEmployee(User userExecuting, int userId);

    void blockUser(User userExecuting, int id);

    void unblockUser(User userExecuting, int id);

    Pages<User> filter(User user,
                       Optional<String> userName,
                       Optional<String> phoneNumber,
                       Optional<String> email,
                       Pageable pageable);

    void inviteFriend(User user, String email);
}
