package io.testrest.datatype;

import io.testrest.datatype.graph.OperationNode;

import java.util.*;

/**
 * A list of OperationNodes (before matching fields name).
 */
public class OperationNodeList {
    private List<OperationNode> operationNodeList;

    public OperationNodeList() {
        operationNodeList = new ArrayList<>();
    }

    public void addOperation(OperationNode operationNode) {
        operationNodeList.add(operationNode);
    }

    public void setOperationNodeList(List<OperationNode> operationNodeList) {
        this.operationNodeList = operationNodeList;
    }

    public List<OperationNode> getOperationNodeList() {
        return operationNodeList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OperationNodeList {\n");
        for(OperationNode operationNode : operationNodeList) {
            sb.append(operationNode.toString());
        }
        return sb.toString();
    }
}
