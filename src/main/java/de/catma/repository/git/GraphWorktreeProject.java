package de.catma.repository.git;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;

import de.catma.backgroundservice.BackgroundService;
import de.catma.document.AccessMode;
import de.catma.document.Corpus;
import de.catma.document.repository.RepositoryPropertyKey;
import de.catma.document.source.ContentInfoSet;
import de.catma.document.source.SourceDocument;
import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollectionReference;
import de.catma.indexer.IndexBufferManagerName;
import de.catma.indexer.IndexedRepository;
import de.catma.indexer.Indexer;
import de.catma.indexer.TermExtractor;
import de.catma.indexer.TermInfo;
import de.catma.indexer.indexbuffer.IndexBufferManager;
import de.catma.project.OpenProjectListener;
import de.catma.project.ProjectReference;
import de.catma.repository.git.graph.FileInfoProvider;
import de.catma.repository.git.graph.GraphProjectHandler;
import de.catma.repository.git.graph.GraphProjectHandler.TagInstanceSynchHandler;
import de.catma.repository.git.graph.indexer.GraphProjectIndexer;
import de.catma.serialization.UserMarkupCollectionSerializationHandler;
import de.catma.tag.Property;
import de.catma.tag.PropertyDefinition;
import de.catma.tag.TagDefinition;
import de.catma.tag.TagInstance;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagLibraryReference;
import de.catma.tag.TagManager;
import de.catma.tag.TagManager.TagManagerEvent;
import de.catma.tag.TagsetDefinition;
import de.catma.tag.Version;
import de.catma.user.User;
import de.catma.util.IDGenerator;
import de.catma.util.Pair;

public class GraphWorktreeProject implements IndexedRepository {
	
	private static final String UTF8_CONVERSION_FILE_EXTENSION = "txt";
	private static final String ORIG_INFIX = "_orig";
	private static final String TOKENIZED_FILE_EXTENSION = "json";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private PropertyChangeSupport propertyChangeSupport;

	private GitUser user;
	private GitProjectHandler gitProjectHandler;
	private ProjectReference projectReference;
	private String rootRevisionHash;
	private GraphProjectHandler graphProjectHandler;
	private String tempDir;
	private BackgroundService backgroundService;

	private boolean tagManagerListenersEnabled = true;

	private IDGenerator idGenerator = new IDGenerator();
	private TagManager tagManager;
	private PropertyChangeListener tagsetDefinitionChangedListener;
	private PropertyChangeListener tagDefinitionChangedListener;
	private PropertyChangeListener userDefinedPropertyChangedListener;
	private GraphProjectIndexer indexer;

	public GraphWorktreeProject(GitUser user,
								GitProjectHandler gitProjectHandler,
								ProjectReference projectReference,
								TagManager tagManager,
								BackgroundService backgroundService) {
		this.user = user;
		this.gitProjectHandler = gitProjectHandler;
		this.projectReference = projectReference;
		this.tagManager = tagManager;
		this.backgroundService = backgroundService;
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		this.graphProjectHandler = 
			new GraphProjectHandler(
				this.projectReference, 
				this.user,
				new FileInfoProvider() {
					
					@Override
					public Path getTokenizedSourceDocumentPath(String documentId) throws Exception {
						return GraphWorktreeProject.this.getTokenizedSourceDocumentPath(documentId);
					}
					
					@Override
					public URI getSourceDocumentFileURI(String documentId) throws Exception {
						return getSourceDocumentURI(documentId);
					}
				});
		this.tempDir = RepositoryPropertyKey.TempDir.getValue();
		this.indexer = new GraphProjectIndexer(user, projectReference, () -> this.rootRevisionHash);
	}
	
	private Path getTokenizedSourceDocumentPath(String documentId) {
		return Paths
			.get(RepositoryPropertyKey.GraphDbGitMountBasePath.getValue())
			.resolve(gitProjectHandler.getSourceDocumentSubmodulePath(documentId))
			.resolve(documentId + "." + TOKENIZED_FILE_EXTENSION);
	}

	@Override
	public Indexer getIndexer() {
		return indexer;
	}
	
