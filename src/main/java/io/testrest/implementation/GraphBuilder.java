package io.testrest.implementation;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.Main;
import io.testrest.datatype.graph.DependencyEdge;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.datatype.normalizer.ParameterComparator;

import java.util.List;
import java.util.Map;

/**
 * Builds OperationDependencyGraph.
 */
public class GraphBuilder {

    public static void buildGraph(OperationDependencyGraph ODG) throws Exception {
        List<OperationNode> operationNodeList = Main.getOperationList().getOperationNodeList();

        // Add all vertexes to graph.
        for (OperationNode operationNode : operationNodeList) {
//            System.out.println(operationNode.getOperationId());
            ODG.addVertex(operationNode);
        }

        // Identify parameter dependencies, add edges to ODG
        for (int i = 0; i < operationNodeList.size(); i++) {
            if (operationNodeList.get(i).getParameters() != null)
                for (int j = 0; j < operationNodeList.size(); j++) {
                    if (i != j && operationNodeList.get(j).getOutputs() != null) {
                        for (Parameter pi: operationNodeList.get(i).getParameters()) {
                            for (String pj: operationNodeList.get(j).getOutputs()) {
                                if (ParameterComparator.matchedNames(operationNodeList.get(i), pi, pj)) {
                                    ODG.addEdge(operationNodeList.get(i), operationNodeList.get(j), new DependencyEdge(ParameterComparator.normalize(operationNodeList.get(i), pi)));
                                }
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
