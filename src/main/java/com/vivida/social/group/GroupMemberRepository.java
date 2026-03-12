package com.vivida.social.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    List<GroupMember> findByGroupId(Integer groupId);

    List<GroupMember> findByUserId(Integer userId);

    List<GroupMember> findByUserIdAndStatus(Integer userId, MembershipStatus status);

    @Query("""
            select gm
            from GroupMember gm
            where gm.user.id = :userId
                and gm.status = :status
            order by
                case when gm.lastAccessedAt is null then 1 else 0 end,
                gm.lastAccessedAt desc,
                gm.joinedAt desc
            """)
    List<GroupMember> findByUserIdAndStatusOrderByRecentAccess(@Param("userId") Integer userId,
            @Param("status") MembershipStatus status);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

    Optional<GroupMember> findByGroupIdAndUserIdAndStatus(Integer groupId, Integer userId, MembershipStatus status);

    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);
}
