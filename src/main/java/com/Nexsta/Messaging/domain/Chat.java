package com.Nexsta.Messaging.domain;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
@NoArgsConstructor
public class Chat {

    @Id
    private String id;

    @CreatedDate
    private Instant createdDate;

    @OneToMany(mappedBy = "chat",fetch = FetchType.LAZY,cascade =CascadeType.ALL,orphanRemoval = true)
    private List<ChatMember> members=new ArrayList<>();

    private String lastMessageId;
    private Instant lastMessageAt;

    private String name;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    public Chat(String id){
        this.id=id;
    }

    public enum ChatType {
        DIRECT,
        GROUP
    }

}

