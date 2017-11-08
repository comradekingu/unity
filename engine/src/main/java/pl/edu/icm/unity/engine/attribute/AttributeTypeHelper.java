/*
 * Copyright (c) 2016 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.engine.attribute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import pl.edu.icm.unity.Constants;
import pl.edu.icm.unity.engine.api.attributes.AttributeSyntaxFactoriesRegistry;
import pl.edu.icm.unity.engine.api.attributes.AttributeValueSyntax;
import pl.edu.icm.unity.engine.api.attributes.AttributeValueSyntaxFactory;
import pl.edu.icm.unity.engine.utils.ClasspathResourceReader;
import pl.edu.icm.unity.store.api.AttributeTypeDAO;
import pl.edu.icm.unity.store.api.tx.Transactional;
import pl.edu.icm.unity.types.basic.AttributeType;


/**
 * Provides utilities allowing for easy access to common {@link AttributeType} related operations.
 * @author K. Benedyczak
 */
@Component
public class AttributeTypeHelper
{	
	public static final String ATTRIBUTE_TYPES_CLASSPATH = "attributeTypes";
	
	private Map<String, AttributeValueSyntax<?>> unconfiguredSyntaxes;
	private AttributeSyntaxFactoriesRegistry atSyntaxRegistry;
	private AttributeTypeDAO attributeTypeDAO;
	private ApplicationContext appContext;
	
	@Autowired
	public AttributeTypeHelper(AttributeSyntaxFactoriesRegistry atSyntaxRegistry, 
			AttributeTypeDAO attributeTypeDAO, ApplicationContext appContext)
	{
		this.atSyntaxRegistry = atSyntaxRegistry;
		this.attributeTypeDAO = attributeTypeDAO;
		this.appContext = appContext;
		unconfiguredSyntaxes = new HashMap<>();
		for (AttributeValueSyntaxFactory<?> f: atSyntaxRegistry.getAll())
			unconfiguredSyntaxes.put(f.getId(), f.createInstance());
	}

	@Transactional
	public AttributeValueSyntax<?> getUnconfiguredSyntaxForAttributeName(String attribute)
	{
		AttributeType attributeType = attributeTypeDAO.get(attribute);
		return getUnconfiguredSyntax(attributeType.getValueSyntax());
	}
	
	@Transactional
	public AttributeValueSyntax<?> getSyntaxForAttributeName(String attribute)
	{
		AttributeType attributeType = attributeTypeDAO.get(attribute);
		return getSyntax(attributeType);
	}
	
	@Transactional
	public AttributeType getTypeForAttributeName(String attribute)
	{
		return attributeTypeDAO.get(attribute);
	}
	
	public Collection<AttributeType> getAttributeTypes()
	{
		return attributeTypeDAO.getAll();
	}
	
	/**
	 * @param name attribute syntax name
	 * @return a value syntax object which was NOT configured with the type settings
	 */
	public AttributeValueSyntax<?> getUnconfiguredSyntax(String name)
	{
		AttributeValueSyntax<?> ret = unconfiguredSyntaxes.get(name);
		if (ret == null)
			throw new IllegalArgumentException("There is no attribute syntax defined with name " + name);
		return ret;
	}
	
	/**
	 * @param at
	 * @return configured value syntax for the attribute type
	 */
	public AttributeValueSyntax<?> getSyntax(AttributeType at)
	{
		AttributeValueSyntaxFactory<?> factory = atSyntaxRegistry.getByName(at.getValueSyntax());
		AttributeValueSyntax<?> ret = factory.createInstance();
		if (at.getValueSyntaxConfiguration() != null)
			ret.setSerializedConfiguration(at.getValueSyntaxConfiguration());
		return ret;
	}
	
	
	/**
	 * If the parameter type has no syntax configuration set, it will be set to the default syntax configuration.
	 * @param unconfigured
	 */
	public void setDefaultSyntaxConfiguration(AttributeType unconfigured)
	{
		if (unconfigured.getValueSyntaxConfiguration() != null)
			return;
		AttributeValueSyntax<?> syntax = getUnconfiguredSyntax(unconfigured.getValueSyntax());
		unconfigured.setValueSyntaxConfiguration(syntax.getSerializedConfiguration());
	}

	public List<AttributeType> loadAttributeTypesFromFile(File file)
	{
		if (file == null)
		{
			return null;
		}
		List<AttributeType> toAdd = new ArrayList<>();

		JsonFactory jsonF = new JsonFactory(Constants.MAPPER);
		jsonF.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file)))
		{
			JsonParser jp = jsonF.createParser(is);
			if (jp.nextToken() == JsonToken.START_ARRAY)
				jp.nextToken();

			while (jp.currentToken() == JsonToken.START_OBJECT)
			{
				AttributeType at = new AttributeType(jp.readValueAsTree());
				toAdd.add(at);
				jp.nextToken();
			}

		} catch (Exception e)
		{
			throw new IllegalArgumentException(
					"Can not parse attribute types file " + file.getName(), e);
		}
		return toAdd;
	}

	public List<File> getAttibuteTypeFilesFromClasspathResource()
	{
		ClasspathResourceReader reader = new ClasspathResourceReader(appContext);
		return reader.getFilesFromClasspathResourceDir(ATTRIBUTE_TYPES_CLASSPATH);
	}
}
