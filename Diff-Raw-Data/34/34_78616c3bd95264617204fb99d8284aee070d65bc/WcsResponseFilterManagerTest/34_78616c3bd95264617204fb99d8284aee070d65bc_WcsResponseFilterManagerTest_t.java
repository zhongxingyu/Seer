 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2011 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.securityproxy.responsefilter.wcs;
 
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCAPABILITIES;
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCOVERAGE;
 import static org.deegree.securityproxy.commons.WcsServiceVersion.VERSION_110;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import javax.servlet.ServletOutputStream;
 
 import org.deegree.securityproxy.authentication.WcsUser;
import org.deegree.securityproxy.filter.StatusCodeResponseBodyWrapper;
 import org.deegree.securityproxy.request.OwsRequest;
 import org.deegree.securityproxy.request.WcsRequest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath*:org/deegree/securityproxy/responsefilter/wcs/WcsResponseFilterManagerTestContext.xml" })
 public class WcsResponseFilterManagerTest {
 
     private static final String COVERAGE_NAME = "coverageName";
 
     private WcsResponseFilterManager wcsResponseFilterManager = new WcsResponseFilterManager();
 
     @Test
     public void testSupportsShouldSupportWcsRequests()
                             throws Exception {
         boolean isSupported = wcsResponseFilterManager.supports( WcsRequest.class );
         assertThat( isSupported, is( true ) );
     }
 
     @Test
     public void testSupportsShouldNotSupportOwsRequests()
                             throws Exception {
         boolean isSupported = wcsResponseFilterManager.supports( OwsRequest.class );
         assertThat( isSupported, is( false ) );
     }
 
     @Test
     public void testSupportsShouldNotSupportNull()
                             throws Exception {
         boolean isSupported = wcsResponseFilterManager.supports( null );
         assertThat( isSupported, is( false ) );
     }
 
     /*
      * #filterResponse()
      */
 
     @Test
     public void testFilterResponseWithWcsCapabilitiesResponseShouldDoNothing()
                             throws Exception {
        StatusCodeResponseBodyWrapper mockedServletResponse = mockResponseWrapper();
         Authentication mockAuthentication = mockAuthentication();
         wcsResponseFilterManager.filterResponse( mockedServletResponse, createWcsGetCapabilitiesRequest(),
                                                  mockAuthentication );
         verifyZeroInteractions( mockedServletResponse );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithNullResponseShouldFail()
                             throws Exception {
         wcsResponseFilterManager.filterResponse( null, createWcsGetCoverageRequest(), mockAuthentication() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithNullRequestShouldFail()
                             throws Exception {
        wcsResponseFilterManager.filterResponse( mockResponseWrapper(), null, mockAuthentication() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithUnsupportedRequestShouldFail()
                             throws Exception {
        wcsResponseFilterManager.filterResponse( mockResponseWrapper(), mockOwsRequest(), mockAuthentication() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithAuthenticationWithoutWcsUserShouldFail()
                             throws Exception {
        wcsResponseFilterManager.filterResponse( mockResponseWrapper(), createWcsGetCoverageRequest(),
                                                  mockAuthenticationWithoutWcsUserPrincipal() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithWcsRequestWithoutCoverageShouldFail()
                             throws Exception {
        wcsResponseFilterManager.filterResponse( mockResponseWrapper(),
                                                  createWcsGetCoverageRequestWithoutCoverageName(), mockAuthentication() );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testFilterResponseWithWcsRequestWithNullCoverageShouldFail()
                             throws Exception {
        wcsResponseFilterManager.filterResponse( mockResponseWrapper(),
                                                  createWcsGetCoverageRequestWithNullCoverageName(),
                                                  mockAuthentication() );
     }
 
     private OwsRequest mockOwsRequest() {
         return mock( OwsRequest.class );
     }
 
     private WcsRequest createWcsGetCoverageRequest() {
         return new WcsRequest( GETCOVERAGE, VERSION_110, Collections.singletonList( COVERAGE_NAME ) );
     }
 
     private WcsRequest createWcsGetCoverageRequestWithoutCoverageName() {
         return new WcsRequest( GETCOVERAGE, VERSION_110, Collections.<String> emptyList() );
     }
 
     private WcsRequest createWcsGetCoverageRequestWithNullCoverageName() {
         return new WcsRequest( GETCOVERAGE, VERSION_110, Collections.<String> singletonList( null ) );
     }
 
     private WcsRequest createWcsGetCapabilitiesRequest() {
         return new WcsRequest( GETCAPABILITIES, VERSION_110 );
     }
 
    private StatusCodeResponseBodyWrapper mockResponseWrapper()
                             throws IOException {
        StatusCodeResponseBodyWrapper mockedServletResponse = mock( StatusCodeResponseBodyWrapper.class );
         when( mockedServletResponse.getOutputStream() ).thenReturn( mock( ServletOutputStream.class ) );
         return mockedServletResponse;
     }
 
     private Authentication mockAuthentication() {
         Authentication mockedAuthentication = mock( Authentication.class );
         when( mockedAuthentication.getPrincipal() ).thenReturn( mock( WcsUser.class ) );
         return mockedAuthentication;
     }
 
     private Authentication mockAuthenticationWithoutWcsUserPrincipal() {
         Authentication mockedAuthentication = mock( Authentication.class );
         when( mockedAuthentication.getPrincipal() ).thenReturn( mock( UserDetails.class ) );
         return mockedAuthentication;
     }
 
 }
