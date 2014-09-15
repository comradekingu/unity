/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile.wizard;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardProgressListener;

import pl.edu.icm.unity.sandbox.SandboxAuthnEvent;
import pl.edu.icm.unity.server.authn.AuthenticationResult.Status;
import pl.edu.icm.unity.server.utils.UnityMessageSource;
import pl.edu.icm.unity.webadmin.tprofile.TranslationProfileEditDialog.Callback;
import pl.edu.icm.unity.webadmin.tprofile.TranslationProfileEditor;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

/**
 * Component that displays the wizard - used in {@link WizardDialog}.
 * 
 * @author Roman Krysinski
 */
public class WizardDialogComponent extends CustomComponent 
{

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Wizard wizard;
	private SandboxStep sandboxStep;
	private ProfileStep profileStep;
	private DryRunStep dryRun;
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param sandboxURL 
	 * @param updateCallback 
	 * @param addCallback 
	 * @param editor 
	 */
	public WizardDialogComponent(UnityMessageSource msg, String sandboxURL, 
			TranslationProfileEditor editor, Callback addCallback, Callback updateCallback) 
	{
		buildMainLayout();
		setCompositionRoot(mainLayout);

		sandboxStep = new SandboxStep(msg, sandboxURL);
		profileStep = new ProfileStep(msg, editor, addCallback, updateCallback);
		dryRun = new DryRunStep(msg, sandboxURL);
		
		wizard.addStep(new IntroStep(msg));
		wizard.addStep(sandboxStep);
		wizard.addStep(profileStep);
		wizard.addStep(dryRun);
	}

	public void addWizardListener(WizardProgressListener listener) 
	{
		wizard.addListener(listener);
	}
	
	public Button getNextButton()
	{
		return wizard.getNextButton();
	}

	public void handle(SandboxAuthnEvent event) 
	{
		if (event.getAuthnInput() != null)
		{
			profileStep.handle(event.getAuthnInput());
			sandboxStep.enableNext();
			wizard.next();
		} else
		{
			dryRun.handle(event.getAuthnResult());
			if (event.getAuthnResult().getStatus() != Status.success)
			{
				wizard.back();
			}
		}
	}
	
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// wizard
		wizard = new Wizard();
		wizard.setImmediate(false);
		wizard.setWidth("100.0%");
		wizard.setHeight("100.0%");
		mainLayout.addComponent(wizard);
		
		return mainLayout;
	}
}
