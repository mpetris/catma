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
package de.catma.queryengine;

import java.util.List;

import de.catma.document.source.SourceDocument;
import de.catma.indexer.Indexer;
import de.catma.indexer.WildcardTermExtractor;
import de.catma.project.Project;
import de.catma.queryengine.result.QueryResult;
import de.catma.queryengine.result.QueryResultRow;

public class WildcardQuery extends Query {

	private String wildcardPhrase;
	
	public WildcardQuery(Phrase phraseQuery) {
		wildcardPhrase = phraseQuery.getPhrase();
	}
	
	@Override
	protected QueryResult execute() throws Exception {
    	QueryOptions queryOptions = getQueryOptions();
        WildcardTermExtractor termExtractor =
        		new WildcardTermExtractor(
        				wildcardPhrase,
        				queryOptions.getUnseparableCharacterSequences(),
        				queryOptions.getUserDefinedSeparatingCharacters(),
        				queryOptions.getLocale());
        
        List<String> termList =  termExtractor.getOrderedTerms();
        		
        Indexer indexer = queryOptions.getIndexer();
        
        QueryResult result = indexer.searchWildcardPhrase(
        	queryOptions.getQueryId(),
        	queryOptions.getRelevantSourceDocumentIDs(), termList,
        	queryOptions.getLimit());
        
        Project repository = queryOptions.getRepository();
    	
    	
        for (QueryResultRow row  : result) {
        	SourceDocument sd = repository.getSourceDocument(row.getSourceDocumentId());
        	
        	row.setPhrase(sd.getContent(row.getRange()));
        }
        
        return result;
	}
}
