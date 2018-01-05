/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.GridDragSource;

import pl.edu.icm.unity.engine.api.authn.remote.RemotelyAuthenticatedInput;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.translation.in.InputTranslationProfile;
import pl.edu.icm.unity.webadmin.tprofile.TranslationProfileEditor;
import pl.edu.icm.unity.webui.common.safehtml.HtmlLabel;

/**
 * UI Component used by {@link ProfileStep}.
 * 
 * @author Roman Krysinski
 */
public class ProfileStepComponent extends CustomComponent 
{
	private HorizontalLayout mainLayout;
	private HorizontalSplitPanel splitPanelLayout;
	private VerticalLayout leftPanel;
	private Grid<DragDropBean> attributesTable;
	private HtmlLabel dragdropHint;
	
	private VerticalLayout rightPanel;
	private static final float DEFAULT_SIZE_OF_SPLIT_POS = 50;
	private UnityMessageSource msg;
	private TranslationProfileEditor editor;
	
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param editor 
	 * @param msg 
	 */
	public ProfileStepComponent(UnityMessageSource msg, TranslationProfileEditor editor) 
	{
		this.msg = msg;
		this.editor = editor;
		
		buildMainLayout();
		setCompositionRoot(mainLayout);
		
		rightPanel.removeAllComponents();
		rightPanel.addComponent(editor);
		
		dragdropHint.setHtmlValue("Wizard.ProfileStepComponent.dragdropHint");
		
		splitPanelLayout.setSplitPosition(DEFAULT_SIZE_OF_SPLIT_POS, Unit.PERCENTAGE, false);
	}
	
	public void handle(RemotelyAuthenticatedInput authnInput) 
	{
		initializeAttributesTable(authnInput);
		editor.setRemoteAuthnInput(authnInput);
	}

	private void initializeAttributesTable(RemotelyAuthenticatedInput input) 
	{
		Collection<DragDropBean> values = getTableContent(input);
		attributesTable.setItems(values);
		attributesTable.addColumn(DragDropBean::getExpression).setCaption(
				msg.getMessage("Wizard.ProfileStepComponent.expression"));
		attributesTable.addColumn(DragDropBean::getValue)
				.setCaption(msg.getMessage("Wizard.ProfileStepComponent.value"));
		attributesTable.setDescriptionGenerator(b -> b.toString());
		attributesTable.setHeightMode(HeightMode.ROW);
		attributesTable.setHeightByRows(values.size());
		
		GridDragSource<DragDropBean> dragSource = new GridDragSource<>(attributesTable);
		dragSource.setEffectAllowed(EffectAllowed.MOVE);
		
		dragSource.addGridDragStartListener(event -> 
		    dragSource.setDragData(event.getDraggedItems().iterator().next())
		);
		dragSource.addGridDragEndListener(event ->
		    dragSource.setDragData(null)
		);
	}
	
	private Collection<DragDropBean> getTableContent(RemotelyAuthenticatedInput input)
	{
		Collection<DragDropBean> items = new ArrayList<>();
		Map<String, String> exprValMap = InputTranslationProfile.createExpresionValueMap(input);
		
		for (Map.Entry<String, String> exprE : exprValMap.entrySet())
		{
			items.add(new DragDropBean(exprE.getKey(), exprE.getValue()));
		}
		
		return items;
	}

	private HorizontalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(false);
		mainLayout.setSizeFull();
		
		// top-level component properties
		setWidth("100.0%");
		
		// splitPanelLayout
		splitPanelLayout = buildSplitPanelLayout();
		mainLayout.addComponent(splitPanelLayout);
		mainLayout.setExpandRatio(splitPanelLayout, 1.0f);
		
		return mainLayout;
	}

	private HorizontalSplitPanel buildSplitPanelLayout() {
		// common part: create layout
		splitPanelLayout = new HorizontalSplitPanel();
		splitPanelLayout.setSizeFull();
		
		// rightPanel
		rightPanel = new VerticalLayout();
		rightPanel.setSpacing(false);
		rightPanel.setSizeFull();
		
		splitPanelLayout.addComponent(rightPanel);
		
		// leftPanel
		leftPanel = buildLeftPanel();
		splitPanelLayout.addComponent(leftPanel);
		
		return splitPanelLayout;
	}

	private VerticalLayout buildLeftPanel() {
		// common part: create layout
		leftPanel = new VerticalLayout();
		leftPanel.setSpacing(false);
		leftPanel.setSizeFull();
		
		// dragdropHint
		dragdropHint = new HtmlLabel(msg);
		dragdropHint.setWidth("-1px");
		dragdropHint.setHeight("-1px");
		leftPanel.addComponent(dragdropHint);
		
		// attributesTable
		attributesTable = new Grid<>();
		attributesTable.setSizeFull();
		leftPanel.addComponent(attributesTable);
		leftPanel.setExpandRatio(attributesTable, 1.0f);
		
		return leftPanel;
	}
}
