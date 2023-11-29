package io.testrest.datatype.graph;

import io.testrest.Main;
import io.testrest.datatype.normalizer.ParameterComparator;
import io.testrest.datatype.parameter.ParameterElement;

import java.io.IOException;
import java.util.List;

/**
 * Builds OperationDependencyGraph.
 */
public class GraphBuilder {

    public static void buildGraph(OperationDependencyGraph ODG) {
        List<OperationNode> operationNodeList = Main.getOperationList().getOperationNodeList();

        // Add all vertexes to graph.
        for (OperationNode operationNode : operationNodeList) {
            ODG.addVertex(operationNode);
        }

        // Identify parameter dependencies, add edges to ODG
        for (int i = 0; i < operationNodeList.size(); i++) {
            if (operationNodeList.get(i).getParameters() != null)
                for (int j = 0; j < operationNodeList.size(); j++) {
                    if (i != j && operationNodeList.get(j).getOutputs() != null) {
                        for (ParameterElement pi: operationNodeList.get(i).getParameterLeafList()) {
                            for (String pj: operationNodeList.get(j).getOutputs()) {
                                if (ParameterComparator.matchedNames(pi, pj)) {
                                    ODG.addEdge(operationNodeList.get(i), operationNodeList.get(j), new DependencyEdge(pi.getNormalizedName().toString()));
                                }
                            }
                        }
                    }
                }
        }

        try {
            ODG.exportToFile();
        } catch (IOException e) {
            System.out.println("Error occurred when export ODG to file.");
        }

        System.out.println(ODG.toMatrix());
    }
}