	@Override
	public void open(OpenProjectListener openProjectListener) {
		try {
			
			this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
			graphProjectHandler.ensureProjectRevisionIsLoaded(
					rootRevisionHash,
					() -> gitProjectHandler.getSourceDocumentStream(),
					() -> gitProjectHandler.getUserMarkupCollectionReferenceStream());
			
			tagManager.load(graphProjectHandler.getTagsets(this.rootRevisionHash));
			
			initTagManagerListeners();
			openProjectListener.ready(this);
		}
		catch(Exception e) {
			openProjectListener.failure(e);
		}
	}

	private void initTagManagerListeners() {
		tagsetDefinitionChangedListener = new PropertyChangeListener() {
			
			public void propertyChange(final PropertyChangeEvent evt) {
				
				if (!tagManagerListenersEnabled) {
					return;
				}
				try {
					if (evt.getOldValue() == null) { //insert
						final TagsetDefinition tagsetDefinition = 
								(TagsetDefinition)evt.getNewValue();
						
						addTagsetDefinition(tagsetDefinition);
					}
					else if (evt.getNewValue() == null) { //delete
						final TagsetDefinition tagsetDefinition = 
							(TagsetDefinition)evt.getOldValue();
						//TODO:
//						execShield.execute(new DBOperation<Void>() {
//							public Void execute() throws Exception {
//								dbTagLibraryHandler.removeTagsetDefinition(args.getSecond());
//								return null;
//							}
//						});
					}
					else { //update //TODO
//						execShield.execute(new DBOperation<Void>() {
//							public Void execute() throws Exception {
//								dbTagLibraryHandler.updateTagsetDefinition(
//										(TagsetDefinition)evt.getNewValue());
//								return null;
//							}
//						});
					}
				}
				catch (Exception e) {
					propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							e);	
				}
			}
		};
		
		tagManager.addPropertyChangeListener(
				TagManagerEvent.tagsetDefinitionChanged,
				tagsetDefinitionChangedListener);
		
