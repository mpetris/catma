package de.catma.ui.analyzenew;

import de.catma.ui.layout.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.Grid.SelectionMode;
import de.catma.document.repository.Repository;
import de.catma.document.source.KeywordInContext;
import de.catma.document.source.SourceDocument;
import de.catma.indexer.KwicProvider;
import de.catma.queryengine.result.QueryResultRow;
import de.catma.queryengine.result.TagQueryResultRow;
import de.catma.ui.analyzenew.treehelper.TreeRowItem;
import de.catma.ui.analyzer.RelevantUserMarkupCollectionProvider;
import de.catma.ui.component.actiongrid.ActionGridComponent;

public class KwicPanelNew extends VerticalLayout{
	
		private enum KwicPropertyName {
			caption,
			leftContext,
			keyword,
			rightContext, 
			startPoint,
			endPoint,
			tagtype,
			propertyname,
			propertyvalue,
			;
		}

		private Repository repository;
		private Grid<KwicItem> kwicGrid;
		private RelevantUserMarkupCollectionProvider relevantUserMarkupCollectionProvider;
		private WeakHashMap<Object, Boolean> itemDirCache = new WeakHashMap<>();
		private int kwicSize = 5;
		private List<KwicItem> kwicItemList;
		private boolean showPropertyColumns;
		private ActionGridComponent<Grid<KwicItem>> kwicGridComponent;
		
		
		public KwicPanelNew(Repository repository) { 
			this.repository = repository;
			
			initComponents();
			initActions();
			setHeight("100%");
				
		}
		public KwicPanelNew(Repository repository, 
				RelevantUserMarkupCollectionProvider relevantUserMarkupCollectionProvider) {
			this(repository, relevantUserMarkupCollectionProvider,  false);
		}
		
		public KwicPanelNew(
				Repository repository, 
				RelevantUserMarkupCollectionProvider relevantUserMarkupCollectionProvider, 
				boolean markupBased) {
			this.repository = repository;
			this.relevantUserMarkupCollectionProvider = relevantUserMarkupCollectionProvider;
	
			
			initComponents();
			initActions();

		}
		


			private void initActions() {
/*		    	kwicGrid.addItemClickListener(itemClickEvent -> handleResourceItemClick(itemClickEvent));
		    	
		        ContextMenu addContextMenu = 
		        	documentsGridComponent.getActionGridBar().getBtnAddContextMenu();
		        addContextMenu.addItem("Annotate selected rows", clickEvent -> handleAddDocumentRequest());
		        addContextMenu.addItem("Annotate single rows", e -> handleAddCollectionRequest());

		        ContextMenu documentsGridMoreOptionsContextMenu = 
		        	documentsGridComponent.getActionGridBar().getBtnMoreOptionsContextMenu();
		        documentsGridMoreOptionsContextMenu.addItem(
		            	"Edit documents / collections",(menuItem) -> handleEditResources());
		        documentsGridMoreOptionsContextMenu.addItem(
		        	"Delete documents / collections",(menuItem) -> handleDeleteResources(menuItem, resourceGrid));
		        documentsGridMoreOptionsContextMenu.addItem(
		            	"Analyze documents / collections",(menuItem) -> handleAnalyzeResources(menuItem, resourceGrid));
		        
		        tagsetsGridComponent.getActionGridBar().addBtnAddClickListener(
		        	click -> handleAddTagsetRequest());*/
		        
		        ContextMenu moreOptionsMenu = 
		        	kwicGridComponent.getActionGridBar().getBtnMoreOptionsContextMenu();
		        moreOptionsMenu.addItem("Annotate all selected rows", clickEvent -> handleAnnotateAllRequest());
		        moreOptionsMenu.addItem("Annotate single row", clickEvent -> handleAnnotateSingleRequest());
		 
		        ContextMenu searchMenu =  kwicGridComponent.getActionGridBar().getBtnSearchContextMenu();
		        
			}
			
			private void handleAnnotateAllRequest() {
				
			}
			
			private void handleAnnotateSingleRequest(){
				
			}
		
			
		
		
		private void initComponents() {
		//	addStyleName("analyze_kwic_panel");
	
			kwicGrid= new Grid<KwicItem>();
	    	kwicItemList= new ArrayList<>(); 
			//kwicGrid.setSelectionMode(SelectionMode.MULTI);
			//kwicGrid.setSelectionMode(SelectionMode.SINGLE);
			kwicGrid.setWidth("100%");
			kwicGrid.setHeight("100%");
			
			kwicGrid.addColumn(KwicItem::getDocCollection).setCaption("Document/Collection").setId("docCollectionID");
			kwicGrid.addColumn(KwicItem::getBackwardContext).setCaption("Left Context").setId("backwardID");
			
			kwicGrid.addColumn(KwicItem::getShortenKeyWord).setCaption("KeyWord").setId("keyWordID");
			kwicGrid.getColumn("keyWordID").setDescriptionGenerator(e -> e.getKeyWord(), ContentMode.HTML);
			
			kwicGrid.addColumn(KwicItem::getForewardContext).setCaption("Right Context").setId("forewardID");
			kwicGrid.addColumn(KwicItem::getRangeStartPoint).setCaption("Start Point").setId("startPointID");
			kwicGrid.addColumn(KwicItem::getRangeEndPoint).setCaption("End Point").setId("endPointID");
			
			kwicGrid.addColumn(KwicItem::getShortenTagDefinitionPath).setCaption("Tag").setId("tagID");
			kwicGrid.getColumn("tagID").setDescriptionGenerator(e -> e.getTagDefinitionPath(), ContentMode.HTML);
			
			kwicGrid.addStyleNames("analyze_kwic_grid");
			
	    	 kwicGridComponent = new ActionGridComponent<>(
					new Label("key word in context visualization"), kwicGrid);
			addComponent(kwicGridComponent);
			
			//addComponent(kwicGrid);
			
		}
		
