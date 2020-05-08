 package rental;
 
 import rental.session.ManagerSession;
 import rental.session.ManagerSessionImpl;
 import rental.session.ReservationSession;
 import rental.session.ReservationSessionImpl;
 
public class AgencyImpl implements Ageny {
 
 	private final CompanyRegistry companyRegistry;
 
 	public AgencyImpl(CompanyRegistry companyRegistry) {
 		this.companyRegistry = companyRegistry;
 	}
 
 	protected CompanyRegistry getCompanyRegistry() {
 		return companyRegistry;
 	}
 
 	@Override
 	public ReservationSession createReservationSession(String clientName) {
 		return new ReservationSessionImpl(getCompanyRegistry(), clientName);
 	}
 
 	@Override
 	public ManagerSession createManagerSession(String managerName) {
 		return new ManagerSessionImpl(getCompanyRegistry(), managerName);
 	}
 
 }
