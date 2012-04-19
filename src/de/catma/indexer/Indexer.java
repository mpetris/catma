package de.catma.indexer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.catma.core.document.Range;
import de.catma.core.document.source.SourceDocument;
import de.catma.core.document.standoffmarkup.usermarkup.TagReference;
import de.catma.core.tag.TagLibrary;
import de.catma.queryengine.QueryResultRowArray;

public interface Indexer {
	public void index(
			SourceDocument sourceDocument, 
			List<String> unseparableCharacterSequences,
            List<Character> userDefinedSeparatingCharacters,
            Locale locale) throws Exception;
	
	public void index(
			List<TagReference> tagReferences,
			String sourceDocumentID,
			String userMarkupCollectionID,
			TagLibrary tagLibrary) throws Exception;
	
	/**
	 * @param documentIdList
	 * @param termList
	 * @return a list of mappings documentIds->list of matching ranges 
	 */
	public Map<String,List<Range>> searchTerm(
			List<String> documentIdList, List<String> termList) throws Exception;
	
	
	/**
	 * @param tagPath
	 * @param isPrefixSearch
	 * @return a list of mappings documentIds->Pair of matching ranges and phrases
	 * @throws Exception
	 */
	public QueryResultRowArray searchTag(
			String tagPath, boolean isPrefixSearch) throws Exception;
	
	public void close();
}
