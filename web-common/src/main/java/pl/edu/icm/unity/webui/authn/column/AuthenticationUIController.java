/*
 * Copyright (c) 2018 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.authn.column;

import com.vaadin.server.VaadinRequest;

import pl.edu.icm.unity.webui.authn.VaadinAuthentication.VaadinAuthenticationUI;

/**
 * Allows for controlling wrapped {@link VaadinAuthenticationUI}
 */
interface AuthenticationUIController
{
	void cancel();
	void clear();
	void refresh(VaadinRequest request);
	boolean focusIfPossible();
	String getAuthenticationOptionId();
}