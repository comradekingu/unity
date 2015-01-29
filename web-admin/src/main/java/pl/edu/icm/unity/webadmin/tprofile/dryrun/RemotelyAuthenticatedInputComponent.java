/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile.dryrun;

import java.util.Collection;
import java.util.Collections;

import pl.edu.icm.unity.server.authn.remote.RemoteAttribute;
import pl.edu.icm.unity.server.authn.remote.RemoteGroupMembership;
import pl.edu.icm.unity.server.authn.remote.RemoteIdentity;
import pl.edu.icm.unity.server.authn.remote.RemotelyAuthenticatedInput;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.safehtml.HtmlLabel;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Component that displays RemotelyAuthenticatedInput.
 * 
 * @author Roman Krysinski
 */
public class RemotelyAuthenticatedInputComponent extends CustomComponent 
{

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private VerticalLayout mappingResultWrap;
	@AutoGenerated
	private HorizontalLayout groupsWrap;
	@AutoGenerated
	private Label groupsLabel;
	@AutoGenerated
	private Label groupsTitleLabel;
	@AutoGenerated
	private VerticalLayout attrsWrap;
	@AutoGenerated
	private Table attrsTable;
	@AutoGenerated
	private Label attrsTitleLabel;
	@AutoGenerated
	private VerticalLayout idsWrap;
	@AutoGenerated
	private Table idsTable;
	@AutoGenerated
	private Label idsTitleLabel;
	@AutoGenerated
	private HorizontalLayout titleWrap;
	@AutoGenerated
	private Label noneLabel;
	@AutoGenerated
	private HtmlLabel titleLabel;
	private UnityMessageSource msg;
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param msg 
	 */
	public RemotelyAuthenticatedInputComponent(UnityMessageSource msg) 
	{
		this.msg = msg;
		buildMainLayout();
		setCompositionRoot(mainLayout);
		
		mappingResultWrap.setStyleName(Styles.smallMargin.toString());
		setVisible(false);
		initLabels();
		initTables();
	}

	private void initLabels() 
	{
		idsTitleLabel.setValue(msg.getMessage("MappingResultComponent.idsTitle"));
		attrsTitleLabel.setValue(msg.getMessage("MappingResultComponent.attrsTitle"));
		groupsTitleLabel.setValue(msg.getMessage("MappingResultComponent.groupsTitle"));
		noneLabel.setValue(msg.getMessage("MappingResultComponent.none"));
		groupsLabel.setValue("");
	}
	
	private void initTables() 
	{
		idsTable.addContainerProperty(msg.getMessage("MappingResultComponent.idsTable.type"), String.class, null);
		idsTable.addContainerProperty(msg.getMessage("MappingResultComponent.idsTable.value"), String.class, null);
		
		attrsTable.addContainerProperty(msg.getMessage("MappingResultComponent.attrsTable.name"), String.class, null);
		attrsTable.addContainerProperty(msg.getMessage("MappingResultComponent.attrsTable.value"), String.class, null);
	}

	public void displayAuthnInput(RemotelyAuthenticatedInput input)
	{
		titleLabel.setHtmlValue("DryRun.RemotelyAuthenticatedContextComponent.title", input.getIdpName());
		if (input == null 
				|| (input.getIdentities().isEmpty()
					&& input.getAttributes().isEmpty()
					&& input.getGroups().isEmpty()))
		{
			displayItsTables(Collections.<RemoteIdentity>emptyList());
			displayAttrsTable(Collections.<RemoteAttribute>emptyList());
			displayGroups(Collections.<RemoteGroupMembership>emptyList());
			noneLabel.setVisible(true);
		} else
		{
			displayItsTables(input.getIdentities().values());
			displayAttrsTable(input.getAttributes().values());
			displayGroups(input.getGroups().values());
			noneLabel.setVisible(false);
		}
		setVisible(true);
	}

	private void displayItsTables(Collection<RemoteIdentity> collection) 
	{
		idsTable.removeAllItems();
		if (collection.isEmpty())
		{
			idsWrap.setVisible(false);
		} else
		{
			idsWrap.setVisible(true);
			RemoteIdentity[] identityArray = collection.toArray(new RemoteIdentity[collection.size()]);
			for (int i=0; i < identityArray.length; ++i)
			{
				RemoteIdentity identity = identityArray[i];
				idsTable.addItem(new Object[] {
								identity.getIdentityType().toString(),
								identity.getName().toString()}, i);
			}
			
			idsTable.setPageLength(collection.size());
	
			idsTable.refreshRowCache();
		}
	}
	
	private void displayAttrsTable(Collection<RemoteAttribute> collection) 
	{
		attrsTable.removeAllItems();
		if (collection.isEmpty())
		{
			attrsWrap.setVisible(false);
		} else
		{
			attrsWrap.setVisible(true);
			RemoteAttribute[] attributeArray = collection.toArray(new RemoteAttribute[collection.size()]);
			for (int i=0; i < collection.size(); ++i)
			{
				RemoteAttribute attr = attributeArray[i];
				attrsTable.addItem(new Object[] {
									attr.getName().toString(),
									attr.getValues().toString()}, i);
			}
			attrsTable.setPageLength(collection.size());
			attrsTable.refreshRowCache();
		}
	}
	
