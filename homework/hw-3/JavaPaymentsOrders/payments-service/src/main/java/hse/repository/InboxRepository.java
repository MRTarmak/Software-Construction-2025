package hse.repository;

import hse.model.InboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEntity, UUID> {
    Optional<InboxEntity> findByMessageId(String messageId);
}