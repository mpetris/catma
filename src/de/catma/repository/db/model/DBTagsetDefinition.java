package de.catma.repository.db.model;

// Generated 22.05.2012 21:58:37 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Tagsetdefinition generated by hbm2java
 */
@Entity
@Table(name = "tagsetdefinition", catalog = "CatmaRepository", uniqueConstraints = @UniqueConstraint(columnNames = {
		"uuid", "tagLibraryID" }))
public class DBTagsetDefinition implements java.io.Serializable {

	public static final String TABLE = "tagsetdefinition";
	
	private Integer tagsetDefinitionId;
	private Date version;
	private byte[] uuid;
	private String name;
	private int tagLibraryId;
	private Set<DBTagDefinition> dbTagDefinitions = new HashSet<DBTagDefinition>();
	
	public DBTagsetDefinition() {
	}
	
	public DBTagsetDefinition(byte[] uuid, Date version, String name, int tagLibraryId) {
		this.uuid = uuid;
		this.version = version;
		this.name = name;
		this.tagLibraryId = tagLibraryId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "tagsetDefinitionID", unique = true, nullable = false)
	public Integer getTagsetDefinitionId() {
		return this.tagsetDefinitionId;
	}

	public void setTagsetDefinitionId(Integer tagsetDefinitionId) {
		this.tagsetDefinitionId = tagsetDefinitionId;
	}

	@Column(name = "version", nullable = false, length = 19)
	public Date getVersion() {
		return this.version;
	}

	public void setVersion(Date version) {
		this.version = version;
	}

	@Column(name = "uuid", nullable = false)
	public byte[] getUuid() {
		return this.uuid;
	}

	public void setUuid(byte[] uuid) {
		this.uuid = uuid;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "tagLibraryID", nullable = false)
	public int getTagLibraryId() {
		return this.tagLibraryId;
	}

	public void setTagLibraryId(int tagLibraryId) {
		this.tagLibraryId = tagLibraryId;
	}
	
	@OneToMany(mappedBy = "dbTagsetDefinition")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<DBTagDefinition> getDbTagDefinitions() {
		return dbTagDefinitions;
	}
	
	public void setDbTagDefinitions(Set<DBTagDefinition> dbTagDefinitions) {
		this.dbTagDefinitions = dbTagDefinitions;
	}

	@Transient
	public DBTagDefinition getDbTagDefinition(Integer dbTagDefinitionId) {
		for (DBTagDefinition dbTagDefinition : getDbTagDefinitions()) {
			Integer id = dbTagDefinition.getTagDefinitionId();
			if ((id != null) &&
					(id.equals(dbTagDefinitionId))) {
				return dbTagDefinition;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + getName();
	}
}
