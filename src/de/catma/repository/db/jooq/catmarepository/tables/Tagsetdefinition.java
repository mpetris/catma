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
public class Tagsetdefinition extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -2077081296;

	/**
	 * The singleton instance of <code>CatmaRepository.tagsetdefinition</code>
	 */
	public static final de.catma.repository.db.jooq.catmarepository.tables.Tagsetdefinition TAGSETDEFINITION = new de.catma.repository.db.jooq.catmarepository.tables.Tagsetdefinition();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.tagsetdefinition.tagsetDefinitionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> TAGSETDEFINITIONID = createField("tagsetDefinitionID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.tagsetdefinition.uuid</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, byte[]> UUID = createField("uuid", org.jooq.impl.SQLDataType.BINARY.length(16), this);

	/**
	 * The column <code>CatmaRepository.tagsetdefinition.version</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.sql.Timestamp> VERSION = createField("version", org.jooq.impl.SQLDataType.TIMESTAMP, this);

	/**
	 * The column <code>CatmaRepository.tagsetdefinition.name</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>CatmaRepository.tagsetdefinition.tagLibraryID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> TAGLIBRARYID = createField("tagLibraryID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>CatmaRepository.tagsetdefinition</code> table reference
	 */
	public Tagsetdefinition() {
		super("tagsetdefinition", de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.tagsetdefinition</code> table reference
	 */
	public Tagsetdefinition(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooq.catmarepository.tables.Tagsetdefinition.TAGSETDEFINITION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmarepository.Keys.IDENTITY_TAGSETDEFINITION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmarepository.Keys.KEY_TAGSETDEFINITION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmarepository.Keys.KEY_TAGSETDEFINITION_PRIMARY, de.catma.repository.db.jooq.catmarepository.Keys.KEY_TAGSETDEFINITION_UK_TSDEF_TLIB_UUID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooq.catmarepository.Keys.FK_TSDEF_TAGLIBRARYID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmarepository.tables.Tagsetdefinition as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmarepository.tables.Tagsetdefinition(alias);
	}
}
