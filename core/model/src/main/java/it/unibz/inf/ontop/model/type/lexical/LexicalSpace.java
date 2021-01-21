package it.unibz.inf.ontop.model.type.lexical;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface LexicalSpace extends Function<String, Optional<Boolean>>, Serializable {
    Optional<Boolean> includes(String lexicalValue);

    @Override
    default Optional<Boolean> apply(String s) {
        return includes(s);
    }
}
