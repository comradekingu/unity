/*
 * Copyright (c) 2016 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.engine.api.identity;

import pl.edu.icm.unity.exceptions.IllegalIdentityValueException;
import pl.edu.icm.unity.types.basic.EntityParam;
import pl.edu.icm.unity.types.basic.IdentityTaV;

/**
 * Various helper operations allowing for converting and checking identity and entity related parameters.
 * @author K. Benedyczak
 */
public interface EntityResolver
{
	/**
	 * Resolves {@link IdentityTaV}, if missing throws exception
	 * @param entity
	 * @return 
	 * @throws IllegalIdentityValueException
	 */
	long getEntityId(IdentityTaV entity) throws IllegalIdentityValueException;

	/**
	 * Resolves {@link EntityParam}, if missing throws exception
	 * @param entity
	 * @return
	 * @throws IllegalIdentityValueException
	 */
	long getEntityId(EntityParam entity) throws IllegalIdentityValueException;
}