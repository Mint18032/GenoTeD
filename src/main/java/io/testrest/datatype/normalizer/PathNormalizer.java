package io.testrest.datatype.normalizer;

public class PathNormalizer extends Normalizer {
    /**
     * To normalize paths that contain param so that it is easier to generate path queries E.g, finding position of path param.
     */
    public static String normalize(String path) {
        return path;
    }
}
