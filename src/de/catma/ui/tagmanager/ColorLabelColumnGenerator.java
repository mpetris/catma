package de.catma.ui.tagmanager;

import java.text.MessageFormat;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

import de.catma.tag.TagDefinition;
import de.catma.util.ColorConverter;

public class ColorLabelColumnGenerator implements ColumnGenerator {
	
	private static final String COLORLABEL_HTML = 
			"<span style=\"background-color:#{0};margin-left:3px;\">" +
					"&nbsp;&nbsp;&nbsp;&nbsp;" +
			"</span>";
	

	public Object generateCell(Table source, Object itemId, Object columnId) {
		if (itemId instanceof TagDefinition) {
			TagDefinition td = (TagDefinition)itemId;
			Label colorLabel = 
				new Label(
					MessageFormat.format(
						COLORLABEL_HTML, 
						ColorConverter.toHex((
							td.getColor()))));
			colorLabel.setContentMode(Label.CONTENT_XHTML);
			return colorLabel;
		}
		
		return new Label();
	}

}
