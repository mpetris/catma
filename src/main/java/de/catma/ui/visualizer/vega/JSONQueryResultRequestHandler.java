package de.catma.ui.visualizer.vega;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

import de.catma.backgroundservice.LogProgressListener;
import de.catma.queryengine.QueryJob;
import de.catma.queryengine.result.QueryResult;
import de.catma.queryengine.result.json.JSONQueryResultBuilder;
import de.catma.ui.analyzer.QueryOptionsProvider;

public class JSONQueryResultRequestHandler implements RequestHandler {
	
	private QueryResult defaultQueryResult;
	private String queryResultUrlPath;
	private QueryOptionsProvider queryOptionsProvider;
	private String vegaViewIdPath;
	private String queryUrlPath;
	
	public JSONQueryResultRequestHandler(QueryResult queryResult, QueryOptionsProvider queryOptionsProvider, String queryResultUrlPath, String vegaViewId) {
		super();
		this.defaultQueryResult = queryResult;
		this.queryOptionsProvider = queryOptionsProvider;
		this.queryResultUrlPath = "/"+queryResultUrlPath.toLowerCase();
		this.vegaViewIdPath = "/"+vegaViewId.toLowerCase();
		this.queryUrlPath = vegaViewIdPath + "/query/"; 
	}

	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {
		
		if (request.getPathInfo().toLowerCase().startsWith(vegaViewIdPath)) {
			if (request.getPathInfo().toLowerCase().equals(queryResultUrlPath)) {

				writeResponse(defaultQueryResult, response);
				
				return true;
			}
			else if (request.getPathInfo().toLowerCase().startsWith(queryUrlPath)) {
				String pathInfo = request.getPathInfo();
				String encodedQuery = pathInfo.substring(queryUrlPath.length());
				String query = URLDecoder.decode(encodedQuery, "UTF-8");
				System.out.println("query: " + query);
				
				QueryJob queryJob = new QueryJob(query, queryOptionsProvider.getQueryOptions());
				queryJob.setProgressListener(new LogProgressListener());
				try {
					QueryResult queryResult = queryJob.call();
					
					writeResponse(queryResult, response);
					
					return true;
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}		
		return false;
	}

	private void writeResponse(QueryResult queryResult, VaadinResponse response) throws IOException {
		//TODO:
//		response.setContentType("json");
		OutputStream outputStream = response.getOutputStream();
		ArrayNode jsonValues = 
				new JSONQueryResultBuilder().createJSONQueryResult(
						queryResult, 
						queryOptionsProvider.getQueryOptions().getRepository());
		
		outputStream.write(jsonValues.toString().getBytes("UTF-8"));
	}

	

}
