package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;


import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static it.unibz.inf.ontop.model.type.impl.AbstractNumericRDFDatatype.createAbstractNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createTopConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleAbstractRDFDatatype;
import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleConcreteRDFDatatype;

@Singleton
public class TypeFactoryImpl implements TypeFactory {

	// Only builds these TermTypes once.
	private final Map<IRI, RDFDatatype> datatypeCache = new ConcurrentHashMap<>();
	private final Map<String, RDFDatatype> langTypeCache = new ConcurrentHashMap<>();

	private final TermType rootTermType;
	private final MetaRDFTermType metaRDFTermType;
	private final RDFTermType rootRDFTermType;
	private final ObjectRDFType objectRDFType, iriTermType, blankNodeTermType;
	private final RDFDatatype rdfsLiteralDatatype, dateOrDatetimeDatatype;
	private final NumericRDFDatatype numericDatatype, owlRealDatatype;
	private final ConcreteNumericRDFDatatype owlRationalDatatype, xsdDecimalDatatype;
	private final ConcreteNumericRDFDatatype xsdDoubleDatatype, xsdFloatDatatype;
	private final ConcreteNumericRDFDatatype xsdIntegerDatatype, xsdLongDatatype, xsdIntDatatype, xsdShortDatatype, xsdByteDatatype;
	private final ConcreteNumericRDFDatatype xsdNonPositiveIntegerDatatype, xsdNegativeIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdNonNegativeIntegerDatatype, xsdPositiveIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdUnsignedLongDatatype, xsdUnsignedIntDatatype, xsdUnsignedShortDatatype, xsdUnsignedByteDatatype;
	private final RDFDatatype defaultUnsupportedDatatype, xsdStringDatatype, xsdBooleanDatatype, xsdBase64Datatype;
	private final RDFDatatype xsdTimeDatatype, xsdDateDatatype, xsdDatetimeDatatype, xsdDatetimeStampDatatype, xsdGYearDatatype;
	private final RDF rdfFactory;
	private final DBTypeFactory dbTypeFactory;

	@Inject
	private TypeFactoryImpl(DBTypeFactory.Factory dbTypeFactoryFactory, RDF rdfFactory) {
		this.rdfFactory = rdfFactory;

		rootTermType = TermTypeImpl.createOriginTermType();

		rootRDFTermType = RDFTermTypeImpl.createRDFTermRoot(rootTermType.getAncestry());
		metaRDFTermType = MetaRDFTermTypeImpl.createMetaRDFTermType(rootRDFTermType.getAncestry());

		objectRDFType = AbstractObjectRDFType.createAbstractObjectRDFType(rootRDFTermType.getAncestry());
		iriTermType = new IRITermType(objectRDFType.getAncestry());
		blankNodeTermType = new BlankNodeTermType(objectRDFType.getAncestry());

		rdfsLiteralDatatype = createSimpleAbstractRDFDatatype(RDFS.LITERAL, rootRDFTermType.getAncestry());
		registerDatatype(rdfsLiteralDatatype);

		numericDatatype = createAbstractNumericTermType(OntopInternal.NUMERIC, rdfsLiteralDatatype.getAncestry());
		registerDatatype(numericDatatype);

		xsdDoubleDatatype = createTopConcreteNumericTermType(XSD.DOUBLE, numericDatatype,
				// TODO: check
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDoubleType());
		registerDatatype(xsdDoubleDatatype);

		// Type promotion: an xsd:float can be promoted into a xsd:double
		xsdFloatDatatype = createConcreteNumericTermType(XSD.FLOAT, numericDatatype.getAncestry(),
				xsdDoubleDatatype.getPromotionSubstitutionHierarchy(),true,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDoubleType());
		registerDatatype(xsdFloatDatatype);

		owlRealDatatype = createAbstractNumericTermType(OWL.REAL, numericDatatype.getAncestry());
		registerDatatype(owlRealDatatype);
		// Type promotion: an owl:rational can be promoted into a xsd:float
		owlRationalDatatype = createConcreteNumericTermType(OWL.RATIONAL, owlRealDatatype.getAncestry(),
				xsdFloatDatatype.getPromotionSubstitutionHierarchy(), true,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDecimalType());
		registerDatatype(owlRationalDatatype);
		xsdDecimalDatatype = createConcreteNumericTermType(XSD.DECIMAL, owlRationalDatatype, true,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDecimalType());
		registerDatatype(xsdDecimalDatatype);
		xsdIntegerDatatype = createConcreteNumericTermType(XSD.INTEGER, xsdDecimalDatatype, true,
				// TODO: check
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdIntegerDatatype);

		xsdNonPositiveIntegerDatatype = createConcreteNumericTermType(XSD.NON_POSITIVE_INTEGER,
				xsdIntegerDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdNonPositiveIntegerDatatype);
		xsdNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NEGATIVE_INTEGER,
				xsdNonPositiveIntegerDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdNegativeIntegerDatatype);

		xsdLongDatatype = createConcreteNumericTermType(XSD.LONG, xsdIntegerDatatype,false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdLongDatatype);
		xsdIntDatatype = createConcreteNumericTermType(XSD.INT, xsdLongDatatype,false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdIntDatatype);
		xsdShortDatatype = createConcreteNumericTermType(XSD.SHORT, xsdIntDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdShortDatatype);
		xsdByteDatatype = createConcreteNumericTermType(XSD.BYTE, xsdShortDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdByteDatatype);

		xsdNonNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NON_NEGATIVE_INTEGER,
				xsdIntegerDatatype,false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdNonNegativeIntegerDatatype);

		xsdUnsignedLongDatatype = createConcreteNumericTermType(XSD.UNSIGNED_LONG, xsdIntegerDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdUnsignedLongDatatype);
		xsdUnsignedIntDatatype = createConcreteNumericTermType(XSD.UNSIGNED_INT, xsdUnsignedLongDatatype,false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdUnsignedIntDatatype);

		xsdUnsignedShortDatatype = createConcreteNumericTermType(XSD.UNSIGNED_SHORT, xsdUnsignedIntDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdUnsignedShortDatatype);
		xsdUnsignedByteDatatype = createConcreteNumericTermType(XSD.UNSIGNED_BYTE, xsdUnsignedShortDatatype, false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdUnsignedByteDatatype);

		xsdPositiveIntegerDatatype = createConcreteNumericTermType(XSD.POSITIVE_INTEGER,
				xsdNonNegativeIntegerDatatype,false,
				// TODO: is there a better type?
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBLargeIntegerType());
		registerDatatype(xsdPositiveIntegerDatatype);

		xsdBooleanDatatype = createSimpleConcreteRDFDatatype(XSD.BOOLEAN, rdfsLiteralDatatype.getAncestry(),
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBBooleanType());
		registerDatatype(xsdBooleanDatatype);

		xsdStringDatatype = createSimpleConcreteRDFDatatype(XSD.STRING, rdfsLiteralDatatype.getAncestry(),
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBStringType());
		registerDatatype(xsdStringDatatype);

		defaultUnsupportedDatatype = UnsupportedRDFDatatype.createUnsupportedDatatype(rdfsLiteralDatatype.getAncestry());

		xsdTimeDatatype = createSimpleConcreteRDFDatatype(XSD.TIME, rdfsLiteralDatatype.getAncestry(),
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBTimeType());
		registerDatatype(xsdTimeDatatype);

		dateOrDatetimeDatatype = createSimpleAbstractRDFDatatype(OntopInternal.DATE_OR_DATETIME, rdfsLiteralDatatype.getAncestry());
		registerDatatype(dateOrDatetimeDatatype);

		xsdDateDatatype = createSimpleConcreteRDFDatatype(XSD.DATE, dateOrDatetimeDatatype.getAncestry(),
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDateType());
		registerDatatype(xsdDateDatatype);
		xsdDatetimeDatatype = createSimpleConcreteRDFDatatype(XSD.DATETIME, dateOrDatetimeDatatype.getAncestry(),
				// TODO: check
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDateTimestampType());
		registerDatatype(xsdDatetimeDatatype);
		xsdDatetimeStampDatatype = createSimpleConcreteRDFDatatype(XSD.DATETIMESTAMP, xsdDatetimeDatatype.getAncestry(),
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBDateTimestampType());
		registerDatatype(xsdDatetimeStampDatatype);
		xsdGYearDatatype = createSimpleConcreteRDFDatatype(XSD.GYEAR, rdfsLiteralDatatype.getAncestry(),
				// TODO: check
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBStringType());
		registerDatatype(xsdGYearDatatype);

		xsdBase64Datatype = createSimpleConcreteRDFDatatype(XSD.BASE64BINARY, rdfsLiteralDatatype.getAncestry(),
				// TODO: is there a better type
				(DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBStringType());
		registerDatatype(xsdBase64Datatype);

		dbTypeFactory = dbTypeFactoryFactory.createDBFactory(rootTermType, this);
	}

	private void registerDatatype(RDFDatatype datatype) {
		datatypeCache.put(datatype.getIRI(), datatype);
	}

	@Override
	public RDFDatatype getLangTermType(String languageTagString) {
		return langTypeCache
				.computeIfAbsent(languageTagString.toLowerCase(), this::createLangStringDatatype);
	}

	@Override
	public RDFDatatype getDatatype(IRI iri) {
		return datatypeCache.computeIfAbsent(
				iri,
				// Non-predefined datatypes cannot be declared as the child of a concrete datatype
				(Function<IRI, RDFDatatype> & Serializable) i -> createSimpleConcreteRDFDatatype(i, rdfsLiteralDatatype.getAncestry(), (DBTypeFactorySerializable) (dbTypeFactory) -> dbTypeFactory.getDBStringType()));
	}

	@Override
	public ObjectRDFType getIRITermType() {
		return iriTermType;
	}

	@Override
	public ObjectRDFType getBlankNodeType() {
		return blankNodeTermType;
	}

	@Override
	public RDFDatatype getUnsupportedDatatype() {
		return defaultUnsupportedDatatype;
	}

	@Override
	public RDFDatatype getAbstractOntopNumericDatatype() {
		return numericDatatype;
	}

	@Override
	public RDFDatatype getAbstractOntopDateOrDatetimeDatatype() {
		return dateOrDatetimeDatatype;
	}

	@Override
	public RDFDatatype getAbstractRDFSLiteral() {
		return rdfsLiteralDatatype;
	}

	@Override
	public TermType getAbstractAtomicTermType() {
		return rootTermType;
	}

	@Override
	public RDFTermType getAbstractRDFTermType() {
		return rootRDFTermType;
	}

    @Override
    public ObjectRDFType getAbstractObjectRDFType() {
		return objectRDFType;
    }

	@Override
	public MetaRDFTermType getMetaRDFTermType() {
		return metaRDFTermType;
	}

	@Override
	public DBTypeFactory getDBTypeFactory() {
		return dbTypeFactory;
	}

	private RDFDatatype createLangStringDatatype(String languageTagString) {
		return LangDatatype.createLangDatatype(
				new LanguageTagImpl(languageTagString), xsdStringDatatype.getAncestry(), this);
	}
}
