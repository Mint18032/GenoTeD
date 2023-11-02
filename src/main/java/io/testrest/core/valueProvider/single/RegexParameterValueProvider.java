package io.testrest.core.valueProvider.single;

import com.github.curiousoddman.rgxgen.RgxGen;
import io.testrest.core.valueProvider.ParameterValueProvider;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.StringParameter;

public class RegexParameterValueProvider extends ParameterValueProvider {

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        if (parameterLeaf instanceof StringParameter) {
            String pattern = ((StringParameter) parameterLeaf).getPattern();
            System.out.println(pattern);

            if (pattern == null) {
                return null;
            }

            RgxGen rgxGen = RgxGen.parse(pattern);
            return rgxGen.generate();
        }

        return null;
    }
}
