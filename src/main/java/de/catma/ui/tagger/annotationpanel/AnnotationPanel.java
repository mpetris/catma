package de.catma.ui.tagger.annotationpanel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.dialogs.ConfirmDialog;

import com.github.appreciated.material.MaterialTheme;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.HtmlRenderer;

import de.catma.document.repository.Repository;
import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;
import de.catma.tag.PropertyDefinition;
import de.catma.tag.TagDefinition;
import de.catma.tag.TagManager.TagManagerEvent;
import de.catma.tag.TagsetDefinition;
import de.catma.ui.component.IconButton;
import de.catma.ui.component.actiongrid.ActionGridComponent;
import de.catma.ui.dialog.SaveCancelListener;
import de.catma.ui.modules.main.ErrorHandler;
import de.catma.util.IDGenerator;
import de.catma.util.Pair;

public class AnnotationPanel extends VerticalLayout {
	
	
	public interface TagReferenceSelectionChangeListener {
		public void tagReferenceSelectionChanged(
				List<TagReference> tagReferences, boolean selected);
	}
	
	private ComboBox<UserMarkupCollection> currentEditableCollectionBox;
	private Button addCollectionButton;
	private TreeGrid<TagsetTreeItem> tagsetGrid;
	private Repository project;
	private Collection<TagsetDefinition> tagsets = Collections.emptyList();
	private List<UserMarkupCollection> collections = Collections.emptyList();
	private TagReferenceSelectionChangeListener selectionListener;
	private ActionGridComponent<TreeGrid<TagsetTreeItem>> tagsetGridComponent;
	private TreeData<TagsetTreeItem> tagsetData;
	private TreeDataProvider<TagsetTreeItem> tagsetDataProvider;
	private IDGenerator idGenerator = new IDGenerator();
	private PropertyChangeListener tagChangedListener;
	private PropertyChangeListener propertyDefinitionChangedListener;

	public AnnotationPanel(Repository project) {
		this.project = project;
		initComponents();
		initActions();
		initListeners();
	}

