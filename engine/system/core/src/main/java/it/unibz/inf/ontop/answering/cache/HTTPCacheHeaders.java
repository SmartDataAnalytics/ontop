package it.unibz.inf.ontop.answering.cache;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableMap;

public interface HTTPCacheHeaders {

    ImmutableMap<String, String> getMap();
}
