package it.unibz.inf.ontop.model.type.impl;

import java.io.Serializable;
import java.util.function.Function;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

@FunctionalInterface
public interface SerializableDBTypeFactory extends Function<DBTypeFactory, DBTermType>, Serializable { }