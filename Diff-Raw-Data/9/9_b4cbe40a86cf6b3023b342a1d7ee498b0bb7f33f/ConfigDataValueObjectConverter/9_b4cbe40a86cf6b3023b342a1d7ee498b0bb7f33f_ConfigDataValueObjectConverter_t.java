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
 package com.rdonasco.config.util;
 
 import com.rdonasco.config.data.ConfigAttribute;
 import com.rdonasco.config.data.ConfigElement;
 import com.rdonasco.config.vo.ConfigAttributeVO;
 import com.rdonasco.config.vo.ConfigAttributeVOBuilder;
 import com.rdonasco.config.vo.ConfigElementVO;
 import com.rdonasco.config.vo.ConfigElementVOBuilder;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Roy F. Donasco
  */
 public class ConfigDataValueObjectConverter
 {
 
 	public static ConfigAttributeVO toConfigAttributeVO(
 			ConfigAttribute configAttribute)
 	{
 		ConfigAttributeVO configAttributeVO = null;
 		if (null != configAttribute)
 		{
 			configAttributeVO = new ConfigAttributeVOBuilder()
 					.setId(configAttribute.getId())
 					.setName(configAttribute.getName())
 					.setRoot(configAttribute.isRoot())
 					.setValue(configAttribute.getValue())
 					.setXpath(configAttribute.getXpath())
 					.createConfigAttributeVO();
 
 			configAttributeVO.setParentConfig(toConfigElementVO((ConfigElement) configAttribute.getParentConfig()));
 		}
 		return configAttributeVO;
 	}
 
 	public static ConfigAttribute toConfigAttribute(
 			ConfigAttributeVO configAttributeVO)
 	{
 		ConfigAttribute configAttribute = new ConfigAttribute();
 		configAttribute.setId(configAttributeVO.getId());
 		configAttribute.setName(configAttributeVO.getName());
 		configAttribute.setValue(configAttributeVO.getValue());
 		configAttribute.setXpath(configAttributeVO.getXpath());
 		configAttribute.setParentConfig((ConfigElement) toConfigElement((ConfigElementVO) configAttributeVO.getParentConfig()));
 		return configAttribute;
 	}
 
 	public static ConfigElementVO toConfigElementVO(ConfigElement configElement)
 	{
 		ConfigElementVO configElementVO = null;
 		if (null != configElement)
 		{
 			configElementVO = new ConfigElementVOBuilder()
 					.setId(configElement.getId())
 					.setName(configElement.getName())
 					.setParentConfig(toConfigElementVO((ConfigElement) configElement.getParentConfig()))
 					.setRoot(configElement.isRoot())
 					.setValue(configElement.getValue())
 					.setXpath(configElement.getXpath())
 					.setVersion(configElement.getVersion())
 					.createConfigElementVO();
 		}
 		return configElementVO;
 	}
 
 	public static ConfigElementVO toConfigElementVOIncludingAggregates(
 			ConfigElement configElement)
 	{
 		ConfigElementVO configElementVO = toConfigElementVO(configElement);
 		if (null != configElement)
 		{
 			if (configElement.getAttributes() != null)
 			{
 				List<ConfigAttributeVO> attributes = new ArrayList<ConfigAttributeVO>();
 				for (ConfigAttribute attribute : configElement.getAttributes())
 				{
 					ConfigAttributeVO attributeVO = toConfigAttributeVO(attribute);
 					attributes.add(attributeVO);
 				}
 				configElementVO.setAttributeVOList(attributes);
 			}
 			if (configElement.getSubConfigElements() != null)
 			{
 				List<ConfigElementVO> subElements = new ArrayList<ConfigElementVO>();
 				for (ConfigElement subElement : configElement.getSubConfigElements())
 				{
					ConfigElementVO elementVO = toConfigElementVOIncludingAggregates(subElement);
 					subElements.add(elementVO);
 				}
 				configElementVO.setSubConfigElementVOList(subElements);
 			}
 		}
 		return configElementVO;
 	}
 
 	public static ConfigElement toConfigElement(ConfigElementVO configElementVO)
 	{
 		ConfigElement configElement = null;
 		if (null != configElementVO)
 		{
 			configElement = new ConfigElement();
 			configElement.setId(configElementVO.getId());
 			configElement.setName(configElementVO.getName());
 			configElement.setValue(configElementVO.getValue());
 			configElement.setVersion(configElementVO.getVersion());
 			configElement.setXpath(configElementVO.getXpath());
 			if (configElementVO.getAttributeVOList() != null)
 			{
 				configElement.setAttributes(new ArrayList<ConfigAttribute>());
 				for (ConfigAttributeVO attributeVO : configElementVO.getAttributeVOList())
 				{
 					configElement.getAttributes().add(toConfigAttribute(attributeVO));
 				}
 			}
 			configElement.setParentConfig(toConfigElement(configElementVO.getParentConfig()));
 		}
 		return configElement;
 	}
 
 	public static ConfigElement toConfigElementIncludingAggregates(
 			ConfigElementVO configElementVO)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 }
