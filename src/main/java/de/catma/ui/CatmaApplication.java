/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.ui;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import de.catma.backgroundservice.BackgroundService;
import de.catma.backgroundservice.BackgroundServiceProvider;
import de.catma.backgroundservice.ExecutionListener;
import de.catma.backgroundservice.LogProgressListener;
import de.catma.backgroundservice.ProgressCallable;
import de.catma.document.Corpus;
import de.catma.document.Range;
import de.catma.document.repository.Repository;
import de.catma.document.repository.RepositoryPropertyKey;
import de.catma.document.source.KeywordInContext;
import de.catma.document.source.SourceDocument;
import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;
import de.catma.indexer.IndexedRepository;
import de.catma.queryengine.result.QueryResult;
import de.catma.queryengine.result.computation.DistributionComputation;
import de.catma.queryengine.result.computation.DistributionSelectionListener;
import de.catma.repository.git.interfaces.IRemoteGitManagerRestricted;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagsetDefinition;
import de.catma.ui.analyzer.AnalyzerProvider;
import de.catma.ui.analyzer.QueryOptionsProvider;
import de.catma.ui.component.HTMLNotification;
import de.catma.ui.events.CloseableEvent;
import de.catma.ui.events.TokenInvalidEvent;
import de.catma.ui.events.TokenValidEvent;
import de.catma.ui.events.routing.RouteToDashboardEvent;
import de.catma.ui.login.InitializationService;
import de.catma.ui.login.LoginService;
import de.catma.ui.modules.main.ErrorHandler;
import de.catma.ui.modules.main.signup.CreateUserDialog;
import de.catma.ui.modules.main.signup.SignupTokenManager;
import de.catma.ui.tagger.TaggerView;
import de.catma.ui.tagmanager.TagsetSelectionListener;
import de.catma.ui.util.Version;

