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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @CreatedDate
    private Instant createddate;

    @OneToMany(mappedBy = "chat",fetch = FetchType.LAZY)
    private List<ChatMember> members;

    private String lastMessageId;
    private Instant lastMessageAt;

}
