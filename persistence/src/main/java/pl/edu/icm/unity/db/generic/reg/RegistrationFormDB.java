/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.db.generic.reg;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.db.DBAttributes;
import pl.edu.icm.unity.db.DBGeneric;
import pl.edu.icm.unity.db.DBGroups;
import pl.edu.icm.unity.db.generic.DependencyChangeListener;
import pl.edu.icm.unity.db.generic.DependencyNotificationManager;
import pl.edu.icm.unity.db.generic.GenericObjectsDB;
import pl.edu.icm.unity.db.generic.ac.AttributeClassHandler;
import pl.edu.icm.unity.db.generic.cred.CredentialHandler;
import pl.edu.icm.unity.db.generic.credreq.CredentialRequirementHandler;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.exceptions.IllegalCredentialException;
import pl.edu.icm.unity.types.authn.CredentialDefinition;
import pl.edu.icm.unity.types.authn.CredentialRequirements;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.basic.AttributeValueSyntax;
import pl.edu.icm.unity.types.basic.AttributesClass;
import pl.edu.icm.unity.types.basic.Group;
import pl.edu.icm.unity.types.registration.AttributeClassAssignment;
import pl.edu.icm.unity.types.registration.AttributeRegistrationParam;
import pl.edu.icm.unity.types.registration.CredentialRegistrationParam;
import pl.edu.icm.unity.types.registration.GroupRegistrationParam;
import pl.edu.icm.unity.types.registration.RegistrationForm;

/**
 * Easy access to {@link RegistrationForm} storage.
 * @author K. Benedyczak
 */
@Component
public class RegistrationFormDB extends GenericObjectsDB<RegistrationForm>
{
	@Autowired
	public RegistrationFormDB(RegistrationFormHandler handler,
			DBGeneric dbGeneric, DependencyNotificationManager notificationManager)
	{
		super(handler, dbGeneric, notificationManager, RegistrationForm.class,
				"registration form");
		notificationManager.addListener(new CredentialChangeListener());
		notificationManager.addListener(new CredentialRequirementChangeListener());
		notificationManager.addListener(new ACChangeListener());
		notificationManager.addListener(new GroupChangeListener());
		notificationManager.addListener(new AttributeTypeChangeListener());
	}
	
	private class CredentialChangeListener implements DependencyChangeListener<CredentialDefinition>
	{
		@Override
		public String getDependencyObjectType()
		{
			return CredentialHandler.CREDENTIAL_OBJECT_TYPE;
		}

		@Override
		public void preAdd(CredentialDefinition newObject, SqlSession sql) throws EngineException { }
		@Override
		public void preUpdate(CredentialDefinition oldObject,
				CredentialDefinition updatedObject, SqlSession sql) throws EngineException {}

