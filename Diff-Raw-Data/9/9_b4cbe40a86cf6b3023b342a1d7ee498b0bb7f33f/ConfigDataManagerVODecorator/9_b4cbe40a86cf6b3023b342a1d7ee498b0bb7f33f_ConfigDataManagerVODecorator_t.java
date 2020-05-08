 /*
  * Copyright 2013 Roy F. Donasco.
  * File Created on: 24-Feb-2013
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.rdonasco.config.services;
 
 import com.rdonasco.common.exceptions.DataAccessException;
 import com.rdonasco.common.exceptions.NonExistentEntityException;
 import com.rdonasco.config.data.ConfigAttribute;
 import com.rdonasco.config.data.ConfigElement;
 import com.rdonasco.config.exceptions.ConfigXPathException;
 import com.rdonasco.config.exceptions.LoadValueException;
 import com.rdonasco.config.util.ConfigDataValueObjectConverter;
 import com.rdonasco.config.vo.ConfigAttributeVO;
 import com.rdonasco.config.vo.ConfigElementVO;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author Roy F. Donasco
  */
 @Stateless
 public class ConfigDataManagerVODecorator implements
 		ConfigDataManagerVODecoratorRemote
 {
 
 	@EJB
 	private ConfigDataManagerLocal configDataManager;
 	
 	@Override
 	public ConfigAttributeVO saveAttribute(ConfigAttributeVO attribute) throws
 			DataAccessException
 	{
 		ConfigAttribute attibuteToSave = ConfigDataValueObjectConverter.toConfigAttribute(attribute);
 		ConfigAttribute savedAttribute = configDataManager.saveAttribute(attibuteToSave);
 		return ConfigDataValueObjectConverter.toConfigAttributeVO(savedAttribute);
 	}
 
 	@Override
 	public void updateAttribute(ConfigAttributeVO attribute) throws
 			DataAccessException
 	{
 		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void deleteAttribute(ConfigAttributeVO configAttribute) throws
 			DataAccessException
 	{
 		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public ConfigElementVO findConfigElementWithXpath(String xpath) throws
 			DataAccessException, NonExistentEntityException
 	{
 		return ConfigDataValueObjectConverter.toConfigElementVO(configDataManager.findConfigElementWithXpath(xpath));
 		
 	}
 
 	@Override
 	public ConfigAttributeVO findConfigAttributeWithXpath(String xpath) throws
 			DataAccessException, NonExistentEntityException
 	{
 		return ConfigDataValueObjectConverter.toConfigAttributeVO(
 				configDataManager.findConfigAttributeWithXpath(xpath));
 	}
 
 	@Override
 	public List<ConfigElementVO> findConfigElementsWithXpath(String xpath)
 			throws DataAccessException
 	{
 		List<ConfigElementVO> elementVOList = new ArrayList<ConfigElementVO>();
 		List<ConfigElement> elements = configDataManager.findConfigElementsWithXpath(xpath);
 		for(ConfigElement element : elements)
 		{
 			elementVOList.add(ConfigDataValueObjectConverter.toConfigElementVO(element));			
 		}
 		return elementVOList;
 	}
 
 	@Override
 	public List<ConfigAttributeVO> findConfigAttributesWithXpath(String xpath)
 			throws DataAccessException
 	{
 		List<ConfigAttributeVO> attributeVOs = new ArrayList<ConfigAttributeVO>();
 		List<ConfigAttribute> attributes = configDataManager.findConfigAttributesWithXpath(xpath);
 		for(ConfigAttribute attribute : attributes)
 		{
 			attributeVOs.add(ConfigDataValueObjectConverter.toConfigAttributeVO(attribute));
 		}
 		return attributeVOs;
 	}
 
 	@Override
 	public <T> T loadValue(String xpath,
 			Class<T> t) throws LoadValueException
 	{
 		return configDataManager.loadValue(xpath, t);
 	}
 
 	@Override
 	public <T> T loadValue(String xpath,
 			Class<T> t, T defaultValue)
 	{
 		return configDataManager.loadValue(xpath, t, defaultValue);
 	}
 
 //	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	@Override
 	public ConfigAttributeVO createAttributeFromXpath(String xpath, Object value)
 			throws DataAccessException, ConfigXPathException
 	{
 		return ConfigDataValueObjectConverter.toConfigAttributeVO(configDataManager.createAttributeFromXpath(xpath, value));
 	}
 
 	@Override
 	public ConfigElementVO saveConfigElement(ConfigElementVO configElementVO)
 			throws DataAccessException
 	{
 		ConfigElement parentElement = ConfigDataValueObjectConverter.toConfigElement(configElementVO.getParentConfig());
 		ConfigElement configElement = ConfigDataValueObjectConverter.toConfigElement(configElementVO);
 		configElement.setParentConfig(parentElement);
 		ConfigElement savedElement = configDataManager.saveData(configElement);
 		ConfigElementVO savedElementVO = ConfigDataValueObjectConverter.toConfigElementVO(savedElement);
 		return savedElementVO;
 	}
 
 	@Override
 	public ConfigElementVO loadConfigElement(ConfigElementVO savedParentConfig)
 			throws DataAccessException
 	{
 		ConfigElement configElementToSearch = new ConfigElement();
 		configElementToSearch.setId(savedParentConfig.getId());
 		ConfigElement configElement = configDataManager.loadData(configElementToSearch);
 		ConfigElementVO configElementFound = ConfigDataValueObjectConverter.toConfigElementVOIncludingAggregates(configElement);
 		return configElementFound;
 	}
 
 	@Override
 	public void deleteConfigElement(ConfigElementVO configElementVO) throws
 			DataAccessException
 	{
 		ConfigElement elementToDelete = ConfigDataValueObjectConverter.toConfigElement(configElementVO);
 		configDataManager.deleteData(elementToDelete);		
 	}
 
 	@Override
 	public List<ConfigElementVO> retrieveAllData() throws DataAccessException
 	{
 		List<ConfigElement> allConfigElement = configDataManager.retrieveAllData();
 		List<ConfigElementVO> allConfigElementVO = new ArrayList<ConfigElementVO>();
 		for (ConfigElement element : allConfigElement)
 		{
			allConfigElementVO.add(ConfigDataValueObjectConverter.toConfigElementVOIncludingAggregates(element));
 		}
 		return allConfigElementVO;
 	}
 
 	@Override
 	public void updateData(ConfigElementVO configElementVO) throws
 			DataAccessException
 	{
 		configDataManager.updateData(ConfigDataValueObjectConverter.toConfigElement(configElementVO));
 	}
 
 	@Override
 	public ConfigElementVO saveData(ConfigElementVO configElement) throws
 			DataAccessException
 	{
 		ConfigElement saveData = configDataManager.saveData(ConfigDataValueObjectConverter.toConfigElement(configElement));
 		return ConfigDataValueObjectConverter.toConfigElementVO(saveData);
 	}
 
 	@Override
 	public void deleteData(ConfigElementVO configElement) throws
 			DataAccessException
 	{
 		configDataManager.deleteData(ConfigDataValueObjectConverter.toConfigElement(configElement));
 	}
 }
