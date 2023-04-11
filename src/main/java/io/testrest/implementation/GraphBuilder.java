package io.testrest.implementation;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.Main;
import io.testrest.datatype.graph.DependencyEdge;
import io.testrest.datatype.graph.DependencyType;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.graph.OperationDependencyGraph;

import java.util.List;

/**
 * Builds OperationDependencyGraph.
 */
public class GraphBuilder {

    public static void buildGraph(OperationDependencyGraph ODG) throws Exception {
        List<OperationNode> operationNodeList = Main.getOperationList().getOperationNodeList();

        // Add all vertexes to graph.
        for (OperationNode operationNode : operationNodeList) {
//            System.out.println(operationNode.getOperationId());
            for (Parameter parameter : operationNode.getParameters()) {
                parameter.setName(ParameterComparator.idCompletion(operationNode, parameter));
            }
            ODG.addVertex(operationNode);
        }

        //TODO: Add edges

        // Identify parameter dependencies
        for (int i = 0; i < operationNodeList.size()-1; i++) {
            for (int j = i+1; j < operationNodeList.size(); j++) {
                for (Parameter pi: operationNodeList.get(i).getParameters()) {
                    for (Parameter pj: operationNodeList.get(j).getParameters()) {
                        if (ParameterComparator.matchedNames(pi, pj)) {
                            ODG.addEdge(operationNodeList.get(i), operationNodeList.get(j), new DependencyEdge(DependencyType.DATA));
                        }
                    }
                }
            }
        }

//        for (String path : OpenAPIParser.getPathUrls()) {
//            List<OperationNode> samePathNodes = operationNodeList.stream().filter(operationNode ->
//                    (operationNode.getPath().equals(path))).collect(Collectors.toList());
//
//        }

//        ODG.exportToFile();
        System.out.println(ODG.toMatrix());
    }
}
