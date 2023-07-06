package io.testrest.datatype.graph;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.Configuration;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.parameter.*;

import java.util.ArrayList;
import java.util.List;

public class OperationNode extends io.swagger.v3.oas.models.Operation {
    private Boolean isReadOnly = false;
    private HttpMethod method;
    private String path;
    private Boolean tested;
    private String operationNodeId;
    private int testingAttempts;

    private List<ParameterLeaf> parameterLeafList;
    private static int idGenerationNum = 0;

    public OperationNode(HttpMethod method) {
        super();
        this.tested = false;
        this.method = method;
        this.operationNodeId = this.getOperationId() + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        this.testingAttempts = 0;
        idGenerationNum++;
    }

    public OperationNode(HttpMethod method, String path) {
        super();
        this.tested = false;
        this.method = method;
        this.path = path;
        this.operationNodeId = this.getOperationId() + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        this.testingAttempts = 0;
        idGenerationNum++;
    }

    public OperationNode(HttpMethod method, String path, Operation operation) {
        super();
        this.tested = false;
        this.method = method;
        this.path = path;
        this.testingAttempts = 0;
        this.setTags(operation.getTags());
        this.setSummary(operation.getSummary());
        this.setDescription(operation.getDescription());
        this.setExternalDocs(operation.getExternalDocs());
        this.setOperationId(operation.getOperationId());
        this.setParameters(operation.getParameters());
        this.setRequestBody(operation.getRequestBody());
        this.setResponses(operation.getResponses());
        this.setCallbacks(operation.getCallbacks());
        this.setDeprecated(operation.getDeprecated());
        this.setSecurity(operation.getSecurity());
        this.setServers(operation.getServers());
        this.setExtensions(operation.getExtensions());
        this.operationNodeId = this.getOperationId() + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        idGenerationNum++;

        for (Parameter p : getParameters()) {
            // TODO: match oneof, anyof, allof types
            switch (p.getSchema().getType()) {
                case "number":
                case "integer":
                    parameterLeafList.add(new NumberParameter(p, this));
                    break;
                case "boolean":
                    parameterLeafList.add(new BooleanParameter(p, this));
                    break;
                default: // "string"
                    parameterLeafList.add(new StringParameter(p, this));
                    break;
//                case "array":
//                    parameterElementList.add(new ParameterArray(p, this));
//                    break;
//                default: //object
//                    parameterElementList.add(new ParameterObject(p, this));
            }
        }
        System.out.println(parameterLeafList);
//        for(ParameterLeaf leaf : parameterLeafList) {
//            System.out.println(leaf.getNormalizedName());
//        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OperationNode {\n");
        sb.append("    operationNodeId: ").append(this.operationNodeId).append("\n");
        sb.append("    path: ").append(this.path).append("\n");
        sb.append("    method: ").append(this.method).append("\n");
        sb.append("    tags: ").append(this.getTags()).append("\n");
        sb.append("    operationId: ").append(this.getOperationId()).append("\n");
        sb.append("    parameters: ").append(this.getParameters()).append("\n");
        sb.append("    requestBody: ").append(this.getRequestBody()).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public String getOperationNodeId() {
        return operationNodeId;
    }

    public void setOperationNodeId(String operationNodeId) {
        this.operationNodeId = operationNodeId;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEndpoint() {
        return Configuration.getOpenApiSpecPath();
    }

    public Boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public Operation deepClone() {
        return new OperationNode(this.method, this.path, this);
    }

    public Boolean isTested() {
        return tested;
    }

    public void markAsTested() {
        tested = true;
    }

    public List<ParameterLeaf> getParameterLeafList() {
        return parameterLeafList;
    }

    public void setParameterLeafList(List<ParameterLeaf> parameterLeafList) {
        this.parameterLeafList = parameterLeafList;
    }

    public int getTestingAttempts() {
        return testingAttempts;
    }

    public void setTestingAttempts(int testingAttempts) {
        this.testingAttempts = testingAttempts;
    }
}
