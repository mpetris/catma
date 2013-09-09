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
public class UserSourcedocument extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -1568399706;

	/**
	 * The singleton instance of <code>CatmaRepository.user_sourcedocument</code>
	 */
	public static final de.catma.repository.db.jooq.catmarepository.tables.UserSourcedocument USER_SOURCEDOCUMENT = new de.catma.repository.db.jooq.catmarepository.tables.UserSourcedocument();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.user_sourcedocument.user_sourcedocumentID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> USER_SOURCEDOCUMENTID = createField("user_sourcedocumentID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.user_sourcedocument.userID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> USERID = createField("userID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.user_sourcedocument.sourceDocumentID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> SOURCEDOCUMENTID = createField("sourceDocumentID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.user_sourcedocument.accessMode</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> ACCESSMODE = createField("accessMode", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.user_sourcedocument.owner</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Byte> OWNER = createField("owner", org.jooq.impl.SQLDataType.TINYINT, this);

	/**
	 * Create a <code>CatmaRepository.user_sourcedocument</code> table reference
	 */
	public UserSourcedocument() {
		super("user_sourcedocument", de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.user_sourcedocument</code> table reference
	 */
	public UserSourcedocument(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooq.catmarepository.tables.UserSourcedocument.USER_SOURCEDOCUMENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmarepository.Keys.IDENTITY_USER_SOURCEDOCUMENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmarepository.Keys.KEY_USER_SOURCEDOCUMENT_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmarepository.Keys.KEY_USER_SOURCEDOCUMENT_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooq.catmarepository.Keys.FK_USERSD_USERID, de.catma.repository.db.jooq.catmarepository.Keys.FK_USERSD_SOURCEDOCUMENTID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmarepository.tables.UserSourcedocument as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmarepository.tables.UserSourcedocument(alias);
	}
}
