package de.catma.ui.tagger.annotationpanel;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.vaadin.ui.UI;

import de.catma.document.Range;
import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.indexer.KeywordInSpanContext;
import de.catma.indexer.KwicProvider;
import de.catma.tag.TagDefinition;
import de.catma.ui.modules.main.ErrorHandler;
import de.catma.ui.util.Cleaner;
import de.catma.util.ColorConverter;

public class AnnotatedTextProvider {
	private static final int SMALL_MAX_ANNOTATED_KEYWORD_DISPLAY_LENGTH = 30;
	private static final int LARGE_MAX_ANNOTATED_KEYWORD_DISPLAY_LENGTH = 300;

	static final String HORIZONTAL_ELLIPSIS = "\u2026";
	static final int MAX_VALUE_LENGTH = 100;

	public static String shorten(String keyword, int maxLength) {
		if (keyword.length() <= maxLength) {
			return keyword;
		}
		
		return keyword.substring(0, maxLength/2) 
				+"["+HORIZONTAL_ELLIPSIS+"]"
				+ keyword.substring(keyword.length()-((maxLength/2)-2), keyword.length());
	}
	
	public static String buildKeywordInContext(
		Collection<TagReference> tagReferences, KwicProvider kwicProvider, 
		TagDefinition tagDefinition, String tagPath) {

		StringBuilder builder = new StringBuilder();
		
		List<Range> ranges = Range.mergeRanges(
				new TreeSet<>(
					tagReferences
					.stream()
					.map(tagRef -> tagRef.getRange())
					.collect(Collectors.toList())));
		try {
			List<KeywordInSpanContext> kwics = kwicProvider.getKwic(ranges, 5);
			String conc = "";
			for (KeywordInSpanContext kwic : kwics) {
				builder.append(Cleaner.clean(kwic.getBackwardContext()));

				builder.append("<span");
				builder.append(" class=\"annotation-details-tag-color\"");
				builder.append(" style=\"");
				builder.append(" background-color:");
				builder.append("#"+ColorConverter.toHex(
						tagDefinition.getColor()));
				builder.append(";");
				builder.append(" color:");
				builder.append(ColorConverter.isLightColor(
					tagDefinition.getColor())?"black":"white");
				builder.append(";");
				builder.append("\">");
				builder.append(
					Cleaner.clean(
						shorten(
								kwic.getKeyword(), 
								LARGE_MAX_ANNOTATED_KEYWORD_DISPLAY_LENGTH)));
				builder.append("</span>");	
			
				builder.append(Cleaner.clean(kwic.getForwardContext()));
				builder.append(conc);
				conc = " [" + HORIZONTAL_ELLIPSIS + "] ";
			}
			
			builder.append("<br /><hr />");
			builder.append("Tag Path: <b>");
			builder.append(tagPath);
			builder.append("</b>");
		}
		catch (IOException e) {
			((ErrorHandler)UI.getCurrent()).showAndLogError(
					"Error loading keyword in context!", e);
		}		
		
		return builder.toString();
	}

	
	public static String buildAnnotatedText(
			Collection<TagReference> tagReferences, KwicProvider kwicProvider, TagDefinition tagDefinition) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div");
		builder.append(" class=\"annotation-details-tag-color\"");
		builder.append(" style=\"");
		builder.append(" background-color:");
		builder.append("#"+ColorConverter.toHex(
				tagDefinition.getColor()));
		builder.append(";");
		builder.append(" color:");
		builder.append(ColorConverter.isLightColor(
			tagDefinition.getColor())?"black":"white");
		builder.append(";");
		builder.append("\">");
		
		List<Range> ranges = Range.mergeRanges(
				new TreeSet<>(
					tagReferences
					.stream()
					.map(tagRef -> tagRef.getRange())
					.collect(Collectors.toList())));
		try {
			List<KeywordInSpanContext> kwics = kwicProvider.getKwic(ranges, 5);

			String joinedAnnotatedText = kwics.stream()
			.map(kwic -> kwic.getKeyword())
			.map(keyword -> shorten(keyword, SMALL_MAX_ANNOTATED_KEYWORD_DISPLAY_LENGTH))
			.collect(Collectors.joining(" [" + HORIZONTAL_ELLIPSIS + "] "));
			
			builder.append(joinedAnnotatedText);
		}
		catch (IOException e) {
			((ErrorHandler)UI.getCurrent()).showAndLogError(
					"Error loading annotated text!", e);
			builder.append("&nbsp;");
		}		
		builder.append("</div>");		
		
		return builder.toString();
	}
	
}