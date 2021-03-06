package de.catma.ui.client.ui.tagger.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Table;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;

import de.catma.ui.client.ui.tagger.shared.AnnotationLayerBuilder;
import de.catma.ui.client.ui.tagger.shared.ClientComment;
import de.catma.ui.client.ui.tagger.shared.ClientCommentReply;
import de.catma.ui.client.ui.tagger.shared.ClientTagInstance;
import de.catma.ui.client.ui.tagger.shared.TextRange;

public class Line implements Comparable<Line> {
	
	private Element lineElement;
	private String lineId;
	private TextRange textRange;
	private Set<TextRange> tagInstanceTextRanges;
	private Set<TextRange> highlightedTextRanges;
	private Set<TextRange> selectedTextRanges;
	private Collection<ClientTagInstance> absoluteTagIntances;
	private String presentationContent;
	private List<ClientComment> comments;

	Line(Element lineElement) {
		super();
		this.lineElement = lineElement;
		
		this.tagInstanceTextRanges = new HashSet<TextRange>();
		this.highlightedTextRanges = new HashSet<TextRange>();
		this.selectedTextRanges = new HashSet<>();
		this.comments = new ArrayList<ClientComment>();
		
		makeLineFromLineNode();
	}
	
	private void makeLineFromLineNode() {
		lineId = lineElement.getId();
		
		Map<String,ClientTagInstance> absoluteTagIntancesByID = new HashMap<String, ClientTagInstance>();
		
		Element lineBodyElement = lineElement.getFirstChildElement();
		
		for (int layerIdx = 0; layerIdx < lineBodyElement.getChildCount(); layerIdx++) {
			Node layerNode = lineBodyElement.getChild(layerIdx);
			if (Element.is(layerNode)) {
				Element layerElement = Element.as(layerNode);
				
				if (layerElement.hasClassName("tagger-display-layer")) {
					handleDisplayLayer(layerElement);
				}
				else if (layerElement.hasClassName("annotation-layer")) {
					handleAnnotationLayer(layerElement, absoluteTagIntancesByID);
				}
				else if (layerElement.hasClassName("highlight-layer")) {
					handleHighlightLayer(layerElement);
				}
			}
		}
		
		for (ClientTagInstance tagInstance : absoluteTagIntancesByID.values()) {
			tagInstanceTextRanges.addAll(tagInstance.getRanges());
		}
		
		this.absoluteTagIntances = new HashSet<>(absoluteTagIntancesByID.values());
	}

	private void handleHighlightLayer(Element layerElement) {
		for (int highlightedSegmentIdx = 0; 
				highlightedSegmentIdx<layerElement.getChildCount(); highlightedSegmentIdx++) {
			Node hightlightedSegmentNode = layerElement.getChild(highlightedSegmentIdx);
			if (Element.is(hightlightedSegmentNode)) {
				Element highlightedSegmentElement = Element.as(hightlightedSegmentNode);
				
				if (highlightedSegmentElement.hasClassName("highlighted-content")) {
					// id is expected to have the form h.startoffset.endoffset
					String[] rangePositions = highlightedSegmentElement.getId().split("\\.");
					TextRange highlightedTextRange = 
						new TextRange(
							Integer.valueOf(rangePositions[1]),
							Integer.valueOf(rangePositions[2]));
					highlightedTextRanges.add(highlightedTextRange);
				}
			}
		}
	}

	private void handleAnnotationLayer(Element layerElement, Map<String, ClientTagInstance> absoluteTagIntancesByID) {
		for (int annotationSegmentIdx = 0; 
				annotationSegmentIdx<layerElement.getChildCount(); annotationSegmentIdx++) {
			Node annotationSegmentNode = layerElement.getChild(annotationSegmentIdx);
			if (Element.is(annotationSegmentNode)) {
				Element annotationSegmentElement = Element.as(annotationSegmentNode);
				
				if (annotationSegmentElement.getId() != null &&
						annotationSegmentElement.getId().startsWith("CATMA_")) {
					
					String annotationSegmentId = annotationSegmentElement.getId();
					Style annotationSegmentStyle = annotationSegmentElement.getStyle();
					String annotationColor = annotationSegmentStyle.getBackgroundColor();
					handleAnnotationSegmment(annotationSegmentId, annotationColor, absoluteTagIntancesByID);
				}
			}
		}
	}

