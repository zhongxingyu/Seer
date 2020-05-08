 package com.site2go.services.impl;
 
 import com.site2go.dao.entities.SiteEntity;
 import com.site2go.dao.repositories.SiteRepository;
 import com.site2go.dto.Site;
 import com.site2go.dto.mapper.Site2goBeanMapper;
 import com.site2go.services.SiteService;
 import org.dozer.Mapper;
 import org.junit.Before;
 import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class SiteServiceTest {
     private Mapper beanMapper;
     private SiteServiceImpl siteService;
     private SiteRepository mockSiteRepository;
 
     @Before
     public void setup() {
         this.beanMapper = new Site2goBeanMapper();
         this.siteService = new SiteServiceImpl();
         this.siteService.setMapper(this.beanMapper);
         this.mockSiteRepository = mock(SiteRepository.class);
         this.siteService.setSiteRepository(this.mockSiteRepository);
     }
 
     @Test
     public void testFindByDomain() {
         SiteEntity entity = new SiteEntity();
         entity.setDomain("test.com");
         when(this.mockSiteRepository.findByDomain("test.com")).thenReturn(entity);
 
         Site site = this.siteService.getSiteByDomain("test.com");
         assertNotNull(site);
         assertEquals("test.com", site.getDomain());
     }
 
     @Test
     public void testFindByNonexistentDomainReturnsNull() {
        when(this.mockSiteRepository.findByDomain("test.com")).thenThrow(new EmptyResultDataAccessException(1));
         Site site = this.siteService.getSiteByDomain("test.com");
         assertNull(site);
     }
 }
