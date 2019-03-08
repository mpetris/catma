package de.catma.ui.analyzer;

import java.io.IOException;

import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table.TableDragMode;
import com.vaadin.v7.ui.Tree;
import com.vaadin.v7.ui.VerticalLayout;

import de.catma.document.repository.Repository;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagLibraryReference;
import de.catma.ui.CatmaApplication;
import de.catma.ui.tagmanager.TagsetTree;

@Deprecated
public class TagResultsDialog extends Window {
	
	private final static String SORTCAP_PROP = "SORTCAP"; //$NON-NLS-1$
	
	private Repository repository;
	private TagLibrary tagLibrary;
	private HierarchicalContainer tagLibraryContainer;
	private Tree tagLibraryTree;
	private TagsetTree tagsetTree;

	public TagResultsDialog(Repository repository, 
			Object lastTagResultsDialogTagLibrarySelection,
			Object lastTagResultsDialogTagsetSelection) {
		super(Messages.getString("TagResultsDialog.Tags")); //$NON-NLS-1$
		
		this.repository = repository;
		initComponents();
		initListeners();
		
		if (lastTagResultsDialogTagLibrarySelection != null &&
				tagLibraryTree.containsId(lastTagResultsDialogTagLibrarySelection)) {
			tagLibraryTree.setValue(lastTagResultsDialogTagLibrarySelection);
		}
		else if (!tagLibraryTree.getItemIds().isEmpty()) {
			tagLibraryTree.setValue(tagLibraryTree.getItemIds().iterator().next());
		}
		
		if (lastTagResultsDialogTagsetSelection != null && 
				tagsetTree.getTagTree().containsId(lastTagResultsDialogTagsetSelection)) {
			tagsetTree.getTagTree().setValue(lastTagResultsDialogTagsetSelection);
			Object parent = tagsetTree.getTagTree().getParent(lastTagResultsDialogTagsetSelection);
			while (parent != null) {
				tagsetTree.getTagTree().setCollapsed(parent, false);
				parent = tagsetTree.getTagTree().getParent(parent);
			}
			tagsetTree.getTagTree().setCurrentPageFirstItemId(lastTagResultsDialogTagsetSelection);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initComponents() {
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		
		Label lblInstructions = new Label(
				Messages.getString("TagResultsDialog.tagResultsHint")); //$NON-NLS-1$
		
		content.addComponent(lblInstructions);
		
		HorizontalSplitPanel tagLibraryPanel = new HorizontalSplitPanel();
		content.addComponent(tagLibraryPanel);
		content.setExpandRatio(tagLibraryPanel, 1.0f);
		tagLibraryPanel.setSizeFull();
		
		tagLibraryContainer = new HierarchicalContainer();
		tagLibraryContainer.addContainerProperty(SORTCAP_PROP, String.class, null);		

		tagLibraryTree = new Tree();
		tagLibraryTree.setContainerDataSource(tagLibraryContainer);
		tagLibraryTree.setWidth("100%"); //$NON-NLS-1$
		tagLibraryTree.setCaption(Messages.getString("TagResultsDialog.tagLibraries")); //$NON-NLS-1$
		tagLibraryTree.addStyleName("bold-label-caption"); //$NON-NLS-1$
		tagLibraryTree.setImmediate(true);
		tagLibraryTree.setItemCaptionMode(ItemCaptionMode.ID);
		
		for (TagLibraryReference tlr : repository.getTagLibraryReferences()) {
			tagLibraryTree.addItem(tlr);
			tagLibraryTree.getItem(tlr).getItemProperty(SORTCAP_PROP).setValue(
					(tlr.toString()==null)?"":tlr.toString()); //$NON-NLS-1$
			tagLibraryTree.setChildrenAllowed(tlr, false);
		}
		tagLibraryContainer.sort(new Object[] {SORTCAP_PROP}, new boolean[] { true });
		
		tagLibraryTree.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				handleTagLibrariesTreeItemClick(event);				
			}
		});
		
		tagLibraryPanel.addComponent(tagLibraryTree);
		

		tagsetTree = new TagsetTree(
				repository.getTagManager(), null, false, false, false, false, false, null);
		tagsetTree.getTagTree().setDragMode(TableDragMode.ROW);
		
		tagLibraryPanel.addComponent(tagsetTree);
		
		content.setMargin(true);
		
		setContent(content);
	}
	
	private void handleTagLibrariesTreeItemClick(ValueChangeEvent event) {
		TagLibraryReference tagLibraryReference = (TagLibraryReference)event.getProperty().getValue();
		if (tagLibraryReference != null) {
			if (tagLibrary == null || tagLibrary.getId() != tagLibraryReference.getId()) {
				try {
					tagLibrary = repository.getTagLibrary(tagLibraryReference);
					((CatmaApplication)UI.getCurrent()).openTagLibrary(repository, tagLibrary, false);
					tagsetTree.setTagLibrary(tagLibrary);
					
				} catch (IOException e) {
					((CatmaApplication)UI.getCurrent()).showAndLogError(
							Messages.getString("TagResultsDialog.errorOpeningTagLibrary"), e); //$NON-NLS-1$
				}
			}
		}
		else {
			tagsetTree.setTagLibrary(null);
			tagLibrary = null;
		}
	}
	
	private void initListeners() {
		//TODO: listen to detach of project view 
		
//		MainMenu menu = ((CatmaApplication)UI.getCurrent()).getMenu();
//		menu.addMenuItemSelectedListener(menuItemSelectedListener);
//		
//		addCloseListener(new Window.CloseListener() {
//			
//			@Override
//			public void windowClose(CloseEvent e) {
//		MainMenu menu = ((CatmaApplication)UI.getCurrent()).getMenu();
//		menu.removeMenuItemSelectedListener(menuItemSelectedListener);
//			}
//		});
	}
	
	public void show(Float height, Float width, Unit lastTagResultsDialogUnit) {
		setPositionX(20);
		setPositionY(80);
		if (height != null) {
			setHeight(height, lastTagResultsDialogUnit);
		}
		else {
			setHeight("50%"); //$NON-NLS-1$
		}
		
		if (width != null) {
			setWidth(width, lastTagResultsDialogUnit);
		}
		else {
			setWidth("40%"); //$NON-NLS-1$
		}
		
		UI.getCurrent().addWindow(this);
	}
	
	public Object getCurrenTagLibraryTreeSelection() {
		return tagLibraryTree.getValue();
	}
	
	public Object getCurrentTagsetTreeSelection() {
		return tagsetTree.getTagTree().getValue();
	}

}