	private void handleAnnotationSegmment(
			String annotationSegmentId, String annotationColor, Map<String, ClientTagInstance> absoluteTagIntancesByID) {
		annotationColor = getConvertedColor(annotationColor);
		
		String annotationId = ClientTagInstance.getTagInstanceIDFromPartId(annotationSegmentId);
		TextRange textRange = ClientTagInstance.getTextRangeFromPartId(annotationSegmentId);
		ClientTagInstance tagInstance = absoluteTagIntancesByID.get(annotationId);
		if (tagInstance != null) {
			TreeSet<TextRange> sortedRanges = new TreeSet<>(tagInstance.getRanges());
			sortedRanges.add(textRange);
			tagInstance = new ClientTagInstance(null, annotationId, annotationColor, ClientTagInstance.mergeRanges(sortedRanges));
		}
		else {
			List<TextRange> textRanges = new ArrayList<>();
			textRanges.add(textRange);
			tagInstance = new ClientTagInstance(null, annotationId, annotationColor, textRanges);
		}
		
		absoluteTagIntancesByID.put(annotationId, tagInstance);
	}

	private String getConvertedColor(String annotationColor) {
		if (annotationColor.startsWith("rgb")) {
			String[] colorStrings = annotationColor.substring(4, annotationColor.length()-1).split(",");
			
			int red = Integer.valueOf(colorStrings[0].trim());
			int green = Integer.valueOf(colorStrings[1].trim());
			int blue = Integer.valueOf(colorStrings[2].trim());
			return fillUp(Integer.toHexString(red).toUpperCase()) 
					+ fillUp(Integer.toHexString(green).toUpperCase()) 
					+ fillUp(Integer.toHexString(blue).toUpperCase());
		}
		return annotationColor;
	}
	
	private String fillUp(String hexString) {
		if (hexString.length() < 2) {
			return "0"+hexString;
		}
		
		return hexString;
	}

	private void handleDisplayLayer(Element layerElement) {
		textRange = getTextRangeFromDisplayLayer(layerElement);
		presentationContent = layerElement.getFirstChildElement().getInnerText();
	}
	
	private TextRange getTextRangeFromDisplayLayer(Element layerElement) {
		String[] rangePositions = layerElement.getId().split("\\.");
		return new TextRange(Integer.valueOf(rangePositions[0]),Integer.valueOf(rangePositions[1]));
	}

	private String getPresentationContent() {
		return presentationContent;
	}
	
