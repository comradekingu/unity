/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.reg.formman;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.engine.api.AttributeTypeManagement;
import pl.edu.icm.unity.engine.api.CredentialManagement;
import pl.edu.icm.unity.engine.api.CredentialRequirementManagement;
import pl.edu.icm.unity.engine.api.GroupsManagement;
import pl.edu.icm.unity.engine.api.MessageTemplateManagement;
import pl.edu.icm.unity.engine.api.NotificationsManagement;
import pl.edu.icm.unity.engine.api.authn.AuthenticatorSupportManagement;
import pl.edu.icm.unity.engine.api.identity.IdentityTypeSupport;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;
import pl.edu.icm.unity.engine.translation.form.RegistrationActionsRegistry;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.types.authn.CredentialRequirements;
import pl.edu.icm.unity.types.registration.ExternalSignupSpec;
import pl.edu.icm.unity.types.registration.RegistrationForm;
import pl.edu.icm.unity.types.registration.RegistrationFormBuilder;
import pl.edu.icm.unity.types.registration.RegistrationFormLayouts;
import pl.edu.icm.unity.types.registration.RegistrationFormNotifications;
import pl.edu.icm.unity.types.registration.layout.FormLayoutSettings;
import pl.edu.icm.unity.types.translation.ProfileType;
import pl.edu.icm.unity.types.translation.TranslationProfile;
import pl.edu.icm.unity.webadmin.tprofile.ActionParameterComponentProvider;
import pl.edu.icm.unity.webadmin.tprofile.RegistrationTranslationProfileEditor;
import pl.edu.icm.unity.webui.common.CompactFormLayout;
import pl.edu.icm.unity.webui.common.FormValidationException;
import pl.edu.icm.unity.webui.common.NotNullComboBox;
import pl.edu.icm.unity.webui.common.i18n.I18nTextField;

/**
 * Allows to edit a registration form. Can be configured to edit an existing form (name is fixed)
 * or to create a new one (name can be chosen).
 * 
 * @author K. Benedyczak
 */
