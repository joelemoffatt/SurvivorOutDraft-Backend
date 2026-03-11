package com.vivida.draft;

import com.vivida.auth.User;

/**
 * DTO representing a user's position in the draft order
 * Calculated on-the-fly, not stored in database
 */
public class DraftPositionDTO {
    public Integer position;        // 0, 1, 2, 3 (their position in draft order)
    public UserDTO user;
    public Integer teamId;
    public String teamName;
    public Integer pickCount;       // How many picks they've made
    public Integer nextPickNumber;  // When they pick next (null if not calculable)

    public DraftPositionDTO(Integer position,
                            User user,
                            Integer teamId,
                            String teamName,
                            Integer pickCount,
                            Integer nextPickNumber) {
        this.position = position;
        this.user = new UserDTO(user);
        this.teamId = teamId;
        this.teamName = teamName;
        this.pickCount = pickCount;
        this.nextPickNumber = nextPickNumber;
    }

    public static class UserDTO {
        public Integer id;
        public String username;

        public UserDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
        }
    }
}
