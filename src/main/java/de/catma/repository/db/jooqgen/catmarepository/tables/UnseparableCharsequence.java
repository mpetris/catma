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
public class UnseparableCharsequence extends TableImpl<Record> {

	private static final long serialVersionUID = 478680465;

	/**
	 * The reference instance of <code>catmarepository.unseparable_charsequence</code>
	 */
	public static final UnseparableCharsequence UNSEPARABLE_CHARSEQUENCE = new UnseparableCharsequence();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<Record> getRecordType() {
		return Record.class;
	}

	/**
	 * The column <code>catmarepository.unseparable_charsequence.uscID</code>.
	 */
	public final TableField<Record, Integer> USCID = createField("uscID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>catmarepository.unseparable_charsequence.charsequence</code>.
	 */
	public final TableField<Record, String> CHARSEQUENCE = createField("charsequence", org.jooq.impl.SQLDataType.VARCHAR.length(45).nullable(false), this, "");

	/**
	 * The column <code>catmarepository.unseparable_charsequence.sourceDocumentID</code>.
	 */
	public final TableField<Record, Integer> SOURCEDOCUMENTID = createField("sourceDocumentID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>catmarepository.unseparable_charsequence</code> table reference
	 */
	public UnseparableCharsequence() {
		this("unseparable_charsequence", null);
	}

	/**
	 * Create an aliased <code>catmarepository.unseparable_charsequence</code> table reference
	 */
	public UnseparableCharsequence(String alias) {
		this(alias, UNSEPARABLE_CHARSEQUENCE);
	}

	private UnseparableCharsequence(String alias, Table<Record> aliased) {
		this(alias, aliased, null);
	}

	private UnseparableCharsequence(String alias, Table<Record> aliased, Field<?>[] parameters) {
		super(alias, Catmarepository.CATMAREPOSITORY, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<Record, Integer> getIdentity() {
		return Keys.IDENTITY_UNSEPARABLE_CHARSEQUENCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return Keys.KEY_UNSEPARABLE_CHARSEQUENCE_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<Record>> getKeys() {
		return Arrays.<UniqueKey<Record>>asList(Keys.KEY_UNSEPARABLE_CHARSEQUENCE_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<Record, ?>> getReferences() {
		return Arrays.<ForeignKey<Record, ?>>asList(Keys.FK_USCS_SOURCEDOCUMENTID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UnseparableCharsequence as(String alias) {
		return new UnseparableCharsequence(alias, this);
	}

	/**
	 * Rename this table
	 */
	public UnseparableCharsequence rename(String name) {
		return new UnseparableCharsequence(name, null);
	}
}