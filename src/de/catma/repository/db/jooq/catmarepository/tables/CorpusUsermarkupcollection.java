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
public class CorpusUsermarkupcollection extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -1705034777;

	/**
	 * The singleton instance of <code>CatmaRepository.corpus_usermarkupcollection</code>
	 */
	public static final de.catma.repository.db.jooq.catmarepository.tables.CorpusUsermarkupcollection CORPUS_USERMARKUPCOLLECTION = new de.catma.repository.db.jooq.catmarepository.tables.CorpusUsermarkupcollection();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.corpus_usermarkupcollection.corpus_usermarkupcollectionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CORPUS_USERMARKUPCOLLECTIONID = createField("corpus_usermarkupcollectionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.corpus_usermarkupcollection.corpusID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CORPUSID = createField("corpusID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.corpus_usermarkupcollection.userMarkupCollectionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> USERMARKUPCOLLECTIONID = createField("userMarkupCollectionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>CatmaRepository.corpus_usermarkupcollection</code> table reference
	 */
	public CorpusUsermarkupcollection() {
		super("corpus_usermarkupcollection", de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.corpus_usermarkupcollection</code> table reference
	 */
	public CorpusUsermarkupcollection(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooq.catmarepository.tables.CorpusUsermarkupcollection.CORPUS_USERMARKUPCOLLECTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmarepository.Keys.IDENTITY_CORPUS_USERMARKUPCOLLECTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_USERMARKUPCOLLECTION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_USERMARKUPCOLLECTION_PRIMARY, de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_USERMARKUPCOLLECTION_UK_CORPUSUMC_CORPUSID_UMCID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooq.catmarepository.Keys.FK_CORPUSUMC_CORPUSID, de.catma.repository.db.jooq.catmarepository.Keys.FK_CORPUSUMC_USERMARKUPCOLLECTIONID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmarepository.tables.CorpusUsermarkupcollection as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmarepository.tables.CorpusUsermarkupcollection(alias);
	}
}
