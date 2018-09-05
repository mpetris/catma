/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooqgen.catmarepository.tables;


import de.catma.repository.db.jooqgen.catmarepository.Catmarepository;
import de.catma.repository.db.jooqgen.catmarepository.Keys;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tagreference extends TableImpl<Record> {

	private static final long serialVersionUID = -1386764404;

	/**
	 * The reference instance of <code>catmarepository.tagreference</code>
	 */
	public static final Tagreference TAGREFERENCE = new Tagreference();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<Record> getRecordType() {
		return Record.class;
	}

	/**
	 * The column <code>catmarepository.tagreference.tagReferenceID</code>.
	 */
	public final TableField<Record, Integer> TAGREFERENCEID = createField("tagReferenceID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>catmarepository.tagreference.characterStart</code>.
	 */
	public final TableField<Record, Integer> CHARACTERSTART = createField("characterStart", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>catmarepository.tagreference.characterEnd</code>.
	 */
	public final TableField<Record, Integer> CHARACTEREND = createField("characterEnd", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>catmarepository.tagreference.userMarkupCollectionID</code>.
	 */
	public final TableField<Record, Integer> USERMARKUPCOLLECTIONID = createField("userMarkupCollectionID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>catmarepository.tagreference.tagInstanceID</code>.
	 */
	public final TableField<Record, Integer> TAGINSTANCEID = createField("tagInstanceID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>catmarepository.tagreference</code> table reference
	 */
	public Tagreference() {
		this("tagreference", null);
	}

	/**
	 * Create an aliased <code>catmarepository.tagreference</code> table reference
	 */
	public Tagreference(String alias) {
		this(alias, TAGREFERENCE);
	}

	private Tagreference(String alias, Table<Record> aliased) {
		this(alias, aliased, null);
	}

	private Tagreference(String alias, Table<Record> aliased, Field<?>[] parameters) {
		super(alias, Catmarepository.CATMAREPOSITORY, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<Record, Integer> getIdentity() {
		return Keys.IDENTITY_TAGREFERENCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return Keys.KEY_TAGREFERENCE_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<Record>> getKeys() {
		return Arrays.<UniqueKey<Record>>asList(Keys.KEY_TAGREFERENCE_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<Record, ?>> getReferences() {
		return Arrays.<ForeignKey<Record, ?>>asList(Keys.FK_TR_USERMARKUPCOLLECTIONID, Keys.FK_TR_TAGINSTANCEID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tagreference as(String alias) {
		return new Tagreference(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Tagreference rename(String name) {
		return new Tagreference(name, null);
	}
}