package io.testrest.datatype.graph;

import io.testrest.Main;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.*;
import java.util.*;


/**
 * A directed graph G = (N, V):
 * N is the node represents an operation, V is the edge with
 * v = n2 → n1, when there exists a data dependency between
 * n2 (request, input) and n1 (response, output).
 * Thus, n1 should be tested before n2.
 */
public class OperationDependencyGraph {
    private final Graph<OperationNode, DependencyEdge> graph = new DirectedMultigraph<>(DependencyEdge.class);
    public OperationDependencyGraph() {
    }

    public Graph<OperationNode, DependencyEdge> getGraph() {
        return graph;
    }

    public void addEdge(OperationNode source, OperationNode target, DependencyEdge dependencyEdge) {
        graph.addEdge(source, target, dependencyEdge);
    }

    public void addVertex(OperationNode operationNode) {
        graph.addVertex(operationNode);
    }

    /**
     * Exports ODG to a file.
     */
    public void exportToFile() throws IOException {
        DOTExporter<OperationNode, DependencyEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(e.toString()));
            return map;
        });

        File file = new File(Main.getConfiguration().getOutputPath() + "/");
        file.mkdirs();

        Writer writer = new FileWriter(Main.getConfiguration().getOutputPath() + "/" +
                Main.getConfiguration().getOdgFileName());
        exporter.exportGraph(this.graph, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Visualizes the graph (matrix form).
     * @return a matrix visualizing the ODG.
     */
    public String toMatrix() {
        StringBuilder matrix = new StringBuilder();
        matrix.append("Operation Dependency Graph Matrix Representation:\n\n");
        Set<OperationNode> vertexSet = graph.vertexSet();
        List<OperationNode> vertexList = new ArrayList<>(vertexSet);
        int gapLength = 0;

        for (OperationNode operationNode : vertexList) {
            if (operationNode.getOperationNodeId().concat("\t\t").length() > gapLength) {
                gapLength = operationNode.getOperationNodeId().concat("\t\t").length();
            }
        }

        matrix.append(" ".repeat(gapLength));

        for (OperationNode operationNode : vertexList)
            matrix.append(operationNode.getOperationNodeId()).append(" ".repeat(gapLength - operationNode.getOperationNodeId().length()));

        for (int i = 0; i < vertexList.size(); ++i) {
            matrix.append("\n").append(vertexList.get(i).getOperationNodeId());
            int len = gapLength;
            while (len > vertexList.get(i).getOperationNodeId().length()) {
                len--;
                matrix.append(" ");
            }
            for (int j = 0; j < vertexList.size(); ++j) {
                if (i != j && graph.containsEdge(vertexList.get(i), vertexList.get(j))) {
                    String param = graph.getEdge(vertexList.get(i), vertexList.get(j)).getParameterName();
                    matrix.append(param);
                    matrix.append(" ".repeat(gapLength > param.length() ? gapLength - param.length() : 0));
                } else {
                    matrix.append(" ".repeat(gapLength));
                }
            }
        }

        return matrix.toString();
    }
}
