package io.testrest.implementation;

import io.testrest.Main;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.parser.OpenAPIParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds OperationDependencyGraph.
 */
public class GraphBuilder {
    public static void buildGraph(OperationDependencyGraph ODG) throws Exception {
        List<OperationNode> operationNodeList = Main.getOperationList().getOperationNodeList();

        // Adds all vertexes to graph.
        for(OperationNode operationNode : operationNodeList) {
//            System.out.println(operationNode.getOperationId());
            ODG.addVertex(operationNode);
        }

        for(String path : OpenAPIParser.getPathUrls()) {
            List<OperationNode> samePathNodes = operationNodeList.stream().filter(operationNode ->
                    (operationNode.getPath().equals(path))).collect(Collectors.toList());

        }
        // TODO: match field names then add egdes

//        ODG.exportToFile();
        System.out.println(ODG.toMatrix());
    }
}
