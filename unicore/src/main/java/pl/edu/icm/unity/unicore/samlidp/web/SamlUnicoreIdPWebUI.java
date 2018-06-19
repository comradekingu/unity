/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.unicore.samlidp.web;

import java.util.Calendar;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unicore.samly2.exceptions.SAMLRequesterException;
import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.engine.api.AttributeTypeManagement;
import pl.edu.icm.unity.engine.api.PreferencesManagement;
import pl.edu.icm.unity.engine.api.attributes.AttributeTypeSupport;
import pl.edu.icm.unity.engine.api.identity.IdentityTypeSupport;
import pl.edu.icm.unity.engine.api.idp.IdPEngine;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.session.SessionManagement;
import pl.edu.icm.unity.engine.api.translation.out.TranslationResult;
import pl.edu.icm.unity.engine.api.utils.FreemarkerAppHandler;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.saml.idp.SamlIdpProperties;
import pl.edu.icm.unity.saml.idp.ctx.SAMLAuthnContext;
import pl.edu.icm.unity.saml.idp.preferences.SamlPreferences.SPSettings;
import pl.edu.icm.unity.saml.idp.web.SAMLContextSupport;
import pl.edu.icm.unity.saml.idp.web.SamlIdPWebUI;
import pl.edu.icm.unity.unicore.samlidp.preferences.SamlPreferencesWithETD;
import pl.edu.icm.unity.unicore.samlidp.preferences.SamlPreferencesWithETD.SPETDSettings;
import pl.edu.icm.unity.unicore.samlidp.saml.AuthnWithETDResponseProcessor;
import pl.edu.icm.unity.webui.UnityWebUI;
import pl.edu.icm.unity.webui.authn.StandardWebAuthenticationProcessor;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.attributes.AttributeHandlerRegistry;
import pl.edu.icm.unity.webui.common.safehtml.HtmlTag;
import pl.edu.icm.unity.webui.common.safehtml.SafePanel;
import pl.edu.icm.unity.webui.forms.enquiry.EnquiresDialogLauncher;
import pl.edu.icm.unity.webui.idpcommon.EopException;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;


/**
 * The main UI of the SAML web IdP. It is an extension of the {@link SamlIdPWebUI}, adding a possibility
 * to configure UNICORE bootstrap ETD generation and using the {@link AuthnWithETDResponseProcessor}.
 *  
 * @author K. Benedyczak
 */
@Component("SamlUnicoreIdPWebUI")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Theme("unityThemeValo")
public class SamlUnicoreIdPWebUI extends SamlIdPWebUI implements UnityWebUI
{
	private static Logger log = Log.getLogger(Log.U_SERVER_SAML, SamlUnicoreIdPWebUI.class);

	private AuthnWithETDResponseProcessor samlWithEtdProcessor;
	private ETDSettingsEditor etdEditor;

	@Autowired
	public SamlUnicoreIdPWebUI(UnityMessageSource msg, FreemarkerAppHandler freemarkerHandler,
			AttributeHandlerRegistry handlersRegistry, PreferencesManagement preferencesMan,
			StandardWebAuthenticationProcessor authnProcessor, IdPEngine idpEngine, 
			IdentityTypeSupport idTypeSupport, SessionManagement sessionMan, 
			AttributeTypeManagement attrMan, 
			EnquiresDialogLauncher enquiryDialogLauncher,
			AttributeTypeSupport aTypeSupport)
	{
		super(msg, freemarkerHandler, handlersRegistry, preferencesMan,	authnProcessor, idpEngine,
				idTypeSupport, sessionMan, attrMan,  
				enquiryDialogLauncher, aTypeSupport);
	}

	@Override
	protected void appInit(VaadinRequest request)
	{
		SAMLAuthnContext samlCtx = SAMLContextSupport.getContext();
		samlWithEtdProcessor = new AuthnWithETDResponseProcessor(aTypeSupport, samlCtx, 
				Calendar.getInstance());
		super.appInit(request);
	}
	
