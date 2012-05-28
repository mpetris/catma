package de.catma.serialization;

import de.catma.tag.TagManager;

public interface SerializationHandlerFactory {

	public SourceDocumentInfoSerializationHandler getSourceDocumentInfoSerializationHandler();
	public StaticMarkupCollectionSerializationHandler getStaticMarkupCollectionSerializationHandler();
	public TagLibrarySerializationHandler getTagLibrarySerializationHandler();
	public UserMarkupCollectionSerializationHandler getUserMarkupCollectionSerializationHandler();
	public void setTagManager(TagManager tagManager);
	
}
