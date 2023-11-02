package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.helper.ObjectHelper;
import io.testrest.parser.EditReadOnlyOperationException;
import io.testrest.datatype.graph.OperationNode;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CombinedSchemaParameter extends ParameterElement {

    protected final List<ParameterElement> parametersSchemas;
    protected ParameterElement outputParameterSchema;
    protected final List<ParameterElement> properties;

    private static final Logger logger = Logger.getLogger(CombinedSchemaParameter.class.getName());

    public CombinedSchemaParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);

        this.parametersSchemas = new LinkedList<>();
        this.properties = new LinkedList<>();

        setupFields(parameterMap);
    }

    public CombinedSchemaParameter(Parameter parameterMap, OperationNode operation, String name) {
        super(parameterMap, operation, name);

        this.parametersSchemas = new LinkedList<>();
        this.properties = new LinkedList<>();

        setupFields(parameterMap);
    }

    protected CombinedSchemaParameter(Parameter other) {
        super(other);

        this.parametersSchemas = new LinkedList<>();
        this.properties = new LinkedList<>();
    }

    protected CombinedSchemaParameter(CombinedSchemaParameter other) {
        name = other.getName();
        schemaName = other.getSchemaName();
        required = other.isRequired();
        type = other.getType(); // Amedeo did: ParameterType.getTypeFromString(other.type.name());
        format = other.getFormat();
        setLocation(other.getLocation());
        setStyle(other.getStyle());
        setExplode(other.isExplode());
        setOperation(other.getOperation());

        setDescription(other.getDescription());

        defaultValue = ObjectHelper.deepCloneObject(other.getDefaultValue());
        enumValues = new HashSet<>(ObjectHelper.deepCloneObject(other.getEnumValues()));
        examples = new HashSet<>(ObjectHelper.deepCloneObject(other.getExamples()));


        this.parametersSchemas = new LinkedList<>();
        other.parametersSchemas.forEach(ps -> this.parametersSchemas.add(ps.deepClone()));
        if (other.outputParameterSchema != null ) {
            this.outputParameterSchema = other.outputParameterSchema.deepClone();
        }
        this.properties = new LinkedList<>();
        other.properties.forEach(p -> this.properties.add(p.deepClone()));
    }

    protected CombinedSchemaParameter(CombinedSchemaParameter other, OperationNode operation, ParameterElement parent) {
        super(operation, parent);
        name = other.getName();
        schemaName = other.getSchemaName();
        required = other.isRequired();
        type = other.getType(); // Amedeo did: ParameterType.getTypeFromString(other.type.name());
        format = other.getFormat();
        setLocation(other.getLocation());
        setStyle(other.getStyle());
        setExplode(other.isExplode());
        setOperation(other.getOperation());

        setDescription(other.getDescription());

        defaultValue = ObjectHelper.deepCloneObject(other.getDefaultValue());
        enumValues = new HashSet<>(ObjectHelper.deepCloneObject(other.getEnumValues()));
        examples = new HashSet<>(ObjectHelper.deepCloneObject(other.getExamples()));

        this.parametersSchemas = new LinkedList<>();
        other.parametersSchemas.forEach(ps -> this.parametersSchemas.add(ps.deepClone()));
        if (other.outputParameterSchema != null ) {
            this.outputParameterSchema = other.outputParameterSchema.deepClone();
        }
        this.properties = new LinkedList<>();
        other.properties.forEach(p -> this.properties.add(p.deepClone()));
    }

    protected abstract String getKeyFiledName();

    private void setupFields(Parameter parameterMap) {
        OperationNode operation = getOperation();
            try {
                this.parametersSchemas.add(ParameterFactory.getParameterElement(this, parameterMap, operation, ""));
            } catch (ParameterCreationException e) {
                logger.warning("Discarding schema in '" + getKeyFiledName() + "' field of '" + getName() + "'.");
            }


        if (this.parametersSchemas.size() == 0) {
            throw new ParameterCreationException("Combined schema with no valid schema listed will be discarded (" +
                    getName() + ").");
        }
    }

    public List<ParameterElement> getParametersSchemas() {
        if (getOperation().isReadOnly()) {
            return Collections.unmodifiableList(parametersSchemas);
        }
        return parametersSchemas;
    }

    public List<ParameterElement> getProperties() {
        if (getOperation().isReadOnly()) {
            return Collections.unmodifiableList(properties);
        }
        return properties;
    }

    public ParameterElement getOutputParameterSchema() {
        return outputParameterSchema;
    }

    public void addProperty(ParameterElement property) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }

        this.properties.add(property);
    }

    // TODO: implement. When a new property is added we should add it to the actually selected parameter
    public ParameterElement updateSelectedParameter() {
        return null;
    }

    @Override
    public String getJSONString(Object value) {
        if (outputParameterSchema != null) {
            return outputParameterSchema.getJSONString(value);
        }
        logger.warning("Requested JSON string for parameter '" + getName() + "(operation '" + getOperation() + "')," +
                " but no schema was selected. Returning an empty string.");
        return "";
    }

    public String getJsonPath() {
        // FIXME: implement when supporting combined parameters
        return "";
    }

    @Override
    public String getValueAsFormattedString(ParameterStyle style, boolean explode, Object value) {
        if (outputParameterSchema != null) {
            return outputParameterSchema.getValueAsFormattedString(style, explode, value);
        }
        logger.warning("Requested formatted string for parameter '" + getName() + "(operation '" + getOperation() + "')," +
                " but no schema was selected. Returning an empty string.");
        return "";
    }

    @Override
    public boolean isSet() {
        return false;
    }

    public Collection<ParameterLeaf> getLeaves() {
        Collection<ParameterLeaf> leaves = new LinkedList<>();

        // If the output parameter schema is already defined, add its leaves
        if (outputParameterSchema != null) {
            leaves.addAll(outputParameterSchema.getLeaves());
        }

        // Otherwise, add the leaves of each possible schema.
        else {
            for(ParameterElement element : parametersSchemas) {
                leaves.addAll(element.getLeaves());
            }
        }


        return leaves;
    }

    public Collection<ParameterLeaf> getReferenceLeaves() {
        Collection<ParameterLeaf> leaves = new LinkedList<>();

        // If the output parameter schema is already defined, add its leaves
        if (outputParameterSchema != null) {
            leaves.addAll(outputParameterSchema.getReferenceLeaves());
        }

        // Otherwise, add the leaves of each possible schema.
        else {
            for(ParameterElement element : parametersSchemas) {
                leaves.addAll(element.getReferenceLeaves());
            }
        }


        return leaves;
    }

