/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.catma.document.annotation.AnnotationCollection;
import de.catma.document.annotation.AnnotationCollectionReference;
import de.catma.document.annotation.TagReference;
import de.catma.document.comment.Comment;
import de.catma.document.comment.Reply;
import de.catma.document.corpus.Corpus;
import de.catma.document.source.ContentInfoSet;
import de.catma.document.source.SourceDocument;
import de.catma.project.event.CollectionChangeEvent;
import de.catma.project.event.DocumentChangeEvent;
import de.catma.rbac.RBACPermission;
import de.catma.rbac.RBACRole;
import de.catma.rbac.RBACSubject;
import de.catma.serialization.TagsetDefinitionImportStatus;
import de.catma.tag.Property;
import de.catma.tag.TagInstance;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagLibraryReference;
import de.catma.tag.TagManager;
import de.catma.tag.TagsetDefinition;
import de.catma.user.Member;
import de.catma.user.User;
import de.catma.util.Pair;

/**
 * A repository to store {@link SourceDocument}s, {@link AnnotationCollection}s and
 * {@link TagLibrary TagLibraries}.
 * 
 * @author marco.petris@web.de
 *
 */
public interface Project {
	
	/**
	 * The Repository emits these change events to listeners that have
	 * been registered with {@link Repository#addPropertyChangeListener(RepositoryChangeEvent, PropertyChangeListener)}.
	 *
	 */
	/**
	 * @author marco.petris@web.de
	 *
	 */
	public static enum RepositoryChangeEvent {
		/**
		 * @deprecated use {@link DocumentChangeEvent}
		 * <p>{@link SourceDocument} added:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link SourceDocument#getID()}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = <code>null</code></li>
		 * </p><br />
		 * <p>{@link SourceDocument} removed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = <code>null</code></li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link SourceDocument}</li>
		 * </p><br />
		 * <p>{@link SourceDocument} Metadata changed or a document reload has taken place
		 * <li>{@link PropertyChangeEvent#getNewValue()} = new {@link SourceDocument}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link SourceDocument#getID()}</li>
		 * </p>
		 */
		@Deprecated
		sourceDocumentChanged,
		/**
		 * @deprecated use {@link CollectionChangeEvent}
		 * <p>{@link UserMarkupCollection} added:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = a {@link Pair} of 
		 * {@link UserMarkupCollectionReference} and corresponding {@link SourceDocument}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = <code>null</code></li>
		 * </p><br />
		 * <p>{@link UserMarkupCollection} removed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = <code>null</code></li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link UserMarkupCollection}</li>
		 * </p><br />
		 * <p>{@link UserMarkupCollection} Metadata changed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link UserMarkupCollectionReference}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = old {@link ContentInfoSet}</li>
		 * </p>
		 */
		@Deprecated
		userMarkupCollectionChanged,
		/**
		 * @deprecated obsolet <br>
		 * 
		 * Updates on the User Markup Collection's inner Tag Library.
		 * <li>{@link PropertyChangeEvent#getNewValue()} =
		 *  {@link java.util.List List} of updated {@link UserMarkupCollection}s</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = changed {@link TagsetDefinition}</li>
		 */
		@Deprecated
		userMarkupCollectionTagLibraryChanged,
		/**
		 * <p>{@link TagLibrary} added:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = a {@link Pair} of 
		 * {@link TagLibraryReference} and corresponding {@link SourceDocument}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = <code>null</code></li>
		 * </p><br />
		 * <p>{@link TagLibrary} removed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = <code>null</code></li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link TagLibraryReference}</li>
		 * </p><br />
		 * <p>{@link TagLibrary} Metadata changed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link TagLibraryReference}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = old {@link ContentInfoSet}</li>
		 * </p>
		 */
		@Deprecated
		tagLibraryChanged,
		/**
		 * <p>{@link Corpus} added:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link Corpus}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = <code>null</code></li>
		 * </p><br />
		 * <p>{@link Corpus} removed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = <code>null</code></li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link SourceDocument}</li>
		 * </p><br />
		 * <p>{@link SourceDocument} added to {@link Corpus}:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link Corpus}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link SourceDocument}</li>
		 * </p>
		 * <p>{@link UserMarkupCollection} added to {@link Corpus}:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link Corpus}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = {@link UserMarkupCollectionReference}</li>
		 * </p>
		 * <p>{@link Corpus} name changed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = {@link Corpus}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = the new name</li>
		 * </p>
		 * <p>{@link Corpus} reload:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = current {@link Corpus}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = old {@link Corpus}</li>
		 * </p>
		 */
		@Deprecated
		corpusChanged,
		/**
		 * Signals an exception:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = the {@link Exception}</li>
		 */
		exceptionOccurred, 
		/**
		 * <p>{@link Property} changed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} =  {@link List} of {@link Property Properties}</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = corresponding {@link TagInstance}</li>
		 * </p>
		 */
		propertyValueChanged,
		/**
		 * <p>{@link TagReference}s added:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = a {@link Pair} of 
		 * a {@link UserMarkupCollection} and a {@link List} of {@link TagReference}s</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = <code>null</code></li>
		 * </p><br />
		 * <p>{@link TagReference}s removed:
		 * <li>{@link PropertyChangeEvent#getNewValue()} = <code>null</code></li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = a {@link Pair} of an 
		 * UUID String of a {@link UserMarkupCollection} and a {@link Collection} of Annotation ID Strings</li>
		 * </p><br />
		 */
		tagReferencesChanged,
		/**
		 * <p> a notification to the repo holder.
		 * <li>{@link PropertyChangeEvent#getNewValue()} = the message of type String</li>
		 * <li>{@link PropertyChangeEvent#getOldValue()} = always <code>null</code></li>
		 * </p>
		 */
		notification,
		;
	}
	
