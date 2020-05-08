 package com.aciertoteam.fastappoint.services.model;
 
 import com.aciertoteam.common.repository.EntityRepository;
 import com.aciertoteam.geo.entity.Country;
 import com.aciertoteam.geo.services.impl.DefaultGeoIpService;
 import jodd.bean.BeanUtil;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class DefaultGeoIpServiceTest {
 
     @Mock
     protected EntityRepository entityRepository;
 
     @InjectMocks
     private DefaultGeoIpService geoIpService = new DefaultGeoIpService();
 
     @Before
     public void setUp() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void defineCountryTest() {
        Country uk = new Country("Ukraine");
         when(entityRepository.findByField(Country.class, "name", "label.country.ukraine")).thenReturn(uk);
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/GeoIP.dat");
         geoIpService.defineCountry("91.218.213.156");
     }
 
     @Test
     public void defineCountryTestWithException() {
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/emptyFile.txt");
         assertNull(geoIpService.defineCountry("91.218.213.156"));
     }
 }
