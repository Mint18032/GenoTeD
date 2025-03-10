package io.testrest.datatype.graph;

import io.testrest.Main;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


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

    public OperationDependencyGraph deepClone() {
        OperationDependencyGraph newODG = new OperationDependencyGraph();

        this.getGraph().vertexSet()
                .forEach(v -> {
                    v.resetTestedTimes();
                    newODG.addVertex(v);
                });
        this.getGraph().edgeSet().forEach(e ->
                newODG.getGraph().addEdge(this.getGraph().getEdgeSource(e), this.getGraph().getEdgeTarget(e), e));

        return newODG;
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
     * Leaves are those operations with no outgoing edges, i.e. operations with no dependencies.
     * No dependency means either no input fields or input fields found in the output of no operations.
     *
     * @return a list of nodes that have no dependencies.
     */
    public List<OperationNode> getLeaves() {
        List<OperationNode> leaves = graph.vertexSet().stream().filter(v -> graph.outgoingEdgesOf(v).size() == 0).collect(Collectors.toList());

        return leaves;
    }

    /**
     * @return a list of nodes that have at least 1 mutual parameter.
     */
    public List<OperationNode> getNextDependentNodes() {
        OperationNode firstNode = this.getGraph().vertexSet().iterator().next();
        List<OperationNode> dependentNodes = new ArrayList<>();
        dependentNodes.add(firstNode);
        dependentNodes.addAll(graph.vertexSet().stream().filter(v -> graph.containsEdge(v, firstNode)).collect(Collectors.toList()));

        return dependentNodes;
    }

    public OperationNode getOperationNodeById(String operationId) {
        return graph.vertexSet().stream().filter(v -> v.getOperationId().equals(operationId)).collect(Collectors.toList()).get(0);
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
