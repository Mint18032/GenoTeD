package io.testrest.datatype.graph;

import io.swagger.v3.oas.models.Operation;
import io.testrest.datatype.Method;

public class OperationNode extends io.swagger.v3.oas.models.Operation {
    private Method method;
    private String path;
    private String operationNodeId;
    private static int idGenerationNum = 0;

    public OperationNode(Method method) {
        super();
        this.method = method;
        this.operationNodeId = this.getOperationId() + idGenerationNum;
        idGenerationNum++;
    }

    public OperationNode(Method method, String path) {
        super();
        this.method = method;
        this.path = path;
        this.operationNodeId = this.getOperationId() + idGenerationNum;
        idGenerationNum++;
    }

    public OperationNode(Method method, String path, Operation operation) {
        super();
        this.method = method;
        this.path = path;
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
        idGenerationNum++;
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

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


}
