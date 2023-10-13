package io.testrest.testing;

import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.HttpStatusCode;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.helper.Taggable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single HTTP test interaction (including a request and a response).
 */
public class TestInteraction extends Taggable {

    // Request fields
    private transient OperationNode operation;
    private HttpMethod requestMethod;
    private String requestURL;
    private String requestHeaders;
    private String requestBody;
    private Timestamp requestSentAt;
    private List<DictionaryEntry> requestInputs;

    // Response fields
    private String responseProtocol;
    private HttpStatusCode responseStatusCode;
    private String responseHeaders;
    private String responseBody;
    private Timestamp responseReceivedAt;

    // Other fields
    private String mutateInfo;
    private Timestamp executionTime;
    private transient TestStatus testStatus = TestStatus.CREATED;


    public TestInteraction(OperationNode operation) {
        this.operation = operation;
        this.requestURL = operation.getPath();
        this.requestMethod = operation.getMethod();
        this.requestInputs = new ArrayList<>();
        this.mutateInfo = "none";
    }

    public TestInteraction(OperationNode operation, List<DictionaryEntry> dictionaryEntryList) {
        this.operation = operation;
        this.requestURL = operation.getPath();
        this.requestMethod = operation.getMethod();
        this.requestInputs = new ArrayList<>();
        this.mutateInfo = "none";

        dictionaryEntryList.forEach(entry -> {
            this.requestInputs.add(new DictionaryEntry(entry.getSource(), entry.getValue()));
        });

    }

    public TestInteraction(OperationNode operation, HttpMethod requestMethod, String requestURL, String requestHeaders, String requestBody, List<DictionaryEntry> requestInputs) {
        this.operation = operation;
        this.requestMethod = requestMethod;
        this.requestURL = requestURL;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.requestInputs = requestInputs;
    }

    public OperationNode getOperation() {
        return operation;
    }

    public void setOperation(OperationNode operation) {
        this.operation = operation;
    }

    public void setResponseStatusCode(HttpStatusCode statusCode) {
        this.responseStatusCode = statusCode;
    }

    public HttpStatusCode getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Timestamp getRequestSentAt() {
        return requestSentAt;
    }

    public void setRequestSentAt(Timestamp requestSentAt) {
        this.requestSentAt = requestSentAt;
    }

    public String getResponseProtocol() {
        return responseProtocol;
    }

    public void setResponseProtocol(String responseProtocol) {
        this.responseProtocol = responseProtocol;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Timestamp getResponseReceivedAt() {
        return responseReceivedAt;
    }

    public void setResponseReceivedAt(Timestamp responseReceivedAt) {
        this.responseReceivedAt = responseReceivedAt;
    }

    public Timestamp getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Timestamp executionTime) {
        this.executionTime = executionTime;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public void setRequestInputs(List<DictionaryEntry> requestInputs) {
        this.requestInputs = requestInputs;
    }

    public List<DictionaryEntry> getRequestInputs() {
        return requestInputs;
    }

    public void addRequestInput(DictionaryEntry dictionaryEntry) {
        requestInputs.add(dictionaryEntry);
    }

    public void setRequestInfo(HttpMethod httpMethod, String requestURL, String requestHeaders, String requestBody) {
        this.requestMethod = httpMethod;
        this.requestURL = requestURL;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public void setResponseInfo(String responseProtocol, HttpStatusCode responseStatusCode, String responseHeaders,
                                String responseBody, Timestamp requestSentAt, Timestamp responseReceivedAt) {
        this.responseProtocol = responseProtocol;
        this.responseStatusCode = responseStatusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.requestSentAt = requestSentAt;
        this.responseReceivedAt = responseReceivedAt;
        this.testStatus = TestStatus.EXECUTED;
    }

    public String getMutateInfo() {
        return mutateInfo;
    }

    public void setMutateInfo(String mutateInfo) {
        this.mutateInfo = mutateInfo;
    }

    public TestInteraction reset() {

        // Reset request info
        requestMethod = null;
        requestURL = null;
        requestHeaders = null;
        requestBody = null;
        requestSentAt = null;
        requestInputs.clear();

        // Reset response info
        responseProtocol = null;
        responseStatusCode = null;
        responseHeaders = null;
        responseBody = null;
        responseReceivedAt = null;

        // Reset test status
        testStatus = TestStatus.CREATED;

        return this;
    }

    public TestInteraction deepClone() {
        List<DictionaryEntry> newInputList = new ArrayList<>();
        requestInputs.forEach(input -> newInputList.add(new DictionaryEntry(input.getSource(), input.getValue())));

        return new TestInteraction((OperationNode) operation.deepClone(), newInputList);
    }

    public void removeInput(DictionaryEntry entry) {
        requestInputs.removeIf(input -> input.getSource().equals(entry.getSource()));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Object TestInteraction\nOperationId: ").append(operation.getOperationId());
        stringBuilder.append("\nHTTP Method: ").append(requestMethod.toString());
        stringBuilder.append("\nRequest URL: ").append(requestURL);
        stringBuilder.append("\nInputs: ").append(requestInputs.toString());

        if (testStatus.equals(TestStatus.EXECUTED)) {
            stringBuilder.append("\nResponse Protocol: ").append(responseProtocol);
            stringBuilder.append("\nResponse Status Code: ").append(responseStatusCode);
            stringBuilder.append("\nResponse Body: ").append(responseBody);
            stringBuilder.append("\nResponse Received At: ").append(responseReceivedAt);
        }

        return stringBuilder.toString();
    }

}
