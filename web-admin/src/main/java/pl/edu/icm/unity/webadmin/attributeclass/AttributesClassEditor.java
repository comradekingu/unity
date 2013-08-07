/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.attributeclass;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pl.edu.icm.unity.exceptions.IllegalTypeException;
import pl.edu.icm.unity.server.attributes.AttributeClassHelper;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.basic.AttributesClass;
import pl.edu.icm.unity.webui.common.DescriptionTextArea;
import pl.edu.icm.unity.webui.common.FormValidationException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;

/**
 * Editing component of an {@link AttributesClass} instance. Currently allows for editing a new AC only.
 * @author K. Benedyczak
 */
public class AttributesClassEditor extends FormLayout
{
	private UnityMessageSource msg;
	private Map<String, AttributesClass> allClasses;
	private Map<String, AttributeType> types;

	private AbstractTextField name;
	private DescriptionTextArea typeDescription;
	private TwinColSelect parents;
	private TwinColSelect allowed;
	private CheckBox allowArbitrary;
	private TwinColSelect mandatory;
	private EffectiveAttrClassViewer effectiveViewer;
	
	public AttributesClassEditor(UnityMessageSource msg, Map<String, AttributesClass> allClasses,
			Collection<AttributeType> allTypes)
	{
		this.msg = msg;
		this.allClasses = allClasses;
		this.types = new HashMap<>(allTypes.size());
		for (AttributeType at: allTypes)
			if (!at.isInstanceImmutable())
				types.put(at.getName(), at);
		initUI();
	}
	
	private void initUI()
	{
		name = new TextField(msg.getMessage("AttributesClass.name"));
		name.setRequired(true);
		name.setImmediate(true);
		
		typeDescription = new DescriptionTextArea(msg.getMessage("AttributesClass.description"));
		
		parents = new TwinColSelect(msg.getMessage("AttributesClass.parents"));
		parents.setRows(5);
		parents.setWidth(100, Unit.PERCENTAGE);
		parents.setImmediate(true);
		
		allowed = new TwinColSelect(msg.getMessage("AttributesClass.allowed"));
		allowed.setRows(5);
		allowed.setWidth(100, Unit.PERCENTAGE);
		allowed.setImmediate(true);
		
		allowArbitrary = new CheckBox(msg.getMessage("AttributesClass.allowArbitrary"));
		allowArbitrary.setImmediate(true);
		
		mandatory = new TwinColSelect(msg.getMessage("AttributesClass.mandatory"));
		mandatory.setRows(5);
		mandatory.setWidth(100, Unit.PERCENTAGE);
		mandatory.setImmediate(true);
		
		effectiveViewer = new EffectiveAttrClassViewer(msg);
		Panel effectiveWrapper = new Panel(effectiveViewer);
		effectiveWrapper.setCaption(msg.getMessage("AttributesClass.resultingClass"));
		
		
		
		for (String ac: allClasses.keySet())
			parents.addItem(ac);
		for (String at: types.keySet())
			allowed.addItem(at);
		for (String at: types.keySet())
			mandatory.addItem(at);
		
		ValueChangeListener listener = new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				updateEffective();
			}
		};
		name.addValueChangeListener(listener);
		parents.addValueChangeListener(listener);
		allowed.addValueChangeListener(listener);
		allowArbitrary.addValueChangeListener(listener);
		allowArbitrary.addValueChangeListener(new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				allowed.setEnabled(!allowArbitrary.getValue());
			}
		});
		mandatory.addValueChangeListener(listener);
		
		addComponents(name, typeDescription, parents, allowed, allowArbitrary, mandatory, effectiveWrapper);
		effectiveViewer.setVisible(false);
	}
	
	private void updateEffective()
	{
		String root = name.getValue();
		if (root == null || root.isEmpty() || allClasses.containsKey(root))
			effectiveViewer.setInput(null, allClasses);
		else
		{
			Map<String, AttributesClass> tmp = new HashMap<>(allClasses);
			AttributesClass cur = getAttributesClassUnsafe();
			try
			{
				AttributeClassHelper.cleanupClass(cur, allClasses);
			} catch (IllegalTypeException e)
			{
				throw new IllegalArgumentException(e);
			}
			tmp.put(root, cur);
			effectiveViewer.setInput(Collections.singleton(root), tmp);
		}
	}
	
	@SuppressWarnings("unchecked")
	private AttributesClass getAttributesClassUnsafe()
	{
		AttributesClass cur = new AttributesClass();
		cur.setName(name.getValue());
		cur.setDescription(typeDescription.getValue());
		cur.setAllowArbitrary(allowArbitrary.getValue());
		cur.setAllowed((Set<String>) allowed.getValue());
		cur.setMandatory((Set<String>) mandatory.getValue());
		cur.setParentClassName((Set<String>) parents.getValue());
		return cur;
	}
	
	public AttributesClass getAttributesClass() throws FormValidationException
	{
		if (!name.isValid())
			throw new FormValidationException();
		return getAttributesClassUnsafe();
	}
	
}
