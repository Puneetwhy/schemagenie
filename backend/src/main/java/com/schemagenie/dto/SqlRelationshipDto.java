package com.schemagenie.dto;

public class SqlRelationshipDto {
    private String type;       // ONE_TO_ONE | ONE_TO_MANY | MANY_TO_MANY
    private String target;
    private String fieldName;
    private String joinTable;  // only for MANY_TO_MANY
    private String reasoning;

    public SqlRelationshipDto() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getJoinTable() { return joinTable; }
    public void setJoinTable(String joinTable) { this.joinTable = joinTable; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
