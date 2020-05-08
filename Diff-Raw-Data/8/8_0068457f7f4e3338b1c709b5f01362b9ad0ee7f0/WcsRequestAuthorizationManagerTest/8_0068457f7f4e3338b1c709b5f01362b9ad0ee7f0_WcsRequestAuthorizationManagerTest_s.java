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
 package org.deegree.securityproxy.authorization.wcs;
 
 import static org.deegree.securityproxy.commons.WcsOperationType.DESCRIBECOVERAGE;
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCAPABILITIES;
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCOVERAGE;
 import static org.deegree.securityproxy.commons.WcsServiceVersion.VERSION_100;
import static org.deegree.securityproxy.commons.WcsServiceVersion.VERSION_130;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.deegree.securityproxy.authentication.wcs.WcsPermission;
 import org.deegree.securityproxy.commons.WcsOperationType;
 import org.deegree.securityproxy.commons.WcsServiceVersion;
 import org.deegree.securityproxy.request.WcsRequest;
 import org.junit.Test;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.core.Authentication;
 
 /**
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 public class WcsRequestAuthorizationManagerTest {
 
     private static final WcsServiceVersion VERSION = VERSION_100;
 
     private static final WcsOperationType OPERATION_TYPE = GETCOVERAGE;
 
     private static final String SERVICE_NAME = "serviceName";
 
     private static final String LAYER_NAME = "layerName";
 
     private WcsRequestAuthorizationManager authorizationManager = new WcsRequestAuthorizationManager();
 
     @Test
     public void testSupportsWcsRequestShouldBeSupoorted()
                             throws Exception {
         boolean isSupported = authorizationManager.supports( WcsRequest.class );
         assertThat( isSupported, is( true ) );
     }
 
     @Test
     public void testSupportsHttpServletRequestShouldBeUnsupported()
                             throws Exception {
         boolean isSupported = authorizationManager.supports( HttpServletRequest.class );
         assertThat( isSupported, is( false ) );
     }
 
     @Test
     public void testDecideWithSingleAuthorizations()
                             throws Exception {
         Authentication authentication = mockDefaultAuthentication();
         WcsRequest request = mockDefaultRequest();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test
     public void testDecideWithMultipleAuthorizations()
                             throws Exception {
         Authentication authentication = mockDefaultAuthenticationWithMultiplePermissions();
         WcsRequest request = mockGetCapabilitiesRequest();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testDecideMultipleAuthorizationsShouldBeRefusedCauseOfVersion()
                             throws Exception {
         Authentication authentication = mockDefaultAuthenticationWithMultiplePermissions();
         WcsRequest request = mockGetCapabilitiesRequestWithUnsupportedVersion();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testDecideSingleAuthorizationShouldBeRefusedCauseOfVersion()
                             throws Exception {
         Authentication authentication = mockDefaultAuthentication();
         WcsRequest request = mockRequestWithUnsupportedVersion();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testDecideSingleAuthorizationShouldBeRefusedCauseOfServiceName()
                             throws Exception {
         Authentication authentication = mockDefaultAuthentication();
         WcsRequest request = mockRequestWithUnsupportedServiceName();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testDecideSingleAuthorizationShouldBeRefusedCauseOfOperationType()
                             throws Exception {
         Authentication authentication = mockDefaultAuthentication();
         WcsRequest request = mockRequestWithUnsupportedOperationType();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testDecideSingleAuthorizationShouldBeRefusedCauseOfLayerName()
                             throws Exception {
         Authentication authentication = mockDefaultAuthentication();
         WcsRequest request = mockRequestWithUnsupportedLayerName();
         authorizationManager.decide( authentication, request, Collections.<ConfigAttribute> emptyList() );
     }
 
     private WcsRequest mockDefaultRequest() {
         return mockRequest( LAYER_NAME, OPERATION_TYPE, SERVICE_NAME, VERSION );
     }
 
     private WcsRequest mockGetCapabilitiesRequest() {
         return mockRequest( null, GETCAPABILITIES, SERVICE_NAME, VERSION );
     }
 
     private WcsRequest mockGetCapabilitiesRequestWithUnsupportedVersion() {
        return mockRequest( null, GETCAPABILITIES, SERVICE_NAME, VERSION_130 );
     }
 
     private WcsRequest mockRequestWithUnsupportedVersion() {
        return mockRequest( LAYER_NAME, OPERATION_TYPE, SERVICE_NAME, VERSION_130 );
     }
 
     private WcsRequest mockRequestWithUnsupportedServiceName() {
         return mockRequest( LAYER_NAME, OPERATION_TYPE, "unknown", VERSION );
     }
 
     private WcsRequest mockRequestWithUnsupportedOperationType() {
         return mockRequest( LAYER_NAME, DESCRIBECOVERAGE, SERVICE_NAME, VERSION );
     }
 
     private WcsRequest mockRequestWithUnsupportedLayerName() {
         return mockRequest( "unknown", OPERATION_TYPE, SERVICE_NAME, VERSION );
     }
 
     private WcsRequest mockRequest( String layerName, WcsOperationType operationType, String serviceName,
                                     WcsServiceVersion version ) {
         WcsRequest mock = mock( WcsRequest.class );
         when( mock.getLayerName() ).thenReturn( layerName );
         when( mock.getOperationType() ).thenReturn( operationType );
         when( mock.getServiceName() ).thenReturn( serviceName );
         when( mock.getServiceVersion() ).thenReturn( version );
         return mock;
     }
 
     private Authentication mockDefaultAuthentication() {
         Authentication authentication = mock( Authentication.class );
         Collection<WcsPermission> authorities = new ArrayList<WcsPermission>();
         authorities.add( new WcsPermission( OPERATION_TYPE, VERSION, LAYER_NAME, SERVICE_NAME ) );
         doReturn( authorities ).when( authentication ).getAuthorities();
         return authentication;
     }
 
     private Authentication mockDefaultAuthenticationWithMultiplePermissions() {
         Authentication authentication = mock( Authentication.class );
         Collection<WcsPermission> authorities = new ArrayList<WcsPermission>();
         authorities.add( new WcsPermission( OPERATION_TYPE, VERSION, LAYER_NAME, SERVICE_NAME ) );
         authorities.add( new WcsPermission( GETCAPABILITIES, VERSION, null, SERVICE_NAME ) );
         doReturn( authorities ).when( authentication ).getAuthorities();
         return authentication;
     }
 }