	private void initListeners() {
		tagChangedListener = new PropertyChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				Object newValue = evt.getNewValue();
				Object oldValue = evt.getOldValue();
				
				if (oldValue == null) { //created
					Pair<TagsetDefinition, TagDefinition> value = 
							(Pair<TagsetDefinition, TagDefinition>)newValue;
					
					TagsetDefinition tagset = value.getFirst();
					TagDefinition tag = value.getSecond();
		            if (tag.getParentUuid().isEmpty()) {
		            	TagsetTreeItem tagsetItem = new TagsetDataItem(tagset);
		            	tagsetData.addItem(
		            		tagsetItem, new TagDataItem(tag));
		            	
		            	tagsetGrid.expand(tagsetItem);
		            }
		            else {
		            	TagDefinition parentTag = 
		            		project.getTagManager().getTagLibrary().getTagDefinition(tag.getParentUuid());
		            	TagsetTreeItem parentTagItem = new TagDataItem(parentTag);
		            	tagsetData.addItem(parentTagItem, new TagDataItem(tag));
		            	
		            	tagsetGrid.expand(parentTagItem);
		            }
		            
					tagsetDataProvider.refreshAll();
		            
				}
				else if (newValue == null) { //removed
					Pair<TagsetDefinition,TagDefinition> deleted = (Pair<TagsetDefinition, TagDefinition>) oldValue;
					
					TagDefinition deletedTag = deleted.getSecond();
					
					tagsetData.removeItem(new TagDataItem(deletedTag));
					tagsetDataProvider.refreshAll();
					
				}
				else { //update
					TagDefinition tag = (TagDefinition) newValue;
					TagsetDefinition tagset = (TagsetDefinition)oldValue;
	            	TagsetTreeItem tagsetItem = new TagsetDataItem(tagset);

					tagsetData.removeItem(new TagDataItem(tag));
					TagDataItem tagDataItem = new TagDataItem(tag);
					tagDataItem.setPropertiesExpanded(true);
					tagsetData.addItem(tagsetItem, tagDataItem);
					//TODO: sort
					
					showExpandedProperties(tagDataItem);
					
					tagsetDataProvider.refreshAll();
				}
				
			}
		};
		project.getTagManager().addPropertyChangeListener(
				TagManagerEvent.tagDefinitionChanged, 
				tagChangedListener);
		
		propertyDefinitionChangedListener = new PropertyChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Object newValue = evt.getNewValue();
				Object oldValue = evt.getOldValue();
				
				TagDefinition tag = null;
				
				if (oldValue == null) { //created
					Pair<PropertyDefinition, TagDefinition> newData =
							(Pair<PropertyDefinition, TagDefinition>) newValue;
					
					tag = newData.getSecond();
					
				}
				else if (newValue == null) { // removed
					Pair<PropertyDefinition, Pair<TagDefinition, TagsetDefinition>> oldData =
							(Pair<PropertyDefinition, Pair<TagDefinition, TagsetDefinition>>) oldValue;
					
					tag = oldData.getSecond().getFirst();
				}
				else { //update
					tag = (TagDefinition) oldValue;
				}
				
				TagDataItem tagDataItem = new TagDataItem(tag);
				
				hideExpandedProperties(tagDataItem);
				showExpandedProperties(tagDataItem);
						
				tagsetDataProvider.refreshAll();
			}
		};
		
		project.getTagManager().addPropertyChangeListener(
				TagManagerEvent.userPropertyDefinitionChanged, 
				propertyDefinitionChangedListener);	
	}

	private void initData() {
        try {
            tagsetData = new TreeData<TagsetTreeItem>();
            
            tagsetData.addRootItems(tagsets.stream().map(ts -> new TagsetDataItem(ts)));

            for (TagsetDefinition tagsetDefinition : tagsets) {
            	addTags(tagsetData, tagsetDefinition);
            }
            tagsetDataProvider = new TreeDataProvider<TagsetTreeItem>(tagsetData);
            tagsetGrid.setDataProvider(tagsetDataProvider);
            for (TagsetDefinition tagset : tagsets) {
            	expandTagsetDefinition(tagset);
            }
            
            currentEditableCollectionBox.setValue(null);
            currentEditableCollectionBox.setDataProvider(new ListDataProvider<>(collections));
        } catch (Exception e) {
			((ErrorHandler)UI.getCurrent()).showAndLogError("Error loading data!", e);
        }
    }

    private void expandTagsetDefinition(TagsetDefinition tagset) {
    	for (TagDefinition tag : tagset) {
    		TagDataItem item = new TagDataItem(tag);
    		tagsetGrid.expand(item);
    		if (!tag.getUserDefinedPropertyDefinitions().isEmpty()) {
    			tagsetGrid.setDetailsVisible(item, true);
    		}
    	}
	}

	private void addTags(TreeData<TagsetTreeItem> tagsetData, TagsetDefinition tagset) {
        for (TagDefinition tag : tagset) {
            if (tag.getParentUuid().isEmpty()) {
                tagsetData.addItem(new TagsetDataItem(tagset), new TagDataItem(tag));
                addTagSubTree(tagset, tag, tagsetData);
            }
        }
	}

	private void addTagSubTree(
    		TagsetDefinition tagset, 
    		TagDefinition tag, TreeData<TagsetTreeItem> tagsetData) {
        for (TagDefinition childDefinition : tagset.getDirectChildren(tag)) {
            tagsetData.addItem(new TagDataItem(tag), new TagDataItem(childDefinition));
            addTagSubTree(tagset, childDefinition, tagsetData);
        }
    }
	private void initActions() {
		tagsetGrid.addColumn(tagsetTreeItem -> tagsetTreeItem.getColor(), new HtmlRenderer())
			.setCaption("Name")
			.setSortable(false)
			.setExpandRatio(1);
		tagsetGrid.setHierarchyColumn(
			tagsetGrid.addColumn(tagsetTreeItem -> tagsetTreeItem.getName())
			.setCaption("Tags")
			.setSortable(false)
			.setExpandRatio(2));

		ButtonRenderer<TagsetTreeItem> propertySummaryRenderer = 
				new ButtonRenderer<>(rendererClickEvent -> handlePropertySummaryClickEvent(rendererClickEvent));
		propertySummaryRenderer.setHtmlContentAllowed(true); //TODO: handle property summary and values js and html injections!
		
		tagsetGrid.addColumn(
			tagsetTreeItem -> tagsetTreeItem.getPropertySummary(), 
			propertySummaryRenderer)
		.setCaption("Properties")
		.setSortable(false)
		.setHidable(true)
		.setExpandRatio(2);
		
		tagsetGrid.addColumn(
			tagsetTreeItem -> tagsetTreeItem.getPropertyValue())
		.setSortable(false)
		.setHidable(true)
		.setExpandRatio(1);
			
		
		ButtonRenderer<TagsetTreeItem> visibilityRenderer = 
			new ButtonRenderer<TagsetTreeItem>(rendererClickEvent -> handleVisibilityClickEvent(rendererClickEvent));
		visibilityRenderer.setHtmlContentAllowed(true);
		tagsetGrid.addColumn(
			tagsetTreeItem -> tagsetTreeItem.getVisibilityIcon(), 
			visibilityRenderer)
		.setSortable(false);
		
		tagsetGrid.setStyleGenerator(new StyleGenerator<TagsetTreeItem>() {
			
			@Override
			public String apply(TagsetTreeItem item) {
				return item.generateStyle();
			}
		});
		
        ContextMenu addContextMenu = 
        		tagsetGridComponent.getActionGridBar().getBtnAddContextMenu();
        addContextMenu.addItem("Add Tag", clickEvent -> handleAddTagRequest());
        addContextMenu.addItem("Add Subtag", clickEvent -> handleAddSubtagRequest());
        addContextMenu.addItem("Add Property", clickEvent -> handleAddPropertyRequest());
		
		ContextMenu moreOptionsContextMenu = 
				tagsetGridComponent.getActionGridBar().getBtnMoreOptionsContextMenu();
		moreOptionsContextMenu.addItem("Edit Tag", clickEvent -> handleEditTagRequest());
		moreOptionsContextMenu.addItem("Delete Tag", clickEvent -> handleDeleteTagRequest());
		moreOptionsContextMenu.addItem("Edit/Deletee Properties", clickEvent -> handlePropertiesTagRequest());
		moreOptionsContextMenu.addItem("Edit Tagset", clickEvent -> handleEditTagsetRequest());
		moreOptionsContextMenu.addItem("Delete Tagset", clickEvent -> handleDeleteTagsetRequest());
		
		currentEditableCollectionBox.setEmptySelectionCaption("Please select or create a Collection...");
	}

	private void handleDeleteTagsetRequest() {
		// TODO Auto-generated method stub
	}

	private void handleEditTagsetRequest() {
		// TODO Auto-generated method stub
	}

	private void handlePropertiesTagRequest() {
		handleAddPropertyRequest();
	}

	private void handleDeleteTagRequest() {
		final List<TagDefinition> targetTags = tagsetGrid.getSelectedItems()
		.stream()
		.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagDataItem)
		.map(tagsetTreeItem -> ((TagDataItem)tagsetTreeItem).getTag())
		.collect(Collectors.toList());
		
		String msg = String.format(
			"Are you sure you want to delete the following Tags: %1$s?", 
			targetTags
			.stream()
			.map(TagDefinition::getName)
			.collect(Collectors.joining(",")));
		
		ConfirmDialog.show(UI.getCurrent(), "Warning", msg, "Delete", "Cancel", dlg -> {
			if (dlg.isConfirmed()) {
				for (TagDefinition tag : targetTags) {
					TagsetDefinition tagset =
							project.getTagManager().getTagLibrary().getTagsetDefinition(tag);
					project.getTagManager().removeTagDefinition(tagset, tag);
				}
			}
		});
		
	}

	private void handleEditTagRequest() {
		final List<TagDefinition> targetTags = tagsetGrid.getSelectedItems()
		.stream()
		.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagDataItem)
		.map(tagsetTreeItem -> ((TagDataItem)tagsetTreeItem).getTag())
		.collect(Collectors.toList());
		
		if (targetTags.isEmpty()) {
			Notification.show("Info", "Please select a Tag first!", Type.TRAY_NOTIFICATION);
		}
		else if (targetTags.size() > 1) {
			handleAddPropertyRequest();
		}
		else {
			
			final TagDefinition targetTag = targetTags.get(0);
			
			EditTagDialog editTagDialog = new EditTagDialog(new TagDefinition(targetTag), 
					new SaveCancelListener<TagDefinition>() {
						public void savePressed(TagDefinition result) {
							
							project.getTagManager().updateTagDefinition(targetTag, result);
							
							//TODO: reload on error
						};
					});
			editTagDialog.show();
		}
		
	}

	private void handleAddPropertyRequest() {
		final List<TagDefinition> targetTags = tagsetGrid.getSelectedItems()
		.stream()
		.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagDataItem)
		.map(tagsetTreeItem -> ((TagDataItem)tagsetTreeItem).getTag())
		.collect(Collectors.toList());
		if (targetTags.isEmpty()) {
			Notification.show("Info", "Please select one ore more Tags first!", Type.TRAY_NOTIFICATION);
		}
		else {
			Multimap<String, PropertyDefinition> propertiesByName = 
					ArrayListMultimap.create();
			
			for (TagDefinition tag : targetTags) {
				for (PropertyDefinition propertyDef : tag.getUserDefinedPropertyDefinitions()) {
					if (!propertiesByName.containsKey(propertyDef.getName()) || 
							propertiesByName.get(propertyDef.getName()).iterator().next().getPossibleValueList()
								.equals(propertyDef.getPossibleValueList())) {
						propertiesByName.put(propertyDef.getName(), propertyDef);
					}
				}
			}
			
			
			List<PropertyDefinition> commonProperties = 
				propertiesByName.asMap().entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() == targetTags.size())
				.map(entry -> new PropertyDefinition(entry.getValue().iterator().next()))
				.collect(Collectors.toList());
			
			AddEditPropertyDialog addPropertyDialog = new AddEditPropertyDialog(
				targetTags.size() > 1, // just a single tag's properties or is it a bulk(>1) edit?
				commonProperties,
				new SaveCancelListener<List<PropertyDefinition>>() {
					@Override
					public void savePressed(List<PropertyDefinition> result) {
						final Set<String> availableCommonPropertyNames = 
								result.stream().map(propertyDef -> propertyDef.getName())
								.collect(Collectors.toSet());
						
						final Set<String> deletedCommonProperyNames = commonProperties
						.stream()
						.map(propertyDef -> propertyDef.getName())
						.filter(name -> !availableCommonPropertyNames.contains(name))
						.collect(Collectors.toSet());
						
						for (TagDefinition tag : targetTags) {
							TagsetDefinition tagset = 
								project.getTagManager().getTagLibrary().getTagsetDefinition(tag);
	
							for (PropertyDefinition existingPropertyDef : 
								new ArrayList<>(tag.getUserDefinedPropertyDefinitions())) {
								
								//handle deleted PropertyDefs
								if (deletedCommonProperyNames.contains(existingPropertyDef.getName())) {
									project.getTagManager().removeUserDefinedPropertyDefinition(
											existingPropertyDef, tag, tagset);
								}
								//handle updated PropertyDefs
								else if (availableCommonPropertyNames.contains(existingPropertyDef.getName())) {
									result
									.stream()
									.filter(possiblyChangedPd -> 
										possiblyChangedPd.getName().equals(existingPropertyDef.getName()))
									.findFirst()
									.ifPresent(possiblyChangedPd -> 
										existingPropertyDef.setPossibleValueList(
											possiblyChangedPd.getPossibleValueList()));
									
									project.getTagManager().updateUserDefinedPropertyDefinition(
										tag, existingPropertyDef);
								}
							}
							
							//handle created PropertyDefs
							for (PropertyDefinition pd : result) {
								if (tag.getPropertyDefinition(pd.getName()) == null) {
									PropertyDefinition createdPropertyDefinition = 
											new PropertyDefinition(pd);
									pd.setUuid(idGenerator.generate());
									
									project.getTagManager().addUserDefinedPropertyDefinition(
										tag, createdPropertyDefinition);
								}
							}
						}
						
					}
			});
			
			addPropertyDialog.show();
		}
	}

	private void handleAddSubtagRequest() {
		final List<TagDefinition> parentTags = tagsetGrid.getSelectedItems()
		.stream()
		.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagDataItem)
		.map(tagsetTreeItem -> ((TagDataItem)tagsetTreeItem).getTag())
		.collect(Collectors.toList());
		
		if (!parentTags.isEmpty()) {
			AddSubtagDialog addTagDialog =
				new AddSubtagDialog(new SaveCancelListener<TagDefinition>() {
					public void savePressed(TagDefinition result) {
						for (TagDefinition parent : parentTags) {
							
							TagsetDefinition tagset = 
								project.getTagManager().getTagLibrary().getTagsetDefinition(parent);
							
							TagDefinition tag = new TagDefinition(result);
							tag.setUuid(idGenerator.generate());
							tag.setParentUuid(parent.getUuid());
							tag.setTagsetDefinitionUuid(tagset.getUuid());
							
							project.getTagManager().addTagDefinition(
									tagset, tag);
						}
					};
				});
			addTagDialog.show();
		}
		else {
			Notification.show("Info", "Please select at least one parent Tag!", Type.HUMANIZED_MESSAGE);
		}
	}

	private void handleAddTagRequest() {
		
		Optional<TagsetDefinition> selectedTagset = tagsetGrid.getSelectedItems()
			.stream()
			.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagsetDataItem)
			.findFirst()
			.map(tagsetTreeItem -> ((TagsetDataItem)tagsetTreeItem).getTagset());
			
		//TODO: check for available and writable tagsets (permissions!)
		
		AddParenttagDialog addTagDialog = 
			new AddParenttagDialog(
				tagsets, 
				selectedTagset, 
				new SaveCancelListener<Pair<TagsetDefinition, TagDefinition>>() {
				
				@Override
				public void savePressed(Pair<TagsetDefinition, TagDefinition> result) {
					project.getTagManager().addTagDefinition(
							result.getFirst(), result.getSecond());
				}
			});
		addTagDialog.show();
		
	}

	private void handlePropertySummaryClickEvent(RendererClickEvent<TagsetTreeItem> rendererClickEvent) {
		if (rendererClickEvent.getItem() instanceof TagDataItem) {
			TagDataItem tagDataItem = (TagDataItem) rendererClickEvent.getItem();
			
			tagDataItem.setPropertiesExpanded(!tagDataItem.isPropertiesExpanded());
			
			if (tagDataItem.isPropertiesExpanded()) {
				showExpandedProperties(tagDataItem);
			}
			else {
				hideExpandedProperties(tagDataItem);
			}
			tagsetDataProvider.refreshAll();
		}
		else if (rendererClickEvent.getItem() instanceof PropertyDataItem) {
			PropertyDataItem propertyDataItem= (PropertyDataItem)rendererClickEvent.getItem();
			
			propertyDataItem.setValuesExpanded(!propertyDataItem.isValuesExpanded());
			
			if (propertyDataItem.isValuesExpanded()) {
				showExpandedPossibleValues(propertyDataItem);
			}
			else {
				hideExpandedPossibleValues(propertyDataItem);
			}
			tagsetDataProvider.refreshAll();
		}
	}

	private void hideExpandedPossibleValues(PropertyDataItem propertyDataItem) {
		TreeData<TagsetTreeItem> tagsetTreeData = tagsetGrid.getTreeData();
		
		for (TagsetTreeItem childTagsetTreeItem : new ArrayList<>(tagsetTreeData.getChildren(propertyDataItem))) {
			childTagsetTreeItem.removePropertyDataItem(tagsetDataProvider);
		}
	}

	private void showExpandedPossibleValues(PropertyDataItem propertyDataItem) {
		PropertyDefinition propertyDefinition = propertyDataItem.getPropertyDefinition();
		
		for (String possibleValue : propertyDefinition.getPossibleValueList()) {
			tagsetGrid.getTreeData().addItem(
				new PropertyDataItem(propertyDefinition), 
				new PossibleValueDataItem(possibleValue));
		}
		
		tagsetGrid.expand(propertyDataItem);
	}

	private void showExpandedProperties(TagDataItem tagDataItem) {
		TagDefinition tag = tagDataItem.getTag();
		
		PropertyDataItem lastPropertyDataItem = null; 
		for (PropertyDefinition propertyDefinition : tag.getUserDefinedPropertyDefinitions()) {
			lastPropertyDataItem = new PropertyDataItem(propertyDefinition);
			tagsetGrid.getTreeData().addItem(tagDataItem, lastPropertyDataItem);
		}
		
		List<TagsetTreeItem> children = 
			tagsetData.getChildren(tagDataItem).stream()
			.filter(tagsetTreeItem -> tagsetTreeItem instanceof TagDataItem)
			.collect(Collectors.toList());
		
		for (int i = children.size()-1; i>=0; i--) {
			tagsetData.moveAfterSibling(children.get(i), lastPropertyDataItem);
		}
		
		tagsetGrid.expand(tagDataItem);
	}

	private void hideExpandedProperties(TagDataItem tagDataItem) {
		TreeData<TagsetTreeItem> tagsetTreeData = tagsetGrid.getTreeData();
		
		for (TagsetTreeItem childTagsetTreeItem : new ArrayList<>(tagsetTreeData.getChildren(tagDataItem))) {
			childTagsetTreeItem.removePropertyDataItem(tagsetDataProvider);
		}
	}

	private void handleVisibilityClickEvent(RendererClickEvent<TagsetTreeItem> rendererClickEvent) {
		rendererClickEvent.getItem().setVisible(!rendererClickEvent.getItem().isVisible());
		tagsetDataProvider.refreshItem(rendererClickEvent.getItem());
		
		TagsetTreeItem tagsetTreeItem = rendererClickEvent.getItem();
		List<TagReference> tagReferences = tagsetTreeItem.getTagReferences(collections);
		
		boolean selected = rendererClickEvent.getItem().isVisible();
		
		if (selectionListener != null) {
			selectionListener.tagReferenceSelectionChanged(tagReferences, selected);
		}
		
		tagsetTreeItem.setChildrenVisible(
				tagsetDataProvider, selected, false);
	}

	private void initComponents() {
		setSizeFull();
		setSpacing(true);
		
		currentEditableCollectionBox = new ComboBox<>("Collection currently being edited");
		currentEditableCollectionBox.setWidth("100%");
		
		addCollectionButton = new IconButton(VaadinIcons.PLUS);
		
		HorizontalLayout editableCollectionPanel = 
				new HorizontalLayout(currentEditableCollectionBox, addCollectionButton);
		editableCollectionPanel.addStyleName("annotate-right-padding");
		
		editableCollectionPanel.setWidth("100%");
		editableCollectionPanel.setExpandRatio(currentEditableCollectionBox, 1.0f);
		editableCollectionPanel.setComponentAlignment(addCollectionButton, Alignment.BOTTOM_CENTER);
		
		addComponent(editableCollectionPanel);
		
		Label tagsetsLabel = new Label("Tagsets");
		
		tagsetGrid = new TreeGrid<>();
		tagsetGrid.addStyleNames("annotate-tagsets-grid", "flat-undecorated-icon-buttonrenderer");
		tagsetGrid.setSizeFull();
		tagsetGrid.setSelectionMode(SelectionMode.SINGLE);
		tagsetGrid.addStyleName(MaterialTheme.GRID_BORDERLESS);

        tagsetGridComponent = new ActionGridComponent<TreeGrid<TagsetTreeItem>>(
                tagsetsLabel,
                tagsetGrid
        );
        
        addComponent(tagsetGridComponent);
        setExpandRatio(tagsetGridComponent, 1.0f);
	}
	
	public void setData(Collection<TagsetDefinition> tagsets, List<UserMarkupCollection> collections) {
		this.tagsets = tagsets;
		this.collections = collections;
		initData();
	}
	
	public void setTagReferenceSelectionChangeListener(TagReferenceSelectionChangeListener selectionListener) {
		this.selectionListener = selectionListener;
	}
	
	public UserMarkupCollection getSelectedEditableCollection() {
		return currentEditableCollectionBox.getValue();
	}

	public void addCollection(UserMarkupCollection collection) {
		this.collections.add(collection);
		currentEditableCollectionBox.getDataProvider().refreshAll();	
	}
	
	private void removeCollection(UserMarkupCollection collection) {
		if ((currentEditableCollectionBox.getValue() != null) 
				&& currentEditableCollectionBox.getValue().equals(collection)) {
			currentEditableCollectionBox.setValue(null);
		}
		collections.remove(collection);
		currentEditableCollectionBox.getDataProvider().refreshAll();	
	}

	public void removeCollection(String collectionId) {
		collections
			.stream()
			.filter(collection -> collection.getUuid().equals(collectionId))
			.findFirst()
			.ifPresent(collection -> removeCollection(collection));
	}
	
	public void setTagsets(Collection<TagsetDefinition> tagsets) {
		tagsets
		.stream()
		.filter(tagset -> !this.tagsets.contains(tagset))
		.forEach(tagset -> addTagset(tagset));
		
		this.tagsets.stream()
		.filter(tagset -> !tagsets.contains(tagset))
		.collect(Collectors.toList())
		.stream()
		.forEach(tagset -> removeTagset(tagset));
	}

	public void addTagset(TagsetDefinition tagset) {
		tagsets.add(tagset);

		tagsetData.addRootItems(new TagsetDataItem(tagset));
		addTags(tagsetData, tagset);
		expandTagsetDefinition(tagset);
		tagsetDataProvider.refreshAll();
	}
	
	public void removeTagset(TagsetDefinition tagset) {
		tagsets.remove(tagset);
		tagsetData.removeItem(new TagsetDataItem(tagset));
		tagsetDataProvider.refreshAll();
	}
	
	public void close() {
		project.getTagManager().removePropertyChangeListener(
				TagManagerEvent.userPropertyDefinitionChanged, 
				propertyDefinitionChangedListener);	
		project.getTagManager().removePropertyChangeListener(
				TagManagerEvent.tagDefinitionChanged, 
				tagChangedListener);
			
	}
}