package io.testrest.core.Mutator;

import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.core.mutator.UnmatchedRegexMutator;
import io.testrest.core.testing.TestInteraction;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.parameter.*;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUnmatchedRegexMutator {
    UnmatchedRegexMutator unmatchedRegexMutator = new UnmatchedRegexMutator();

    @RepeatedTest(10)
    public void test1() {
        String pattern = "^[a-zA-Z0-9][a-zA-Z0-9_.-]*$"; // begins with a letter or number, followed by letters, numbers, periods, underscores, or dashes
        OperationNode operationNode = new OperationNode(HttpMethod.GET);
        StringParameter stringParameter = new StringParameter() {
            @Override
            public ParameterName getName() {
                return new ParameterName("accountName");
            }
            @Override
            public OperationNode getOperation() {
                return operationNode;
            }
        };
        stringParameter.setPattern(pattern);

        List<DictionaryEntry> dictionaryEntryList = new ArrayList<>();
        DictionaryEntry entry_to_test = new DictionaryEntry(stringParameter, "es");
        dictionaryEntryList.add(entry_to_test);
        dictionaryEntryList.add(new DictionaryEntry(new StringParameter() {
            @Override
            public ParameterName getName() {
                return new ParameterName("gmail");
            }
            @Override
            public OperationNode getOperation() {
                return operationNode;
            }
        }, "test@gmail.com"));

        TestInteraction testInteraction = new TestInteraction(operationNode, dictionaryEntryList);

        unmatchedRegexMutator.mutate(entry_to_test, testInteraction);

        System.out.println(testInteraction);
        Matcher matcher = Pattern.compile(pattern).matcher(testInteraction.getRequestInputs().get(testInteraction.getRequestInputs().size()-1).getValue().toString());
        assertFalse(matcher.find());
    }

    @RepeatedTest(10)
    public void test2() {
        String pattern = "^[0-9]+$";
        OperationNode operationNode = new OperationNode(HttpMethod.GET);
        StringParameter stringParameter = new StringParameter() {
            @Override
            public ParameterName getName() {
                return new ParameterName("accountName");
            }
            @Override
            public OperationNode getOperation() {
                return operationNode;
            }
        };
        stringParameter.setPattern(pattern);

        List<DictionaryEntry> dictionaryEntryList = new ArrayList<>();
        DictionaryEntry entry_to_test = new DictionaryEntry(stringParameter, "es");
        dictionaryEntryList.add(entry_to_test);
        dictionaryEntryList.add(new DictionaryEntry(new StringParameter() {
            @Override
            public ParameterName getName() {
                return new ParameterName("gmail");
            }
            @Override
            public OperationNode getOperation() {
                return operationNode;
            }
        }, "test@gmail.com"));

        TestInteraction testInteraction = new TestInteraction(operationNode, dictionaryEntryList);

        unmatchedRegexMutator.mutate(entry_to_test, testInteraction);

        System.out.println(testInteraction);
        Matcher matcher = Pattern.compile(pattern).matcher(testInteraction.getRequestInputs().get(testInteraction.getRequestInputs().size()-1).getValue().toString());
        assertFalse(matcher.find());
    }
}