@PrototypeComponent
public class RegistrationFormEditor extends BaseFormEditor
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_WEB, RegistrationFormEditor.class);
	private UnityMessageSource msg;
	private GroupsManagement groupsMan;
	private NotificationsManagement notificationsMan;
	private MessageTemplateManagement msgTempMan;
	private CredentialRequirementManagement credReqMan;
	private AuthenticatorSupportManagement authenticatorSupport;
	
	private TabSheet tabs;
	private CheckBox ignoreRequests;
	
	private CheckBox publiclyAvailable;
	private CheckBox byInvitationOnly;
	private RegistrationFormNotificationsEditor notificationsEditor;
	private Slider captcha;
	
	private I18nTextField title2ndStage;
	private CheckBox showGotoSignin;
	private TextField signInUrl;
	private TextField registrationCode;
	private RemoteAuthnProvidersSelection remoteAuthnSelections;

	private NotNullComboBox<String> credentialRequirementAssignment;
	private RegistrationActionsRegistry actionsRegistry;
	private RegistrationTranslationProfileEditor profileEditor;
	private RegistrationFormLayoutSettingsEditor layoutSettingsEditor;
	private RegistrationFormLayoutEditor layoutEditor;
	private ActionParameterComponentProvider actionComponentFactory;
	private CheckBox showCancel;
	private CheckBox localSignupEmbeddedAsButton;
	
	@Autowired
	public RegistrationFormEditor(UnityMessageSource msg, GroupsManagement groupsMan,
			NotificationsManagement notificationsMan,
			MessageTemplateManagement msgTempMan, IdentityTypeSupport identitiesMan,
			AttributeTypeManagement attributeMan,
			CredentialManagement credMan, RegistrationActionsRegistry actionsRegistry,
			CredentialRequirementManagement credReqMan,
			ActionParameterComponentProvider actionComponentFactory,
			AuthenticatorSupportManagement authenticatorSupport)
			throws EngineException
	{
		super(msg, identitiesMan, attributeMan, credMan);
		this.actionsRegistry = actionsRegistry;
		this.msg = msg;
		this.groupsMan = groupsMan;
		this.notificationsMan = notificationsMan;
		this.msgTempMan = msgTempMan;
		this.credReqMan = credReqMan;
		this.actionComponentFactory = actionComponentFactory;
		this.actionComponentFactory.init();
		this.authenticatorSupport = authenticatorSupport;
	}
	
	public RegistrationFormEditor init(boolean copyMode)
			throws EngineException
	{
		this.copyMode = copyMode;
		initUI();
		return this;
	}

	private void initUI() throws EngineException
	{
		setWidth(100, Unit.PERCENTAGE);
		setHeight(100, Unit.PERCENTAGE);
		setSpacing(true);
		tabs = new TabSheet();
		initMainTab();
		initCollectedTab();
		initDisplaySettingsTab();
		initLayoutTab();
		initWrapUpTab();
		initAssignedTab();
		ignoreRequests = new CheckBox(msg.getMessage("RegistrationFormEditDialog.ignoreRequests"));
		addComponent(ignoreRequests);
		setComponentAlignment(ignoreRequests, Alignment.TOP_RIGHT);
		ignoreRequests.setVisible(false);
		addComponent(tabs);
		setComponentAlignment(tabs, Alignment.TOP_LEFT);
		setExpandRatio(tabs, 1);
	}

	public RegistrationForm getForm() throws FormValidationException
	{
		RegistrationFormBuilder builder = getFormBuilderBasic();
		builder.withTranslationProfile(profileEditor.getProfile());
		RegistrationFormNotifications notCfg = notificationsEditor.getValue();
		builder.withNotificationsConfiguration(notCfg);
		RegistrationFormLayouts layouts = layoutEditor.getLayouts();
		layouts.setLocalSignupEmbeddedAsButton(localSignupEmbeddedAsButton.getValue());
		builder.withLayouts(layouts);
		
		RegistrationForm form = builder.build();
		try
		{
			form.validateLayouts();
		} catch (Exception e)
		{
			throw new FormValidationException(e);
		}
		return form;
	}
	
	private RegistrationFormBuilder getFormBuilderBasic() throws FormValidationException
	{
		RegistrationFormBuilder builder = new RegistrationFormBuilder();
		super.buildCommon(builder);
		
		builder.withDefaultCredentialRequirement(credentialRequirementAssignment.getValue());
		builder.withCaptchaLength(captcha.getValue().intValue());
		builder.withPubliclyAvailable(publiclyAvailable.getValue());
		builder.withByInvitationOnly(byInvitationOnly.getValue());
		String code = registrationCode.getValue();
		if (code != null && !code.equals(""))
			builder.withRegistrationCode(code);
		builder.withExternalSignupSpec(new ExternalSignupSpec(remoteAuthnSelections.getSelectedItems()));
		FormLayoutSettings settings = layoutSettingsEditor.getSettings();
		settings.setShowCancel(showCancel.getValue());
		builder.withFormLayoutSettings(settings);
		builder.withTitle2ndStage(title2ndStage.getValue());
		builder.withShowGotoSignIn(showGotoSignin.getValue(), signInUrl.getValue());
		RegistrationFormLayouts layouts = new RegistrationFormLayouts(); //FIXME
		layouts.setLocalSignupEmbeddedAsButton(localSignupEmbeddedAsButton.getValue());
		builder.withLayouts(layouts);
		return builder;
	}
	
	public void setForm(RegistrationForm toEdit)
	{
		super.setValue(toEdit);
		publiclyAvailable.setValue(toEdit.isPubliclyAvailable());
		byInvitationOnly.setValue(toEdit.isByInvitationOnly());
		RegistrationFormNotifications notCfg = toEdit.getNotificationsConfiguration();
		notificationsEditor.setValue(notCfg);
		captcha.setValue(Double.valueOf(toEdit.getCaptchaLength()));
		if (toEdit.getRegistrationCode() != null)
			registrationCode.setValue(toEdit.getRegistrationCode());
		if (toEdit.getTitle2ndStage() != null)
			title2ndStage.setValue(toEdit.getTitle2ndStage());
		credentialRequirementAssignment.setValue(toEdit.getDefaultCredentialRequirement());
		TranslationProfile profile = new TranslationProfile(
				toEdit.getTranslationProfile().getName(), "",
				ProfileType.REGISTRATION,
				toEdit.getTranslationProfile().getRules());
		profileEditor.setValue(profile);
		layoutSettingsEditor.setSettings(toEdit.getLayoutSettings());
		layoutEditor.setFormLayouts(toEdit.getFormLayouts());
		showGotoSignin.setValue(toEdit.isShowSignInLink());
		signInUrl.setValue(toEdit.getSignInLink() == null ? "" : toEdit.getSignInLink());
		signInUrl.setEnabled(showGotoSignin.getValue());
		if (!copyMode)
			ignoreRequests.setVisible(true);
		remoteAuthnSelections.setSelectedItems(toEdit.getExternalSignupSpec().getSpecs());
		showCancel.setValue(toEdit.getLayoutSettings().isShowCancel());
		localSignupEmbeddedAsButton.setValue(toEdit.getFormLayouts().isLocalSignupEmbeddedAsButton());
	}
	
	private void initMainTab() throws EngineException
	{
		FormLayout main = new CompactFormLayout();
		VerticalLayout wrapper = new VerticalLayout(main);
		wrapper.setMargin(true);
		wrapper.setSpacing(false);
		tabs.addTab(wrapper, msg.getMessage("RegistrationFormViewer.mainTab"));
		
		initNameAndDescFields(msg.getMessage("RegistrationFormEditor.defaultName"));
		
		publiclyAvailable = new CheckBox(msg.getMessage("RegistrationFormEditor.publiclyAvailable"));
		publiclyAvailable.addValueChangeListener(event -> {
			boolean isPublic = publiclyAvailable.getValue();
			if (!isPublic)
				byInvitationOnly.setValue(false);
			byInvitationOnly.setEnabled(isPublic);
		});
		
		byInvitationOnly = new CheckBox(msg.getMessage("RegistrationFormEditor.byInvitationOnly"));
		byInvitationOnly.setEnabled(false);

		main.addComponents(name, description, publiclyAvailable, byInvitationOnly);

		notificationsEditor = new RegistrationFormNotificationsEditor(msg, groupsMan, 
				notificationsMan, msgTempMan);
		notificationsEditor.addToLayout(main);
		
		captcha = new Slider(msg.getMessage("RegistrationFormViewer.captcha"), 0, 
				RegistrationForm.MAX_CAPTCHA_LENGTH);
		captcha.setWidth(10, Unit.EM);
		captcha.setDescription(msg.getMessage("RegistrationFormEditor.captchaDescription"));
		
		main.addComponents(captcha);
	}
	
	private void initWrapUpTab() throws EngineException
	{
		tabs.addTab(getWrapUpComponent(t -> t.isSuitableForRegistration()), 
				msg.getMessage("RegistrationFormEditor.wrapUpTab"));
	}
	
	private void initCollectedTab() throws EngineException
	{
		FormLayout main = new CompactFormLayout();
		collectComments = new CheckBox(msg.getMessage("RegistrationFormEditor.collectComments"));
		registrationCode = new TextField(msg.getMessage("RegistrationFormViewer.registrationCode"));		
		main.addComponents(registrationCode, collectComments);

		TabSheet tabOfLists = createCollectedParamsTabs(notificationsEditor.getGroups(), false);
		Component remoteSignUpMetnodsTab = createRemoteSignupMethodsTab();
		tabOfLists.addTab(remoteSignUpMetnodsTab, 1);
		tabOfLists.setSelectedTab(0);

		VerticalLayout wrapper = new VerticalLayout(main, tabOfLists);
		tabs.addTab(wrapper, msg.getMessage("RegistrationFormViewer.collectedTab"));
	}

	private void initDisplaySettingsTab() throws EngineException
	{
		FormLayout main = new CompactFormLayout();
		
		initCommonDisplayedFields();
		title2ndStage = new I18nTextField(msg, msg.getMessage("RegistrationFormViewer.title2ndStage"));
		showGotoSignin = new CheckBox(msg.getMessage("RegistrationFormViewer.showGotoSignin"));
		showGotoSignin.addValueChangeListener(v -> signInUrl.setEnabled(showGotoSignin.getValue()));
		signInUrl = new TextField(msg.getMessage("RegistrationFormEditor.signinURL"));
		signInUrl.setWidth(100, Unit.PERCENTAGE);
		signInUrl.setEnabled(false);
		showCancel = new CheckBox(msg.getMessage("FormLayoutEditor.showCancel"));
		localSignupEmbeddedAsButton = new CheckBox(msg.getMessage("FormLayoutEditor.localSignupEmbeddedAsButton"));
		
		main.addComponents(displayedName, title2ndStage, formInformation, pageTitle, 
				showGotoSignin, signInUrl, showCancel, localSignupEmbeddedAsButton);

		layoutSettingsEditor = new RegistrationFormLayoutSettingsEditor(msg);
		VerticalLayout wrapper = new VerticalLayout(main, layoutSettingsEditor);
		wrapper.setMargin(true);
		wrapper.setSpacing(false);
		tabs.addTab(wrapper, msg.getMessage("RegistrationFormViewer.displayTab"));
	}
	
	private Component createRemoteSignupMethodsTab() throws EngineException
	{
		remoteAuthnSelections = new RemoteAuthnProvidersSelection(authenticatorSupport, 
				msg.getMessage("RegistrationFormEditor.availableRemoteAuthnOptions"), 
				msg.getMessage("RegistrationFormEditor.selectedRemoteAuthnOptions"), 
				msg.getMessage("RegistrationFormEditor.remoteAuthenOptions"),
				msg.getMessage("RegistrationFormEditor.remoteAuthenOptions.description"));
		
		FormLayout main = new CompactFormLayout();
		main.setWidth(60, Unit.PERCENTAGE);
		main.addComponents(remoteAuthnSelections);
		
		VerticalLayout remoteSignupLayout = new VerticalLayout(main);
		remoteSignupLayout.setSizeFull();
		remoteSignupLayout.setSpacing(false);
		remoteSignupLayout.setMargin(true);
		remoteSignupLayout.setCaption(msg.getMessage("RegistrationFormEditor.remoteSignupMethods"));
		return remoteSignupLayout;
	}
	
	private void initLayoutTab()
	{
		VerticalLayout wrapper = new VerticalLayout();
		layoutEditor = new RegistrationFormLayoutEditor(msg, this::formProvider);
		wrapper.addComponent(layoutEditor);
		tabs.addSelectedTabChangeListener(event -> layoutEditor.updateFromForm());
		tabs.addTab(wrapper, msg.getMessage("RegistrationFormViewer.layoutTab"));
	}
	
	private RegistrationForm formProvider()
	{
		try
		{
			return getFormBuilderBasic().build();
		} catch (Exception e)
		{
			log.debug("Ignoring layout update, form is invalid", e);
			return null;
		}
	}
	
	private void initAssignedTab() throws EngineException
	{
		FormLayout main = new CompactFormLayout();
		VerticalLayout wrapper = new VerticalLayout(main);
		wrapper.setMargin(true);
		wrapper.setSpacing(false);
		tabs.addTab(wrapper, msg.getMessage("RegistrationFormViewer.assignedTab"));
		
		credentialRequirementAssignment = new NotNullComboBox<>(
				msg.getMessage("RegistrationFormViewer.credentialRequirementAssignment"));
		List<String> credentialReqirementNames = credReqMan.getCredentialRequirements().stream()
				.map(CredentialRequirements::getName)
				.collect(Collectors.toList());
		credentialRequirementAssignment.setItems(credentialReqirementNames);
		
		profileEditor = new RegistrationTranslationProfileEditor(msg, actionsRegistry, actionComponentFactory);
		profileEditor.setValue(new TranslationProfile("form profile", "", ProfileType.REGISTRATION,
				new ArrayList<>()));
		main.addComponents(credentialRequirementAssignment);
		wrapper.addComponent(profileEditor);
	}

	public boolean isIgnoreRequests()
	{
		return ignoreRequests.getValue();
	}
}
