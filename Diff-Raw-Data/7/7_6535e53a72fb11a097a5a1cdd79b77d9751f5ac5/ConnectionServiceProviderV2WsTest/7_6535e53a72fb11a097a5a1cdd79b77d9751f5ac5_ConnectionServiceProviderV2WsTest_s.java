 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.nsi.v2;
 
 import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.empty;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.ogf.schemas.nsi._2013._07.connection.types.LifecycleStateEnumType.CREATED;
 import static org.ogf.schemas.nsi._2013._07.connection.types.LifecycleStateEnumType.TERMINATED;
 import static org.ogf.schemas.nsi._2013._07.connection.types.ProvisionStateEnumType.PROVISIONED;
 import static org.ogf.schemas.nsi._2013._07.connection.types.ProvisionStateEnumType.RELEASED;
 import static org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType.RESERVE_HELD;
 import static org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType.RESERVE_START;
 
 import java.net.URI;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 
 import javax.xml.ws.Holder;
 
 import com.google.common.base.Optional;
 
 import nl.surfnet.bod.domain.ConnectionV2;
 import nl.surfnet.bod.domain.NsiV2RequestDetails;
 import nl.surfnet.bod.domain.oauth.NsiScope;
 import nl.surfnet.bod.nsi.NsiHelper;
 import nl.surfnet.bod.repo.ConnectionV2Repo;
 import nl.surfnet.bod.support.ConnectionV2Factory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.security.Security;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.ogf.schemas.nsi._2013._07.connection.provider.QuerySummarySyncFailed;
 import org.ogf.schemas.nsi._2013._07.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2013._07.connection.types.QuerySummaryResultCriteriaType;
 import org.ogf.schemas.nsi._2013._07.connection.types.QuerySummaryResultType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ReservationRequestCriteriaType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ScheduleType;
 import org.ogf.schemas.nsi._2013._07.framework.headers.CommonHeaderType;
 import org.ogf.schemas.nsi._2013._07.framework.types.TypeValuePairListType;
 import org.ogf.schemas.nsi._2013._07.services.point2point.P2PServiceBaseType;
 import org.ogf.schemas.nsi._2013._07.services.types.DirectionalityType;
 import org.ogf.schemas.nsi._2013._07.services.types.StpType;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConnectionServiceProviderV2WsTest {
 
   private static final String UNAUTHORIZED = "Unauthorized";
   private static final String SERVICE_TYPE = "ServiceType";
 
   @InjectMocks private ConnectionServiceProviderV2Ws subject;
 
   @Mock private Environment bodEnvironmentMock;
   @Mock private ConnectionV2Repo connectionRepoMock;
   @Mock private ConnectionServiceV2 connectionService;
   @Mock private NsiHelper nsiHelper;
 
   private Holder<CommonHeaderType> headerHolder;
 
   @Before
   public void setUp() {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.allOf(NsiScope.class)).create());
     headerHolder = new Holder<>(headers());
 
     when(nsiHelper.getProviderNsaV2()).thenReturn("providerNsa2");
    when(nsiHelper.getUrnStpV2()).thenReturn("urnStpV2");
     when(nsiHelper.generateGlobalReservationId()).thenReturn("globalReservationId");
     when(bodEnvironmentMock.getNsiV2ServiceType()).thenReturn(SERVICE_TYPE);
   }
 
   @After
   public void tearDown() {
     Security.clearUserDetails();
   }
 
   @Test
   public void reserve_should_not_support_labels_in_sourceStp(){
     Holder<String> connectionIdHolder = new Holder<>();
 
     ReservationRequestCriteriaType criteria = initialReservationCriteria();
     P2PServiceBaseType service = ConnectionsV2.findPointToPointService(criteria).get();
     service.getSourceSTP().setLabels(new TypeValuePairListType());
     criteria.getAny().clear();
     ConnectionsV2.addPointToPointService(criteria.getAny(), service);
     try {
       subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
     }
 
   }
 
   @Test
   public void reserve_should_not_support_labels_in_destStp(){
     Holder<String> connectionIdHolder = new Holder<>();
 
     ReservationRequestCriteriaType criteria = initialReservationCriteria();
     P2PServiceBaseType service = ConnectionsV2.findPointToPointService(criteria).get();
     service.getDestSTP().setLabels(new TypeValuePairListType());
     criteria.getAny().clear();
     ConnectionsV2.addPointToPointService(criteria.getAny(), service);
     try {
       subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
     }
   }
 
 
   @Test
   public void should_create_connection_on_initial_reserve() throws Exception {
     Holder<String> connectionIdHolder = new Holder<>();
 
     subject.reserve(connectionIdHolder, "globalReservationId", "description", initialReservationCriteria(), headerHolder);
 
     assertThat(connectionIdHolder.value, is(notNullValue()));
 
     ArgumentCaptor<ConnectionV2> connection = ArgumentCaptor.forClass(ConnectionV2.class);
     ArgumentCaptor<NsiV2RequestDetails> nsiRequestDetails = ArgumentCaptor.forClass(NsiV2RequestDetails.class);
     verify(connectionService).reserve(connection.capture(), nsiRequestDetails.capture(), eq(Security.getUserDetails()));
     assertThat(connection.getValue().getDesiredBandwidth(), is(100L));
     assertThat(connection.getValue().getGlobalReservationId(), is("globalReservationId"));
     assertThat(connection.getValue().getReservationState(), is(nullValue()));
     assertThat(connection.getValue().getReserveVersion(), is(3));
     assertThat(connection.getValue().getCommittedVersion(), isAbsent());
     assertThat(connection.getValue().getDataPlaneActive(), is(false));
     assertThat(connection.getValue().getLifecycleState(), is(CREATED));
     assertThat(connection.getValue().getProvisionState(), isAbsent());
     assertThat(nsiRequestDetails.getValue().getReplyTo(), is(Optional.of(URI.create("replyTo"))));
     assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
   }
 
   @Rule
   public ExpectedException thrown = ExpectedException.none();
 
   @Test
   public void should_require_matching_provider_nsa() throws Exception {
     Holder<String> connectionIdHolder = new Holder<>();
     ReservationRequestCriteriaType criteria = initialReservationCriteria();
     headerHolder.value.setProviderNSA("BadProviderNsa");
 
     thrown.expect(ServiceException.class);
     thrown.expectMessage(allOf(containsString("The attribute 'providerNSA'"), containsString("BadProviderNsa")));
 
     subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
   }
 
   @Test
   public void should_require_service_type_in_initial_reserve() throws Exception {
     Holder<String> connectionIdHolder = new Holder<>();
     ReservationRequestCriteriaType criteria = initialReservationCriteria().withServiceType(null);
 
     thrown.expect(ServiceException.class);
     thrown.expectMessage(is("Missing parameter 'serviceType'"));
 
     subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
   }
 
   @Test
   public void should_require_service_type_to_match_service_definition() throws Exception {
     Holder<String> connectionIdHolder = new Holder<>();
     ReservationRequestCriteriaType criteria = initialReservationCriteria().withServiceType("AnotherServiceType");
 
     thrown.expect(ServiceException.class);
     thrown.expectMessage(allOf(containsString("The attribute 'serviceType'"), containsString("AnotherServiceType")));
 
     subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
   }
 
   @Test
   public void should_reject_a_reserve_with_directionality_unidirectional() throws Exception {
     Holder<String> connectionIdHolder = new Holder<>();
     ReservationRequestCriteriaType criteria = initialReservationCriteria();
     P2PServiceBaseType service = ConnectionsV2.findPointToPointService(criteria).get();
     service.setDirectionality(DirectionalityType.UNIDIRECTIONAL);
     criteria.getAny().clear();
     ConnectionsV2.addPointToPointService(criteria.getAny(), service);
 
     thrown.expect(ServiceException.class);
     thrown.expectMessage(allOf(containsString("Directionality"), containsString("not supported")));
 
     subject.reserve(connectionIdHolder, "globalReservationId", "description", criteria, headerHolder);
   }
 
   @Test
   public void should_require_nsi_reserve_scope_on_reserve() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.reserve(new Holder<String>(), null, null, initialReservationCriteria(), headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_reject_reserve_with_connection_id_until_modify_is_supported() {
     try {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_START).create());
       subject.reserve(new Holder<>("connectionId"), null, null, initialReservationCriteria(), headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getErrorId(), is("103"));
       assertThat(expected.getFaultInfo().getText(), is("Not Implemented"));
     }
   }
 
   @Test
   public void should_only_commit_when_reserve_held() {
     try {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_START).create());
 
       subject.reserveCommit("connectionId", headerHolder);
 
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getErrorId(), is("201"));
     }
   }
 
   @Test
   public void should_commit_when_reserve_held() throws Exception {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_HELD).create());
 
       subject.reserveCommit("connectionId", headerHolder);
 
       ArgumentCaptor<NsiV2RequestDetails> requestDetails = ArgumentCaptor.forClass(NsiV2RequestDetails.class);
       verify(connectionService).asyncReserveCommit(eq("connectionId"), requestDetails.capture());
       assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
       assertThat(requestDetails.getValue().getReplyTo(), is(Optional.of(URI.create("replyTo"))));
   }
 
   @Test
   public void should_require_nsi_reserve_scope_on_reserve_commit() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.reserveCommit("connectionId", headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_only_abort_when_reserve_held() {
     try {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_START).create());
 
       subject.reserveAbort("connectionId", headerHolder);
 
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getErrorId(), is("201"));
     }
   }
 
   @Test
   public void should_abort_when_reserve_held() throws Exception {
     when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_HELD).create());
 
     subject.reserveAbort("connectionId", headerHolder);
 
     ArgumentCaptor<NsiV2RequestDetails> requestDetails = ArgumentCaptor.forClass(NsiV2RequestDetails.class);
     verify(connectionService).asyncReserveAbort(eq("connectionId"), requestDetails.capture(), eq(Security.getUserDetails()));
     assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
     assertThat(requestDetails.getValue().getReplyTo(), is(Optional.of(URI.create("replyTo"))));
   }
 
   @Test
   public void should_require_nsi_reserve_scope_on_reserve_abort() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.reserveAbort("connectionId", headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_only_provision_when_released() {
     try {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setProvisionState(PROVISIONED).create());
 
       subject.provision("connectionId", headerHolder);
 
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getErrorId(), is("201"));
     }
   }
 
   @Test
   public void should_not_provision_when_psm_does_not_exists() {
     try {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_START).setProvisionState(null).create());
 
       subject.provision("connectionId", headerHolder);
 
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getErrorId(), is("201"));
     }
   }
 
   @Test
   public void should_provision_when_released() throws Exception {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setProvisionState(RELEASED).create());
 
       subject.provision("connectionId", headerHolder);
 
       ArgumentCaptor<NsiV2RequestDetails> requestDetails = ArgumentCaptor.forClass(NsiV2RequestDetails.class);
       verify(connectionService).asyncProvision(eq("connectionId"), requestDetails.capture());
       assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
       assertThat(requestDetails.getValue().getReplyTo(), is(Optional.of(URI.create("replyTo"))));
   }
 
   @Test
   public void should_require_nsi_provision_scope_on_provision() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.provision("connectionId", headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_respond_illegal_statechange_when_already_terminated_when_sending_a_terminate() throws Exception {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(RESERVE_START).setLifecycleState(TERMINATED).create());
 
       try {
         subject.terminate("connectionId", headerHolder);
         fail("ServiceException expected");
       } catch (ServiceException expected) {
         assertThat(expected.getFaultInfo().getErrorId(), is("201"));
       }
   }
 
   @Test
   public void should_terminate() throws Exception {
       when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setLifecycleState(CREATED).create());
 
       subject.terminate("connectionId", headerHolder);
 
       ArgumentCaptor<NsiV2RequestDetails> requestDetails = ArgumentCaptor.forClass(NsiV2RequestDetails.class);
       verify(connectionService).asyncTerminate(eq("connectionId"), requestDetails.capture(), eq(Security.getUserDetails()));
       assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
       assertThat(requestDetails.getValue().getReplyTo(), is(Optional.of(URI.create("replyTo"))));
   }
 
   @Test
   public void should_require_nsi_terminate_scope_on_terminate() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.terminate("connectionId", headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_require_nsi_query_scope_on_query_summary() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.querySummary(Collections.<String>emptyList(), null, headerHolder);
       fail("ServiceException expected");
     } catch (ServiceException expected) {
       assertThat(expected.getFaultInfo().getText(), is(UNAUTHORIZED));
     }
   }
 
   @Test
   public void should_return_query_information_synchronously() throws Exception {
     ConnectionV2 connection = new ConnectionV2Factory().setRequesterNsa("requesterNSA").create();
     when(connectionService.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNSA")).thenReturn(Collections.singletonList(connection));
 
     List<QuerySummaryResultType> results = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), headerHolder);
 
     assertThat(results, hasSize(1));
     QuerySummaryResultType result = results.get(0);
     assertThat(result.getConnectionId(), is(connection.getConnectionId()));
     assertThat(result.getRequesterNSA(), is("requesterNSA"));
   }
 
   @Test
   public void query_should_not_include_criteria_when_connection_has_not_been_committed() throws Exception {
     ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_START).create();
     when(connectionService.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNSA")).thenReturn(Collections.singletonList(connection));
 
     List<QuerySummaryResultType> results = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), headerHolder);
 
     assertThat(results, hasSize(1));
     QuerySummaryResultType result = results.get(0);
     assertThat(result.getCriteria(), empty());
   }
 
   @Test
   public void query_should_include_criteria_for_committed_connection() throws Exception {
     ConnectionV2 connection = new ConnectionV2Factory().committed(3).setDesiredBandwidth(100).create();
     when(connectionService.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNSA")).thenReturn(Collections.singletonList(connection));
 
     List<QuerySummaryResultType> results = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), headerHolder);
 
     assertThat(results, hasSize(1));
     QuerySummaryResultType result = results.get(0);
     assertThat(result.getCriteria(), hasSize(1));
     QuerySummaryResultCriteriaType criteria = result.getCriteria().get(0);
     assertThat(criteria.getVersion(), is(3));
     assertThat(ConnectionsV2.findPointToPointService(criteria.getAny()).get().getCapacity(), is(100L));
   }
 
   @Test
   public void should_require_nsi_query_scope_on_query_summary_sync() throws Exception {
     Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());
 
     try {
       subject.querySummarySync(Collections.<String>emptyList(), null, headerHolder);
       fail("QuerySummarySyncFailed expected");
     } catch (QuerySummarySyncFailed expected) {
       assertThat(expected.getFaultInfo().getServiceException().getText(), is(UNAUTHORIZED));
     }
   }
 
 
   private CommonHeaderType headers() {
     return new CommonHeaderType().withCorrelationId("correlationId").withProviderNSA("providerNsa2").withRequesterNSA("requesterNSA").withReplyTo("replyTo");
   }
 
   private ReservationRequestCriteriaType initialReservationCriteria() {
     ReservationRequestCriteriaType result = new ReservationRequestCriteriaType()
         .withSchedule(new ScheduleType())
         .withServiceType(SERVICE_TYPE)
         .withVersion(3);
     ConnectionsV2.addPointToPointService(result.getAny(), new P2PServiceBaseType()
         .withCapacity(100)
        .withSourceSTP(new StpType().withNetworkId("urnStpV2").withLocalId("source"))
        .withDestSTP(new StpType().withNetworkId("urnStpV2").withLocalId("dest")));
 
     return result;
   }
 }
