package io.testrest.core.dictionary;


import io.testrest.datatype.parameter.NormalizedParameterName;
import io.testrest.datatype.parameter.ParameterName;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.ParameterType;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * An entry for the dictionary.
 */
public class DictionaryEntry {

    private ParameterName parameterName;
    private NormalizedParameterName normalizedParameterName;
    private ParameterType type;
    private ParameterLeaf source;
    private Timestamp discoveryTime;
    private Object value;

    public DictionaryEntry(ParameterLeaf leaf, Object value) {
        if (leaf.getName() != null && leaf.getOperation() != null) {
            this.parameterName = leaf.getName();
            this.normalizedParameterName = leaf.getNormalizedName();
            this.type = leaf.getType();
            this.source = leaf;
            this.discoveryTime = Timestamp.from(Instant.now());
            this.value = value;
            leaf.setValue(value);
        } else {
            throw new RuntimeException("Can not create dictionary entry from leaf with some null values.");
        }
    }

    public DictionaryEntry(String name, Object value) {
        this.parameterName = new ParameterName(name);
        this.normalizedParameterName = new NormalizedParameterName(name);
        this.discoveryTime = Timestamp.from(Instant.now());
        this.value = value;
    }

    public ParameterName getParameterName() {
        return parameterName;
    }

    public void setParameterName(ParameterName parameterName) {
        this.parameterName = parameterName;
    }

    public NormalizedParameterName getNormalizedParameterName() {
        return normalizedParameterName;
    }

    public void setNormalizedParameterName(NormalizedParameterName normalizedParameterName) {
        this.normalizedParameterName = normalizedParameterName;
    }

    public ParameterType getParameterType() {
        return type;
    }

    public void setParameterType(ParameterType type) {
        this.type = type;
    }

    public ParameterLeaf getSource() {
        return source;
    }

    public void setSource(ParameterLeaf source) {
        this.source = source;
    }

    public Timestamp getDiscoveryTime() {
        return discoveryTime;
    }

    public void setDiscoveryTime(Timestamp discoveryTime) {
        this.discoveryTime = discoveryTime;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + normalizedParameterName + " : " + value + "]";
    }
}
