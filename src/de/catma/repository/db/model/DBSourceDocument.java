package de.catma.repository.db.model;

// Generated 22.05.2012 21:58:37 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.catma.core.document.Range;
import de.catma.core.document.source.FileOSType;
import de.catma.core.document.source.FileType;
import de.catma.core.document.source.ISourceDocument;
import de.catma.core.document.source.SourceDocument;
import de.catma.core.document.source.SourceDocumentHandler;
import de.catma.core.document.source.SourceDocumentInfo;
import de.catma.core.document.source.contenthandler.SourceContentHandler;
import de.catma.core.document.standoffmarkup.staticmarkup.StaticMarkupCollectionReference;
import de.catma.core.document.standoffmarkup.usermarkup.UserMarkupCollectionReference;

/**
 * Sourcedocument generated by hbm2java
 */
@Entity
@Table(name = "sourcedocument", catalog = "CatmaRepository")
public class DBSourceDocument implements java.io.Serializable, ISourceDocument {

	private Integer sourceDocumentId;
	private SourceDocument sourceDocumentDelegate;
	private SourceDocumentInfo sourceDocumentInfo;
	private Set<DBUserSourceDocument> dbUserSourceDocuments = 
			new HashSet<DBUserSourceDocument>();
	private Set<DBUserMarkupCollection> dbUserMarkupCollections = 
			new HashSet<DBUserMarkupCollection>();
	
	public DBSourceDocument(SourceDocument sourceDocument) {
		sourceDocumentDelegate = sourceDocument;
		sourceDocumentInfo = 
			sourceDocumentDelegate.getSourceContentHandler().getSourceDocumentInfo();
	}
	
	public DBSourceDocument() {
		sourceDocumentDelegate = 
				new SourceDocumentHandler().createEmptySourceDocument();
		sourceDocumentInfo = 
			sourceDocumentDelegate.getSourceContentHandler().getSourceDocumentInfo();
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "sourceDocumentID", unique = true, nullable = false)
	public Integer getSourceDocumentId() {
		return this.sourceDocumentId;
	}

	public void setSourceDocumentId(Integer sourceDocumentId) {
		this.sourceDocumentId = sourceDocumentId;
	}

	@Column(name = "title", length = 300)
	public String getTitle() {
		return sourceDocumentInfo.getContentInfoSet().getTitle();
	}

	public void setTitle(String title) {
		sourceDocumentInfo.getContentInfoSet().setTitle(title);
	}

	@Column(name = "publisher", length = 300)
	public String getPublisher() {
		return sourceDocumentInfo.getContentInfoSet().getPublisher();
	}

	public void setPublisher(String publisher) {
		sourceDocumentInfo.getContentInfoSet().setPublisher(publisher);
	}

	@Column(name = "author", length = 300)
	public String getAuthor() {
		return sourceDocumentInfo.getContentInfoSet().getAuthor();
	}

	public void setAuthor(String author) {
		sourceDocumentInfo.getContentInfoSet().setAuthor(author);
	}

	@Column(name = "description", length = 300)
	public String getDescription() {
		return sourceDocumentInfo.getContentInfoSet().getDescription();
	}

	public void setDescription(String description) {
		sourceDocumentInfo.getContentInfoSet().setDescription(description);
	}

	@Column(name = "uri", nullable = false, length = 300)
	public String getUri() {
		return sourceDocumentInfo.getTechInfoSet().getURI().toString();
	}

	public void setUri(String uri) throws URISyntaxException {
		sourceDocumentInfo.getTechInfoSet().setURI(new URI(uri));
	}

	@Column(name = "fileType", nullable = false, length = 5)
	public String getFileType() {
		return sourceDocumentInfo.getTechInfoSet().getFileType().name();
	}

	public void setFileType(String fileType) 
			throws IOException, InstantiationException, IllegalAccessException {
		
		FileType oldFileType = sourceDocumentInfo.getTechInfoSet().getFileType();
		
		sourceDocumentInfo.getTechInfoSet().setFileType(
				FileType.valueOf(fileType));
		
		if ((oldFileType == null) 
				|| (!oldFileType.equals(FileType.valueOf(fileType)))) {
			sourceDocumentDelegate = 
				new SourceDocumentHandler().loadSourceDocument(
						getID(), sourceDocumentInfo);
		}
		
	}

	@Column(name = "charset", nullable = false, length = 50)
	public String getCharset() {
		return sourceDocumentInfo.getTechInfoSet().getCharset().toString();
	}

	public void setCharset(String charset) {
		sourceDocumentInfo.getTechInfoSet().setCharset(
				Charset.forName(charset));
	}

