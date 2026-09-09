package com.schemagenie.dto;

public class MongoRelationshipDto {
    private String type;       // ONE_TO_ONE | ONE_TO_MANY | MANY_TO_MANY
    private String target;
    private String strategy;   // REFERENCE | EMBED
    private String fieldName;
    private String reasoning;

    public MongoRelationshipDto() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
