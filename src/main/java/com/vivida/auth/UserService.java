package com.vivida.auth;

import com.vivida.game.castaway.Castaway;
import com.vivida.game.castaway.CastawayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final CastawayRepository castawayRepository;
    private final PasswordEncoder passwordEncoder;
    private static final long MAX_AVATAR_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public UserService(UserRepository userRepository, UserAvatarRepository userAvatarRepository, CastawayRepository castawayRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAvatarRepository = userAvatarRepository;
        this.castawayRepository = castawayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with id " + id
        ));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with username " + username
        ));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with email " + email
        ));
    }

    public void insertUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        user.setPassword(passwordEncoder.encode("123"));

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        List<Castaway> favoriteCastaways = resolveFavoriteCastaways(user.getFavoriteCastaways());
        if (favoriteCastaways != null && favoriteCastaways.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 3 favorite castaways allowed");
        }
        user.setFavoriteCastaways(favoriteCastaways);
        userRepository.save(user);
    }

    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required for update");
        }

        User existingUser = getUserById(user.getId());

        String nextUsername = user.getUsername();
        if (nextUsername == null || nextUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        String nextEmail = user.getEmail();
        if (nextEmail == null || nextEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        userRepository.findByUsername(nextUsername)
                .filter(found -> !found.getId().equals(existingUser.getId()))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                });

        userRepository.findByEmail(nextEmail)
                .filter(found -> !found.getId().equals(existingUser.getId()))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                });

        existingUser.setUsername(nextUsername);
        existingUser.setEmail(nextEmail);
        existingUser.setRole(user.getRole() == null ? existingUser.getRole() : user.getRole());
        existingUser.setEnabled(user.getEnabled() == null ? existingUser.getEnabled() : user.getEnabled());
        
        List<Castaway> favoriteCastaways = resolveFavoriteCastaways(user.getFavoriteCastaways());
        if (favoriteCastaways != null && favoriteCastaways.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 3 favorite castaways allowed");
        }
        existingUser.setFavoriteCastaways(favoriteCastaways);
        
        existingUser.setBio(user.getBio());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(existingUser);
    }

    public void uploadAvatar(int userId, User requestingUser, MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is required");
        }

        if (avatarFile.getSize() > MAX_AVATAR_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file size must be 5MB or less");
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, WEBP, and GIF avatars are supported");
        }

        if (!requestingUser.getId().equals(userId) && requestingUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only upload your own avatar");
        }

        User user = getUserById(userId);
        UserAvatar avatar = userAvatarRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserAvatar created = new UserAvatar();
                    created.setUser(user);
                    return created;
                });

        try {
            avatar.setImageData(avatarFile.getBytes());
            avatar.setContentType(contentType);
            userAvatarRepository.save(avatar);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read avatar upload", ex);
        }
    }

    @Transactional(readOnly = true)
    public UserAvatar getAvatarByUserId(int userId) {
        return userAvatarRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found"));
    }

    public void deleteAvatar(int userId, User requestingUser) {
        if (!requestingUser.getId().equals(userId) && requestingUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own avatar");
        }

        if (!userAvatarRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found");
        }

        UserAvatar avatar = getAvatarByUserId(userId);
        userAvatarRepository.delete(avatar);
    }

    public String getAvatarImageUrl(Integer userId) {
        if (userId == null || !userAvatarRepository.existsByUserId(userId)) {
            return null;
        }
        return AvatarImageUrlResolver.buildAvatarUrl(userId);
    }

    public UserDTO toUserDTO(User user) {
        return new UserDTO(user, userAvatarRepository.existsByUserId(user.getId()));
    }

    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

    private List<Castaway> resolveFavoriteCastaways(List<Castaway> requestedFavorites) {
        if (requestedFavorites == null || requestedFavorites.isEmpty()) {
            return new ArrayList<>();
        }

        if (requestedFavorites.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can select up to 3 favorite castaways");
        }

        Set<Integer> castawayIds = new LinkedHashSet<>();
        for (Castaway castaway : requestedFavorites) {
            if (castaway == null || castaway.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each favorite castaway must include an id");
            }
            castawayIds.add(castaway.getId());
        }

        if (castawayIds.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can select up to 3 unique favorite castaways");
        }

        List<Castaway> resolved = castawayRepository.findAllById(castawayIds);
        if (resolved.size() != castawayIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more favorite castaways were not found");
        }

        return resolved;
    }
}
