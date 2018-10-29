package de.catma.v10ui.di;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.vaadin.guice.annotation.UIScope;
import de.catma.project.ProjectManager;
import de.catma.repository.git.GitProjectManager;
import de.catma.v10ui.modules.analyze.AnalyzeView;
import de.catma.v10ui.modules.dashboard.DashboardView;
import de.catma.v10ui.modules.main.ErrorLogger;
import de.catma.v10ui.modules.project.ProjectView;
import de.catma.v10ui.modules.tags.TagsView;

/**
 * Provides all views
 */
public class UIModule extends AbstractModule {


    @Provides
    @UIScope
    ProjectView provideProject(GitProjectManager gitProjectManager, ErrorLogger errorLogger, EventBus eventBus){
        return new ProjectView(gitProjectManager, errorLogger, eventBus);
    }

    @Provides
    @UIScope()
    public AnalyzeView provideAnalyzeView(){
        return new AnalyzeView();
    }

    @Provides
    public DashboardView provideDashboardView(GitProjectManager gitProjectManager, ErrorLogger errorLogger){
        return new DashboardView(gitProjectManager, errorLogger);
    }

    @Provides
    @UIScope
    TagsView provideTags(GitProjectManager projectManager, ErrorLogger errorLogger, EventBus eventBus){
        return new TagsView(projectManager, errorLogger, eventBus);
    }

}