	/**
	 */
	public void open(OpenProjectListener openProjectListener);
	
	public void close();
	
	/**
	 * @param propertyChangeEvent event to listen for
	 * @param propertyChangeListener
	 */
	public void addPropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent, 
			PropertyChangeListener propertyChangeListener);
	
	public void removePropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent, 
			PropertyChangeListener propertyChangeListener);
	
	/**
	 * @return name of the repository
	 */
	public String getName();
	
	
	public String getProjectId();
	
	/**
	 * @param uri may be used for ID creation
	 * @return an ID for the given URI, subsequent calls can produce different results
	 */
	public String getIdFromURI(URI uri);
	/**
	 * @param catmaID the {@link SourceDocument#getUuid()}
	 * @param path a list of path elements
	 * @return the constructed file url
	 */
	public String getFileURL(String sourceDocumentID, String... path);

	/**
	 * Inserts the given SourceDocument into the repository.
	 * @param sourceDocument
	 */
	public void insert(SourceDocument sourceDocument) throws IOException;
	/**
	 * @param sourceDocument document to be updated
	 * @param contentInfoSet new meta data
	 */
	public void update(SourceDocument sourceDocument, ContentInfoSet contentInfoSet);
	/**
	 * @return the available Source Documents
	 * @throws Exception 
	 */
	public Collection<SourceDocument> getSourceDocuments() throws Exception;

	/**
	 *
	 * @return the available Tagsets
	 * @throws Exception
	 */
	Collection<TagsetDefinition> getTagsets() throws Exception;


	/**
	 * @param id ID of the SourceDocument
	 * @return the SourceDocument with the given ID
	 * @throws Exception 
	 */
	public SourceDocument getSourceDocument(String id) throws Exception;
	public void delete(SourceDocument sourceDocument) throws Exception;
	/**
	 * @param umcRef
	 * @return the SourceDocument that belongs to the given UserMarkupCollection
	 */
	public SourceDocument getSourceDocument(AnnotationCollectionReference umcRef);

	public boolean hasDocument(String documentId) throws Exception;

	/**
	 * Creates a User Markup Collection with that name for the given Source Document.
	 * @param name
	 * @param sourceDocument
	 * @throws IOException
	 */
	public void createUserMarkupCollection(String name, SourceDocument sourceDocument);

	/**
	 * @param userMarkupCollectionReference
	 * @return the User Markup Collection for the given reference.
	 * @throws IOException
	 */
	public AnnotationCollection getUserMarkupCollection(
			AnnotationCollectionReference userMarkupCollectionReference) throws IOException;

	/**
	 * Add the Tag References to the given User Markup Collection or remove the 
	 * given Tag References from the User Markup Collection.
	 * @param userMarkupCollection
	 * @param tagReferences
	 */
	public void update(
			AnnotationCollection userMarkupCollection, 
			List<TagReference> tagReferences);
	/**
	 * Updates the given Properties in the Tag Instance.
	 * @param userMarkupCollection 
	 * @param tagInstance
	 * @param property 
	 * @throws IOException
	 */
	public void update(
			AnnotationCollection userMarkupCollection, TagInstance tagInstance, Collection<Property> properties) throws IOException;

	/**
	 * Updates the User Markup Collection's metadata.
	 * @param userMarkupCollectionReference
	 * @param contentInfoSet metadata
	 */
	public void update(
			AnnotationCollectionReference userMarkupCollectionReference, 
			ContentInfoSet contentInfoSet) throws Exception;
	public void delete(
			AnnotationCollectionReference userMarkupCollectionReference) throws Exception;

	/**
	 * @param inputStream the tag library
	 * @return 
	 * @throws IOException
	 */
	public List<TagsetDefinitionImportStatus> loadTagLibrary(InputStream inputStream) throws IOException;

	/**
	 * @return current user of this repository instance
	 */
	public User getUser();
	
	/**
	 * @return the Tag Manager for this repository
	 */
	public TagManager getTagManager();
	
	
	/**
	 * @param sourceDocument
	 * @return the file object that belongs to the given SourceDocument
	 */
	public File getFile(SourceDocument sourceDocument);
	
	public Set<Member> getProjectMembers() throws IOException;

	public boolean hasUncommittedChanges() throws Exception;

	public void commitChanges(String commitMsg) throws Exception;

	public void synchronizeWithRemote(OpenProjectListener openProjectListener) throws Exception;

	void printStatus();

	RBACRole getRoleForTagset(String tagsetId);

	RBACRole getRoleForCollection(String collectionId);

	RBACRole getRoleForDocument(String documentId);

	boolean hasPermission(RBACRole role, RBACPermission permission);

	boolean isAuthorizedOnProject(RBACPermission permission);

	void unassignFromResource(RBACSubject subject, String resourceId) throws IOException;

	RBACSubject assignOnResource(RBACSubject subject, RBACRole role, String resourceId) throws IOException;

	void unassignFromProject(RBACSubject subject) throws IOException;

	RBACSubject assignOnProject(RBACSubject subject, RBACRole role) throws IOException;

	List<User> findUser(String usernameOrEmail, int offset, int limit) throws IOException;

	Set<Member> getResourceMembers(String resourceId) throws IOException;

	String getDescription();

	RBACRole getRoleOnProject() throws IOException;

	void createUserMarkupCollectionWithAssignment(
		String name, SourceDocument sourceDocument, Integer userId, RBACRole role);

	public void importTagsets(List<TagsetDefinitionImportStatus> tagsetDefinitionImportStatusList) throws IOException;

	public Pair<AnnotationCollection, List<TagsetDefinitionImportStatus>> loadAnnotationCollection(
			InputStream inputStream, SourceDocument document) throws IOException;

	public void importCollection(
		List<TagsetDefinitionImportStatus> tagsetDefinitionImportStatusList, AnnotationCollection annotationCollection) throws IOException;

	boolean inProjectHistory(String resourceId) throws IOException;

	void insert(SourceDocument sourceDocument, boolean deleteTempFile) throws IOException;

	public List<CommitInfo> getUnsynchronizedCommits() throws Exception;

	public void addComment(Comment comment) throws IOException;

	public void updateComment(Comment comment) throws IOException;

	public void removeComment(Comment comment) throws IOException;

	List<Comment> getComments(String documentId) throws IOException;

	public void addReply(Comment comment, Reply reply) throws IOException;

	public List<Reply> getCommentReplies(Comment comment) throws IOException;
	
	void removeReply(Comment comment, Reply reply) throws IOException;

	void updateReply(Comment comment, Reply reply) throws IOException;

}
