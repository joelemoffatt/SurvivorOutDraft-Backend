package com.vivida.social.group;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vivida.social.member.MembershipStatus;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    List<GroupMember> findByGroupId(Integer groupId);
    List<GroupMember> findByUserId(Integer userId);
    List<GroupMember> findByUserIdAndStatus(Integer userId, MembershipStatus status);
    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);
    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);
}
