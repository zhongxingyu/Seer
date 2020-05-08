 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.unit;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.patientview.user.UserUtils;
 import org.patientview.service.UnitManager;
 import org.patientview.service.UserManager;
 import org.patientview.utils.LegacySpringUtils;
 import org.apache.commons.beanutils.BeanUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public final class UnitUtils {
 
     public static final String PATIENT_ENTERS_UNITCODE = "PATIENT";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UnitUtils.class);
 
     private UnitUtils() {
     }
 
     /**
      * this method will search units depend on login user's role and sourceType of unit('radargroup', 'renalunit').
      * superadmin will get all 'radargroup' and 'renalunit' units, unitadmin will get all units which he belongs to,
      * other role user won't get any unit, like radaradmin.
      * searching result will be as a attribute in request.
      * @param request
      */
     public static void setUserUnits(HttpServletRequest request) {
         UserManager userManager = LegacySpringUtils.getUserManager();
         UnitManager unitManager = LegacySpringUtils.getUnitManager();
         User user =  LegacySpringUtils.getUserManager().getLoggedInUser();
        List items;
        final String role = userManager.getCurrentSpecialtyRole(user);
         if (userManager.getCurrentSpecialtyRole(user).equals("superadmin")) {
             items = unitManager.getAll(null, new String[]{"radargroup", "renalunit"});
        } else if (role.equals("unitadmin") || role.equals("unitstaff")) {
             items = unitManager.getLoggedInUsersUnits();
         } else {
             items = new ArrayList();
         }
 
         request.setAttribute("units", items);
     }
 
     /**
      * this method will search units depend on login user's role and 'renalunit' unit
      * superadmin will get all 'renalunit' units, unitadmin will get all 'renalunit' units which he belongs to,
      * other role user won't get any unit, like radaradmin.
      * searching result will be as a attribute in request.
      * @param request
      */
     public static void setUserRenalUnits(HttpServletRequest request) {
         UserManager userManager = LegacySpringUtils.getUserManager();
         UnitManager unitManager = LegacySpringUtils.getUnitManager();
         User user =  LegacySpringUtils.getUserManager().getLoggedInUser();
         List items = unitManager.getAll(null, new String[]{"renalunit"});
         if (userManager.getCurrentSpecialtyRole(user).equals("superadmin")) {
             request.setAttribute("units", items);
         } else if (userManager.getCurrentSpecialtyRole(user).equals("unitadmin")) {
             List userUnits = unitManager.getLoggedInUsersUnits();
             Collection units = CollectionUtils.intersection(userUnits, items);
             request.setAttribute("units", units);
         }
     }
 
     public static void putRelevantUnitsInRequest(HttpServletRequest request) throws Exception {
         putRelevantUnitsInRequestMinusSomeUnits(request, new String[]{PATIENT_ENTERS_UNITCODE}, new String[]{});
     }
 
     public static void putRelevantUnitsInRequestMinusSomeUnits(HttpServletRequest request, String[] notTheseUnitCodes,
                                                                String[] plusTheseUnitCodes) throws Exception {
         List items = LegacySpringUtils.getUnitManager().getLoggedInUsersUnits(notTheseUnitCodes, plusTheseUnitCodes);
         request.getSession().setAttribute("units", items);
     }
 
     public static Unit retrieveUnit(String unitcode) {
         unitcode = unitcode.toUpperCase();
         Unit unit = null;
         try {
             unit = LegacySpringUtils.getUnitManager().get(unitcode);
         } catch (Exception e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
         return unit;
     }
 
     public static String retrieveUnitcode(HttpServletRequest request) {
         User user = UserUtils.retrieveUser(request);
 
         UserMapping userMapping = UserUtils.retrieveUserMappingsPatientEntered(user);
 
         return userMapping.getUnitcode();
     }
 
     // update the unit by setting it's properties
     public static void buildUnit(Unit unit, Object form) throws Exception {
 
         // set defaults for sourceType and country, note this runs for updates as well as creates
         unit.setSourceType(BeanUtils.getProperty(form, "sourceType"));
         if (unit.getSourceType() == null || unit.getSourceType().length() == 0) {
             unit.setSourceType("renalunit");
         }
 
         if (unit.getCountry() == null || unit.getCountry().length() == 0) {
             unit.setCountry("1");
         }
 
         // build object
         unit.setUnitcode(BeanUtils.getProperty(form, "unitcode"));
         unit.setName(BeanUtils.getProperty(form, "name"));
         unit.setShortname(BeanUtils.getProperty(form, "shortname"));
         unit.setUnituser(BeanUtils.getProperty(form, "unituser"));
         unit.setAddress1(BeanUtils.getProperty(form, "address1"));
         unit.setAddress2(BeanUtils.getProperty(form, "address2"));
         unit.setAddress3(BeanUtils.getProperty(form, "address3"));
         unit.setPostcode(BeanUtils.getProperty(form, "postcode"));
         unit.setUniturl(BeanUtils.getProperty(form, "uniturl"));
         unit.setTrusturl(BeanUtils.getProperty(form, "trusturl"));
         unit.setRenaladminname(BeanUtils.getProperty(form, "renaladminname"));
         unit.setRenaladminphone(BeanUtils.getProperty(form, "renaladminphone"));
         unit.setRenaladminemail(BeanUtils.getProperty(form, "renaladminemail"));
         unit.setUnitenquiriesphone(BeanUtils.getProperty(form, "unitenquiriesphone"));
         unit.setUnitenquiriesemail(BeanUtils.getProperty(form, "unitenquiriesemail"));
         unit.setAppointmentphone(BeanUtils.getProperty(form, "appointmentphone"));
         unit.setAppointmentemail(BeanUtils.getProperty(form, "appointmentemail"));
         unit.setOutofhours(BeanUtils.getProperty(form, "outofhours"));
         unit.setPeritonealdialysisphone(BeanUtils.getProperty(form, "peritonealdialysisphone"));
         unit.setPeritonealdialysisemail(BeanUtils.getProperty(form, "peritonealdialysisemail"));
 
         unit.setHaemodialysisunitname1(BeanUtils.getProperty(form, "haemodialysisunitname1"));
         unit.setHaemodialysisunitphone1(BeanUtils.getProperty(form, "haemodialysisunitphone1"));
         unit.setHaemodialysisunitlocation1(BeanUtils.getProperty(form, "haemodialysisunitlocation1"));
         unit.setHaemodialysisuniturl1(BeanUtils.getProperty(form, "haemodialysisuniturl1"));
 
         unit.setHaemodialysisunitname2(BeanUtils.getProperty(form, "haemodialysisunitname2"));
         unit.setHaemodialysisunitphone2(BeanUtils.getProperty(form, "haemodialysisunitphone2"));
         unit.setHaemodialysisunitlocation2(BeanUtils.getProperty(form, "haemodialysisunitlocation2"));
         unit.setHaemodialysisuniturl2(BeanUtils.getProperty(form, "haemodialysisuniturl2"));
 
         unit.setHaemodialysisunitname3(BeanUtils.getProperty(form, "haemodialysisunitname3"));
         unit.setHaemodialysisunitphone3(BeanUtils.getProperty(form, "haemodialysisunitphone3"));
         unit.setHaemodialysisunitlocation3(BeanUtils.getProperty(form, "haemodialysisunitlocation3"));
         unit.setHaemodialysisuniturl3(BeanUtils.getProperty(form, "haemodialysisuniturl3"));
 
         unit.setHaemodialysisunitname4(BeanUtils.getProperty(form, "haemodialysisunitname4"));
         unit.setHaemodialysisunitphone4(BeanUtils.getProperty(form, "haemodialysisunitphone4"));
         unit.setHaemodialysisunitlocation4(BeanUtils.getProperty(form, "haemodialysisunitlocation4"));
         unit.setHaemodialysisuniturl4(BeanUtils.getProperty(form, "haemodialysisuniturl4"));
 
         unit.setHaemodialysisunitname5(BeanUtils.getProperty(form, "haemodialysisunitname5"));
         unit.setHaemodialysisunitphone5(BeanUtils.getProperty(form, "haemodialysisunitphone5"));
         unit.setHaemodialysisunitlocation5(BeanUtils.getProperty(form, "haemodialysisunitlocation5"));
         unit.setHaemodialysisuniturl5(BeanUtils.getProperty(form, "haemodialysisuniturl5"));
 
         unit.setHaemodialysisunitname6(BeanUtils.getProperty(form, "haemodialysisunitname6"));
         unit.setHaemodialysisunitphone6(BeanUtils.getProperty(form, "haemodialysisunitphone6"));
         unit.setHaemodialysisunitlocation6(BeanUtils.getProperty(form, "haemodialysisunitlocation6"));
         unit.setHaemodialysisuniturl6(BeanUtils.getProperty(form, "haemodialysisuniturl6"));
 
         unit.setHaemodialysisunitname7(BeanUtils.getProperty(form, "haemodialysisunitname7"));
         unit.setHaemodialysisunitphone7(BeanUtils.getProperty(form, "haemodialysisunitphone7"));
         unit.setHaemodialysisunitlocation7(BeanUtils.getProperty(form, "haemodialysisunitlocation7"));
         unit.setHaemodialysisuniturl7(BeanUtils.getProperty(form, "haemodialysisuniturl7"));
 
         unit.setHaemodialysisunitname8(BeanUtils.getProperty(form, "haemodialysisunitname8"));
         unit.setHaemodialysisunitphone8(BeanUtils.getProperty(form, "haemodialysisunitphone8"));
         unit.setHaemodialysisunitlocation8(BeanUtils.getProperty(form, "haemodialysisunitlocation8"));
         unit.setHaemodialysisuniturl8(BeanUtils.getProperty(form, "haemodialysisuniturl8"));
 
         unit.setHaemodialysisunitname9(BeanUtils.getProperty(form, "haemodialysisunitname9"));
         unit.setHaemodialysisunitphone9(BeanUtils.getProperty(form, "haemodialysisunitphone9"));
         unit.setHaemodialysisunitlocation9(BeanUtils.getProperty(form, "haemodialysisunitlocation9"));
         unit.setHaemodialysisuniturl9(BeanUtils.getProperty(form, "haemodialysisuniturl9"));
 
         unit.setHaemodialysisunitname10(BeanUtils.getProperty(form, "haemodialysisunitname10"));
         unit.setHaemodialysisunitphone10(BeanUtils.getProperty(form, "haemodialysisunitphone10"));
         unit.setHaemodialysisunitlocation10(BeanUtils.getProperty(form, "haemodialysisunitlocation10"));
         unit.setHaemodialysisuniturl10(BeanUtils.getProperty(form, "haemodialysisuniturl10"));
 
         unit.setHaemodialysisunitname11(BeanUtils.getProperty(form, "haemodialysisunitname11"));
         unit.setHaemodialysisunitphone11(BeanUtils.getProperty(form, "haemodialysisunitphone11"));
         unit.setHaemodialysisunitlocation11(BeanUtils.getProperty(form, "haemodialysisunitlocation11"));
         unit.setHaemodialysisuniturl11(BeanUtils.getProperty(form, "haemodialysisuniturl11"));
 
         unit.setHaemodialysisunitname12(BeanUtils.getProperty(form, "haemodialysisunitname12"));
         unit.setHaemodialysisunitphone12(BeanUtils.getProperty(form, "haemodialysisunitphone12"));
         unit.setHaemodialysisunitlocation12(BeanUtils.getProperty(form, "haemodialysisunitlocation12"));
         unit.setHaemodialysisuniturl12(BeanUtils.getProperty(form, "haemodialysisuniturl12"));
     }
 }
