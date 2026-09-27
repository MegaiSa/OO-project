package fr.efrei.messaging.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "conversation_participant",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    protected Conversation() {
        // required by JPA
    }

    public Conversation(String name, Set<User> participants) {
        this.name = name;
        this.participants = new LinkedHashSet<>(participants);
        this.createdAt = Instant.now();
    }

    public boolean hasParticipant(Long userId) {
        return participants.stream().anyMatch(u -> u.getId().equals(userId));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
