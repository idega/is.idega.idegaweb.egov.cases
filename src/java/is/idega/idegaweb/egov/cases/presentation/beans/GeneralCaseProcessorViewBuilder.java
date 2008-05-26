package is.idega.idegaweb.egov.cases.presentation.beans;

import java.rmi.RemoteException;

import javax.faces.component.UIComponent;

import com.idega.presentation.IWContext;

public interface GeneralCaseProcessorViewBuilder {

	public static final String SPRING_BEAN_IDENTIFIER = "GenericCaseProcessorViewBuilder";
	
	public UIComponent getCaseProcessorView(IWContext iwc) throws RemoteException;
	
}