		@Override
		public void preRemove(CredentialDefinition removedObject, SqlSession sql)
				throws EngineException
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				for (CredentialRegistrationParam crParam: form.getCredentialParams())
					if (removedObject.getName().equals(crParam.getCredentialName()))
						throw new IllegalCredentialException("The credential is used by a registration form " 
							+ form.getName());
			}
		}
	}

	private class CredentialRequirementChangeListener implements DependencyChangeListener<CredentialRequirements>
	{
		@Override
		public String getDependencyObjectType()
		{
			return CredentialRequirementHandler.CREDENTIAL_REQ_OBJECT_TYPE;
		}

		@Override
		public void preAdd(CredentialRequirements newObject, SqlSession sql) throws EngineException { }
		@Override
		public void preUpdate(CredentialRequirements oldObject,
				CredentialRequirements updatedObject, SqlSession sql) throws EngineException {}

		@Override
		public void preRemove(CredentialRequirements removedObject, SqlSession sql)
				throws EngineException
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				if (removedObject.getName().equals(form.getCredentialRequirementAssignment()))
					throw new IllegalCredentialException("The credential requirement is used by a registration form " 
							+ form.getName());
			}
		}
	}

	private class ACChangeListener implements DependencyChangeListener<AttributesClass>
	{
		@Override
		public String getDependencyObjectType()
		{
			return AttributeClassHandler.ATTRIBUTE_CLASS_OBJECT_TYPE;
		}

		@Override
		public void preAdd(AttributesClass newObject, SqlSession sql) throws EngineException { }
		@Override
		public void preUpdate(AttributesClass oldObject,
				AttributesClass updatedObject, SqlSession sql) throws EngineException {}

		@Override
		public void preRemove(AttributesClass removedObject, SqlSession sql)
				throws EngineException
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				for (AttributeClassAssignment ac: form.getAttributeClassAssignments())
					if (removedObject.getName().equals(ac.getAcName()))
						throw new IllegalCredentialException("The attribute class is used by a registration form " 
							+ form.getName());
			}
		}
	}
	
	private class GroupChangeListener implements DependencyChangeListener<Group>
	{
		@Override
		public String getDependencyObjectType()
		{
			return DBGroups.GROUPS_NOTIFICATION_ID;
		}

		@Override
		public void preAdd(Group newObject, SqlSession sql) throws EngineException { }
		@Override
		public void preUpdate(Group oldObject,
				Group updatedObject, SqlSession sql) throws EngineException {}

		@Override
		public void preRemove(Group removedObject, SqlSession sql)
				throws EngineException
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				for (String group: form.getGroupAssignments())
					if (group.startsWith(removedObject.toString()))
						throw new IllegalCredentialException("The group is used by a registration form " 
							+ form.getName());
				for (GroupRegistrationParam group: form.getGroupParams())
					if (group.getGroupPath().startsWith(removedObject.toString()))
						throw new IllegalCredentialException("The group is used by a registration form " 
							+ form.getName());
				for (Attribute<?> attr: form.getAttributeAssignments())
					if (attr.getGroupPath().startsWith(removedObject.toString()))
						throw new IllegalCredentialException("The group is used by an attribute in registration form " 
							+ form.getName());
				for (AttributeRegistrationParam attr: form.getAttributeParams())
					if (attr.getGroup().startsWith(removedObject.toString()))
						throw new IllegalCredentialException("The group is used by an attribute in registration form " 
							+ form.getName());
			}
		}
	}

	private class AttributeTypeChangeListener implements DependencyChangeListener<AttributeType>
	{
		@Override
		public String getDependencyObjectType()
		{
			return DBAttributes.ATTRIBUTE_TYPES_NOTIFICATION_ID;
		}

		@Override
		public void preAdd(AttributeType newObject, SqlSession sql) throws EngineException { }
		
		@SuppressWarnings("unchecked")
		@Override
		public void preUpdate(AttributeType oldObject,
				AttributeType updatedObject, SqlSession sql) throws EngineException 
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				for (Attribute<?> attr: form.getAttributeAssignments())
					if (attr.getName().equals(updatedObject.getName()))
					{
						@SuppressWarnings("rawtypes")
						AttributeValueSyntax syntax = updatedObject.getValueType();
						for (Object o: attr.getValues())
							syntax.validate(o);
					}
			}			
		}

		@Override
		public void preRemove(AttributeType removedObject, SqlSession sql)
				throws EngineException
		{
			List<RegistrationForm> forms = getAll(sql);
			for (RegistrationForm form: forms)
			{
				for (Attribute<?> attr: form.getAttributeAssignments())
					if (attr.getName().equals(removedObject.getName()))
						throw new IllegalCredentialException("The attribute type is used by an attribute in registration form " 
							+ form.getName());
				for (AttributeRegistrationParam attr: form.getAttributeParams())
					if (attr.getAttributeType().equals(removedObject.getName()))
						throw new IllegalCredentialException("The attribute type is used by an attribute in registration form " 
							+ form.getName());
			}
		}
	}
}
