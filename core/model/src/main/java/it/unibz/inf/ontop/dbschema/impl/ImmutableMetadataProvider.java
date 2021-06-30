package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;


public class ImmutableMetadataProvider extends ImmutableMetadataLookup implements MetadataProvider {

    private final DBParameters dbParameters;
    private final ImmutableList<RelationID> relationIds;

    ImmutableMetadataProvider(DBParameters dbParameters, ImmutableMap<RelationID, NamedRelationDefinition> map) {
        super(dbParameters.getQuotedIDFactory(), map);
        this.dbParameters = dbParameters;
        this.relationIds = getRelations().stream()
                .map(NamedRelationDefinition::getID)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public DBParameters getDBParameters() { return dbParameters; }

    @Override
    public void normalizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        // Does nothing
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs()  { return relationIds; }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) {
        // NO-OP
    }
}
