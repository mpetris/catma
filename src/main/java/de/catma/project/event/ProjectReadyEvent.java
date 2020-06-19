package de.catma.project.event;

import de.catma.project.Project;

public class ProjectReadyEvent {
	private final Project project;

	public ProjectReadyEvent(Project project) {
		super();
		this.project = project;
	}
	
	public Project getProject() {
		return project;
	}
}
