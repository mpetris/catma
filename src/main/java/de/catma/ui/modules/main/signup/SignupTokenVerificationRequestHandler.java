package de.catma.ui.modules.main.signup;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

public class SignupTokenVerificationRequestHandler implements RequestHandler {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final SignupTokenManager signupTokenManager = new SignupTokenManager();

	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {		
		/* routing of verify account! */
		if(! session.getUIs().isEmpty()){
			if(request.getPathInfo() != null ){
				if(signupTokenManager.parseUri(request.getPathInfo())) {
					EventBus eventBus = VaadinSession.getCurrent().getAttribute(EventBus.class);
					SignupTokenManager tokenManager = new SignupTokenManager();
					tokenManager.handleVerify( request.getParameter("token"), eventBus);
				}
			}
		}
		return false;
	}

}