//    @Override
//    public boolean remove() {
//        return false;
//    }

    @Override
    public CombinedSchemaParameter merge(ParameterElement other) {
        // TODO: implement
        return null;
    }

    public abstract ParameterElement merge();

    public ParameterElement merge(List<Integer> schemaIndexes) {
        // remove all indexes out of bound
        schemaIndexes = schemaIndexes.stream().filter(i -> i < this.parametersSchemas.size())
                .collect(Collectors.toList());

        ParameterElement merged = this.parametersSchemas.get(schemaIndexes.get(0));

        for (Integer index : schemaIndexes) {
            merged = merged.merge(this.parametersSchemas.get(index));
        }

        return merged;
    }

    public void merge(int numberOfSchemas) {
        if (numberOfSchemas <= 0) {
            logger.warning("Tried to merge a non-positive number of schemas. The number of schemas to be" +
                    " merged MUST be greater than zero. Setting selected parameter to null.");
            this.outputParameterSchema = this;
        } else if (numberOfSchemas >= this.parametersSchemas.size()) {
            logger.warning("Tried to merge more schemas than the declared ones. All the declared schemas will be " +
                    "merged.");
            numberOfSchemas = this.parametersSchemas.size();
        }

        List<Integer> schemaIndexes = new LinkedList<>();
        for (int i = 0; i < this.parametersSchemas.size(); ++i) {
            schemaIndexes.add(i);
        }

        List<Integer> indexes = new LinkedList<>();
        Random random = new Random();

        while (numberOfSchemas-- > 0) {
            int index = random.nextInt(schemaIndexes.size());
            indexes.add(schemaIndexes.get(index));
            schemaIndexes.remove(index);
        }

        merge(indexes);
    }

    /**
     *
     * @return an empty list
     */
    @Override
    public Collection<ArrayParameter> getArrays() {
        Collection<ArrayParameter> arrays = new LinkedList<>();

        // If the output parameter schema is already defined, add its arrays
        if (outputParameterSchema != null) {
            arrays.addAll(outputParameterSchema.getArrays());
        }

        // Otherwise, add the arrays of each possible schema.
        else {
            for(ParameterElement element : parametersSchemas) {
                arrays.addAll(element.getArrays());
            }
        }

        return arrays;
    }

    // FIXME: currently not supported
    @Override
    public Collection<ObjectParameter> getObjects() {
        return new LinkedList<>();
    }

    // FIXME: currently not supported
    @Override
    public Collection<ObjectParameter> getReferenceObjects() {
        return new LinkedList<>();
    }

    @Override
    public Collection<ParameterElement> getAllParameters() {
        return new HashSet<>();
    }

    @Override
    public Collection<CombinedSchemaParameter> getCombinedSchemas() {
        Collection<CombinedSchemaParameter> combinedSchemas = new LinkedList<>();

        // If the output parameter schema is already defined, add its arrays
        if (outputParameterSchema != null) {
            combinedSchemas.addAll(outputParameterSchema.getCombinedSchemas());
        }

        // Otherwise, add the arrays of each possible schema.
        else {
            for(ParameterElement element : parametersSchemas) {
                combinedSchemas.addAll(element.getCombinedSchemas());
            }
        }

        return combinedSchemas;
    }

    @Override
    public ParameterElement getParameterFromJsonPath(String jsonPath) {
        return null;
    }
}

