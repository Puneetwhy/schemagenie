package com.schemagenie.repository;

import com.schemagenie.model.GeneratedSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface GeneratedSchemaRepository extends MongoRepository<GeneratedSchema, String> {
    List<GeneratedSchema> findByUserIdOrderByCreatedAtDesc(String userId);
}
