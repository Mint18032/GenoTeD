package io.testrest.datatype.graph;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OperationsSorter {
    /**
     * @param operationNodeList a list of operationNodes.
     * @return sorted list according to CRUD semantic.
     * Priorities: HEAD -> POST -> GET -> PUT & PATCH -> OPTIONS -> TRACE -> DELETE
     */
    public static List<OperationNode> semanticSort(List<OperationNode> operationNodeList) {
        if (operationNodeList.size() < 1) {
            return operationNodeList;
        }

        System.out.println("Original List: ");
        operationNodeList.forEach(node -> System.out.println(node.getOperationNodeId()));
        List<OperationNode> sortedList = operationNodeList.stream().sorted(Comparator.comparing(OperationNode::getMethod)).collect(Collectors.toList());

        System.out.println("Sorted List: ");
        sortedList.forEach(node -> System.out.println(node.getOperationNodeId()));

        return sortedList;
    }
}
