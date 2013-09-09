/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooq.catmaindex.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Position extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -683040002;

	/**
	 * The singleton instance of <code>CatmaIndex.position</code>
	 */
	public static final de.catma.repository.db.jooq.catmaindex.tables.Position POSITION = new de.catma.repository.db.jooq.catmaindex.tables.Position();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaIndex.position.positionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> POSITIONID = createField("positionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaIndex.position.termID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> TERMID = createField("termID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaIndex.position.characterStart</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CHARACTERSTART = createField("characterStart", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaIndex.position.characterEnd</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> CHARACTEREND = createField("characterEnd", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaIndex.position.tokenOffset</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> TOKENOFFSET = createField("tokenOffset", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>CatmaIndex.position</code> table reference
	 */
	public Position() {
		super("position", de.catma.repository.db.jooq.catmaindex.Catmaindex.CATMAINDEX);
	}

	/**
	 * Create an aliased <code>CatmaIndex.position</code> table reference
	 */
	public Position(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmaindex.Catmaindex.CATMAINDEX, de.catma.repository.db.jooq.catmaindex.tables.Position.POSITION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmaindex.Keys.IDENTITY_POSITION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmaindex.Keys.KEY_POSITION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmaindex.Keys.KEY_POSITION_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooq.catmaindex.Keys.FK_POSITIONTERMID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmaindex.tables.Position as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmaindex.tables.Position(alias);
	}
}
