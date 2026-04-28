package com.Nexsta.SocialGraph.persistence;

import com.Nexsta.SocialGraph.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlocksRepo extends JpaRepository<Block, UUID> {
    void deleteByBlockerIdAndBlockedId(String blockerId,String blockedId);
    boolean existsByBlockerIdAndBlockedId(String blockerId,String blockedId);
}
