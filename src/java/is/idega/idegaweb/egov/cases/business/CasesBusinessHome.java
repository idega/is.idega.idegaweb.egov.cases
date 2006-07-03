package is.idega.idegaweb.egov.cases.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface CasesBusinessHome extends IBOHome {

	public CasesBusiness create() throws CreateException, RemoteException;
}