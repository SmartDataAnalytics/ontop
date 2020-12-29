package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public abstract class DefaultSchemaCatalogDBMetadataProvider extends AbstractDBMetadataProvider {

    protected static final int SCHEMA_INDEX = 1;
    protected static final int CATALOG_INDEX = 2;

    private final QuotedID defaultCatalog, defaultSchema;

    DefaultSchemaCatalogDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider, TypeFactory typeFactory, String sql) throws MetadataExtractionException {
        super(connection, idFactoryProvider, typeFactory);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            RelationID id = rawIdFactory.createRelationID(rs.getString("TABLE_CAT"), rs.getString("TABLE_SCHEM"), "DUMMY");
            defaultCatalog = id.getComponents().get(CATALOG_INDEX);
            defaultSchema = id.getComponents().get(SCHEMA_INDEX);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        switch (id.getComponents().size()) {
            case CATALOG_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        id.getComponents().get(SCHEMA_INDEX),
                        defaultCatalog));
            case SCHEMA_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        defaultSchema,
                        defaultCatalog));
            default:
                return id;
        }
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        if (defaultCatalog.equals(id.getComponents().get(CATALOG_INDEX))) {
            RelationID schemaTableId = new RelationIDImpl(id.getComponents().subList(TABLE_INDEX, CATALOG_INDEX));
            if (defaultSchema.equals(id.getComponents().get(SCHEMA_INDEX)))
                return ImmutableList.of(id.getTableOnlyID(), schemaTableId, id);
            return ImmutableList.of(schemaTableId, id);
        }
        return ImmutableList.of(id);
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return id.getComponents().get(CATALOG_INDEX).getName(); }

    @Override
    protected String getRelationSchema(RelationID id) { return id.getComponents().get(SCHEMA_INDEX).getName(); }

    @Override
    protected String getRelationName(RelationID id) { return id.getComponents().get(TABLE_INDEX).getName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }
}
