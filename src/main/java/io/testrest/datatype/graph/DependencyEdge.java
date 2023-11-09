package io.testrest.datatype.graph;

import org.jgrapht.graph.DefaultEdge;

public class DependencyEdge extends DefaultEdge {
    private DependencyType dependencyType;

    private String parameterName;

    public DependencyEdge(String parameterName) {
        this.parameterName = parameterName;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public String toString() {
        return parameterName;
    }
}
