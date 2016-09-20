/*
 * Copyright (c) 2016 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.reg.reqman;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.CustomComponent;

import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;
import pl.edu.icm.unity.types.registration.EnquiryForm;
import pl.edu.icm.unity.types.registration.EnquiryResponse;
import pl.edu.icm.unity.types.registration.EnquiryResponseState;
import pl.edu.icm.unity.types.registration.RegistrationForm;
import pl.edu.icm.unity.types.registration.RegistrationRequest;
import pl.edu.icm.unity.types.registration.RegistrationRequestState;

/**
 * Component showing either {@link RegistrationReviewPanel} or {@link EnquiryReviewPanel} depending on an input to
 * show.
 * @author K. Benedyczak
 */
@PrototypeComponent
public class GenericReviewPanel extends CustomComponent
{
	private EnquiryReviewPanel enquiryPanel;
	private RegistrationReviewPanel registrationPanel;

	@Autowired
	public GenericReviewPanel(EnquiryReviewPanel enquiryPanel,
			RegistrationReviewPanel registrationPanel)
	{
		this.enquiryPanel = enquiryPanel;
		this.registrationPanel = registrationPanel;
	}

	public void setEnquiry(EnquiryResponseState requestState, EnquiryForm form)
	{
		enquiryPanel.setInput(requestState, form);
		setCompositionRoot(enquiryPanel);
	}
	
	public void setRegistration(RegistrationRequestState requestState, RegistrationForm form)
	{
		registrationPanel.setInput(requestState, form);
		setCompositionRoot(registrationPanel);
	}
	
	public RegistrationRequest getUpdatedRequest()
	{
		return registrationPanel.getUpdatedRequest();
	}
	
	public EnquiryResponse getUpdatedResponse()
	{
		return enquiryPanel.getUpdatedRequest();
	}
	
}
