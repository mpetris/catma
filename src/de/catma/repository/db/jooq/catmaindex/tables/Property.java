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
public class Property extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = 230075810;

	/**
	 * The singleton instance of <code>CatmaIndex.property</code>
	 */
	public static final de.catma.repository.db.jooq.catmaindex.tables.Property PROPERTY = new de.catma.repository.db.jooq.catmaindex.tables.Property();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaIndex.property.propertyID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> PROPERTYID = createField("propertyID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaIndex.property.tagInstanceID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, byte[]> TAGINSTANCEID = createField("tagInstanceID", org.jooq.impl.SQLDataType.BINARY.length(16), this);

	/**
	 * The column <code>CatmaIndex.property.propertyDefinitionID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, byte[]> PROPERTYDEFINITIONID = createField("propertyDefinitionID", org.jooq.impl.SQLDataType.BINARY.length(16), this);

	/**
	 * The column <code>CatmaIndex.property.value</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * Create a <code>CatmaIndex.property</code> table reference
	 */
	public Property() {
		super("property", de.catma.repository.db.jooq.catmaindex.Catmaindex.CATMAINDEX);
	}

	/**
	 * Create an aliased <code>CatmaIndex.property</code> table reference
	 */
	public Property(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooq.catmaindex.Catmaindex.CATMAINDEX, de.catma.repository.db.jooq.catmaindex.tables.Property.PROPERTY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooq.catmaindex.Keys.IDENTITY_PROPERTY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooq.catmaindex.Keys.KEY_PROPERTY_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooq.catmaindex.Keys.KEY_PROPERTY_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooq.catmaindex.tables.Property as(java.lang.String alias) {
		return new de.catma.repository.db.jooq.catmaindex.tables.Property(alias);
	}
}
