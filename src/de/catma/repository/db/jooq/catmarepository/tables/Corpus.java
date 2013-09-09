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
public class Corpus extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -1029617817;

	/**
	 * The singleton instance of <code>CatmaRepository.corpus</code>
	 */
	public static final de.catma.repository.db.jooq.catmarepository.tables.Corpus CORPUS = new de.catma.repository.db.jooq.catmarepository.tables.Corpus();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.corpus.corpusID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CORPUSID = createField("corpusID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.corpus.name</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(45), this);

	/**
	 * Create a <code>CatmaRepository.corpus</code> table reference
	 */
	public Corpus() {
		super("corpus", de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.corpus</code> table reference
	 */
	public Corpus(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooq.catmarepository.tables.Corpus.CORPUS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmarepository.Keys.IDENTITY_CORPUS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmarepository.Keys.KEY_CORPUS_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmarepository.tables.Corpus as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmarepository.tables.Corpus(alias);
	}
}
