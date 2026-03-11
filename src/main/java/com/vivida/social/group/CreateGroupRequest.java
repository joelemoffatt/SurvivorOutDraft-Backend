package com.vivida.social.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    private String name;
    private AdminRef admin;
    private SeasonRef season;
    private Integer teamSize;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminRef {
        private Integer id;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeasonRef {
        private Integer id;
    }
}
