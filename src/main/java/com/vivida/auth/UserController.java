package com.vivida.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getAllUsers().stream()
                .map(userService::toUserDTO)
                .toList();
    }

    @GetMapping("{id}")
    public UserDTO getUserById(@PathVariable Integer id) {
        return userService.toUserDTO(userService.getUserById(id));
    }

    @GetMapping("username/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.toUserDTO(userService.getUserByUsername(username));
    }

    @GetMapping("email/{email}")
    public UserDTO getUserByEmail(@PathVariable String email) {
        return userService.toUserDTO(userService.getUserByEmail(email));
    }

    @PostMapping(value = "{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadAvatar(@PathVariable Integer id,
                             @RequestPart("file") MultipartFile file,
                             Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        userService.uploadAvatar(id, requestingUser, file);
    }

    @GetMapping("{id}/avatar")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable Integer id) {
        UserAvatar avatar = userService.getAvatarByUserId(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String contentType = avatar.getContentType();
        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(avatar.getImageData());
    }

    @DeleteMapping("{id}/avatar")
    public void deleteAvatar(@PathVariable Integer id, Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        userService.deleteAvatar(id, requestingUser);
    }

    @PostMapping
    public void addUser(@RequestBody User user) {
        userService.insertUser(user);
    }

    @PutMapping
    public void updateUser(@RequestBody User user) {
        userService.updateUser(user);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUserById(id);
    }
}
