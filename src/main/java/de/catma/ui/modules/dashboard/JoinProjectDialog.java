package de.catma.ui.modules.dashboard;

import java.io.IOException;

import javax.cache.Cache;
import javax.cache.Caching;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.jsoniter.JsonIterator;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.catma.DammAlgorithm;
import de.catma.config.HazelcastConfiguration;
import de.catma.rbac.IRBACManager;
import de.catma.rbac.RBACRole;
import de.catma.rbac.RBACSubject;
import de.catma.ui.events.ResourcesChangedEvent;
import de.catma.ui.layout.FlexLayout.JustifyContent;
import de.catma.ui.layout.HorizontalLayout;
import de.catma.ui.layout.VerticalLayout;
import de.catma.ui.modules.main.ErrorHandler;
import de.catma.ui.modules.project.ProjectInvitation;

/**
 * Dialog that creates a Project
 * 
 * @author db
 *
 */
public class JoinProjectDialog extends Window {

	private final TextField tfCode = new TextField("Code");
	private final TextField tfName = new TextField("Name");
	private final TextArea taDescription = new TextArea("Decription");
	private final ComboBox<RBACRole> cbRole = new ComboBox<RBACRole>("role", 
			Lists.newArrayList(RBACRole.values()));
    private final Cache<Integer, String> invitationCache = 
    		Caching.getCachingProvider().getCacheManager().getCache(HazelcastConfiguration.CACHE_KEY_INVITATIONS);
    private final ErrorHandler errorLogger;
    private final VerticalLayout content = new VerticalLayout();
    private final IRBACManager privilegedRBACManager;
    private final RBACSubject currentUser;

    private final Button btnJoin = new Button("Join");
    private final Button btnCancel = new Button("Cancel");
    private final EventBus eventBus;

	private ProjectInvitation invitation;
		
	@Inject
	public JoinProjectDialog(IRBACManager privilegedRBACManager, RBACSubject currentUser, EventBus eventBus) {
		super("Join project");
		this.privilegedRBACManager = privilegedRBACManager;
		this.currentUser = currentUser;
		this.eventBus = eventBus;
	    this.errorLogger = (ErrorHandler) UI.getCurrent();
		initComponents();
	}
	
	private void initComponents() {		
		content.addStyleName("spacing");
		content.addStyleName("margin");

		Label lDescription = new Label("Please enter your invitation code to find and join a project");
		content.addComponent(lDescription);
		
		tfCode.setWidth("100%");
		tfCode.setCaption("Invitation code");
		tfCode.setDescription("Enter your invitation code here");
		tfCode.addValueChangeListener(this::onCodeEntered);
		content.addComponent(tfCode);
		
		tfName.setWidth("100%");
		tfName.setCaption("");
		tfName.setReadOnly(true);
		tfName.setVisible(false);
		content.addComponent(tfName);
		
		cbRole.setWidth("100%");
		cbRole.setItemCaptionGenerator(RBACRole::name);
		cbRole.setEmptySelectionAllowed(false);
		cbRole.setReadOnly(true);
		cbRole.setVisible(false);
		content.addComponent(cbRole);
		
		taDescription.setWidth("100%");
		taDescription.setHeight("100%");
		taDescription.setVisible(false);
		
		content.addComponent(taDescription);
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.addStyleName("spacing-left-right");
		buttonPanel.setJustifyContent(JustifyContent.FLEX_END);
		
		btnJoin.addClickListener(this::handleJoinPressed);
		btnJoin.setEnabled(false);
		
		btnCancel.addClickListener(evt -> close());

		buttonPanel.addComponent(btnJoin);
		buttonPanel.addComponent(btnCancel);
		
		content.addComponent(buttonPanel);
		setContent(content);

	}

	private void handleJoinPressed(ClickEvent event) {
		if(invitation != null) {
			try {
				privilegedRBACManager.assignOnProject(currentUser, 
						RBACRole.forValue(invitation.getDefaultRole()), invitation.getProjectId());
				
				Notification.show("Joined successfully", "sucessfully join project " + invitation.getName() , Type.HUMANIZED_MESSAGE);
				eventBus.post(new ResourcesChangedEvent<Component>(this));
				this.close();
				
			} catch (IOException e) {
				errorLogger.showAndLogError("Can't join project", e);
			}
		}
	}
	
	private void onCodeEntered(ValueChangeEvent<String> changeEvent){
		try { 
			Integer code = Integer.parseInt(changeEvent.getValue());
			if(DammAlgorithm.validate(code)){
				String marshalledInvitation = invitationCache.get(code);
				if(marshalledInvitation != null){
					invitation = JsonIterator.deserialize(marshalledInvitation, ProjectInvitation.class);
					
					tfCode.setReadOnly(true);
					tfName.setValue(invitation.getName());
					tfName.setVisible(true);
					taDescription.setValue(invitation.getDescription() == null ? "nonexistent description": invitation.getDescription() );
					taDescription.setVisible(true);
					cbRole.setValue(RBACRole.forValue(invitation.getDefaultRole()));
					cbRole.setVisible(true);
					btnJoin.setEnabled(true);
					
				}
			}
		} catch (NumberFormatException ne){
			//NOOP
		} 
	}
	
	public void show(){
		UI.getCurrent().addWindow(this);
	}
	
	@Override
	public void close() {
		super.close();
	}

}
