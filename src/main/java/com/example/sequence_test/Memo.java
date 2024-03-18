package com.example.sequence_test;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "memos", indexes = {
        @Index(name = "idx_sequence", columnList = "sequence")
})
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class Memo {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id")
    private UUID id;

    private Integer sequence;

    private String title;

    @Basic(fetch=FetchType.LAZY)
    private String content;

    public void updateSequence(int newSequence) {
        this.sequence = newSequence;
    }
}
