package com.vivida.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    long countByRecipientIdAndReadFalse(Integer recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id IN :ids AND n.recipient.id = :recipientId")
    void markAsRead(@Param("recipientId") Integer recipientId, @Param("ids") List<Integer> ids);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :recipientId")
    void markAllAsRead(@Param("recipientId") Integer recipientId);

    void deleteByInvitationId(Integer invitationId);

    void deleteByGroupId(Integer groupId);
}
