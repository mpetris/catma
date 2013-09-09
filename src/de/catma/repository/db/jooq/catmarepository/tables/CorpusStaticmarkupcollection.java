/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooq.catmarepository.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CorpusStaticmarkupcollection extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = 809124117;

	/**
	 * The singleton instance of <code>CatmaRepository.corpus_staticmarkupcollection</code>
	 */
	public static final de.catma.repository.db.jooq.catmarepository.tables.CorpusStaticmarkupcollection CORPUS_STATICMARKUPCOLLECTION = new de.catma.repository.db.jooq.catmarepository.tables.CorpusStaticmarkupcollection();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.corpus_staticmarkupcollection.corpus_staticmarkupcollectionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CORPUS_STATICMARKUPCOLLECTIONID = createField("corpus_staticmarkupcollectionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.corpus_staticmarkupcollection.corpusID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CORPUSID = createField("corpusID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.corpus_staticmarkupcollection.staticMarkupCollectionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> STATICMARKUPCOLLECTIONID = createField("staticMarkupCollectionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>CatmaRepository.corpus_staticmarkupcollection</code> table reference
	 */
	public CorpusStaticmarkupcollection() {
		super("corpus_staticmarkupcollection", de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.corpus_staticmarkupcollection</code> table reference
	 */
	public CorpusStaticmarkupcollection(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooq.catmarepository.tables.CorpusStaticmarkupcollection.CORPUS_STATICMARKUPCOLLECTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmarepository.Keys.IDENTITY_CORPUS_STATICMARKUPCOLLECTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_STATICMARKUPCOLLECTION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_STATICMARKUPCOLLECTION_PRIMARY, de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_STATICMARKUPCOLLECTION_UK_CORPUSSMC_CORPUSID_SMCID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooq.catmarepository.Keys.FK_CORPUSSMC_CORPUSID, de.catma.repository.db.jooq.catmarepository.Keys.FK_CORPUSSMC_STATICMARKUPCOLLECTIONID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmarepository.tables.CorpusStaticmarkupcollection as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmarepository.tables.CorpusStaticmarkupcollection(alias);
	}
}
