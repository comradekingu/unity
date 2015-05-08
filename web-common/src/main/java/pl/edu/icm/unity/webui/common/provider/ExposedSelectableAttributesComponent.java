/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.common.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.webui.common.ExpandCollapseButton;
import pl.edu.icm.unity.webui.common.ListOfSelectableElements;
import pl.edu.icm.unity.webui.common.ListOfSelectableElements.DisableMode;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.attributes.AttributeHandlerRegistry;
import pl.edu.icm.unity.webui.common.safehtml.HtmlLabel;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Component showing all attributes that are going to be sent to the requesting service. User
 * can select attributes which should be hidden.
 * By default attributes are collapsed.
 * @author K. Benedyczak
 */
public class ExposedSelectableAttributesComponent extends CustomComponent
{
	private UnityMessageSource msg;
	protected AttributeHandlerRegistry handlersRegistry;
	
	protected Map<String, Attribute<?>> attributes;
	protected ListOfSelectableElements attributesHiding;

	public ExposedSelectableAttributesComponent(UnityMessageSource msg, AttributeHandlerRegistry handlersRegistry,
			Collection<Attribute<?>> attributesCol)
	{
		super();
		this.handlersRegistry = handlersRegistry;
		this.msg = msg;

		attributes = new HashMap<String, Attribute<?>>();
		for (Attribute<?> a: attributesCol)
			attributes.put(a.getName(), a);
		initUI();
	}
	
	/**
	 * @return collection of attributes without the ones hidden by the user.
	 */
	public Collection<Attribute<?>> getUserFilteredAttributes()
	{
		Set<String> hidden = new HashSet<String>();
		for (CheckBox cb: attributesHiding.getSelection())
			if (cb.getValue())
				hidden.add((String) cb.getData());
		
		List<Attribute<?>> ret = new ArrayList<Attribute<?>>(attributes.size());
		for (Attribute<?> a: attributes.values())
			if (!hidden.contains(a.getName()))
				ret.add(a);
		return ret;
	}
	
	public void setHidden(Set<String> hidden)
	{
		for (CheckBox cb: attributesHiding.getSelection())
		{
			String a = (String) cb.getData();
			if (hidden.contains(a))
				cb.setValue(true);
		}
	}
	
	public Set<String> getHidden()
	{
		Set<String> hidden = new HashSet<String>();
		for (CheckBox h: attributesHiding.getSelection())
		{
			if (!h.getValue())
				continue;
			String a = (String) h.getData();
			hidden.add(a);
		}
		return hidden;
	}
	
	private void initUI()
	{
		VerticalLayout contents = new VerticalLayout();
		contents.setSpacing(true);

		final VerticalLayout details = new VerticalLayout();
		final ExpandCollapseButton showDetails = new ExpandCollapseButton(true, details);

		Label attributesL = new Label(msg.getMessage("ExposedAttributesComponent.attributes"));
		attributesL.addStyleName(Styles.bold.toString());
		
		HtmlLabel credInfo = new HtmlLabel(msg);
		credInfo.setHtmlValue("ExposedAttributesComponent.credInfo");
		credInfo.addStyleName(Styles.vLabelSmall.toString());
		
		contents.addComponent(attributesL);
		contents.addComponent(showDetails);
		contents.addComponent(details);
		
		details.addComponent(credInfo);

		HtmlLabel attributesInfo = new HtmlLabel(msg, "ExposedAttributesComponent.attributesInfo");
		attributesInfo.addStyleName(Styles.vLabelSmall.toString());
		details.addComponent(attributesInfo);

		Label hideL = new Label(msg.getMessage("ExposedAttributesComponent.hide"));
		attributesHiding = new ListOfSelectableElements(null, hideL, DisableMode.WHEN_SELECTED);
		for (Attribute<?> at: attributes.values())
		{
			Label attrInfo = new Label();
			String representation = handlersRegistry.getSimplifiedAttributeRepresentation(at, 80);
			attrInfo.setValue(representation);
			attributesHiding.addEntry(attrInfo, false, at.getName());
		}
		details.addComponent(attributesHiding);
		
		setCompositionRoot(contents);
	}

}