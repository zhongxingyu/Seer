 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.sdm4.vaccination.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import dk.nsi.sdm4.testutils.TestDbConfiguration;
 import dk.nsi.sdm4.vaccination.config.VaccinationimporterApplicationConfig;
 import dk.nsi.sdm4.vaccination.model.Diseases;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 @ContextConfiguration(classes = {VaccinationimporterApplicationConfig.class, TestDbConfiguration.class})
 public class VaccinationParserIntegrationTest
 {
 	@Autowired
 	private VaccinationParser parser;
 
 	@Autowired
 	private JdbcTemplate jdbcTemplate;
 
 	@Test
 	public void unmarshallDiseases() throws IOException {
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("data/ExpDiseases.xml"));
         
         Object obj = parser.unmarshallFile(file, Diseases.class);
         
         if(obj instanceof Diseases) {
             Diseases diseases = (Diseases)obj;
             assertEquals(25, diseases.getDiseasesList().size());
         } else {
             fail("Unknown object: " + obj);
         }
 	    
 	}
 	
     @Test
     public void parseAndPersistDiseases() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("diseases"));
         
         parser.process(file);
         
         assertEquals(25, jdbcTemplate.queryForInt("select count(*) from ddv_diseases"));
         
         assertEquals("Brucellose vacciner", jdbcTemplate.queryForObject("select ATCText from ddv_diseases where DiseaseIdentifier = 2", String.class));
        assertEquals("2002-09-01T00:00:00.000", jdbcTemplate.queryForObject("select ddvValidFrom from ddv_diseases where DiseaseIdentifier = 2", String.class));
         
     }
 
     @Test
     public void parseAndPersistDiseasesVaccines() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("diseasesVaccines"));
         
         parser.process(file);
         assertEquals(1800, jdbcTemplate.queryForInt("select count(*) from ddv_diseases_vaccines"));
        assertEquals("2012-09-04T12:37:56.000", jdbcTemplate.queryForObject("select ddvModifiedDate from ddv_diseases_vaccines where DiseaseIdentifier = 25 and VaccineIdentifier = 1617209100", String.class));
     }
     
     @Test
     public void parseAndPersistDosageoptions() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("dosageoptions"));
         
         parser.process(file);
         assertEquals(10, jdbcTemplate.queryForInt("select count(*) from ddv_dosageoptions"));
         assertEquals(4, jdbcTemplate.queryForInt("select count(*) from ddv_dosageoptions where DrugIdentifier = 28101565493"));
         assertEquals("1 * 1/4 dosis - barn under 20 kg", jdbcTemplate.queryForObject("select DosageText from ddv_dosageoptions where DrugIdentifier = 28101565493 and dosageoptionIdentifier = 111111", String.class));
     }
 
     @Test
     public void parseAndPersistSSIDrugs() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("ssidrugs"));
         
         parser.process(file);
         assertEquals(4, jdbcTemplate.queryForInt("select count(*) from ddv_ssidrugs"));
         assertEquals("Zopiklon Merck NM", jdbcTemplate.queryForObject("select Name from ddv_ssidrugs where drugidentifier = 4", String.class));
     }
 
     @Test
     public void parseAndPersistVaccinationPlanItems() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("vaccinationplanitems"));
         
         parser.process(file);
         assertEquals(12, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinationplanitems"));
     }
 
     @Test
     public void parseAndPersistVaccinationPlans() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("vaccinationplans"));
         
         parser.process(file);
         assertEquals(2, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinationplans"));
     }
 
     @Test
     public void parseAndPersistVaccinesDrugs() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("vaccinesdrugs"));
         
         parser.process(file);
         assertEquals(38, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinesdrugs"));
     }
 
     @Test
     public void parseAndPersistVaccines() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("vaccines"));
         
         parser.process(file);
         assertEquals(72, jdbcTemplate.queryForInt("select count(*) from ddv_vaccines"));
     }
 
     @Test
     public void parseAndPersistAllFiles() throws IOException {
         // 1 folder containing 1 file
         File file = FileUtils.toFile(getClass().getClassLoader().getResource("data"));
         
         parser.process(file);
         assertEquals(72, jdbcTemplate.queryForInt("select count(*) from ddv_vaccines"));
         assertEquals(38, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinesdrugs"));
         assertEquals(2, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinationplans"));
         assertEquals(12, jdbcTemplate.queryForInt("select count(*) from ddv_vaccinationplanitems"));
         assertEquals(4, jdbcTemplate.queryForInt("select count(*) from ddv_ssidrugs"));
         assertEquals(10, jdbcTemplate.queryForInt("select count(*) from ddv_dosageoptions"));
         assertEquals(1800, jdbcTemplate.queryForInt("select count(*) from ddv_diseases_vaccines"));
         assertEquals(25, jdbcTemplate.queryForInt("select count(*) from ddv_diseases"));
     }
 }
