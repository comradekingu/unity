/*
 * Copyright (c) 2016 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.api;

import java.util.List;

/**
 * Basic DAO with typical CRUD operations.
 * @author K. Benedyczak
 */
public interface BasicCRUDDAO<T>
{	
	long create(T obj);
	
	void updateByKey(long id, T obj);

	void deleteByKey(long id);

	T getByKey(long id);

	List<T> getAll();
}