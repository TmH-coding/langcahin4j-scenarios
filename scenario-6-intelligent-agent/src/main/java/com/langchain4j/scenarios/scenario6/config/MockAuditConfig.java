package com.langchain4j.scenarios.scenario6.config;

import com.langchain4j.scenarios.common.audit.AuditLog;
import com.langchain4j.scenarios.common.audit.AuditLogRepository;
import com.langchain4j.scenarios.common.entity.ConversationRecord;
import com.langchain4j.scenarios.common.repository.ConversationRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class MockAuditConfig {

    @Bean
    public AuditLogRepository auditLogRepository() {
        return new AuditLogRepository() {
            @Override public <S extends AuditLog> S save(S entity) { return entity; }
            @Override public <S extends AuditLog> List<S> saveAll(Iterable<S> entities) { return List.of(); }
            @Override public Optional<AuditLog> findById(Long id) { return Optional.empty(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public List<AuditLog> findAll() { return List.of(); }
            @Override public List<AuditLog> findAllById(Iterable<Long> ids) { return List.of(); }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(AuditLog entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends AuditLog> entities) {}
            @Override public void deleteAll() {}
            @Override public List<AuditLog> findAll(Sort sort) { return List.of(); }
            @Override public Page<AuditLog> findAll(Pageable pageable) { return new PageImpl<>(List.of()); }
            @Override public <S extends AuditLog> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends AuditLog> List<S> findAll(Example<S> example) { return List.of(); }
            @Override public <S extends AuditLog> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
            @Override public <S extends AuditLog> Page<S> findAll(Example<S> example, Pageable pageable) { return new PageImpl<>(List.of()); }
            @Override public <S extends AuditLog> long count(Example<S> example) { return 0; }
            @Override public <S extends AuditLog> boolean exists(Example<S> example) { return false; }
            @Override public <S extends AuditLog, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public void flush() {}
            @Override public <S extends AuditLog> S saveAndFlush(S entity) { return entity; }
            @Override public <S extends AuditLog> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
            @Override public void deleteInBatch(Iterable<AuditLog> entities) {}
            @Override public void deleteAllInBatch() {}
            @Override public void deleteAllInBatch(Iterable<AuditLog> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public AuditLog getById(Long id) { return null; }
            @Override public AuditLog getReferenceById(Long id) { return null; }
            @Override public AuditLog getOne(Long id) { return null; }
            @Override public List<AuditLog> findByOperator(String operator) { return List.of(); }
            @Override public List<AuditLog> findByOperationType(String operationType) { return List.of(); }
            @Override public List<AuditLog> findByOperationTimeBetween(LocalDateTime startTime, LocalDateTime endTime) { return List.of(); }
        };
    }

    @Bean
    public ConversationRecordRepository conversationRecordRepository() {
        return new ConversationRecordRepository() {
            @Override public <S extends ConversationRecord> S save(S entity) { return entity; }
            @Override public <S extends ConversationRecord> List<S> saveAll(Iterable<S> entities) { return List.of(); }
            @Override public Optional<ConversationRecord> findById(Long id) { return Optional.empty(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public List<ConversationRecord> findAll() { return List.of(); }
            @Override public List<ConversationRecord> findAllById(Iterable<Long> ids) { return List.of(); }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(ConversationRecord entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends ConversationRecord> entities) {}
            @Override public void deleteAll() {}
            @Override public List<ConversationRecord> findAll(Sort sort) { return List.of(); }
            @Override public Page<ConversationRecord> findAll(Pageable pageable) { return new PageImpl<>(List.of()); }
            @Override public <S extends ConversationRecord> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
            @Override public <S extends ConversationRecord> List<S> findAll(Example<S> example) { return List.of(); }
            @Override public <S extends ConversationRecord> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
            @Override public <S extends ConversationRecord> Page<S> findAll(Example<S> example, Pageable pageable) { return new PageImpl<>(List.of()); }
            @Override public <S extends ConversationRecord> long count(Example<S> example) { return 0; }
            @Override public <S extends ConversationRecord> boolean exists(Example<S> example) { return false; }
            @Override public <S extends ConversationRecord, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public void flush() {}
            @Override public <S extends ConversationRecord> S saveAndFlush(S entity) { return entity; }
            @Override public <S extends ConversationRecord> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
            @Override public void deleteInBatch(Iterable<ConversationRecord> entities) {}
            @Override public void deleteAllInBatch() {}
            @Override public void deleteAllInBatch(Iterable<ConversationRecord> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public ConversationRecord getById(Long id) { return null; }
            @Override public ConversationRecord getReferenceById(Long id) { return null; }
            @Override public ConversationRecord getOne(Long id) { return null; }
            @Override public List<ConversationRecord> findBySessionIdAndDeletedFalse(String sessionId) { return List.of(); }
            @Override public Page<ConversationRecord> findByUserIdAndDeletedFalse(String userId, Pageable pageable) { return new PageImpl<>(List.of()); }
            @Override public List<ConversationRecord> findByScenarioTypeAndDeletedFalse(String scenarioType) { return List.of(); }
            @Override public List<ConversationRecord> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) { return List.of(); }
            @Override public long countByUserIdAndDeletedFalse(String userId) { return 0; }
            @Override public long countByScenarioTypeAndDeletedFalse(String scenarioType) { return 0; }
        };
    }
}
