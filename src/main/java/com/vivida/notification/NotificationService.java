package com.vivida.notification;

import com.vivida.auth.User;
import com.vivida.game.episode.Episode;
import com.vivida.social.group.Group;
import com.vivida.social.group.GroupMember;
import com.vivida.social.group.GroupMemberRepository;
import com.vivida.social.group.MembershipStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GroupMemberRepository groupMemberRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               GroupMemberRepository groupMemberRepository) {
        this.notificationRepository = notificationRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // ── Creation ─────────────────────────────────────────────────────────────────

    /** Called after a group invite is saved. Actor is the user who sent the invite. */
    @Transactional
    public void createInviteReceived(GroupMember invitation, User actor) {
        Notification n = new Notification();
        n.setRecipient(invitation.getUser());
        n.setActor(actor);
        n.setGroup(invitation.getGroup());
        n.setType(NotificationType.INVITE_RECEIVED);
        n.setInvitationId(invitation.getId());
        n.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        notificationRepository.save(n);
    }

    /** Called after a member accepts an invitation. Notifies the group admin. */
    @Transactional
    public void createInviteAccepted(GroupMember member) {
        User admin = member.getGroup().getAdmin();
        // Don't notify if the admin accepted their own invite (edge case)
        if (admin.getId().equals(member.getUser().getId())) return;

        Notification n = new Notification();
        n.setRecipient(admin);
        n.setActor(member.getUser());
        n.setGroup(member.getGroup());
        n.setType(NotificationType.INVITE_ACCEPTED);
        n.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        notificationRepository.save(n);
    }

    /** Called after episode scores are recalculated. Notifies all non-admin members. */
    @Transactional
    public void createEpisodeScored(Group group, Episode episode) {
        if (episode == null) return;

        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatus(
                group.getId(), MembershipStatus.ACCEPTED);

        List<Notification> notifications = members.stream()
                .map(m -> {
                    Notification n = new Notification();
                    n.setRecipient(m.getUser());
                    n.setGroup(group);
                    n.setEpisode(episode);
                    n.setType(NotificationType.EPISODE_SCORED);
                    n.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    return n;
                })
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
    }

    // ── Query ─────────────────────────────────────────────────────────────────────

    public List<NotificationDTO> getForUser(Integer userId) {
        return NotificationDTO.fromList(
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId));
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────────

    @Transactional
    public void markRead(Integer userId, List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            notificationRepository.markAsRead(userId, ids);
        }
    }

    @Transactional
    public void markAllRead(Integer userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void deleteInviteNotification(Integer invitationId) {
        if (invitationId != null) {
            notificationRepository.deleteByInvitationId(invitationId);
        }
    }

    @Transactional
    public void deleteAllForGroup(Integer groupId) {
        if (groupId != null) {
            notificationRepository.deleteByGroupId(groupId);
        }
    }
}
