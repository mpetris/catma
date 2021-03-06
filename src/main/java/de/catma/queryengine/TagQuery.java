/*
 *    CATMA Computer Aided Text Markup and Analysis
 * 
 *    Copyright (C) 2009  University Of Hamburg
 * 
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 * 
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.catma.queryengine;

import java.util.List;

import de.catma.document.Range;
import de.catma.document.source.SourceDocument;
import de.catma.indexer.Indexer;
import de.catma.project.Project;
import de.catma.queryengine.result.QueryResult;
import de.catma.queryengine.result.QueryResultRow;
import de.catma.queryengine.result.QueryResultRowArray;
import de.catma.queryengine.result.TagQueryResultRow;

/**
 * A query for tagged tokens.
 *
 * @author Marco Petris <marco.petris@web.de>
 */
public class TagQuery extends Query {

    private String tagPhrase;

    /**
     * Constructor.
     * @param query the name of the {@link org.catma.tag.Tag}
     */
    public TagQuery(Phrase query) {
        this.tagPhrase = query.getPhrase();
    }

    @Override
    protected QueryResult execute() throws Exception {

    	QueryOptions queryOptions = getQueryOptions();
    	Project repository = queryOptions.getRepository();
    	
        Indexer indexer = queryOptions.getIndexer();
        List<String> relevantUserMarkupCollIDs = 
        		queryOptions.getRelevantUserMarkupCollIDs();
        	
    	if (relevantUserMarkupCollIDs.isEmpty()) {
    		return new QueryResultRowArray();
    	}
        
        QueryResult result = 
				indexer.searchTagDefinitionPath(
						queryOptions.getQueryId(),
						relevantUserMarkupCollIDs,
						tagPhrase);
        
        
        for (QueryResultRow row  : result) {
        	SourceDocument sd = 
        			repository.getSourceDocument(row.getSourceDocumentId());

        	TagQueryResultRow tRow = (TagQueryResultRow)row;
        	
        	if (tRow.getRanges().size() > 1) {
	        	StringBuilder builder = new StringBuilder();
	        	String conc = "";
	        	for (Range range : tRow.getRanges()) {
	        		builder.append(conc);
	        		builder.append(sd.getContent(range));
	        		conc = "[...]";
	        	}
	        	row.setPhrase(builder.toString());
        	}
        	else {
        		row.setPhrase(sd.getContent(row.getRange()));
        	}
        }

        return result;
    }

}

