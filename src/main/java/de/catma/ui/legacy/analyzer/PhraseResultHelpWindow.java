package de.catma.ui.legacy.analyzer;


import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PhraseResultHelpWindow extends Window {
		
	public PhraseResultHelpWindow() {
		super(Messages.getString("helpTitle")); //$NON-NLS-1$
		initComponents();
		setHeight("500px"); //$NON-NLS-1$
		setWidth("400px"); //$NON-NLS-1$
		center();
		setStyleName("help-windows"); //$NON-NLS-1$
	}

	private void initComponents() {
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		content.setSpacing(true);
		
		Label helpText = new Label(Messages.getString("helpText"), ContentMode.HTML);  //$NON-NLS-1$
		content.addComponent( helpText);
		setContent(content);
		
	}

	
}