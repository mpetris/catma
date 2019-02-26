package de.catma.repository.git.graph;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Multimap;

import de.catma.document.source.SourceDocument;
import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollectionReference;
import de.catma.tag.Property;
import de.catma.tag.PropertyDefinition;
import de.catma.tag.TagDefinition;
import de.catma.tag.TagInstance;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagManager;
import de.catma.tag.TagsetDefinition;

public interface GraphProjectHandler {
	
	public interface CollectionsSupplier {
		public List<UserMarkupCollection> get(TagLibrary tagLibrary);
	}

	void ensureProjectRevisionIsLoaded(String revisionHash, 
			TagManager tagManager,
			Supplier<List<TagsetDefinition>> tagsetsSupplier,
			Supplier<List<SourceDocument>> documentsSupplier,
			CollectionsSupplier collectionsSupplier) throws Exception;

	void addSourceDocument(String oldRootRevisionHash, String rootRevisionHash, SourceDocument document,
			Path tokenizedSourceDocumentPath) throws Exception;

	int getSourceDocumentsCount(String rootRevisionHash) throws Exception;

	Collection<SourceDocument> getDocuments(String rootRevisionHash) throws Exception;

	SourceDocument getSourceDocument(String rootRevisionHash, String sourceDocumentId) throws Exception;

	void addCollection(String rootRevisionHash, String collectionId, String name, String umcRevisionHash,
			SourceDocument document, String oldRootRevisionHash) throws Exception;

	void addTagset(String rootRevisionHash, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	void addTagDefinition(String rootRevisionHash, TagDefinition tagDefinition, TagsetDefinition tagset,
			String oldRootRevisionHash) throws Exception;

	void updateTagDefinition(String rootRevisionHash, TagDefinition tagDefinition, TagsetDefinition tagset,
			String oldRootRevisionHash) throws Exception;

	List<UserMarkupCollectionReference> getCollectionReferences(String rootRevisionHash, int offset, int limit)
			throws Exception;

	int getCollectionReferenceCount(String rootRevisionHash) throws Exception;

	Collection<TagsetDefinition> getTagsets(String rootRevisionHash) throws Exception;

	int getTagsetsCount(String rootRevisionHash) throws Exception;

	void addPropertyDefinition(String rootRevisionHash, PropertyDefinition propertyDefinition,
			TagDefinition tagDefinition, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	void createOrUpdatePropertyDefinition(String rootRevisionHash, PropertyDefinition propertyDefinition,
			TagDefinition tagDefinition, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	UserMarkupCollection getCollection(String rootRevisionHash, TagLibrary tagLibrary,
			UserMarkupCollectionReference collectionReference) throws Exception;

	void addTagReferences(String rootRevisionHash, UserMarkupCollection collection, List<TagReference> tagReferences)
			throws Exception;

	void removeTagReferences(String rootRevisionHash, UserMarkupCollection collection, List<TagReference> tagReferences)
			throws Exception;

	void removeProperties(String rootRevisionHash, String collectionId, String collectionRevisionHash,
			String propertyDefId) throws Exception;

	void updateProperties(String rootRevisionHash, TagInstance tagInstance, Collection<Property> properties)
			throws Exception;

	Multimap<String, String> getAnnotationIdsByCollectionId(String rootRevisionHash, TagDefinition tagDefinition)
			throws Exception;

	Multimap<String, TagReference> getTagReferencesByCollectionId(String rootRevisionHash,
			PropertyDefinition propertyDefinition, TagLibrary tagLibrary) throws Exception;

	void removeTagInstances(String rootRevisionHash, String collectionId, Collection<String> tagInstanceIds,
			String collectionRevisionHash) throws Exception;

	void removeTagDefinition(String rootRevisionHash, TagDefinition tagDefinition, TagsetDefinition tagset)
			throws Exception;

	void updateProjectRevisionHash(String oldRootRevisionHash, String rootRevisionHash) throws Exception;

	void updateCollectionRevisionHash(String rootRevisionHash, UserMarkupCollectionReference collectionReference) throws Exception;

	void removePropertyDefinition(String rootRevisionHash, PropertyDefinition propertyDefinition,
			TagDefinition tagDefinition, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	void removeTagset(String rootRevisionHash, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	void updateTagset(String rootRevisionHash, TagsetDefinition tagset, String oldRootRevisionHash)
			throws Exception;

	void updateCollection(String rootRevisionHash, UserMarkupCollectionReference collectionRef,
			String oldRootRevisionHash) throws Exception;

	void removeCollection(String rootRevisionHash, UserMarkupCollectionReference collectionReference,
			String oldRootRevisionHash) throws Exception;

	void removeDocument(String rootRevisionHash, SourceDocument document, String oldRootRevisionHash)
			throws Exception;

}