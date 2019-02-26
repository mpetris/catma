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

import javax.servlet.ServletException;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinServlet;

import de.catma.document.repository.RepositoryPropertyKey;
import de.catma.ui.di.GitlabModule;
import de.catma.ui.di.Vaadin8Module;
import de.catma.ui.login.GitlabLoginService;
import de.catma.ui.modules.main.signup.SignupTokenVerificationRequestHandler;

public class CatmaApplicationServlet extends VaadinServlet {
	
	private enum JsLib {
//		JQUERY("jquery/jquery-1.7.2.min.js"),
		HIGHCHARTS_SL("highcharts/standalone-framework-4.0.3.js"),
		HIGHCHARTS("highcharts/highcharts-4.0.3.js"),
//		EXPORTING("highcharts/exporting.js"),
//		D3("doubletreejs/d3.min.js"),
//		CLASSLISTSUBSTITUTE("doubletreejs/classListSubstitute.js"),
//		DOUBLETREE("doubletreejs/DoubleTree.js"),
//		DT_TRIE("doubletreejs/Trie.js"),
		D3("doubletreejs/d3.min.js"),
		CLASSLISTSUBSTITUTE("doubletreejs/classListSubstitute.min.js"),
		DOUBLETREE("doubletreejs/DoubleTree.min.js"),
		DT_TRIE("doubletreejs/Trie.min.js"),
		VEGA("vega/vega.js"),
		VEGA_LITE("vega/vega-lite.js"),
		VEGA_EMBED("vega/vega-embed.js"),
		;
		String relFilePath;

		private JsLib(String relFilePath) {
			this.relFilePath = relFilePath;
		}
		
		@Override
		public String toString() {
			return relFilePath;
		}
	}
	
	private enum CssLib {
		DOUBLETREE("doubletreejs/doubletree.css"),
		;
		String relFilePath;

		private CssLib(String relFilePath) {
			this.relFilePath = relFilePath;
		}
		
		@Override
		public String toString() {
			return relFilePath;
		}
	}
	
	private static class CatmaBootstrapListener implements BootstrapListener {
		@Override
		public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
			// noop
		}
		@Override
		public void modifyBootstrapPage(BootstrapPageResponse response) {
//			response.getDocument().head().append("<script>"
//					+ "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){"
//					+ "(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),"
//					+ "m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)"
//					+ "})(window,document,'script','//www.google-analytics.com/analytics.js','ga');"
//					+ "ga('create', 'UA-38728736-8', 'auto');"
//					+ "ga('send', 'pageview');"
//					+ "</script>");
			
			for (CssLib lib : CssLib.values()) {
				response.getDocument().head().append(
					"<link rel=\"stylesheet\" href=\"" 
					+ response.getRequest().getContextPath() +"/VAADIN/" + lib + "\" />");
			}

			StringBuilder scriptBuilder = new StringBuilder();
			
			scriptBuilder.append(
					"<script type=\"text/javascript\">\n");
			scriptBuilder.append("//<![CDATA[\n");
			for (JsLib lib : JsLib.values()) {
				scriptBuilder.append(
						"document.write(\"<script language='javascript' src='" 
								+ response.getRequest().getContextPath() 
								+ "/VAADIN/" + lib + "'><\\/script>\");\n");
			}
//			scriptBuilder.append(
//				"document.write(\"<script language='javascript' src='https://cdnjs.cloudflare.com/ajax/libs/vega/3.0.0-rc3/vega.js'><\\/script>\");\n");
//			scriptBuilder.append(
//				"document.write(\"<script language='javascript' src='https://cdnjs.cloudflare.com/ajax/libs/vega-lite/2.0.0-beta.10/vega-lite.js'><\\/script>\");\n");
//			scriptBuilder.append(
//				"document.write(\"<script language='javascript' src='https://cdnjs.cloudflare.com/ajax/libs/vega-embed/3.0.0-beta.19/vega-embed.js'><\\/script>\");\n");
			
			
			scriptBuilder.append("//]]>\n</script>\n");
			
			response.getDocument().body().prepend(scriptBuilder.toString());
		}
	}
	
	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		getService().addSessionInitListener(new SessionInitListener() {
			
			@Override
			public void sessionInit(SessionInitEvent event) throws ServiceException {
				event.getSession().addBootstrapListener(new CatmaBootstrapListener());
			}
		});
		
		getService().setSystemMessagesProvider(new SystemMessagesProvider() {
			@Override
			public SystemMessages getSystemMessages(
					SystemMessagesInfo systemMessagesInfo) {
				CustomizedSystemMessages messages = new CustomizedSystemMessages();
				try {
					String problemRedirectURL = 
							RepositoryPropertyKey.BaseURL.getValue( 
									RepositoryPropertyKey.BaseURL.getDefaultValue());

					messages.setAuthenticationErrorURL(problemRedirectURL);
					messages.setInternalErrorURL(problemRedirectURL);
					messages.setSessionExpiredURL(problemRedirectURL);
					messages.setCommunicationErrorURL(problemRedirectURL);
					messages.setCookiesDisabledURL(problemRedirectURL);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return messages;
			}
		});
		
		/* initialize global Eventbus */
		getService().addSessionInitListener(new SessionInitListener() {
			@Override
			public void sessionInit(SessionInitEvent event)
					throws ServiceException {
				event.getSession().setAttribute(EventBus.class, new EventBus());
			}
		});

		/* init token verifier */
		getService().addSessionInitListener(new SessionInitListener() {
			@Override
			public void sessionInit(SessionInitEvent event)
					throws ServiceException {				
				event.getSession().addRequestHandler(new SignupTokenVerificationRequestHandler());
			}
		});
	
		/* DI with google guice */
		getService().addSessionInitListener(new SessionInitListener() {
			@Override
			public void sessionInit(SessionInitEvent event)
					throws ServiceException {				
				event.getSession().setAttribute(Injector.class, Guice.createInjector(
						new ServletModule(), new GitlabModule(), new Vaadin8Module()
						));
			}
		});
	}
}