	@Override
	protected void createExposedDataPart(SAMLAuthnContext samlCtx, VerticalLayout contents) throws EopException
	{
		SafePanel exposedInfoPanel = new SafePanel();
		contents.addComponent(exposedInfoPanel);
		VerticalLayout eiLayout = new VerticalLayout();
		exposedInfoPanel.setContent(eiLayout);
		try
		{
			TranslationResult translationResult = getUserInfo(samlCtx, samlWithEtdProcessor);
			createIdentityPart(translationResult, eiLayout);
			eiLayout.addComponent(HtmlTag.br());
			createAttributesPart(translationResult, eiLayout, samlCtx.getSamlConfiguration().getBooleanValue(
					SamlIdpProperties.USER_EDIT_CONSENT));
			eiLayout.addComponent(HtmlTag.br());
			createETDPart(eiLayout);
		} catch (SAMLRequesterException e)
		{
			//we kill the session as the user may want to log as different user if has access to several entities.
			log.debug("SAML problem when handling client request", e);
			samlResponseHandler.handleException(e, true);
			return;
		} catch (Exception e)
		{
			log.error("Engine problem when handling client request", e);
			//we kill the session as the user may want to log as different user if has access to several entities.
			samlResponseHandler.handleException(e, true);
			return;
		}
		
		rememberCB = new CheckBox("Remember the settings for this service and do not show this dialog again");
		contents.addComponent(rememberCB);
	}

	protected void createETDPart(VerticalLayout eiLayout)
	{
		Label titleL = new Label(msg.getMessage("SamlUnicoreIdPWebUI.gridSettings"));
		titleL.addStyleName(Styles.bold.toString());
		eiLayout.addComponents(titleL);
		etdEditor = new ETDSettingsEditor(msg, eiLayout);
	}
	
	
	@Override
	protected void loadPreferences(SAMLAuthnContext samlCtx) throws EopException
	{
		try
		{
			SamlPreferencesWithETD preferences = SamlPreferencesWithETD.getPreferences(preferencesMan);
			NameIDType samlRequester = samlCtx.getRequest().getIssuer();
			SPSettings baseSettings = preferences.getSPSettings(samlRequester);
			SPETDSettings settings = preferences.getSPETDSettings(samlRequester);
			updateETDUIFromPreferences(settings, samlCtx);
			super.updateUIFromPreferences(baseSettings, samlCtx);
		} catch (EopException e)
		{
			throw e;
		} catch (Exception e)
		{
			log.error("Engine problem when processing stored preferences", e);
			//we kill the session as the user may want to log as different user if has access to several entities.
			samlResponseHandler.handleException(e, true);
			return;
		}
	}
	
	protected void updateETDUIFromPreferences(SPETDSettings settings, SAMLAuthnContext samlCtx) throws EngineException
	{
		if (settings == null)
			return;
		etdEditor.setValues(settings);
	}
	
	/**
	 * Applies UI selected values to the given preferences object
	 * @param preferences
	 * @param samlCtx
	 * @param defaultAccept
	 * @throws EngineException
	 */
	protected void updatePreferencesFromUI(SamlPreferencesWithETD preferences, SAMLAuthnContext samlCtx, 
			boolean defaultAccept) throws EngineException
	{
		super.updatePreferencesFromUI(preferences, samlCtx, defaultAccept);
		if (!rememberCB.getValue())
			return;
		SPETDSettings settings = etdEditor.getSPETDSettings();
		preferences.setSPETDSettings(samlCtx.getRequest().getIssuer(), settings);
	}
	
	@Override
	protected void storePreferences(boolean defaultAccept)
	{
		try
		{
			SAMLAuthnContext samlCtx = SAMLContextSupport.getContext();
			SamlPreferencesWithETD preferences = SamlPreferencesWithETD.getPreferences(preferencesMan);
			updatePreferencesFromUI(preferences, samlCtx, defaultAccept);
			SamlPreferencesWithETD.savePreferences(preferencesMan, preferences);
		} catch (EngineException e)
		{
			log.error("Unable to store user's preferences", e);
		}
	}
	
	@Override
	protected void confirm(SAMLAuthnContext samlCtx) throws EopException
	{
		storePreferences(true);
		ResponseDocument respDoc;
		try
		{
			respDoc = samlWithEtdProcessor.processAuthnRequest(idSelector.getSelectedIdentity(), 
					getExposedAttributes(), samlCtx.getResponseDestination(),
					etdEditor.getSPETDSettings().toDelegationRestrictions());
		} catch (Exception e)
		{
			samlResponseHandler.handleException(e, false);
			return;
		}
		addSessionParticipant(samlCtx, samlWithEtdProcessor.getAuthenticatedSubject().getNameID(), 
				samlWithEtdProcessor.getSessionId());
		samlResponseHandler.returnSamlResponse(respDoc);
	}
}