@Theme("catma")
@PreserveOnRefresh
@Push(value=PushMode.MANUAL,transport=Transport.WEBSOCKET_XHR )
public class CatmaApplication extends UI implements 
	BackgroundServiceProvider, ErrorHandler, AnalyzerProvider, ParameterProvider, FocusHandler {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Map<String, String[]> parameters = new HashMap<String, String[]>();
		
	private final List<WeakReference<Closeable>> closeListener = Lists.newArrayList();
	
	private final SignupTokenManager signupTokenManager = new SignupTokenManager();

	private final Class<? extends Annotation> loginType; 
	private final Class<? extends Annotation> initType;
	 
	private LoginService loginservice;
	private InitializationService initService;

	private final Injector injector;
	private final EventBus eventBus;
	
	@Inject
	public CatmaApplication(Injector injector, EventBus eventbus) throws IOException {
		this.injector = injector;
		this.eventBus = eventbus;
		this.eventBus.register(this);
			try {
				
				loginType = 
						(Class<? extends Annotation>)Class.forName(RepositoryPropertyKey.LoginType.getValue());
				initType = 
						(Class<? extends Annotation>)Class.forName(RepositoryPropertyKey.InitType.getValue());
				loginservice = injector.getInstance(Key.get(LoginService.class, loginType));
				initService = injector.getInstance(Key.get(InitializationService.class, initType));
			} catch (ClassNotFoundException e) {
				throw new IOException("Runtime configuration error", e);
			}
	}

	@Override
	protected void init(VaadinRequest request) {
		loginservice = injector.getInstance(Key.get(LoginService.class, loginType));
		initService = injector.getInstance(Key.get(InitializationService.class, initType));

		logger.info("Session: " + request.getWrappedSession().getId());
		storeParameters(request.getParameterMap());

		Page.getCurrent().setTitle(Version.LATEST.toString()); //$NON-NLS-1$
		
		try {
			Component component = initService.newEntryPage(loginservice);
			setContent(component);


	        // implement a custom resize propagation for all Layouts including CSSLayouts
	        JavaScript.getCurrent().addFunction("browserWindowResized", e -> {
	        	this.markAsDirtyRecursive();
	        });
	        Page.getCurrent().getJavaScript().execute(
	        		"var timeout = null;"
	        				+ "window.onresize = function() { "
	        				+ "  if (timeout != null) clearTimeout(timeout); "
	        				+ "  timeout = setTimeout(function() {"
	        				+ "    browserWindowResized(); "
	        				+ "  }, 250);"
	        				+ "}");
	                

		} catch (IOException e) {
			showAndLogError("error creating landing page",e);			
		}

		eventBus.post(new RouteToDashboardEvent());

		// A fresh UI and session doesn't have a request handler registered yet.
		// we need to verify tokens here too.
		
		if(signupTokenManager.parseUri(request.getPathInfo())) {
			SignupTokenManager tokenManager = new SignupTokenManager();
			tokenManager.handleVerify( request.getParameter("token"), eventBus);
		}
		
//		SignupTokenManager.parseToken(request.getPathInfo(), request.getParameter("token"));
	}
	
//			logger.info("closing session and redirecting to " + afterLogoutRedirectURL);
//			Page.getCurrent().setLocation(afterLogoutRedirectURL);
//			VaadinSession.getCurrent().close();
//	
	private void storeParameters(Map<String, String[]> parameters) {
		this.parameters.putAll(parameters);
	}

	public Map<String, String[]> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public String getParameter(Parameter parameter) {
		return getParameter(parameter.getKey());
	}

	public String getParameter(Parameter parameter, String defaultValue) {
		String value = getParameter(parameter.getKey());
		return value == null ? defaultValue : value;
	}

	public String getParameter(String key) {
		String[] values = parameters.get(key);
		if ((values != null) && (values.length > 0)) {
			return values[0];
		}

		return null;
	}

	public String[] getParameters(Parameter parameter) {
		return getParameters(parameter.getKey());
	}

	public String[] getParameters(String key) {
		return parameters.get(key);
	}

	
	@Deprecated
	public void addTagsetToActiveDocument(TagsetDefinition tagsetDefinition, TagsetSelectionListener tagsetSelectionListener) {
		
		//TODO: not needed anymore
	}
	
	public void addTagsetToActiveDocument(TagsetDefinition tagsetDefinition) {
		addTagsetToActiveDocument(tagsetDefinition, null);
	}

	public void openTagLibrary(Repository repository, TagLibrary tagLibrary) {
		openTagLibrary(repository, tagLibrary, true);
	}

	@Deprecated
	public void openTagLibrary(Repository repository, TagLibrary tagLibrary, boolean switchToTagManagerView) {
	}

	public TaggerView openSourceDocument(String sourceDocumentId) {
		//TODO:
//		RepositoryManager repositoryManager = repositoryManagerView.getRepositoryManager();
//
//		if (repositoryManager.hasOpenRepository()) {
//			Repository repository = repositoryManager.getFirstOpenRepository();
//
//			SourceDocument sourceDocument = repository.getSourceDocument(sourceDocumentId);
//			if (sourceDocument != null) {
//				return openSourceDocument(sourceDocument, repository);
//			}
//		}

		return null;
	}

	@Deprecated
	public TaggerView openSourceDocument(SourceDocument sourceDocument, Repository repository) {
		return null;
	}

	public String accquirePersonalTempFolder() throws IOException {
		return initService.accquirePersonalTempFolder();
	}

	public BackgroundService accuireBackgroundService() {
		return initService.accuireBackgroundService();
	}

	public <T> void submit(String caption, final ProgressCallable<T> callable, final ExecutionListener<T> listener) {
		logger.info("submitting job '" + caption + "' " + callable); //$NON-NLS-1$ //$NON-NLS-2$
		accuireBackgroundService().submit(callable, new ExecutionListener<T>() {
			public void done(T result) {
				listener.done(result);
			};

			public void error(Throwable t) {
				listener.error(t);
			}
		}, new LogProgressListener());
	}

	@Deprecated
	public void openUserMarkupCollection(SourceDocument sourceDocument, UserMarkupCollection userMarkupCollection,
			Repository repository) {
		//projectManagerView.openUserMarkupCollection(sourceDocument, userMarkupCollection, repository);
	}

	@Deprecated
	public void analyze(Corpus corpus, IndexedRepository repository) {
//		projectManagerView.analyze(corpus, repository);
	}

	public void analyzeCurrentlyActiveDocument() {
		//TODO:
//		projectManagerView.analyzeCurrentlyActiveDocument(repository);
	}
	
	

	public int addVisualization(Integer visualizationId, String caption,
			DistributionComputation distributionComputation,
			DistributionSelectionListener distributionSelectionListener) {

		//TODO:
//		menu.executeEntry(visualizationManagerView);
//
//		return visualizationManagerView.addVisualization(visualizationId, caption, distributionComputation,
//				distributionSelectionListener);
		
		return 0;
	}

	@Override
	public void close() {
		IRemoteGitManagerRestricted api = loginservice.getAPI();

		if(api != null){
			logger.info("application for user " + api.getUsername() + " has been closed");
		}
		
		for (WeakReference<Closeable> weakReference : closeListener) {
			Closeable ref = weakReference.get();
			if (ref != null) {
				try {
					ref.close();
				} catch (IOException e) {
					logger.log(Level.INFO,"couldn't cleanup resource",e);
				}
			}
		}
		
		initService.shutdown();
		super.close();
	}

	@Override
	public void showAndLogError(String message, Throwable e) {
		IRemoteGitManagerRestricted api = loginservice.getAPI();
		
		if(api != null){
			logger.log(Level.SEVERE, "[" + api.getUsername() + "]" + message, e); //$NON-NLS-1$ //$NON-NLS-2$
			
		}

		if (message == null) {
			message = Messages.getString("CatmaApplication.internalError"); //$NON-NLS-1$
		}
		if (Page.getCurrent() != null) {
			HTMLNotification.show(Messages.getString("CatmaApplication.error"), //$NON-NLS-1$
					MessageFormat.format(Messages.getString("CatmaApplication.errorOccurred"), message, e.getMessage()), //$NON-NLS-1$
					Type.ERROR_MESSAGE);
		}
	}

	public void openSourceDocument(SourceDocument sd, Repository repository, Range range) {
		TaggerView tv = openSourceDocument(sd, repository);
		tv.show(range);
	}

	public void addDoubleTree(List<KeywordInContext> kwics) {
		//TODO:
//		menu.executeEntry(visualizationManagerView);
//		visualizationManagerView.addDoubleTree(kwics);
	}

	public void addVega(QueryResult queryResult, QueryOptionsProvider queryOptionsProvider) {
		//TODO:
//		menu.executeEntry(visualizationManagerView);
//		visualizationManagerView.addVega(queryResult, queryOptionsProvider);
	}
	
	@Override
	public void focusDeferred(Focusable focusable) {
		schedule(() -> {
			getUI().access(() -> {
				focusable.focus();
				//push();
			});
			
		}, 1, TimeUnit.SECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable command,
			long delay, TimeUnit unit) {
		return accuireBackgroundService().schedule(command, delay, unit);
	}
	
	@Subscribe
	public void handleTokenValid(TokenValidEvent tokenValidEvent){
		getUI().access(() -> {
			CreateUserDialog createUserDialog = new CreateUserDialog("Create User", tokenValidEvent.getSignupToken());
			createUserDialog.show();
		});
	}
	
	@Subscribe
	public void handleTokenValid(TokenInvalidEvent tokenInvalidEvent){
		getUI().access(() -> {
			Notification.show(tokenInvalidEvent.getReason(), Type.WARNING_MESSAGE);
		});
	}
	
	@Subscribe
	public void handleClosableResources(CloseableEvent closeableEvent ){
		closeListener.add(new WeakReference<Closeable>(closeableEvent.getCloseable()));
	}
}
