package io.testrest.parser;

public class UnsupportedSpecificationFeature extends RuntimeException {

    public UnsupportedSpecificationFeature(String description) {
        super(description);
    }
}