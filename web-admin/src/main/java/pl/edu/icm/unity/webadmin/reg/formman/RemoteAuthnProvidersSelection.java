/*
 * Copyright (c) 2018 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.reg.formman;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import pl.edu.icm.unity.engine.api.authn.AuthenticationFlow;
import pl.edu.icm.unity.engine.api.authn.Authenticator;
import pl.edu.icm.unity.engine.api.authn.AuthenticatorSupportManagement;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;
import pl.edu.icm.unity.types.authn.AuthenticationOptionKey;
import pl.edu.icm.unity.webui.authn.VaadinAuthentication;
import pl.edu.icm.unity.webui.authn.VaadinAuthentication.Context;
import pl.edu.icm.unity.webui.authn.VaadinAuthentication.VaadinAuthenticationUI;
import pl.edu.icm.unity.webui.common.chips.ChipsWithDropdown;

/**
 * Customization of the {@link ChipsWithDropdown} for authentication flows
 * selection.
 *
 * @author Roman Krysinski (roman@unity-idm.eu)
 */
public class RemoteAuthnProvidersSelection extends ChipsWithDropdown<AuthenticationOptionKey>
{
	public RemoteAuthnProvidersSelection(AuthenticatorSupportManagement authenticatorSupport, String leftCaption,
			String rightCaption, String caption, String description) throws EngineException
	{
		super(AuthenticationOptionKey::toGlobalKey, true);
		setCaption(caption);
		setDescription(description);
		setWidth(100, Unit.PERCENTAGE);
		init(authenticatorSupport);
	}
	
	private void init(AuthenticatorSupportManagement authenticatorSupport) throws EngineException
	{
		List<AuthenticationFlowDefinition> definitions = authenticatorSupport.resolveAllRemoteAuthenticatorFlows(
				VaadinAuthentication.NAME);
		List<AuthenticationFlow> authenticators = authenticatorSupport.getAuthenticatorUIs(definitions);
		
		List<AuthenticationOptionKey> authnOptions = Lists.newArrayList();
		for (AuthenticationFlow authenticatorFlow : authenticators)
		{
			String authenticatorKey = authenticatorFlow.getId();
			for (Authenticator authenticator : authenticatorFlow.getFirstFactorAuthenticators())
			{
				VaadinAuthentication vaadinAuthenticator = (VaadinAuthentication) authenticator.getRetrieval();
				Collection<VaadinAuthenticationUI> instances = vaadinAuthenticator.createUIInstance(Context.REGISTRATION);
				for (VaadinAuthenticationUI instance : instances)
				{
					String optionKey = instance.getId();
					authnOptions.add(new AuthenticationOptionKey(authenticatorKey, optionKey));
				}
			}
		}
		setItems(authnOptions);
	}
}
