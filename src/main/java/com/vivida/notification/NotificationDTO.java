package com.vivida.notification;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDTO {

    public Integer id;
    public NotificationType type;
    public boolean read;
    public String createdAt;      // UTC ISO string
    public Integer groupId;
    public String groupName;
    public String actorUsername;
    public String episodeTitle;
    public Integer episodeNumber;
    public Integer invitationId;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = n.getId();
        dto.type = n.getType();
        dto.read = n.isRead();
        dto.createdAt = n.getCreatedAt() != null
                ? n.getCreatedAt().toInstant(ZoneOffset.UTC).toString()
                : null;
        if (n.getGroup() != null) {
            dto.groupId = n.getGroup().getId();
            dto.groupName = n.getGroup().getName();
        }
        if (n.getActor() != null) {
            dto.actorUsername = n.getActor().getUsername();
        }
        if (n.getEpisode() != null) {
            dto.episodeTitle = n.getEpisode().getEpisodeTitle();
            dto.episodeNumber = n.getEpisode().getEpisodeNumber();
        }
        dto.invitationId = n.getInvitationId();
        return dto;
    }

    public static List<NotificationDTO> fromList(List<Notification> notifications) {
        return notifications.stream().map(NotificationDTO::from).collect(Collectors.toList());
    }
}