	private Element createLineElement() {
		
		List<TextRange> rangeParts = new ArrayList<>();
		
		rangeParts.add(new TextRange(this.textRange));
		Set<TextRange> segmentTextRanges = new HashSet<>(tagInstanceTextRanges);
		segmentTextRanges.addAll(highlightedTextRanges);
		segmentTextRanges.addAll(selectedTextRanges);
		
		for (TextRange currentTextRange : segmentTextRanges) {
			
			for (TextRange rangePart : new TreeSet<TextRange>(rangeParts)) {
				if (rangePart.hasOverlappingRange(currentTextRange)) {
					rangeParts.remove(rangePart);
					rangeParts.add(rangePart.getOverlappingRange(currentTextRange));
					for (TextRange disjointRange : 
							rangePart.getDisjointRanges(currentTextRange)) {
						
						if (rangePart.hasOverlappingRange(disjointRange)) {
							rangeParts.add(disjointRange);
						}
						else {
							currentTextRange = disjointRange;
						}
					}
				}
			}
			
		}
		
		Collections.sort(rangeParts);

		Element table = DOM.createTable();
		table.setAttribute("class", "taggerline-table");
		table.setAttribute("id", lineId);
		
		Element tbody = DOM.createTBody();
		table.appendChild(tbody);
		
		// display layer
		
		Element contentDisplayLayer = DOM.createTR();
		contentDisplayLayer.setAttribute("class", "tagger-display-layer");
		contentDisplayLayer.setAttribute(
				"id", textRange.getStartPos()+"."+textRange.getEndPos());
		
		tbody.appendChild(contentDisplayLayer);
		Element visibleContentLayerContent = DOM.createTD();
		contentDisplayLayer.appendChild(visibleContentLayerContent);
		visibleContentLayerContent.setAttribute(
				"colspan", String.valueOf(rangeParts.size()));
		visibleContentLayerContent.setInnerText(getPresentationContent());
		
		// selection layer 
		
		if (!selectedTextRanges.isEmpty()) {
			Element selectionLayer = DOM.createTR();
			selectionLayer.setAttribute("class", "selection-layer");
			selectionLayer.setAttribute("unselectable", "on");
			tbody.appendChild(selectionLayer);
			
			for (TextRange rangePart : rangeParts) {
				Element selectionLayerContent = DOM.createTD();
				selectionLayer.appendChild(selectionLayerContent);
				selectionLayerContent.setInnerText(TextRange.NBSP);
				selectionLayerContent.setAttribute(
						"id", "s"+rangePart.getStartPos()+"."+rangePart.getEndPos());

				if (rangePartIsSelected(rangePart)) {
					selectionLayerContent.setAttribute(
							"class", "selected-content");
				}
				else {
					selectionLayerContent.setAttribute(
							"class", "empty-selection-layer");
				}
			}
		}		
		
		// highlight layer
		
		if (!highlightedTextRanges.isEmpty()) {
			Element highlightLayer = DOM.createTR();
			highlightLayer.setAttribute("class", "highlight-layer");
			highlightLayer.setAttribute("unselectable", "on");
			tbody.appendChild(highlightLayer);
			
			for (TextRange rangePart : rangeParts) {
				Element highlightLayerContent = DOM.createTD();
				highlightLayer.appendChild(highlightLayerContent);
				highlightLayerContent.setInnerText(TextRange.NBSP);
				highlightLayerContent.setAttribute(
						"id", "h."+rangePart.getStartPos()+"."+rangePart.getEndPos());

				if (rangePartIsHighlighted(rangePart)) {
					highlightLayerContent.setAttribute(
							"class", "highlighted-content");
				}
				else {
					highlightLayerContent.setAttribute(
							"class", "empty-highlight-layer");
				}
			}
		}
		
		
		// annotation layers
		
		AnnotationLayerBuilder annotationLayerBuilder =
				new AnnotationLayerBuilder(absoluteTagIntances, rangeParts);
		
		Table<Integer, TextRange, ClientTagInstance> annotationLayerTable = 
				annotationLayerBuilder.getLayerTable();
		int rowCount = annotationLayerTable.rowKeySet().size();
		for (int rowIdx = 0; rowIdx<rowCount; rowIdx++) {
			Element annotationLayer = DOM.createTR();
			
			annotationLayer.setAttribute("class", "annotation-layer");
			annotationLayer.setAttribute("unselectable", "on");
			tbody.appendChild(annotationLayer);

			for (TextRange rangePart : rangeParts) {
				
				Element annotationLayerContent = DOM.createTD();
				annotationLayer.appendChild(annotationLayerContent);
				annotationLayerContent.setInnerText(TextRange.NBSP);
				
				ClientTagInstance relativeTagInstance = 
						annotationLayerTable.get(rowIdx, rangePart);
				
				if (relativeTagInstance != null) {
					annotationLayerContent.setAttribute(
							"class", "unselected-tag-instance");
					
					annotationLayerContent.setAttribute(
							"id", 
							relativeTagInstance.getInstanceID() 
								+ "."
								+ rangePart.getStartPos()
								+ "."
								+ rangePart.getEndPos());
					annotationLayerContent.setAttribute(
							"style", 
							"background:#"+relativeTagInstance.getColor()
							+";color:#"+relativeTagInstance.getColor());
				}
				else {
					annotationLayerContent.setAttribute(
							"class", "empty-annotation-layer");
				}
			}
		}
		
		// segmentation layer
		
		Element segmentationLayer = DOM.createTR();

		segmentationLayer.setAttribute("class", "segmentation-layer");
		segmentationLayer.setAttribute("unselectable", "on");
		tbody.appendChild(segmentationLayer);
		boolean isRtl = isRtlWriting();
		
		String lastPresentationContent = null;
		for (TextRange rangePart : rangeParts) {
			
			Element segmentationLayerContent = DOM.createTD();
			segmentationLayer.appendChild(segmentationLayerContent);
			String presentationContent = getPresentationContent(rangePart);
			
			if (isRtl 
				&& (lastPresentationContent != null) 
				&& !lastPresentationContent.endsWith(TextRange.NBSP)) {
				segmentationLayerContent.setInnerText(presentationContent+TextRange.ZWJ);
			}
			else {
				segmentationLayerContent.setInnerText(presentationContent);
			}
			
			lastPresentationContent = presentationContent;
		}
		
		return table;
	}

