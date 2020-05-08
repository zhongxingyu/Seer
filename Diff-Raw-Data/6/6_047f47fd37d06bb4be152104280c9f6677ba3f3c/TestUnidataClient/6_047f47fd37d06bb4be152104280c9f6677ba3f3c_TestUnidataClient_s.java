 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampuse.unidataservice;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.trentorise.smartcampus.unidataservice.CanteenService;
 import eu.trentorise.smartcampus.unidataservice.StudentInfoService;
 import eu.trentorise.smartcampus.unidataservice.UnidataServiceException;
 import eu.trentorise.smartcampus.unidataservice.UniversityPlannerService;
 import eu.trentorise.smartcampus.unidataservice.model.AdData;
 import eu.trentorise.smartcampus.unidataservice.model.CanteenOpening;
 import eu.trentorise.smartcampus.unidataservice.model.CdsData;
 import eu.trentorise.smartcampus.unidataservice.model.FacoltaData;
 import eu.trentorise.smartcampus.unidataservice.model.Menu;
 import eu.trentorise.smartcampus.unidataservice.model.OperaStudent;
 import eu.trentorise.smartcampus.unidataservice.model.PdsData;
 import eu.trentorise.smartcampus.unidataservice.model.StudentInfoData;
 import eu.trentorise.smartcampus.unidataservice.model.StudentInfoExams;
 import eu.trentorise.smartcampus.unidataservice.model.TimeTableData;
 
 public class TestUnidataClient {
 
 	private StudentInfoService studentConnector;
 	private UniversityPlannerService UPConnector;
 	private CanteenService canteenConnector;
 
 	@Before
 	public void init() {
 		studentConnector = new StudentInfoService(Constants.PROFILE_SRV_URL);
 		UPConnector = new UniversityPlannerService(Constants.PROFILE_SRV_URL);
 		canteenConnector = new CanteenService(Constants.PROFILE_SRV_URL);
 	}
 
 	@Test
 	public void test() throws Exception {
		List<Menu> result = canteenConnector.getMenu(Constants.CLIENT_AUTH_TOKEN, "2013-11-1", "2013-11-2");
		System.out.println(result);
 		List<CanteenOpening> result2 = canteenConnector.getOpening(Constants.USER_AUTH_TOKEN);
 		System.out.println(result2);		
 	}
 	
 //	@Test
 	public void _testStudent() throws Exception {
 		Object result1, result2;
 		
 		result1 = studentConnector.getStudentData(Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(result1);
 		System.out.println(((StudentInfoData)result1).getFiscalCode());
 		
 		result2 = studentConnector.getStudentData(Constants.CLIENT_AUTH_TOKEN, "1");
 		Assert.assertNotNull(result2);
 		System.out.println(((StudentInfoData)result2).getFiscalCode());		
 		
 		Assert.assertEquals(((StudentInfoData)result1).getFiscalCode(), ((StudentInfoData)result2).getFiscalCode());
 		
 		result1 = studentConnector.getStudentExams(Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(result1);
 		System.out.println(((StudentInfoExams)result1).getExams().get(0).getName());		
 		
 		result2 = studentConnector.getStudentExams(Constants.CLIENT_AUTH_TOKEN, "1");
 		Assert.assertNotNull(result2);
 		System.out.println(((StudentInfoExams)result2).getExams().get(0).getName());				
 		
 		Assert.assertEquals(((StudentInfoExams)result1).getExams().get(0).getName(),((StudentInfoExams)result2).getExams().get(0).getName());			
 		
 		result1 = studentConnector.getOperaCard(Constants.USER_AUTH_TOKEN);
 		Assert.assertNotNull(result1);
 		System.out.println(((OperaStudent)result1).getName());			
 		
 		result2 = studentConnector.getOperaCard(Constants.CLIENT_AUTH_TOKEN, "1");
 		Assert.assertNotNull(result2);
 		System.out.println(((OperaStudent)result2).getName());					
 		
 		Assert.assertEquals(((OperaStudent)result1).getName(), ((OperaStudent)result2).getName());			
 		
 	}
 	
 //	@Test
 	public void _testUP() throws Exception {
 		long start;
 		Object result;
 		
 		start = System.currentTimeMillis();
 		result = UPConnector.getFacoltaData(Constants.CLIENT_AUTH_TOKEN);
 		System.out.println(result);
 		System.out.println(System.currentTimeMillis() - start);
 		
 		start = System.currentTimeMillis();
 		FacoltaData fd = ((List<FacoltaData>)result).get(2);
 		result = UPConnector.getCdsData(Constants.CLIENT_AUTH_TOKEN, fd.getFacId());
 		System.out.println(result);		
 		System.out.println(System.currentTimeMillis() - start);
 		
 		start = System.currentTimeMillis();
 		CdsData cd = ((List<CdsData>)result).get(0);
 		result = UPConnector.getAdData(Constants.CLIENT_AUTH_TOKEN,  cd.getCdsId(), cd.getAaOrd(), "2013");
 		System.out.println(result);		
 		System.out.println(System.currentTimeMillis() - start);
 
 		start = System.currentTimeMillis();
 		AdData ad = ((List<AdData>)result).get(0);
 		PdsData pds = cd.getPds().get(0);
 		result = UPConnector.getTimetable(Constants.CLIENT_AUTH_TOKEN,  cd.getCdsCod(), cd.getAaOrd(), "2013", pds.getPdsId(), pds.getPdsCod(), ad.getAdcod(), ad.getDomPart().get(0), ad.getFatPart().get(0));
 		System.out.println(result);				
 		System.out.println(System.currentTimeMillis() - start);
 		
 	}
 	
 
 }
