package io.testrest.datatype.graph;

import org.jgrapht.graph.DefaultEdge;

public class DependencyEdge extends DefaultEdge {
    private DependencyType dependencyType;

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }
}
