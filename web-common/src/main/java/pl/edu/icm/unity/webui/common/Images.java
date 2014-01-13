/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.common;

import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;

/**
 * Allows to easily get image resources.
 * @author K. Benedyczak
 */
public enum Images
{
	info64		(I.PH + "64/bullet_info.png"),
	key64		(I.PH + "64/key.png"),
	settings64	(I.PH + "64/settings.png"),

	stderror64	(I.P + "64/error.png"),
	stdwarn64	(I.P + "64/warning.png"),
	stdinfo64	(I.P + "64/info.png"),
	
	exit32		(I.P + "32/exit.png"),
	error32		(I.P + "32/error.png"),
	warn32		(I.P + "32/warn.png"),
	info32		(I.P + "32/information.png"),
	toAdmin32	(I.P + "32/manager.png"),
	toProfile32	(I.P + "32/personal.png"),

	
	refresh		(I.P + "16/reload.png"),
	userMagnifier	(I.P + "16/search.png"),
	folder		(I.P + "16/folder.png"),
	add		(I.P + "16/add.png"),
	addIdentity	(I.P + "16/identity_add.png"),
	addEntity	(I.P + "16/entity_add.png"),
	addFolder	(I.P + "16/folder_add.png"),
	delete		(I.P + "16/remove.png"),
	deleteFolder	(I.P + "16/folder_delete.png"),
	deleteEntity	(I.P + "16/entity_delete.png"),
	deleteIdentity	(I.P + "16/identity_delete.png"),
	edit		(I.P + "16/edit.png"),
	editUser	(I.P + "16/user_edit.png"),
	editFolder	(I.P + "16/folder_edit.png"),
	ok		(I.P + "16/ok.png"),
	save		(I.P + "16/save.png"),
	trashBin	(I.P + "16/trash.png"),
	addFilter	(I.P + "16/search.png"),
	noAuthzGrp	(I.P + "16/folder_locked.png"),
	collapse	(I.P + "16/collapse.png"),
	expand		(I.P + "16/expand.png"),
	addColumn	(I.P + "16/column_add.png"),
	removeColumn	(I.P + "16/column_delete.png"),
	key		(I.P + "16/key.png"),
	attributes	(I.P + "16/three_tags.png"),
	warn		(I.P + "16/warn.png"),
	error		(I.P + "16/error.png"),
	zoomin          (I.P + "16/zoom_in.png"),
	zoomout         (I.P + "16/zoom_out.png");
	
	
	private final String classpath;
	
	private Images(String classpath)
	{
		this.classpath = classpath;
	}
	
	public Resource getResource()
	{
		return new ClassResource(classpath);
	}

	/**
	 * Trick - otherwise we won't be able to use P in enum constructor arguments
	 * @author K. Benedyczak
	 */
	private static interface I
	{
		public static final String P = "/pl/edu/icm/unity/webui/img/standard/";
		public static final String PH = "/pl/edu/icm/unity/webui/img/hand/";
	}
}
