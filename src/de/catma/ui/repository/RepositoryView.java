package de.catma.ui.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.catma.CleaApplication;
import de.catma.core.document.Corpus;
import de.catma.core.document.repository.Repository;
import de.catma.core.document.source.SourceDocument;
import de.catma.core.document.standoffmarkup.staticmarkup.StaticMarkupCollectionReference;
import de.catma.core.document.standoffmarkup.usermarkup.UserMarkupCollectionReference;
import de.catma.core.tag.TagLibrary;
import de.catma.core.tag.TagLibraryReference;
import de.catma.ui.repository.treeentry.ContentInfo;
import de.catma.ui.repository.treeentry.MarkupCollectionEntry;
import de.catma.ui.repository.treeentry.MarkupCollectionsEntry;
import de.catma.ui.repository.treeentry.SourceDocumentEntry;
import de.catma.ui.repository.treeentry.StandardContentInfo;
import de.catma.ui.repository.treeentry.TagLibraryEntry;
import de.catma.ui.repository.treeentry.TreeEntry;
import de.catma.ui.repository.wizard.WizardFactory;
import de.catma.ui.repository.wizard.WizardResult;
import de.catma.ui.repository.wizard.WizardWindow;


public class RepositoryView extends VerticalLayout {
	
	private Repository repository;
	private Tree documentsTree;
	private Tree corporaTree;
	private Tree tagLibrariesTree;
	private Form contentInfoForm;
	private String allDocuments = "All documents";
	private HierarchicalContainer documentsContainer;
	private HashMap<String, SourceDocumentFilter> filters = 
			new HashMap<String, SourceDocumentFilter>();
	
	private Button btOpenDocument;
	private Button btAddDocument;
	private MenuItem miMoreDocumentActions;
	
	private Button btCreateCorpus;
	private MenuItem miMoreCorpusActions;
	private MenuItem miRemoveCorpus;
	
	private Button btOpenTagLibrary;
	private Button btCreateTagLibrary;
	private MenuItem miMoreTagLibraryActions;
	
	private Button btEditContentInfo;
	private Button btSaveContentInfoChanges;
	private Button btDiscardContentInfoChanges;
	private PropertyChangeListener repositoryListener;
	
