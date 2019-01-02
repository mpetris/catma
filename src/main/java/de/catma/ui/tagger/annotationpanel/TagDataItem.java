package de.catma.ui.tagger.annotationpanel;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;

import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;
import de.catma.tag.TagDefinition;
import de.catma.util.ColorConverter;

public class TagDataItem implements TagTreeItem {
	
	private TagDefinition tag;
	private boolean visible;
	
	public TagDataItem(TagDefinition tag) {
		super();
		this.tag = tag;
	}

	@Override
	public String getColor() {
		String htmlColor = "#"+ColorConverter.toHex(tag.getColor());
		return "<div class=\"annotate-tag-tree-item\" style=\"background-color:"+htmlColor+"\">&nbsp;</div>";
	}

	@Override
	public String getName() {
		return tag.getName();
	}

	@Override
	public String getTagsetName() {
		return "";
	}
	
	public TagDefinition getTag() {
		return tag;
	}
	
	@Override
	public String getVisibilityIcon() {
		return visible?VaadinIcons.EYE.getHtml():VaadinIcons.EYE_SLASH.getHtml();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TagDataItem other = (TagDataItem) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return tag.getName();
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public List<TagReference> getTagReferences(List<UserMarkupCollection> collections) {
		List<TagReference> result = new ArrayList<>();
		
		for (UserMarkupCollection collection : collections) {
			result.addAll(collection.getTagReferences(tag));
		}
		
		return result;
	}
	
	@Override
	public void setChildrenVisible(TreeDataProvider<TagTreeItem> dataProvider, boolean visible, boolean explicit) {
		if (explicit) {
			for (TagTreeItem tagTreeItem : dataProvider.getTreeData().getChildren(this)) {
				setChildrenVisible(tagTreeItem, visible, dataProvider);
			}
		}
	}

	private void setChildrenVisible(TagTreeItem tagTreeItem, boolean visible, TreeDataProvider<TagTreeItem> dataProvider) {
		tagTreeItem.setVisible(visible);
		dataProvider.refreshItem(tagTreeItem);
		for (TagTreeItem tagTreeChildItem : dataProvider.getTreeData().getChildren(tagTreeItem)) {
			setChildrenVisible(tagTreeChildItem, visible, dataProvider);
		}		
	}
}
