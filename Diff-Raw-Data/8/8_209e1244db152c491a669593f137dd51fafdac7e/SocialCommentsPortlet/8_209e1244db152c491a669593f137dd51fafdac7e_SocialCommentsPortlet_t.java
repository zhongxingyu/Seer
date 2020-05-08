 /**
  * Copyright (c) 2013 Mika Koivisto <mika@javaguru.fi>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fi.javaguru.socialcomments.portlet;
 
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.util.PropertiesParamUtil;
 import com.liferay.portal.kernel.util.UnicodeProperties;
 import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
 import com.liferay.portal.service.GroupServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.ServiceContextFactory;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 import java.io.IOException;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 
 /**
  * @author Mika Koivisto
  */
 public class SocialCommentsPortlet extends MVCPortlet {
 
 	public void updateConfiguration(
 			ActionRequest actionRequest, ActionResponse actionResponse)
 		throws IOException, PortletException {
 
 		try {
 			ServiceContext serviceContext =
 				ServiceContextFactory.getInstance(actionRequest);
 
			long scopeGroupId = serviceContext.getScopeGroupId();
 
			Group scopeGroup = GroupLocalServiceUtil.getGroup(scopeGroupId);
 
 			if (scopeGroup.isStagingGroup()) {
 				scopeGroup = scopeGroup.getLiveGroup();
 			}
 
 			UnicodeProperties typeSettingsProperties =
 				scopeGroup.getTypeSettingsProperties();
 
 			UnicodeProperties properties = PropertiesParamUtil.getProperties(
 				actionRequest, "settings--");
 
 			typeSettingsProperties.putAll(properties);
 
 			GroupServiceUtil.updateGroup(
 				scopeGroup.getGroupId(), scopeGroup.getTypeSettings());
 		}
 		catch (Exception e) {
 			SessionErrors.add(actionRequest, e.getClass().getName());
 		}
 	}
 
 }
