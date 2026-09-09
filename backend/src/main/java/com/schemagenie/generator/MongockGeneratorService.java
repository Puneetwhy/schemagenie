package com.schemagenie.generator;

import com.schemagenie.dto.CollectionDto;
import com.schemagenie.dto.MongoSchemaDto;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MongockGeneratorService {

    public String generate(MongoSchemaDto schema) {
        List<CollectionDto> ordered = orderByDependency(schema.getCollections());
        StringBuilder out = new StringBuilder();

        int index = 1;
        for (CollectionDto collection : ordered) {
            out.append(generateChangeUnit(collection, index++)).append("\n\n");
        }
        return out.toString();
    }

    /** Referenced-by collections are created after the collections they depend on. */
    private List<CollectionDto> orderByDependency(List<CollectionDto> collections) {
        Map<String, CollectionDto> byName = new LinkedHashMap<>();
        for (CollectionDto c : collections) byName.put(c.getName(), c);

        List<CollectionDto> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (CollectionDto c : collections) {
            visit(c, byName, visited, ordered, new HashSet<>());
        }
        return ordered;
    }

    private void visit(CollectionDto c, Map<String, CollectionDto> byName, Set<String> visited,
                        List<CollectionDto> ordered, Set<String> visiting) {
        if (visited.contains(c.getName()) || visiting.contains(c.getName())) return;
        visiting.add(c.getName());

        if (c.getRelationships() != null) {
            for (var rel : c.getRelationships()) {
                if ("REFERENCE".equalsIgnoreCase(rel.getStrategy())) {
                    CollectionDto target = byName.get(rel.getTarget());
                    if (target != null && !target.getName().equals(c.getName())) {
                        visit(target, byName, visited, ordered, visiting);
                    }
                }
            }
        }

        visiting.remove(c.getName());
        visited.add(c.getName());
        ordered.add(c);
    }

    private String generateChangeUnit(CollectionDto collection, int order) {
        String className = collection.getName() + "ChangeUnit";
        String collectionName = Character.toLowerCase(collection.getName().charAt(0))
                + collection.getName().substring(1) + "s";
        String indexField = (collection.getFields() != null && !collection.getFields().isEmpty())
                ? collection.getFields().get(0).getName() : "id";

        return """
                package com.schemagenie.generated.migration;

                import io.mongock.api.annotations.ChangeUnit;
                import io.mongock.api.annotations.Execution;
                import io.mongock.api.annotations.RollbackExecution;
                import org.springframework.data.mongodb.core.MongoTemplate;
                import org.springframework.data.mongodb.core.index.Index;

                @ChangeUnit(id = "create-%s", order = "%03d", author = "schemagenie")
                public class %s {

                    @Execution
                    public void execution(MongoTemplate mongoTemplate) {
                        mongoTemplate.createCollection("%s");
                        mongoTemplate.indexOps("%s").ensureIndex(new Index().on("%s", org.springframework.data.domain.Sort.Direction.ASC));
                    }

                    @RollbackExecution
                    public void rollback(MongoTemplate mongoTemplate) {
                        mongoTemplate.dropCollection("%s");
                    }
                }
                """.formatted(collectionName, order, className, collectionName, collectionName, indexField, collectionName);
    }
}
