package is.idega.idegaweb.egov.cases.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class CasesBusinessHomeImpl extends IBOHomeImpl implements CasesBusinessHome {

	public Class getBeanInterfaceClass() {
		return CasesBusiness.class;
	}

	public CasesBusiness create() throws CreateException {
		return (CasesBusiness) super.createIBO();
	}
}