	private void displayGroups(Collection<RemoteGroupMembership> collection) 
	{
		if (collection.isEmpty())
		{
			groupsWrap.setVisible(false);
		} else
		{
			groupsWrap.setVisible(true);
			groupsLabel.setValue(collection.toString());
		}
	}
	
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// titleWrap
		titleWrap = buildTitleWrap();
		mainLayout.addComponent(titleWrap);
		
		// mappingResultWrap
		mappingResultWrap = buildMappingResultWrap();
		mainLayout.addComponent(mappingResultWrap);
		mainLayout.setExpandRatio(mappingResultWrap, 1.0f);
		
		return mainLayout;
	}


	@AutoGenerated
	private HorizontalLayout buildTitleWrap() {
		// common part: create layout
		titleWrap = new HorizontalLayout();
		titleWrap.setImmediate(false);
		titleWrap.setWidth("-1px");
		titleWrap.setHeight("-1px");
		titleWrap.setMargin(false);
		titleWrap.setSpacing(true);
		
		// titleLabel
		titleLabel = new HtmlLabel(msg);
		titleLabel.setImmediate(false);
		titleLabel.setWidth("-1px");
		titleLabel.setHeight("-1px");
		titleWrap.addComponent(titleLabel);
		
		// noneLabel
		noneLabel = new Label();
		noneLabel.setImmediate(false);
		noneLabel.setWidth("-1px");
		noneLabel.setHeight("-1px");
		noneLabel.setValue("Label");
		titleWrap.addComponent(noneLabel);
		
		return titleWrap;
	}


	@AutoGenerated
	private VerticalLayout buildMappingResultWrap() {
		// common part: create layout
		mappingResultWrap = new VerticalLayout();
		mappingResultWrap.setImmediate(false);
		mappingResultWrap.setWidth("100.0%");
		mappingResultWrap.setHeight("-1px");
		mappingResultWrap.setMargin(true);
		mappingResultWrap.setSpacing(true);
		
		// idsWrap
		idsWrap = buildIdsWrap();
		mappingResultWrap.addComponent(idsWrap);
		
		// attrsWrap
		attrsWrap = buildAttrsWrap();
		mappingResultWrap.addComponent(attrsWrap);
		
		// groupsWrap
		groupsWrap = buildGroupsWrap();
		mappingResultWrap.addComponent(groupsWrap);
		
		return mappingResultWrap;
	}


	@AutoGenerated
	private VerticalLayout buildIdsWrap() {
		// common part: create layout
		idsWrap = new VerticalLayout();
		idsWrap.setImmediate(false);
		idsWrap.setWidth("100.0%");
		idsWrap.setHeight("-1px");
		idsWrap.setMargin(false);
		
		// idsTitleLabel
		idsTitleLabel = new Label();
		idsTitleLabel.setImmediate(false);
		idsTitleLabel.setWidth("-1px");
		idsTitleLabel.setHeight("-1px");
		idsTitleLabel.setValue("Label");
		idsWrap.addComponent(idsTitleLabel);
		
		// idsTable
		idsTable = new Table();
		idsTable.setImmediate(false);
		idsTable.setWidth("100.0%");
		idsTable.setHeight("-1px");
		idsWrap.addComponent(idsTable);
		
		return idsWrap;
	}


	@AutoGenerated
	private VerticalLayout buildAttrsWrap() {
		// common part: create layout
		attrsWrap = new VerticalLayout();
		attrsWrap.setImmediate(false);
		attrsWrap.setWidth("100.0%");
		attrsWrap.setHeight("-1px");
		attrsWrap.setMargin(false);
		
		// attrsTitleLabel
		attrsTitleLabel = new Label();
		attrsTitleLabel.setImmediate(false);
		attrsTitleLabel.setWidth("-1px");
		attrsTitleLabel.setHeight("-1px");
		attrsTitleLabel.setValue("Label");
		attrsWrap.addComponent(attrsTitleLabel);
		
		// attrsTable
		attrsTable = new Table();
		attrsTable.setImmediate(false);
		attrsTable.setWidth("100.0%");
		attrsTable.setHeight("-1px");
		attrsWrap.addComponent(attrsTable);
		
		return attrsWrap;
	}


	@AutoGenerated
	private HorizontalLayout buildGroupsWrap() {
		// common part: create layout
		groupsWrap = new HorizontalLayout();
		groupsWrap.setImmediate(false);
		groupsWrap.setWidth("-1px");
		groupsWrap.setHeight("-1px");
		groupsWrap.setMargin(false);
		groupsWrap.setSpacing(true);
		
		// groupsTitleLabel
		groupsTitleLabel = new Label();
		groupsTitleLabel.setImmediate(false);
		groupsTitleLabel.setWidth("-1px");
		groupsTitleLabel.setHeight("-1px");
		groupsTitleLabel.setValue("Label");
		groupsWrap.addComponent(groupsTitleLabel);
		
		// groupsLabel
		groupsLabel = new Label();
		groupsLabel.setImmediate(false);
		groupsLabel.setWidth("-1px");
		groupsLabel.setHeight("-1px");
		groupsLabel.setValue("Label");
		groupsWrap.addComponent(groupsLabel);
		
		return groupsWrap;
	}

}