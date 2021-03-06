package de.catma.ui.module.dashboard;

import java.io.IOException;
import java.util.Objects;

import org.vaadin.dialogs.ConfirmDialog;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import de.catma.project.ProjectManager;
import de.catma.project.ProjectReference;
import de.catma.rbac.IRBACManager;
import de.catma.rbac.RBACConstraint;
import de.catma.rbac.RBACConstraintEnforcer;
import de.catma.rbac.RBACPermission;
import de.catma.rbac.RBACRole;
import de.catma.ui.component.IconButton;
import de.catma.ui.events.ProjectChangedEvent;
import de.catma.ui.events.routing.RouteToProjectEvent;
import de.catma.ui.layout.FlexLayout;
import de.catma.ui.layout.HorizontalFlexLayout;
import de.catma.ui.layout.VerticalFlexLayout;
import de.catma.ui.module.main.ErrorHandler;

/**
 * Displays a single project reference as a card
 *
 * @author db
 */
public class ProjectCard extends VerticalFlexLayout  {
	
	public interface ClickAction {
		public void projectCardClicked(ProjectReference projectReference);
	}

    private ProjectReference projectReference;

    private final ErrorHandler errorLogger;
    private final ProjectManager projectManager;

	private final EventBus eventBus;

	private final IRBACManager rbacManager;
	
	private final RBACConstraintEnforcer<RBACRole> rbacEnforcer = new RBACConstraintEnforcer<>();

	private Label descriptionLabel;
	private Label nameLabel;

	private final ClickAction clickAction;
	
	ProjectCard(ProjectReference projectReference, ProjectManager projectManager, 
    		EventBus eventBus, IRBACManager rbacManager) {
		this(projectReference, projectManager, eventBus, rbacManager, 
				ref -> eventBus.post(new RouteToProjectEvent(ref, false)));
	}
	
	public ProjectCard(ProjectReference projectReference, ProjectManager projectManager, 
    		EventBus eventBus, ClickAction clickAction) {
		this(projectReference, projectManager, eventBus, new NoopRBACManager(), 
				ref -> clickAction.projectCardClicked(ref));
	}

    private ProjectCard(
    		ProjectReference projectReference, ProjectManager projectManager, 
    		EventBus eventBus, IRBACManager rbacManager, 
    		ClickAction clickAction){
        this.projectReference = Objects.requireNonNull(projectReference) ;
        this.projectManager = projectManager;
        this.eventBus = eventBus;
        this.rbacManager = rbacManager;
        this.errorLogger = (ErrorHandler) UI.getCurrent();
        this.clickAction = clickAction;
        initComponents();
        initData();
    }



    private void initData() {
		try {
			RBACRole projectRole = rbacManager.getRoleOnProject(projectManager.getUser(), projectReference.getProjectId());
			rbacEnforcer.enforceConstraints(projectRole); // normally done in reload();
		} catch (IOException e) {
            errorLogger.showAndLogError("can't fetch permissions" + projectReference.getName(), e);
		}
	}

	protected void initComponents() {
        addStyleName("projectlist__card");

        CssLayout preview = new CssLayout();
        preview.addStyleName("projectlist__card__preview");
        descriptionLabel = new Label(projectReference.getDescription());
        descriptionLabel.setWidth("100%");
        preview.addComponents(descriptionLabel);

        preview.addLayoutClickListener(evt -> handleOpenProjectRequest());
        addComponent(preview);

        HorizontalFlexLayout descriptionBar = new HorizontalFlexLayout();
        descriptionBar.addStyleName("projectlist__card__descriptionbar");
        descriptionBar.setAlignItems(FlexLayout.AlignItems.BASELINE);
        descriptionBar.setWidth("100%");
        
        
        nameLabel = new Label(projectReference.getName());
        nameLabel.setWidth("100%");
        
        descriptionBar.addComponent(nameLabel);

        IconButton btnRemove = new IconButton(VaadinIcons.TRASH);
        descriptionBar.addComponents(btnRemove);

        btnRemove.addClickListener(
            (event -> {
                ConfirmDialog.show(UI.getCurrent(),"Delete Project",
                        "Do you want to delete the whole Project '" + projectReference.getName() + "'?",
                        "OK",
                        "Cancel"
                , (evt) -> {
                    try {
                        if(evt.isConfirmed()){
                        	projectManager.delete(projectReference.getProjectId());
                        	eventBus.post(new ProjectChangedEvent(projectReference.getProjectId()));
                        }
                    } catch (Exception e) {
                        errorLogger.showAndLogError("can't delete Project " + projectReference.getName(), e);
                    }
                });
            })
        );
        
        IconButton btnEdit = new IconButton(VaadinIcons.PENCIL);
        btnEdit.addClickListener(click -> {
        	new EditProjectDialog(
        			projectReference, 
        			projectManager,
        			result -> {
        				try {
							projectManager.updateProject(result);
							descriptionLabel.setValue(result.getDescription());
							nameLabel.setValue(result.getName());
						} catch (IOException e) {
							errorLogger.showAndLogError("Failed to update Project", e);
							eventBus.post(new ProjectChangedEvent());
						}
        			}).show();
        });
        descriptionBar.addComponent(btnEdit);
        
        IconButton btnLeave = new IconButton(VaadinIcons.EXIT);
        btnLeave.addClickListener(
		   (event -> {
               ConfirmDialog.show(UI.getCurrent(),"Leave Project",
                       "Do you want to leave '" + projectReference.getName() + "'?",
                       "OK",
                       "Cancel"
               , (evt) -> {
                   try {
                       if(evt.isConfirmed()) {
                    	   projectManager.leaveProject(projectReference.getProjectId());
                       }
                   } catch (Exception e) {
                       errorLogger.showAndLogError("can't leave project " + projectReference.getName(), e);
                   }
                   eventBus.post(new ProjectChangedEvent());
               });
           })
		
		);
        
        descriptionBar.addComponent(btnLeave);
        
        rbacEnforcer.register(
        		RBACConstraint.ifNotAuthorized((role) -> 
        			(rbacManager.hasPermission(role, RBACPermission.PROJECT_EDIT)),
        			() -> { 
        				btnEdit.setVisible(false);
        				btnEdit.setEnabled(false);
        			})
        		);
        
        rbacEnforcer.register(
        		RBACConstraint.ifNotAuthorized((role) -> 
        			(rbacManager.hasPermission(role, RBACPermission.PROJECT_DELETE)),
        			() -> { 
        				btnRemove.setVisible(false);
        				btnRemove.setEnabled(false);
        			})
        		);
        
        rbacEnforcer.register(
        		RBACConstraint.ifNotAuthorized(
        				(role) -> 
        					rbacManager.hasPermission(role, RBACPermission.PROJECT_LEAVE)
        					&&
        					! rbacManager.hasPermission(role, RBACPermission.PROJECT_DELETE)	
        				,
        			() -> { 
        				btnLeave.setVisible(false);
        				btnLeave.setEnabled(false);
        			})
        		);
        
//        IconButton buttonAction = new IconButton(VaadinIcons.ELLIPSIS_DOTS_V);
//        descriptionBar.addComponents(buttonAction);

        addComponents(descriptionBar);
        
    }

    private void handleOpenProjectRequest() {
    	clickAction.projectCardClicked(this.projectReference);
    }



	public String toString() {
    	return projectReference.getProjectId() + " " + projectReference.getName() + " "+ projectReference.getDescription();
    }
}
