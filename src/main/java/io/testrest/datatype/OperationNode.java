package io.testrest.datatype;

import io.swagger.v3.oas.models.Operation;

public class OperationNode extends io.swagger.v3.oas.models.Operation {
    public enum METHOD {GET, PUT, POST, DELETE, OPTIONS, HEAD, PATCH, TRACE}
    public METHOD method;
    public String path;

    public OperationNode(METHOD method) {
        super();
        this.method = method;
    }

    public OperationNode(METHOD method, String path) {
        super();
        this.method = method;
        this.path = path;
    }

    public OperationNode(METHOD method, String path, Operation operation) {
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
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OperationNode {\n");
        sb.append("    path: ").append(this.path).append("\n");
        sb.append("    method: ").append(this.method).append("\n");
        sb.append("    tags: ").append(this.getTags()).append("\n");
        sb.append("    operationId: ").append(this.getOperationId()).append("\n");
        sb.append("    parameters: ").append(this.getParameters()).append("\n");
        sb.append("    requestBody: ").append(this.getRequestBody()).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