		tagDefinitionChangedListener = new PropertyChangeListener() {
			
			public void propertyChange(final PropertyChangeEvent evt) {
				
				if (!tagManagerListenersEnabled) {
					return;
				}
				try {
					if (evt.getOldValue() == null) {
						@SuppressWarnings("unchecked")
						final Pair<TagsetDefinition, TagDefinition> args = 
								(Pair<TagsetDefinition, TagDefinition>)evt.getNewValue();
						TagsetDefinition tagsetDefinition = args.getFirst();
						TagDefinition tagDefinition = args.getSecond();
						addTagDefinition(tagDefinition, tagsetDefinition);
					}
					else if (evt.getNewValue() == null) {
						@SuppressWarnings("unchecked")
						final Pair<TagsetDefinition, TagDefinition> args = 
							(Pair<TagsetDefinition, TagDefinition>)evt.getOldValue();
						TagsetDefinition tagsetDefinition = args.getFirst();
						TagDefinition tagDefinition = args.getSecond();
						removeTagDefinition(tagDefinition, tagsetDefinition);
					}
					else {
						TagDefinition tag = (TagDefinition) evt.getNewValue();
						TagsetDefinition tagset = (TagsetDefinition) evt.getOldValue();
						
						updateTagDefinition(tag, tagset);
					}
				}
				catch (Exception e) {
					propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							e);				
				}
			}

		};
		
		tagManager.addPropertyChangeListener(
				TagManagerEvent.tagDefinitionChanged,
				tagDefinitionChangedListener);	
		
		
		userDefinedPropertyChangedListener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				
				if (!tagManagerListenersEnabled) {
					return;
				}
				Object oldValue = evt.getOldValue();
				Object newValue = evt.getNewValue();
				try {
					if (oldValue == null) { // insert
						
						@SuppressWarnings("unchecked")
						Pair<PropertyDefinition, TagDefinition> newPair = 
								(Pair<PropertyDefinition, TagDefinition>)newValue;
						
						PropertyDefinition propertyDefinition = newPair.getFirst();
						TagDefinition tagDefinition = newPair.getSecond();
						
						addPropertyDefinition(propertyDefinition, tagDefinition);
					}
					else if (newValue == null) { // delete
						@SuppressWarnings("unchecked")
						Pair<PropertyDefinition, Pair<TagDefinition, TagsetDefinition>> oldPair = 
								(Pair<PropertyDefinition, Pair<TagDefinition, TagsetDefinition>>)oldValue;
						PropertyDefinition propertyDefinition = oldPair.getFirst();
						TagDefinition tagDefinition = oldPair.getSecond().getFirst();
						TagsetDefinition tagsetDefinition = oldPair.getSecond().getSecond();
						
						removePropertyDefinition(propertyDefinition, tagDefinition, tagsetDefinition);
					}
					else { // update
						PropertyDefinition propertyDefinition = (PropertyDefinition)evt.getNewValue();
						TagDefinition tagDefinition = (TagDefinition)evt.getOldValue();
						updatePropertyDefinition(propertyDefinition, tagDefinition);
					}
				}
				catch (Exception e) {
					propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							e);				
				}				
			}
		};
		
		tagManager.addPropertyChangeListener(
				TagManagerEvent.userPropertyDefinitionChanged,
				userDefinedPropertyChangedListener);
	}

	private void addPropertyDefinition(PropertyDefinition propertyDefinition, TagDefinition tagDefinition) throws Exception {
		
		TagsetDefinition tagsetDefinition = 
			tagManager.getTagLibrary().getTagsetDefinition(tagDefinition);
		
		String tagsetRevision = gitProjectHandler.createOrUpdateTag(tagsetDefinition.getUuid(), tagDefinition);
		tagsetDefinition.setRevisionHash(tagsetRevision);
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		graphProjectHandler.addPropertyDefinition(
			rootRevisionHash, propertyDefinition, tagDefinition, tagsetDefinition, oldRootRevisionHash);
	}

	private void removePropertyDefinition(
			PropertyDefinition propertyDefinition, TagDefinition tagDefinition, TagsetDefinition tagsetDefinition) throws Exception {
		
		String tagsetRevision = gitProjectHandler.removePropertyDefinition(
				propertyDefinition, tagDefinition, tagsetDefinition);
		
		tagsetDefinition.setRevisionHash(tagsetRevision);
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		
		graphProjectHandler.removePropertyDefinition(
			rootRevisionHash, propertyDefinition, tagDefinition, tagsetDefinition, oldRootRevisionHash);
	}

	private void updatePropertyDefinition(PropertyDefinition propertyDefinition, TagDefinition tagDefinition) throws Exception {
		
		TagsetDefinition tagsetDefinition = 
			tagManager.getTagLibrary().getTagsetDefinition(tagDefinition);
		
		String tagsetRevision = gitProjectHandler.createOrUpdateTag(tagsetDefinition.getUuid(), tagDefinition);
		tagsetDefinition.setRevisionHash(tagsetRevision);
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		graphProjectHandler.createOrUpdatePropertyDefinition(
			rootRevisionHash, propertyDefinition, tagDefinition, tagsetDefinition, oldRootRevisionHash);
	}

	private void addTagDefinition(TagDefinition tagDefinition, TagsetDefinition tagsetDefinition) throws Exception {
		String tagsetRevision = 
			gitProjectHandler.createOrUpdateTag(tagsetDefinition.getUuid(), tagDefinition);
		tagsetDefinition.setRevisionHash(tagsetRevision);
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		graphProjectHandler.addTagDefinition(
				rootRevisionHash, tagDefinition, tagsetDefinition, oldRootRevisionHash);
	}

	private void updateTagDefinition(TagDefinition tagDefinition, TagsetDefinition tagsetDefinition) throws Exception {
		String tagsetRevision = gitProjectHandler.createOrUpdateTag(tagsetDefinition.getUuid(), tagDefinition);
		tagsetDefinition.setRevisionHash(tagsetRevision);
		
		//TODO: update and commit annotations, commit project
		
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		graphProjectHandler.updateTagDefinition(
				rootRevisionHash, tagDefinition, tagsetDefinition, oldRootRevisionHash);
	}
	
	private void removeTagDefinition(TagDefinition tagDefinition, TagsetDefinition tagsetDefinition) throws Exception {

		// remove Annotations
		Multimap<String, String> annotationIdsByCollectionId =
			graphProjectHandler.getAnnotationIdsByCollectionId(this.rootRevisionHash, tagDefinition);
		
		for (String collectionId : annotationIdsByCollectionId.keySet()) {
			// TODO: check permissions if commit is allowed, if that is not the case skip git removal
			String collectionRevisionHash = gitProjectHandler.removeTagInstances(
				collectionId, annotationIdsByCollectionId.get(collectionId), 
				String.format("Annotations removed, caused by the removal of Tag %1$s ", 
						tagDefinition.toString()));
			
			graphProjectHandler.removeTagInstances(
				this.rootRevisionHash, collectionId,
				annotationIdsByCollectionId.get(collectionId), 
				collectionRevisionHash);
		}
		
		// remove Tag
		String tagsetRevision = gitProjectHandler.removeTag(tagsetDefinition, tagDefinition);
		tagsetDefinition.setRevisionHash(tagsetRevision);

		graphProjectHandler.removeTagDefinition(
				rootRevisionHash, tagDefinition, tagsetDefinition);
			
		// commit Project
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = 
				gitProjectHandler.commitProject(String.format("Tag removed %1$s ", 
						tagDefinition.toString()), true);
		
		graphProjectHandler.updateProjectRevisionHash(oldRootRevisionHash, this.rootRevisionHash);
		
		for (String collectionId : annotationIdsByCollectionId.keySet()) {
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.tagReferencesChanged.name(), 
					new Pair<>(collectionId, annotationIdsByCollectionId.get(collectionId)), null);
		}

	}
	
	private void addTagsetDefinition(TagsetDefinition tagsetDefinition) throws Exception {
		String tagsetRevisionHash = 
			gitProjectHandler.createTagset(
				tagsetDefinition.getUuid(), tagsetDefinition.getName(), "");//TODO:
		
		tagsetDefinition.setRevisionHash(tagsetRevisionHash);
		
		String oldRootRevisionHash = this.rootRevisionHash;
		this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
		
		graphProjectHandler.addTagset(
			rootRevisionHash, 
			tagsetDefinition,
			oldRootRevisionHash); 
	}

	@Override
	public void reload() throws IOException {
		// TODO Auto-generated method stub 

	}

	@Override
	public void close() {
		try {
//			synchTagInstancesToGit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addPropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent,
			PropertyChangeListener propertyChangeListener) {
		this.propertyChangeSupport.addPropertyChangeListener(
				propertyChangeEvent.name(), propertyChangeListener);
	}
	
	@Override
	public void removePropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent,
			PropertyChangeListener propertyChangeListener) {
		this.propertyChangeSupport.removePropertyChangeListener(
				propertyChangeEvent.name(), propertyChangeListener);
	}

	@Override
	public String getName() {
		return projectReference.getName();
	}

	@Override
	public String getProjectId() {
		return projectReference.getProjectId();
	}

	@Override
	public String getIdFromURI(URI uri) {
		// TODO: is this really necessary?
		if (uri.getScheme().toLowerCase().equals("file")) {
			File file = new File(uri);
			return file.getName();
		}
		else {
			return new IDGenerator().generate();
		}
	}

	@Override
	public String getFileURL(String sourceDocumentID, String... path) {
		return null; //TODO
	}
	
	private URI getSourceDocumentURI(String sourceDocumentId) {
		return Paths
		.get(new File(RepositoryPropertyKey.GitBasedRepositoryBasePath.getValue()).toURI())
		.resolve(gitProjectHandler.getSourceDocumentSubmodulePath(sourceDocumentId))
		.resolve(sourceDocumentId + "." + UTF8_CONVERSION_FILE_EXTENSION)
		.toUri();
	}

	@Override
	public void insert(SourceDocument sourceDocument) throws IOException {
		try {
			File sourceTempFile = Paths.get(new File(this.tempDir).toURI()).resolve(sourceDocument.getID()).toFile();
	
			String convertedFilename = 
					sourceDocument.getID() + "." + UTF8_CONVERSION_FILE_EXTENSION;
			
			final IndexBufferManager indexBufferManager = 
					IndexBufferManagerName.INDEXBUFFERMANAGER.getIndeBufferManager();
	
			logger.info("start tokenizing sourcedocument");
			
			List<String> unseparableCharacterSequences = 
					sourceDocument.getSourceContentHandler().getSourceDocumentInfo()
					.getIndexInfoSet().getUnseparableCharacterSequences();
			List<Character> userDefinedSeparatingCharacters = 
					sourceDocument.getSourceContentHandler().getSourceDocumentInfo()
					.getIndexInfoSet().getUserDefinedSeparatingCharacters();
			Locale locale = 
					sourceDocument.getSourceContentHandler().getSourceDocumentInfo()
					.getIndexInfoSet().getLocale();
			
			TermExtractor termExtractor = 
					new TermExtractor(
							sourceDocument.getContent(), 
							unseparableCharacterSequences, 
							userDefinedSeparatingCharacters, 
							locale);
			
			final Map<String, List<TermInfo>> terms = termExtractor.getTerms();
			
			logger.info("tokenization finished");
			
			
			indexBufferManager.add(sourceDocument, terms);
			
			logger.info("buffering tokens finished");	
			
			try (FileInputStream originalFileInputStream = new FileInputStream(sourceTempFile)) {
				
				String sourceDocRevisionHash = gitProjectHandler.createSourceDocument(
					sourceDocument.getID(), 
					originalFileInputStream,
					sourceDocument.getID() 
						+ ORIG_INFIX 
						+ "." 
						+ sourceDocument
							.getSourceContentHandler()
							.getSourceDocumentInfo()
							.getTechInfoSet()
							.getFileType()
							.getDefaultExtension(),
					new ByteArrayInputStream(
						sourceDocument.getContent().getBytes(Charset.forName("UTF-8"))), 
					convertedFilename, 
					terms,
					sourceDocument.getID() + "." + TOKENIZED_FILE_EXTENSION,
					sourceDocument.getSourceContentHandler().getSourceDocumentInfo());
	
				sourceDocument.unload();
				sourceDocument.setRevisionHash(sourceDocRevisionHash);
			}
			
			sourceTempFile.delete();

			String oldRootRevisionHash = this.rootRevisionHash;
			this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();
	
			graphProjectHandler.addSourceDocument(
				oldRootRevisionHash, this.rootRevisionHash,
				sourceDocument,
				getTokenizedSourceDocumentPath(sourceDocument.getID()));
			
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.sourceDocumentChanged.name(),
					null, sourceDocument.getID());

		}
		catch (Exception e) {
			e.printStackTrace();
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.exceptionOccurred.name(),
					null, e);
		}
	}

	@Override
	public void update(SourceDocument sourceDocument, ContentInfoSet contentInfoSet) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<TagsetDefinition> getTagsets() throws Exception {
		return graphProjectHandler.getTagsets( this.rootRevisionHash);
	}

	@Override
	public int getTagsetsCount() throws Exception {
		return graphProjectHandler.getTagsetsCount( this.rootRevisionHash);
	}


	@Override
	public Collection<SourceDocument> getSourceDocuments() throws Exception {
		return graphProjectHandler.getSourceDocuments( this.rootRevisionHash);
	}

	@Override
	public int getSourceDocumentsCount() throws Exception {
		return graphProjectHandler.getSourceDocumentsCount( this.rootRevisionHash);
	}

	@Override
	public SourceDocument getSourceDocument(String sourceDocumentId) throws Exception {
		return graphProjectHandler.getSourceDocument(this.rootRevisionHash, sourceDocumentId);
	}

	@Override
	public void delete(SourceDocument sourceDocument) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public SourceDocument getSourceDocument(UserMarkupCollectionReference umcRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public void share(SourceDocument sourceDocument, String userIdentification, AccessMode accessMode)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Deprecated
	@Override
	public Collection<Corpus> getCorpora() {
		return null;
	}

	@Deprecated
	@Override
	public void createCorpus(String name) throws IOException {
	}

	@Deprecated
	@Override
	public void update(Corpus corpus, SourceDocument sourceDocument) throws IOException {
	}

	@Deprecated
	@Override
	public void update(Corpus corpus, UserMarkupCollectionReference userMarkupCollectionReference) throws IOException {
	}

	@Deprecated
	@Override
	public void delete(Corpus corpus) throws IOException {
	}

	@Deprecated
	@Override
	public void update(Corpus corpus, String name) throws IOException {
	}

	@Deprecated
	@Override
	public void share(Corpus corpus, String userIdentification, AccessMode accessMode) throws IOException {
	}

	@Override
	public void createUserMarkupCollection(String name, SourceDocument sourceDocument) {
		try {
			String collectionId = idGenerator.generate();
			
			String umcRevisionHash = gitProjectHandler.createMarkupCollection(
						collectionId, 
						name, 
						null, //description
						sourceDocument.getID(), 
						sourceDocument.getRevisionHash());
			
			String oldRootRevisionHash = this.rootRevisionHash;
			this.rootRevisionHash = gitProjectHandler.getRootRevisionHash();

			graphProjectHandler.addUserMarkupCollection(
				rootRevisionHash, 
				collectionId, name, umcRevisionHash, 
				sourceDocument, oldRootRevisionHash);
			
			UserMarkupCollectionReference reference = 
					new UserMarkupCollectionReference(
							collectionId, 
							umcRevisionHash,
							new ContentInfoSet(name),
							sourceDocument.getID(),
							sourceDocument
							.getSourceContentHandler().getSourceDocumentInfo()
							.getContentInfoSet().getTitle());
			
			sourceDocument.addUserMarkupCollectionReference(reference);
			
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.userMarkupCollectionChanged.name(),
					null, new Pair<UserMarkupCollectionReference, SourceDocument>(
							reference,sourceDocument));
		}
		catch (Exception e) {
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.exceptionOccurred.name(),
					null, e);
		}
	}
	
	@Override
	public List<UserMarkupCollectionReference> getUserMarkupCollectionReferences(int offset, int limit) throws Exception {
		return graphProjectHandler.getUserMarkupCollectionReferences(rootRevisionHash, offset, limit);
	}
	
	@Override
	public int getUserMarkupCollectionReferenceCount() throws Exception {
		return graphProjectHandler.getUserMarkupCollectionReferenceCount(rootRevisionHash);
	}

	@Override
	public void importUserMarkupCollection(InputStream inputStream, SourceDocument sourceDocument) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void importUserMarkupCollection(InputStream inputStream, SourceDocument sourceDocument,
			UserMarkupCollectionSerializationHandler userMarkupCollectionSerializationHandler) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public UserMarkupCollection getUserMarkupCollection(UserMarkupCollectionReference userMarkupCollectionReference)
			throws IOException {
		try {
			return graphProjectHandler.getUserMarkupCollection(rootRevisionHash, getTagLibrary(), userMarkupCollectionReference);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public UserMarkupCollection getUserMarkupCollection(UserMarkupCollectionReference userMarkupCollectionReference,
			boolean refresh) throws IOException {
		return getUserMarkupCollection(userMarkupCollectionReference);
	}

	@Override
	public void update(UserMarkupCollection userMarkupCollection, List<TagReference> tagReferences) {
		try {
			if (userMarkupCollection.getTagReferences().containsAll(
					tagReferences)) {
				gitProjectHandler.addOrUpdate(
						userMarkupCollection.getUuid(), tagReferences);
				graphProjectHandler.addTagReferences(
						GraphWorktreeProject.this.rootRevisionHash, userMarkupCollection, tagReferences);
				propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.tagReferencesChanged.name(), 
						null, new Pair<>(userMarkupCollection, tagReferences));
			}
			else {
				graphProjectHandler.removeTagReferences(
					GraphWorktreeProject.this.rootRevisionHash, userMarkupCollection, tagReferences);

				Collection<String> tagInstanceIds = tagReferences
						.stream()
						.map(tr -> tr.getTagInstanceID())
						.collect(Collectors.toSet());
				
				for (String tagInstanceId : tagInstanceIds) {
					gitProjectHandler.removeTagInstance(
							userMarkupCollection.getUuid(), tagInstanceId);
				}
				propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.tagReferencesChanged.name(), 
						new Pair<>(userMarkupCollection.getUuid(), tagInstanceIds), null);
			}
		}
		catch (Exception e) {
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.exceptionOccurred.name(),
					null, 
					e);				
		}
	}

	@Override
	public void update(
			UserMarkupCollection userMarkupCollection, 
			TagInstance tagInstance, Collection<Property> properties) throws IOException {
		try {
			gitProjectHandler.addOrUpdate(
					userMarkupCollection.getUuid(), userMarkupCollection.getTagReferences(tagInstance));
			graphProjectHandler.updateProperties(
					GraphWorktreeProject.this.rootRevisionHash, tagInstance, properties);
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.propertyValueChanged.name(),
					tagInstance, properties);
		}
		catch (Exception e) {
			propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.exceptionOccurred.name(),
					null, 
					e);				
		}

	}

	@Override
	public void update(List<UserMarkupCollection> userMarkupCollections, TagsetDefinition tagsetDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(UserMarkupCollectionReference userMarkupCollectionReference, ContentInfoSet contentInfoSet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(UserMarkupCollectionReference userMarkupCollectionReference) throws IOException {
		// TODO Auto-generated method stub

	}

	@Deprecated
	@Override
	public void share(UserMarkupCollectionReference userMarkupCollectionRef, String userIdentification,
			AccessMode accessMode) throws IOException {
	}

	@Override
	public List<UserMarkupCollectionReference> getWritableUserMarkupCollectionRefs(SourceDocument sd)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void createTagLibrary(String name) throws IOException {

	}

	@Override
	@Deprecated
	public void importTagLibrary(InputStream inputStream) throws IOException {

	}

	@Override
	@Deprecated
	public Collection<TagLibraryReference> getTagLibraryReferences() {

		return Collections.emptyList();
	}

	@Deprecated
	private TagLibrary getTagLibrary() {
		return tagManager.getTagLibrary();
	}
	
	@Override
	@Deprecated
	public TagLibrary getTagLibrary(TagLibraryReference tagLibraryReference) throws IOException {
		return getTagLibrary();
	}

	@Override
	@Deprecated
	public void delete(TagLibraryReference tagLibraryReference) throws IOException {
	}

	@Deprecated
	@Override
	public void share(TagLibraryReference tagLibraryReference, String userIdentification, AccessMode accessMode)
			throws IOException {
	}

	@Override
	public boolean isAuthenticationRequired() {
		// TODO
		return false;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public TagManager getTagManager() {
		return tagManager;
	}

	@Override
	public File getFile(SourceDocument sourceDocument) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public int getNewUserMarkupCollectionRefs(Corpus corpus) {
		return 0;
	}

	@Override
	public void spawnContentFrom(String userIdentifier, boolean copyCorpora, boolean copyTagLibs) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public TagLibrary getTagLibraryFor(String uuid, Version version) throws IOException {
		return null;
	}

	@Override
	public User createIfAbsent(Map<String, String> userIdentification) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void synchTagInstancesToGit() throws Exception {
		graphProjectHandler.synchTagInstanceToGit(
			this.rootRevisionHash,
			new TagInstanceSynchHandler() {
				
				@Override
				public void synch(String collectionId, String deletedTagInstanceId) throws Exception {
					gitProjectHandler.removeTagInstance(collectionId, deletedTagInstanceId);
				}
				
				@Override
				public void synch(String collectionId, List<TagReference> tagReferences) throws Exception {
					gitProjectHandler.addOrUpdate(collectionId, tagReferences);
				}
			}
		);
	}
	
	@Override
	public void addAndCommitChanges(UserMarkupCollectionReference ref) throws Exception {
		gitProjectHandler.addAndCommitChanges(ref);
		graphProjectHandler.updateCollectionRevisionHash(this.rootRevisionHash, ref);
	}
	
	@Override
	public List<User> getProjectMembers() throws Exception {
		return gitProjectHandler.getProjectMembers();
	}

}