	public RepositoryView(Repository repository) {
		super();
		this.repository = repository;
		repositoryListener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
						Repository.PropertyChangeEvent.sourceDocumentAdded.name())) {
					SourceDocument sd = RepositoryView.this.repository.getSourceDocument(
							(String)evt.getNewValue());
					addSourceDocumentToTree(sd);
				}
			}
		};
		this.repository.addPropertyChangeListener(
				Repository.PropertyChangeEvent.sourceDocumentAdded,
				repositoryListener);
		
		initComponents();
		initActions();
	}

	private void initActions() {
		btAddDocument.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				WizardFactory factory = new WizardFactory();
				final WizardResult wizardResult = new WizardResult();
				WizardWindow sourceDocCreationWizardWindow = 
						factory.createWizardWindow(new WizardProgressListener() {
					
					public void wizardCompleted(WizardCompletedEvent event) {
						event.getWizard().removeListener(this);
						try {
							repository.insert(wizardResult.getSourceDocument());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					public void wizardCancelled(WizardCancelledEvent event) {
						event.getWizard().removeListener(this);
					}
					
					public void stepSetChanged(WizardStepSetChangedEvent event) {/*not needed*/}
					
					public void activeStepChanged(WizardStepActivationEvent event) {/*not needed*/}
				}, 
				wizardResult);
				getApplication().getMainWindow().addWindow(sourceDocCreationWizardWindow);
				sourceDocCreationWizardWindow.center();
			}
		});
		
		btOpenDocument.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
				/*
				 * hier gehts weiter:
				 * tagger einbauen + document laden
				 */
				Object value = documentsTree.getValue();
				
				if (value instanceof SourceDocumentEntry) {
					((CleaApplication)getApplication()).openSourceDocument(
							((SourceDocumentEntry)value).getSourceDocument());
				}
				
			}
		});
		corporaTree.addListener(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				boolean corpusRemoveButtonEnabled = false;
				if (value != null) {
					documentsContainer.removeAllContainerFilters();
					
					if (!value.equals(allDocuments)) {
						Corpus corpus = (Corpus)value;
						
						if (!filters.containsKey(value)) {
							filters.put(
									corpus.toString(), new SourceDocumentFilter(corpus));
						}
						SourceDocumentFilter sdf = filters.get(corpus.toString());
						documentsContainer.addContainerFilter(sdf);
						if(documentsContainer.size() > 0) {
							documentsTree.setValue(documentsContainer.getIdByIndex(0));
						}
						corpusRemoveButtonEnabled = true;
					}

				}
				
				miRemoveCorpus.setEnabled(corpusRemoveButtonEnabled);
			}
		});
		
		miMoreCorpusActions.addItem("Analyze Corpus", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miRemoveCorpus = miMoreCorpusActions.addItem("Remove Corpus", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miRemoveCorpus.setEnabled(false);
		
		documentsTree.addListener(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				if (value != null) {
					TreeEntry entry = (TreeEntry)value;
					contentInfoForm.setItemDataSource(entry.getContentInfo());
					contentInfoForm.setReadOnly(true);
				}
			}
			
		});
		
		tagLibrariesTree.addListener(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				btOpenTagLibrary.setEnabled(event.getProperty().getValue()!=null);
			}
		});
		
		btOpenTagLibrary.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				Object value = tagLibrariesTree.getValue();
				if (value != null) {
					TagLibraryReference tagLibraryReference = 
							((TagLibraryEntry)value).getTagLibraryReference();
					TagLibrary tagLibrary = repository.getTagLibrary(tagLibraryReference);
					((CleaApplication)getApplication()).openTagLibrary(tagLibrary);
				}				
			}
		});
		
		miMoreDocumentActions.addItem("Remove Document", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addItem("Export Document", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addSeparator();
		
		miMoreDocumentActions.addItem("Create User Markup Collection", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
			}
		});
		
		miMoreDocumentActions.addItem("Import User Markup Collection", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addItem("Export User Markup Collection", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addItem("Remove User Markup Collection", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addSeparator();
		
		miMoreDocumentActions.addItem("Create Static Markup Collection", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreDocumentActions.addItem("Import Static Markup Collection", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});	
		
		miMoreDocumentActions.addItem("Export Static Markup Collection", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});	
		
		
		miMoreDocumentActions.addItem("Remove Static Markup Collection", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});	
		
		btEditContentInfo.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				btEditContentInfo.setVisible(false);
				btSaveContentInfoChanges.setVisible(true);
				btDiscardContentInfoChanges.setVisible(true);
				contentInfoForm.setReadOnly(false);
			}
		});
		
		btSaveContentInfoChanges.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				btEditContentInfo.setVisible(true);
				btSaveContentInfoChanges.setVisible(false);
				btDiscardContentInfoChanges.setVisible(false);
				contentInfoForm.commit();
				contentInfoForm.setReadOnly(true);				
			}
		});
		
		btDiscardContentInfoChanges.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				btEditContentInfo.setVisible(true);
				btSaveContentInfoChanges.setVisible(false);
				btDiscardContentInfoChanges.setVisible(false);
				contentInfoForm.discard();
				contentInfoForm.setReadOnly(true);				
			}
		});

		miMoreTagLibraryActions.addItem("Import Tag Library", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreTagLibraryActions.addItem("Export Tag Library", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
		
		miMoreTagLibraryActions.addItem("Remove Tag Library", new Command() {
			
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	

	private void initComponents() {
		this.setMargin(true, true, true, true);
		this.setSpacing(true);
		Label documentsLabel = new Label("Document Manager");
		documentsLabel.addStyleName("repo-title-label");
		addComponent(documentsLabel);
		
		HorizontalLayout documentsManagerPanel = new HorizontalLayout();
		documentsManagerPanel.setSpacing(true);
		documentsManagerPanel.setSizeFull();
		documentsManagerPanel.setMargin(false, false, true, false);
		
		VerticalLayout outerCorporaPanel = new VerticalLayout();
		outerCorporaPanel.setSpacing(true);
		
		Panel corporaPanel = new Panel();
		corporaPanel.getContent().setSizeUndefined();
		corporaPanel.setHeight("200px");
		
		corporaTree = new Tree();
		corporaTree.addStyleName("repo-tree");
		corporaTree.setCaption("Corpora");
		corporaTree.addItem(allDocuments);
		corporaTree.setChildrenAllowed(allDocuments, false);
		corporaTree.setImmediate(true);
		
		for (Corpus c : repository.getCorpora()) {
			corporaTree.addItem(c);
			corporaTree.setChildrenAllowed(c, false);
		}
		
		corporaPanel.addComponent(corporaTree);
		outerCorporaPanel.addComponent(corporaPanel);
		
		Panel corporaButtonsPanel = new Panel(new HorizontalLayout());
		corporaButtonsPanel.setStyleName(Reindeer.PANEL_LIGHT);
		((HorizontalLayout)corporaButtonsPanel.getContent()).setSpacing(true);
		
		btCreateCorpus = new Button("Create Corpus");
		
		corporaButtonsPanel.addComponent(btCreateCorpus);
		MenuBar menuMoreCorpusActions = new MenuBar();
		miMoreCorpusActions = menuMoreCorpusActions.addItem("More actions...", null);
		corporaButtonsPanel.addComponent(menuMoreCorpusActions);
		
		outerCorporaPanel.addComponent(corporaButtonsPanel);
		
		documentsManagerPanel.addComponent(outerCorporaPanel);
		documentsManagerPanel.setExpandRatio(outerCorporaPanel, 1.3f);
		
		VerticalLayout outerDocumentsPanel = new VerticalLayout();
		outerDocumentsPanel.setSpacing(true);
		
		Panel documentsPanel = new Panel();
		
		documentsContainer = new HierarchicalContainer();
		documentsTree = new Tree();
		documentsTree.setContainerDataSource(documentsContainer);
		documentsTree.setCaption("Documents");
		documentsTree.addStyleName("repo-tree");
		documentsTree.setImmediate(true);
		
		documentsPanel.addComponent(documentsTree);
		documentsPanel.getContent().setSizeUndefined();
		documentsPanel.setHeight("200px");
		
		for (SourceDocument sd : repository.getSourceDocuments()) {
			addSourceDocumentToTree(sd);
		}
		outerDocumentsPanel.addComponent(documentsPanel);
		
		Panel documentButtonsPanel = new Panel(new HorizontalLayout());
		documentButtonsPanel.setStyleName(Reindeer.PANEL_LIGHT);

		((HorizontalLayout)documentButtonsPanel.getContent()).setSpacing(true);
		
		btOpenDocument = new Button("Open Document");
		documentButtonsPanel.addComponent(btOpenDocument);
		btAddDocument = new Button("Add Document");
		documentButtonsPanel.addComponent(btAddDocument);

		MenuBar menuMoreDocumentActions = new MenuBar();
		miMoreDocumentActions = menuMoreDocumentActions.addItem("More actions...", null);
		documentButtonsPanel.addComponent(menuMoreDocumentActions);
		
		outerDocumentsPanel.addComponent(documentButtonsPanel);
		
		documentsManagerPanel.addComponent(outerDocumentsPanel);
		documentsManagerPanel.setExpandRatio(outerDocumentsPanel, 2);
		
		VerticalLayout contentInfoPanel = new VerticalLayout();
		contentInfoPanel.setSpacing(true);
		
		contentInfoForm = new Form();
		contentInfoForm.setCaption("Information");
		contentInfoForm.setWriteThrough(false);
		
		BeanItem<ContentInfo> contentInfoItem = new BeanItem<ContentInfo>(new StandardContentInfo());
		contentInfoForm.setItemDataSource(contentInfoItem);
		contentInfoForm.setVisibleItemProperties(new String[] {
				"title", "author", "description", "publisher"
		});
		
		contentInfoForm.setReadOnly(true);
		contentInfoPanel.addComponent(contentInfoForm);
		
		Panel contentInfoButtonsPanel = new Panel(new HorizontalLayout());
		contentInfoButtonsPanel.setStyleName(Reindeer.PANEL_LIGHT);
		((HorizontalLayout)contentInfoButtonsPanel.getContent()).setSpacing(true);
		
		btEditContentInfo = new Button("Edit");
		contentInfoButtonsPanel.addComponent(btEditContentInfo);
		btSaveContentInfoChanges = new Button("Save");
		btSaveContentInfoChanges.setVisible(false);
		contentInfoButtonsPanel.addComponent(btSaveContentInfoChanges);
		btDiscardContentInfoChanges = new Button("Discard");
		btDiscardContentInfoChanges.setVisible(false);
		contentInfoButtonsPanel.addComponent(btDiscardContentInfoChanges);
		
		contentInfoPanel.addComponent(contentInfoButtonsPanel);
		
		documentsManagerPanel.addComponent(contentInfoPanel);
		documentsManagerPanel.setExpandRatio(contentInfoPanel, 1);
		
		addComponent(documentsManagerPanel);
		corporaTree.setValue(allDocuments);
		
		VerticalLayout tagLibraryContainer = new VerticalLayout();
		tagLibraryContainer.setSpacing(true);
		
		Panel tagLibraryPanel = new Panel();
		
		tagLibrariesTree = new Tree();
		tagLibrariesTree.setCaption("Tag Libraries");
		tagLibrariesTree.addStyleName("repo-tree");
		tagLibrariesTree.setImmediate(true);
		
		for (TagLibraryReference tlr : repository.getTagLibraryReferences()) {
			TreeEntry entry = new TagLibraryEntry(tlr);
			tagLibrariesTree.addItem(entry);
			tagLibrariesTree.setChildrenAllowed(entry, false);
		}
		
		tagLibraryPanel.addComponent(tagLibrariesTree);
		
		HorizontalLayout tagLibraryButtonPanel = new HorizontalLayout();
		tagLibraryButtonPanel.setSpacing(true);
		
		btOpenTagLibrary = new Button("Open Tag Library");
		btOpenTagLibrary.setEnabled(false);
		tagLibraryButtonPanel.addComponent(btOpenTagLibrary);

		btCreateTagLibrary = new Button("Create Tag Library");
		tagLibraryButtonPanel.addComponent(btCreateTagLibrary);
		
		MenuBar menuMoreTagLibraryActions = new MenuBar();
		miMoreTagLibraryActions = menuMoreTagLibraryActions.addItem("More actions...", null);
		tagLibraryButtonPanel.addComponent(menuMoreTagLibraryActions);
		
		tagLibraryContainer.addComponent(tagLibraryPanel);
		tagLibraryContainer.addComponent(tagLibraryButtonPanel);
		
		addComponent(tagLibraryContainer);
	}

	public Repository getRepository() {
		return repository;
	}
	
	@Override
	public void detach() {
		this.repository.removePropertyChangeListener(
				Repository.PropertyChangeEvent.sourceDocumentAdded,
				repositoryListener);
		super.detach();
	}
	
	private void addSourceDocumentToTree(SourceDocument sd) {
		TreeEntry sourceDocEntry = new SourceDocumentEntry(sd);
		documentsTree.addItem(sourceDocEntry);
		documentsTree.setChildrenAllowed(sourceDocEntry, true);
		MarkupCollectionsEntry staticMarkupCollEntry = 
				new MarkupCollectionsEntry( 
						new MarkupCollectionsNode("Static Markup Collections"));
		
		documentsTree.addItem(staticMarkupCollEntry);
		documentsTree.setParent(staticMarkupCollEntry, sourceDocEntry);
		
		for (StaticMarkupCollectionReference smcr : sd.getStaticMarkupCollectionRefs()) {
			TreeEntry staticMarkupCollRefEntry = new MarkupCollectionEntry(smcr);
			documentsTree.addItem(staticMarkupCollRefEntry);
			documentsTree.setParent(staticMarkupCollRefEntry, staticMarkupCollEntry);
			documentsTree.setChildrenAllowed(staticMarkupCollRefEntry, false);
		}
		
		MarkupCollectionsEntry userMarkupCollEntry =
				new MarkupCollectionsEntry(
						new MarkupCollectionsNode("User Markup Collections"));
		documentsTree.addItem(userMarkupCollEntry);
		documentsTree.setParent(userMarkupCollEntry, sourceDocEntry);
	
		for (UserMarkupCollectionReference ucr : sd.getUserMarkupCollectionRefs()) {
			TreeEntry userMarkupCollRefEntry = new MarkupCollectionEntry(ucr);
			documentsTree.addItem(userMarkupCollRefEntry);
			documentsTree.setParent(userMarkupCollRefEntry, userMarkupCollEntry);
			documentsTree.setChildrenAllowed(userMarkupCollRefEntry, false);

		}
	}
}

