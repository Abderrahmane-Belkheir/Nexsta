package com.Nexsta.Messaging.domain;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant createdDate;

    @OneToMany(mappedBy = "chat",fetch = FetchType.LAZY,cascade =CascadeType.PERSIST)
    private List<ChatMember> members;

    private String lastMessageId;
    private Instant lastMessageAt;

    private String name;
    private String photo;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    public enum ChatType {
        DIRECT,
        GROUP
    }

}