	@Column(name = "fileOSType", nullable = false, length = 15)
	public String getFileOstype() {
		return sourceDocumentInfo.getTechInfoSet().getFileOSType().name();
	}

	public void setFileOstype(String fileOstype) {
		sourceDocumentInfo.getTechInfoSet().setFileOSType(
				FileOSType.valueOf(fileOstype));
	}

	@Column(name = "checksum", nullable = false)
	public long getChecksum() {
		return sourceDocumentInfo.getTechInfoSet().getChecksum();
	}

	public void setChecksum(long checksum) {
		sourceDocumentInfo.getTechInfoSet().setChecksum(checksum);
	}

	@Column(name = "mimeType", length = 45)
	public String getMimeType() {
		return sourceDocumentInfo.getTechInfoSet().getMimeType();
	}

	public void setMimeType(String mimeType) {
		sourceDocumentInfo.getTechInfoSet().setMimeType(mimeType);
	}

	@Column(name = "xsltDocumentLocalUri", length = 300)
	public String getXsltDocumentLocalUri() {
		return sourceDocumentInfo.getTechInfoSet().getXsltDocumentLocalUri();
	}

	public void setXsltDocumentLocalUri(String xsltDocumentLocalUri) {
		sourceDocumentInfo.getTechInfoSet().setXsltDocumentLocalUri(
				xsltDocumentLocalUri);
	}

	@Column(name = "locale", nullable = false, length = 15)
	public String getLocale() {
		return sourceDocumentInfo.getIndexInfoSet().getLocale().toString();
	}

	public void setLocale(String locale) {
		sourceDocumentInfo.getIndexInfoSet().setLocale(new Locale(locale)); //TODO: problem when country or variant is included
	}

	@Column(name = "localUri", length = 300)
	public String getLocalUri() {
		return this.sourceDocumentDelegate.getID();
	}

	public void setLocalUri(String localUri) {
		this.sourceDocumentDelegate.setId(localUri);
	}
	
	@OneToMany(mappedBy = "dbSourceDocument")
	public Set<DBUserSourceDocument> getDbUserSourceDocuments() {
		return dbUserSourceDocuments;
	}
	
	public void setDbUserSourceDocuments(
			Set<DBUserSourceDocument> dbUserSourceDocuments) {
		this.dbUserSourceDocuments = dbUserSourceDocuments;
	}

	@OneToMany(mappedBy = "sourceDocumentId")
	public Set<DBUserMarkupCollection> getDbUserMarkupCollections() {
		return dbUserMarkupCollections;
	}
	
	public void setDbUserMarkupCollections(
			Set<DBUserMarkupCollection> dbUserMarkupCollections) {
		this.dbUserMarkupCollections = dbUserMarkupCollections;
	}
	
	@Transient
	public String getContent(Range range) throws IOException {
		return sourceDocumentDelegate.getContent(range);
	}

	@Transient
	public String getContent() throws IOException {
		return sourceDocumentDelegate.getContent();
	}

	public void addStaticMarkupCollectionReference(
			StaticMarkupCollectionReference staticMarkupCollRef) {
		sourceDocumentDelegate
				.addStaticMarkupCollectionReference(staticMarkupCollRef);
	}

	public void addUserMarkupCollectionReference(
			UserMarkupCollectionReference userMarkupCollRef) {
		sourceDocumentDelegate
				.addUserMarkupCollectionReference(userMarkupCollRef);
	}

	@Transient
	public String getID() {
		return sourceDocumentDelegate.getID();
	}

	@Transient
	public List<StaticMarkupCollectionReference> getStaticMarkupCollectionRefs() {
		return sourceDocumentDelegate.getStaticMarkupCollectionRefs();
	}

	@Transient
	public List<UserMarkupCollectionReference> getUserMarkupCollectionRefs() {
		return sourceDocumentDelegate.getUserMarkupCollectionRefs();
	}

	@Transient
	public UserMarkupCollectionReference getUserMarkupCollectionReference(
			String id) {
		return sourceDocumentDelegate.getUserMarkupCollectionReference(id);
	}

	@Transient
	public SourceContentHandler getSourceContentHandler() {
		return sourceDocumentDelegate.getSourceContentHandler();
	}

	@Transient
	public int getLength() throws IOException {
		return sourceDocumentDelegate.getLength();
	}

	public void unload() {
		sourceDocumentDelegate.unload();
	}

	@Transient
	public boolean isLoaded() {
		return sourceDocumentDelegate.isLoaded();
	}

	@Override
	public String toString() {
		return sourceDocumentDelegate.toString();
	}
	
}
