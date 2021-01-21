package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.io.Serializable;
import java.util.function.Function;


public interface SerializableDBFunctionSymbolFactory extends Function<DBTermType, DBFunctionSymbol>, Serializable {}