	public void addQueryResultRows(Iterable<QueryResultRow> queryResult) throws Exception {
		
		if (kwicGrid.getColumn("propValueID") != null) {
			kwicGrid.removeColumn("propValueID");
			kwicGrid.removeColumn("propNameID");
		}
		
		HashMap<String, KwicProvider> kwicProviders = new HashMap<String, KwicProvider>();
		kwicItemList.removeAll(kwicItemList);

		showPropertyColumns = false;
		boolean markupBased = true;

		for (QueryResultRow row : queryResult) {

			SourceDocument sourceDocument = repository.getSourceDocument(row.getSourceDocumentId());

			if (!kwicProviders.containsKey(sourceDocument.getID())) {
				kwicProviders.put(sourceDocument.getID(), new KwicProvider(sourceDocument));
			}

			KwicProvider kwicProvider = kwicProviders.get(sourceDocument.getID());
			KeywordInContext kwic = kwicProvider.getKwic(row.getRange(), kwicSize);
			String sourceDocOrMarkupCollectionDisplay = sourceDocument.toString();

			if (markupBased && (row instanceof TagQueryResultRow)) {
				sourceDocOrMarkupCollectionDisplay = sourceDocument
						.getUserMarkupCollectionReference(((TagQueryResultRow) row).getMarkupCollectionId()).getName();
			}

			itemDirCache.put(row, kwic.isRightToLeft());

			KwicItem kwicItem = createKwicItemFromQueryResultRow(row, kwic, showPropertyColumns);

			kwicItemList.add(kwicItem);
		}
		if (showPropertyColumns) {
			kwicGrid.addColumn(KwicItem::getPropertyName).setCaption("Property Name").setId("propNameID");
			kwicGrid.addColumn(KwicItem::getPropertyValue).setCaption("Property Value").setId("propValueID");
		}
		kwicGrid.setItems(kwicItemList);
	}
		

		
	private KwicItem createKwicItemFromQueryResultRow(QueryResultRow queryResultRow, KeywordInContext kwic, boolean showPropertyColumns ) throws Exception {
		KwicItem kwicItem = new KwicItem();

		if(queryResultRow instanceof TagQueryResultRow) {
			
			TagQueryResultRow tagQueryResultRow = (TagQueryResultRow) queryResultRow;
			
		
	     	 SourceDocument sourceDoc = repository.getSourceDocument(queryResultRow.getSourceDocumentId());
			 kwicItem.setDocCollection(sourceDoc
						.getUserMarkupCollectionReference(tagQueryResultRow.getMarkupCollectionId()).getName());
			
		
			kwicItem.setKeyWord(tagQueryResultRow.getPhrase());
			kwicItem.setBackwardContext(kwic.getBackwardContext());
			kwicItem.setForewardContext(kwic.getForwardContext());
			kwicItem.setRangeStartPoint(queryResultRow.getRange().getStartPoint());
			kwicItem.setRangeEndPoint(queryResultRow.getRange().getEndPoint());
			kwicItem.setTagDefinitionPath(tagQueryResultRow.getTagDefinitionPath());
			
			if(tagQueryResultRow.getPropertyName()!=null) {
				
			this.showPropertyColumns = true;
				kwicItem.setPropertyName(tagQueryResultRow.getPropertyName());
				kwicItem.setPropertyValue(tagQueryResultRow.getPropertyValue());
						
			}

			return kwicItem;
			
		}else {
			
			String sourceDocName=repository.getSourceDocument(queryResultRow.getSourceDocumentId()).toString();
			
			kwicItem.setDocCollection(sourceDocName);
			kwicItem.setKeyWord(queryResultRow.getPhrase());
			kwicItem.setBackwardContext(kwic.getBackwardContext());
			kwicItem.setForewardContext(kwic.getForwardContext());
			kwicItem.setRangeStartPoint(queryResultRow.getRange().getStartPoint());
			kwicItem.setRangeEndPoint(queryResultRow.getRange().getEndPoint());

			return kwicItem;
			
		}

	}
}


