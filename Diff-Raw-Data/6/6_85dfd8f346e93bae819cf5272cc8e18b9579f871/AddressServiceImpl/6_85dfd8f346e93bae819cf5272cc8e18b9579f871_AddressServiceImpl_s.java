 package com.res.service.impl;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.res.dao.hibernate.AddressDao;
 import com.res.domain.Address;
 import com.res.exception.ServiceException;
 import com.res.service.AddressService;
 import com.res.util.MessageLoader;
 
 @Service("addressService")
 @Transactional
 public class AddressServiceImpl implements AddressService {
 	
 	private static Logger logger = Logger.getLogger(AddressServiceImpl.class);
 	
 	@Autowired 
 	private AddressDao addressDao;
 	
 	@Autowired 
 	private MessageLoader messageLoader;
 
 	public void saveOrUpdate(Address address) throws ServiceException {
		if(address.getState() == null){
			throw new ServiceException("state.is.required");
 		}
		address.setState(address.getState().toUpperCase());
 		
 		Long addressId = addressDao.isAddressUnique(address.getStreet1(), address.getCity());
 		if(addressId == null){
 			addressDao.save(address);
 		}else{
 			if(logger.isDebugEnabled()){
 				logger.debug("address is not unique, updating.");
 			}
 			address.setAddressId(addressId);
 			addressDao.update(address);
 		}	
 	}
 
 	public void update(Address address) {
 		addressDao.update(address);
 
 	}
 
 	public void delete(Address address) {
 		addressDao.delete(address);
 
 	}
 
 	public Address findByAddressId(long id) {
 		return addressDao.findByAddressId(id);
 	}
 
 	@Override
 	public List<Address> listAddress() {
 		return addressDao.listAddress();
 	}
 
 	@Override
 	public void deleteAddress(long id) {
 		addressDao.deleteAddress(id);
 	}
 
 	@Override
 	public List<String> typeaheadStreet1(long restaurantId) {
 		return addressDao.typeaheadAttribute(restaurantId, "street1");
 	}
 
 	@Override
 	public List<String> typeaheadStreet2(long restaurantId) {
 		return addressDao.typeaheadAttribute(restaurantId, "street2");
 	}
 
 	@Override
 	public List<String> typeaheadCity(long restaurantId) {
 		return addressDao.typeaheadAttribute(restaurantId, "city");
 	}
 
 	@Override
 	public List<String> typeaheadState(long restaurantId) {
 		return addressDao.typeaheadAttribute(restaurantId, "state");
 	}
 
 	@Override
 	public List<String> typeaheadZipcode(long restaurantId) {
 		return addressDao.typeaheadAttribute(restaurantId, "zipCode");
 	}
 
 }
