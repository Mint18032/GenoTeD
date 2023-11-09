package io.testrest.datatype.graph;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.testrest.Configuration;
import io.testrest.Environment;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.parameter.*;

import java.util.ArrayList;
import java.util.List;

public class OperationNode extends io.swagger.v3.oas.models.Operation {
    private Boolean isReadOnly = false;
    private Boolean containsHeader = false;
    private HttpMethod method;
    private String path;
    private String operationNodeId;
    private int testingAttempts;
    private List<String> outputs;

    private List<ParameterLeaf> parameterLeafList;
    private static int idGenerationNum = 1;

    public OperationNode(HttpMethod method) {
        super();
        this.method = method;
        this.operationNodeId = this.getOperationId() != null ? this.getOperationId() : "Operation" + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        this.testingAttempts = 0;
        idGenerationNum++;

        if (getOperationId() == null) {
            setOperationId(this.operationNodeId);
        }
    }

    public OperationNode(HttpMethod method, String path) {
        super();
        this.method = method;
        this.path = path;
        this.operationNodeId = this.getOperationId() != null ? this.getOperationId() : "Operation" + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        this.testingAttempts = 0;
        idGenerationNum++;

        if (getOperationId() == null) {
            setOperationId(this.operationNodeId);
        }
    }

    public OperationNode(HttpMethod method, String path, Operation operation) {
        super();
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
        this.operationNodeId = this.getOperationId() != null ? this.getOperationId() : "Operation" + idGenerationNum;
        this.parameterLeafList = new ArrayList<>();
        idGenerationNum++;
        parseOutput();

        if (getOperationId() == null) {
            setOperationId(this.operationNodeId);
        }

        if (getParameters() != null) {
            for (Parameter p : getParameters()) {
                if (p.getIn().equals("header")) {
                    setContainsHeader(true);
                }

                // TODO: match oneof, anyof, allof types
                switch (p.getSchema().getType()) {
                    case "number":
                    case "integer":
                        NumberParameter numberParameter = new NumberParameter(p, this);
                        parameterLeafList.add(numberParameter);
                        break;
                    case "boolean":
                        parameterLeafList.add(new BooleanParameter(p, this));
                        break;
                    default: // "string"
                        StringParameter stringParameter = new StringParameter(p, this);
                        parameterLeafList.add(stringParameter);
                        break;
        //                case "array":
        //                    parameterElementList.add(new ArrayParameter(p, this));
        //                    break;
        //                default: //object
        //                    parameterElementList.add(new ObjectParameter(p, this));
                }
            }
            System.out.println(parameterLeafList);
        //        for(ParameterLeaf leaf : parameterLeafList) {
        //            System.out.println(leaf.getNormalizedName());
        //        }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.operationNodeId).append(" {");
        sb.append("    ").append(this.method);
        sb.append("    ").append(this.path);
//        sb.append("    parameters: ").append(this.getParameters()).append("\n");
//        sb.append("    requestBody: ").append(this.getRequestBody()).append("\n");
        sb.append(" }");
        return sb.toString();
    }

    /**
     * Reads all output parameters of an operation that is listed in OpenAPI.
     */
    public void parseOutput() {
        outputs = new ArrayList<>();

        ApiResponse apiResponse = this.getResponses().values().stream().findFirst().get();

        if (apiResponse.getContent() == null) return; // no output

        List<MediaType> mediaTypeStream = new ArrayList<>(apiResponse.getContent().values());

        try {
            // OAS v3
            if (Environment.getConfiguration().getSpecVersion() >= 3) {
                mediaTypeStream.forEach(mediaType -> {
                    if (mediaType.getSchema().getItems() != null) {
                        for (Object key : mediaType.getSchema().getItems().getProperties().keySet())
                            outputs.add(key.toString());
                    }
                });
            }

            // OAS v2
            if (outputs.isEmpty()) {
                mediaTypeStream.forEach(objectSchema -> {
                    if (objectSchema.getSchema().getRequired() != null)
                        for (Object param : objectSchema.getSchema().getRequired())
                            outputs.add(param.toString());
                    // TODO: parse output from example & properties
                });
            }
        } catch (NullPointerException e) {
            // ignore, no output to read
        }
    }

    /**
     * @return all output parameters of an operation that is listed in OpenAPI.
     */
    public List<String> getOutputs() {
        return this.outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public Boolean containsHeader() {
        return containsHeader;
    }

    public void setContainsHeader(Boolean containsHeader) {
        this.containsHeader = containsHeader;
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

    public int getTestedTimes() {
        return testingAttempts;
    }

    public void markAsTested() {
        testingAttempts++;
    }

    public List<ParameterLeaf> getParameterLeafList() {
        return parameterLeafList;
    }

    public void setParameterLeafList(List<ParameterLeaf> parameterLeafList) {
        this.parameterLeafList = parameterLeafList;
    }
}
