/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.server.authn;

import java.util.Map;

import pl.edu.icm.unity.server.endpoint.BindingAuthn;
import pl.edu.icm.unity.types.authn.AuthenticationOptionDescription;

/**
 * Stores information about a single authentication option, as configured by administrator and selectable by 
 * a user or client. The option contains a primary authenticator and optionally advanced settings related to 
 * MFA or RBA.
 * <p>
 * This class is a working instance of what can be described by the {@link AuthenticationOptionDescription}.
 * 
 * @author K. Benedyczak
 */
public class AuthenticationOption
{
	private Map<String, BindingAuthn> authenticators;

	public AuthenticationOption(Map<String, BindingAuthn> authenticators)
	{
		this.authenticators = authenticators;
	}

	public Map<String, BindingAuthn> getAuthenticators()
	{
		return authenticators;
	}
}
