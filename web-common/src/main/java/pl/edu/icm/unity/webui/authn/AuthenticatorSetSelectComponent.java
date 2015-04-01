/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licensing information.
 */
package pl.edu.icm.unity.webui.authn;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.endpoint.EndpointDescription;
import pl.edu.icm.unity.webui.authn.VaadinAuthentication.VaadinAuthenticationUI;
import pl.edu.icm.unity.webui.common.Styles;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays and allows to select authenticator set.
 * @author K. Benedyczak
 */
@SuppressWarnings("serial")
public class AuthenticatorSetSelectComponent extends VerticalLayout
{
	private UnityMessageSource msg;
	private OptionGroup buttons;
	
	public AuthenticatorSetSelectComponent(UnityMessageSource msg, final AuthenticatorSetChangedListener listener,
			EndpointDescription description,  List<Map<String, VaadinAuthenticationUI>> authenticators)
	{
		this.msg = msg;
		
		buttons = new OptionGroup(msg.getMessage("AuthenticationUI.selectMethod"));
		buttons.addStyleName(Styles.authnSetSelect.toString());
		addComponent(buttons);
		setSizeUndefined();
		
		for (int i=0; i<authenticators.size(); i++)
		{
			buttons.addItem(i);
			
			Set<String> set = description.getAuthenticatorSets().get(i).getAuthenticators();
			Map<String, VaadinAuthenticationUI> authenticatorsInSet = authenticators.get(i); 
			String caption = getAuthenticatorSetName(set, authenticatorsInSet);
			Resource icon = getAuthenticatorSetIcon(set, authenticatorsInSet);
			buttons.setItemCaption(i, caption);
			if (icon != null)
				buttons.setItemIcon(i, icon);
		}
		buttons.addValueChangeListener(new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				listener.setWasChanged((Integer)event.getProperty().getValue());
			}
		});
		buttons.select(0);
		buttons.setImmediate(true);
	}

	public void setSelectionTitle(String title)
	{
		buttons.setCaption(title);
	}
	
	private String getAuthenticatorSetName(Set<String> set, 
			Map<String, VaadinAuthenticationUI> authenticators)
	{
		Set<String> withLabels = new HashSet<String>();
		for (String s: set)
			if (authenticators.get(s).getLabel() != null)
				withLabels.add(s);
		
		Iterator<String> setIterator = withLabels.iterator();
		int size = set.size();
		int i=0;
		StringBuilder sb = new StringBuilder();
		for (; i<size-2; i++)
		{
			I18nString label = authenticators.get(setIterator.next()).getLabel();
			sb.append(label.getValue(msg));
			sb.append(msg.getMessage("comma"));
		}
		if (i < size-1)
		{
			I18nString label = authenticators.get(setIterator.next()).getLabel();
			sb.append(label.getValue(msg));
			sb.append(" ").append(msg.getMessage("and")).append(" ");
		}
		sb.append(authenticators.get(setIterator.next()).getLabel().getValue(msg));
		return sb.toString();
	}
	
	private Resource getAuthenticatorSetIcon(Set<String> set, 
			Map<String, VaadinAuthenticationUI> authenticators)
	{
		for (String a: set)
		{
			VaadinAuthenticationUI va = authenticators.get(a);
			if (va.getImage() != null)
				return va.getImage();
		}
		return null;
	}
}