	private boolean rangePartIsHighlighted(TextRange rangePart) {
		for (TextRange highlightedTextRange : highlightedTextRanges) {
			if (rangePart.isCoveredBy(highlightedTextRange)) {
				return true;
			}
		}
		return false;
	}

	private boolean rangePartIsSelected(TextRange rangePart) {
		for (TextRange selectedTextRange : selectedTextRanges) {
			if (rangePart.isCoveredBy(selectedTextRange)) {
				return true;
			}
		}
		return false;
	}
	
	private String getPresentationContent(TextRange range) {
		return presentationContent.substring(
				range.getStartPos()-getLineOffset(), 
				range.getEndPos()-getLineOffset());
	}

	public void addTagInstance(ClientTagInstance clientTagInstance) {
		absoluteTagIntances.add(clientTagInstance);
		tagInstanceTextRanges.addAll(clientTagInstance.getRanges());
	}

	public int getLineOffset() {
		return textRange.getStartPos();
	}
	
	public TextRange getTextRange() {
		return textRange;
	}

	public Element getLineElement() {
		return lineElement;
	}

	public void updateLineElement() {
		Element newLineElement = createLineElement();
		lineElement.getParentElement().replaceChild(newLineElement, lineElement);
		this.lineElement = newLineElement;
	}
	
	public int getLineId() {
		return Integer.valueOf(lineId.substring(lineId.indexOf('.')+1));
	}
	
	public void addHighlightedTextRange(TextRange textRange) {
		highlightedTextRanges.add(textRange);
	}

	public boolean hasHighlightedTextRanges() {
		return !highlightedTextRanges.isEmpty();
	}
	
	public void removeHighlightedTextRanges() {
		highlightedTextRanges.clear();
	}
	
	public void addSelectedTextRange(TextRange textRange) {
		selectedTextRanges.add(textRange);
	}
	
	public boolean hasSelectedTextRanges() {
		return !selectedTextRanges.isEmpty();
	}
	
	public void removeSelectedTextRanges() {
		selectedTextRanges.clear();
	}
	
	public Set<TextRange> getSelectedTextRanges() {
		return Collections.unmodifiableSet(selectedTextRanges);
	}
	
	private boolean isRtlWriting() {
		Element parent = lineElement.getParentElement();
		if (parent.hasAttribute("dir")) {
			return parent.getAttribute("dir").trim().toLowerCase().equals("rtl");
		}
		
		return false;
	}
	
	public Set<String> getTagInstanceIDs(TextRange relativeTextRange) {
		HashSet<String> tagInstanceIDs = new HashSet<>();
		for (ClientTagInstance relativeClientTagInstance : absoluteTagIntances) {
			if (relativeClientTagInstance.hasOverlappingRange(relativeTextRange)) {
				tagInstanceIDs.add(relativeClientTagInstance.getInstanceID());
			}
		}
		
		return tagInstanceIDs;
	}

	public void addComment(ClientComment comment) {
		this.comments.add(comment);
	}
	
	@Override
	public int compareTo(Line o) {
		return Integer.compare(this.getLineId(), o.getLineId());
	}

	public void updateComment(String uuid, String body) {
		for (ClientComment comment : comments) {
			if (comment.getUuid().equals(uuid)) {
				comment.setBody(body);
			}
		}
	}

	public void removeComment(String uuid) {
		ClientComment comment = findComment(uuid);
		if (comment != null) {
			this.comments.remove(comment);
		}
	}

	private ClientComment findComment(String uuid) {
		for (ClientComment comment : comments) {
			if (comment.getUuid().equals(uuid)) {
				return comment;
			}
		}

		return null;
	}

	public void updateComment(String uuid, List<ClientCommentReply> replies) {
		for (ClientComment comment : comments) {
			if (comment.getUuid().equals(uuid)) {
				comment.setReplies(replies);
			}
		}
	}
}
