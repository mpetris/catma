package de.catma.ui.analyzenew;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.catma.ui.layout.VerticalLayout;
import de.catma.ui.layout.HorizontalLayout;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;



public class VizSnapshot extends Panel {
	private Button btEdit;
	private Button btRemove;
	private ResourceOrganiserPanel kwicVizPanel;
	private EditVizSnapshotListener editVizSnapshotListener;
	private DeleteVizSnapshotListener deleteVizSnapshotListener;
	
	
	public EditVizSnapshotListener getEditVizSnapshotListener() {
		return editVizSnapshotListener;
	}

	public void setEditVizSnapshotListener(EditVizSnapshotListener editVizSnapshotListener) {
		this.editVizSnapshotListener = editVizSnapshotListener;
	}
	
	public DeleteVizSnapshotListener getDeleteVizSnapshotListener() {
		return deleteVizSnapshotListener;
	}

	public void setDeleteVizSnapshotListener(DeleteVizSnapshotListener deleteVizSnapshotListener) {
		this.deleteVizSnapshotListener = deleteVizSnapshotListener;
	}

	public ResourceOrganiserPanel getKwicVizPanel() {
		return kwicVizPanel;
	}

	public void setKwicVizPanel(ResourceOrganiserPanel kwicVizPanel) {
		this.kwicVizPanel = kwicVizPanel;
	}

	public VizSnapshot(String title) {
		initComponents(title);
		initListeners();
	}
	
	private void initComponents(String title) {

		this.addStyleName("analyze_queryresultpanel__card_frame");
		VerticalLayout content = new VerticalLayout();
		content.addStyleName("analyze_queryresultpanel__card");
		
		Label titleLabel = new Label(title);
		titleLabel.addStyleName("analyze_queryresultpanel_infobar");
		
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.addStyleName("analyze_queryresultpanel_buttonbar");

		btRemove = new Button ("",VaadinIcons.ERASER);
		btRemove.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		
		btEdit = new Button ("",VaadinIcons.ARROW_RIGHT);
		btEdit.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		
		buttonBar.addComponents(btRemove,btEdit);
		content.addComponents(titleLabel,buttonBar);
		
		setContent(content);
	}
	
	private void initListeners() {
		btEdit.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				editVizSnapshotListener.reopenKwicView();
				
			}
		});
				
		btRemove.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
			deleteVizSnapshotListener.deleteSnapshot();
				
			}
		});
	}
	
	
	
	

}
