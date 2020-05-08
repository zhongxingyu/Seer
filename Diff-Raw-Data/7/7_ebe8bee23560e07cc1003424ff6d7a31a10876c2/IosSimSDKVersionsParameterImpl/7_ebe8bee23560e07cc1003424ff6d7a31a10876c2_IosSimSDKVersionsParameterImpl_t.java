 /**
  * Phresco Framework Implementation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.framework.param.impl;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.parsers.*;
 
import org.apache.commons.collections.CollectionUtils;
 import org.xml.sax.*;
 
 import com.photon.phresco.api.*;
 import com.photon.phresco.exception.*;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.*;
 import com.photon.phresco.util.*;
 import com.photon.phresco.util.IosSdkUtil.*;
 
 public class IosSimSDKVersionsParameterImpl implements DynamicParameter  {
 
 	public PossibleValues getValues(Map<String, Object> map)
 			throws IOException, ParserConfigurationException, SAXException,
 			ConfigurationException, PhrescoException {
 		PossibleValues possibleValues = new PossibleValues();
 		List<String> iphoneSimSdkVersions = IosSdkUtil.getMacSdksVersions(MacSdkType.iphonesimulator);
		if (CollectionUtils.isNotEmpty(iphoneSimSdkVersions)) {
			for (String iphoneSimSdkVersion : iphoneSimSdkVersions) {
 				Value value = new Value();
 				value.setValue(iphoneSimSdkVersion);
 				possibleValues.getValue().add(value);
			}
 		}
 		return possibleValues;
 	}
 
 }
