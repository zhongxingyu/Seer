 package com.westchase.ejb;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 
import org.apache.commons.lang.StringUtils;
 import org.hibernate.criterion.Order;
 import org.jboss.ejb3.annotation.SecurityDomain;
 
 import com.westchase.persistence.criteria.CompanySearchCriteria;
 import com.westchase.persistence.dao.CompanyCodeDAO;
 import com.westchase.persistence.dao.CompanyDAO;
 import com.westchase.persistence.dao.CompanyMapnoDAO;
 import com.westchase.persistence.dao.CompanyTypeDAO;
 import com.westchase.persistence.dao.NaicsDAO;
 import com.westchase.persistence.dao.StateDAO;
 import com.westchase.persistence.dao.StreetDAO;
 import com.westchase.persistence.model.Company;
 import com.westchase.persistence.model.CompanyCode;
 import com.westchase.persistence.model.CompanyMapno;
 import com.westchase.persistence.model.CompanyType;
 import com.westchase.persistence.model.Naics;
 import com.westchase.persistence.model.Property;
 import com.westchase.persistence.model.State;
 import com.westchase.persistence.model.Street;
 import com.westchase.utils.FormatUtils;
 
 /**
  * @author marc
  *
  */
 @Stateless
 @SecurityDomain("WestchaseRealm")
 @Local(CompanyService.class)
 public class CompanyServiceBean implements CompanyService {
 
 	@EJB
 	private AuditService audServ;
 	
 	@Override
 	public List<Company> findAll(CompanySearchCriteria criteria) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.findAll(criteria);
 	}
 
 	@Override
 	public long findAllCount(CompanySearchCriteria criteria) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.findAllCount(criteria);
 	}
 	
 	@Override
 	public Company get(Integer companyId) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.get(companyId);
 	}
 
 	@Override
 	public void saveOrUpdate(Company company, int employeeId) {
 		if (company != null) {
 	    	if (company.getId() == null) {
 	    		company.setInputDate(new Date());
 	    	}
 			company.setLastUpdate(new Date());
 			
 			// format phone numbers
 			company.setWkPhone(FormatUtils.formatPhoneNumber(company.getWkPhone()));
 			company.setFaxPhone(FormatUtils.formatPhoneNumber(company.getFaxPhone()));
 			
 			// not sure why this is necessary, but...
 			if (company.getCompanyType() != null && company.getCompanyType().getId() == null) {
 				company.setCompanyType(null);
 			}
			if (company.getCompanyCode() != null && StringUtils.isBlank(company.getCompanyCode().getCode())) {
				company.setCompanyCode(null);
			}
 			
 			CompanyDAO dao = new CompanyDAO();
 			dao.saveOrUpdate(company);
 			
         	if (audServ != null) {
     			audServ.save(employeeId, company);
         	}			
 		}
 	}
 
 	@Override
 	public List<Company> findAllWithOrder(String col) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.findAll(Order.asc(col));
 	}
 
 	@Override
 	public Company getByPhoneBook(Integer phoneBookId) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.getByPhoneBook(phoneBookId);
 	}
 
 	@Override
 	public boolean saveCompanyProperty(Integer id, Integer companyId, Integer propertyId, boolean primary) {
 		CompanyMapnoDAO dao = new CompanyMapnoDAO();
 		CompanyMapno cm = new CompanyMapno();
 		if (id != null && id.intValue() > 0) {
 			cm = new CompanyMapno(id);
 		}
 		cm.setPrimary(primary);
 		cm.setCompany(new Company(companyId));
 		cm.setProperty(new Property(propertyId));
 		dao.saveOrUpdate(cm);
 		return true;
 	}
 
 	@Override
 	public void removeCompanyProperty(Integer companyPropertyId) {
 		CompanyMapnoDAO dao = new CompanyMapnoDAO();
 		dao.delete(companyPropertyId);
 	}
 
 	@Override
 	public CompanyMapno getCompanyProperty(Integer companyPropertyId) {
 		CompanyMapnoDAO dao = new CompanyMapnoDAO();
 		return dao.findById(companyPropertyId);
 	}
 
 	@Override
 	public List<Company> getByProperty(Integer propertyId) {
 		CompanyDAO dao = new CompanyDAO();
 		return dao.getByProperty(propertyId);
 	}
 
 	@Override
 	public List<State> findAllStates() {
 		StateDAO dao = new StateDAO();
 		return dao.findAll(Order.asc("name"));
 	}
 
 	@Override
 	public List<Naics> findAllNaics() {
 		NaicsDAO dao = new NaicsDAO();
 		return dao.findAll(Order.asc("description"));
 	}
 
 	@Override
 	public void delete(Integer id) {
 		CompanyDAO dao = new CompanyDAO();
 		dao.delete(id);
 	}
 	
 	@Override
 	public List<CompanyType> listCompanyTypes() {
 		CompanyTypeDAO dao = new CompanyTypeDAO();
 		return dao.findAll(Order.asc("name"));
 	}
 	
 	@Override
 	public List<Company> findAllWithType(int companyTypeId) {
 		CompanyDAO dao = new CompanyDAO();
 		if (companyTypeId <= 0)
 			return dao.findAllWithType();
 		return dao.findAllWithType(companyTypeId);
 	}
 
 	@Override
 	public List<Street> listStreets() {
 		final StreetDAO dao = new StreetDAO();
 		return dao.findAll(Order.asc("name"));
 	}
 
 	@Override
 	public List<CompanyCode> listCompanyCodes() {
 		CompanyCodeDAO dao = new CompanyCodeDAO();
 		return dao.findAll(Order.asc("name"));
 	}
 
 }
