/*
 * Copyright (c) 2015, Jirav All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.types.registration.invite;

import java.time.Instant;

import pl.edu.icm.unity.types.NamedObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Complete invitation as stored in the system. 
 * This class is a common base for backend and REST API variants which store attributes differently.
 *   
 * @author Krzysztof Benedyczak
 */
public class InvitationWithCode extends InvitationParam implements NamedObject
{
	private String registrationCode;
	private Instant lastSentTime;
	private int numberOfSends;

	public InvitationWithCode(String formId, Instant expiration, String contactAddress,
			String registrationCode)
	{
		super(formId, expiration, contactAddress);
		this.registrationCode = registrationCode;
	}

	public InvitationWithCode(InvitationParam base, String registrationCode,
			Instant lastSentTime, int numberOfSends)
	{
		super(base.getFormId(), base.getExpiration(), base.getContactAddress());
		this.registrationCode = registrationCode;
		this.lastSentTime = lastSentTime;
		this.numberOfSends = numberOfSends;
		this.getIdentities().putAll(base.getIdentities());
		this.getGroupSelections().putAll(base.getGroupSelections());
		this.getAttributes().putAll(base.getAttributes());
		this.getMessageParams().putAll(base.getMessageParams());
		this.setExpectedIdentity(base.getExpectedIdentity());
	}

	@JsonCreator
	public InvitationWithCode(ObjectNode json)
	{
		super(json);
		fromJson(json);
	}
	
	public String getRegistrationCode()
	{
		return registrationCode;
	}

	public Instant getLastSentTime()
	{
		return lastSentTime;
	}

	public int getNumberOfSends()
	{
		return numberOfSends;
	}
	
	public void setLastSentTime(Instant lastSentTime)
	{
		this.lastSentTime = lastSentTime;
	}

	public void setNumberOfSends(int numberOfSends)
	{
		this.numberOfSends = numberOfSends;
	}

	@JsonValue
	@Override
	public ObjectNode toJson()
	{
		ObjectNode json = super.toJson();
		
		json.put("registrationCode", getRegistrationCode());
		if (getLastSentTime() != null)
			json.put("lastSentTime", getLastSentTime().toEpochMilli());
		json.put("numberOfSends", getNumberOfSends());
		return json;
	}
	
	private void fromJson(ObjectNode json)
	{
		registrationCode = json.get("registrationCode").asText();
		if (json.has("lastSentTime"))
		{
			Instant lastSent = Instant.ofEpochMilli(json.get("lastSentTime").asLong());
			setLastSentTime(lastSent);
		}
		setNumberOfSends(json.get("numberOfSends").asInt());
	}

	@Override
	public String getName()
	{
		return getRegistrationCode();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " [registrationCode=" + registrationCode + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lastSentTime == null) ? 0 : lastSentTime.hashCode());
		result = prime * result + numberOfSends;
		result = prime * result
				+ ((registrationCode == null) ? 0 : registrationCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvitationWithCode other = (InvitationWithCode) obj;
		if (lastSentTime == null)
		{
			if (other.lastSentTime != null)
				return false;
		} else if (!lastSentTime.equals(other.lastSentTime))
			return false;
		if (numberOfSends != other.numberOfSends)
			return false;
		if (registrationCode == null)
		{
			if (other.registrationCode != null)
				return false;
		} else if (!registrationCode.equals(other.registrationCode))
			return false;
		return true;
	}
}



