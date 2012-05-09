package de.catma.ui.repository;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet.CloseHandler;

import de.catma.core.document.repository.Repository;
import de.catma.core.document.repository.RepositoryManager;
import de.catma.ui.tabbedview.TabbedView;

public class RepositoryManagerView extends TabbedView implements CloseHandler {

	private RepositoryListView repositoryListView;
	
	
	public RepositoryManagerView(RepositoryManager repositoryManager) {
		super("No repositories available.");
		repositoryListView = new RepositoryListView(repositoryManager);
		addTab(repositoryListView, "Repositories Overview");
	}


	public void openRepository(Repository repository) {
		RepositoryView repositoryView = getRepositoryView(repository);
		if (repositoryView != null) {
			setSelectedTab(repositoryView);
		}
		else {
			RepositoryView repoView = new RepositoryView(repository);
			addClosableTab(repoView, repository.getName());
			setSelectedTab(repoView);
		}
	}
	
	private RepositoryView getRepositoryView(Repository repository) {
		for (Component tabContent : this) {
			if (tabContent != repositoryListView) {
				RepositoryView view = (RepositoryView)tabContent;
				Repository curRepo = view.getRepository();
				if ((curRepo !=null) && curRepo.equals(repository)) {
					return view;
				}
			}
		}
		return null;
	}
}
