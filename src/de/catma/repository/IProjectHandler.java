package de.catma.repository;

import de.catma.repository.git.exceptions.ProjectHandlerException;

public interface IProjectHandler {
	String create(String name, String description) throws ProjectHandlerException;
	void delete(String projectId) throws ProjectHandlerException;
}
