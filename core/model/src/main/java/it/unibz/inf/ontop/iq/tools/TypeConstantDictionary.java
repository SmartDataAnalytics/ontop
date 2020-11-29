package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableBiMap;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

import java.util.Collection;

public interface TypeConstantDictionary {
    DBConstant convert(RDFTermTypeConstant termTypeConstant);

    RDFTermTypeConstant convert(DBConstant constant);

    ImmutableBiMap<DBConstant, RDFTermTypeConstant> createConversionMap(Collection<RDFTermTypeConstant> termTypeConstants);
}
