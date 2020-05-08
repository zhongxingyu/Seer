 package com.okaru.dtomapper.dto;
 
 import com.okaru.dtomapper.annotation.Embedded;
import com.okaru.dtomapper.annotation.MappedObject;
 
@MappedObject(key="businessAddress")
 public class BusinessAddressEmbeddedDTO {
 	@Embedded
 	private AddressDTO addressDTO;
 	@Embedded
 	private BusinessDTO businessDTO;
 	
 	public AddressDTO getAddressDTO() {
 		return addressDTO;
 	}
 	public void setAddressDTO(AddressDTO addressDTO) {
 		this.addressDTO = addressDTO;
 	}
 	public BusinessDTO getBusinessDTO() {
 		return businessDTO;
 	}
 	public void setBusinessDTO(BusinessDTO businessDTO) {
 		this.businessDTO = businessDTO;
 	}
 }
