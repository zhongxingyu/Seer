 package org.karmaexchange.bootstrap;
 
 import static com.google.common.base.Preconditions.checkState;
 import static java.util.Arrays.asList;
 import static org.karmaexchange.bootstrap.TestResourcesBootstrapTask.TestUser.*;
 import static org.karmaexchange.bootstrap.TestResourcesBootstrapTask.TestOrganization.*;
 import static org.karmaexchange.util.OfyService.ofy;
 
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.karmaexchange.dao.Address;
 import org.karmaexchange.dao.AlbumRef;
 import org.karmaexchange.dao.Badge;
 import org.karmaexchange.dao.BadgeSummary;
 import org.karmaexchange.dao.BaseDao;
 import org.karmaexchange.dao.CauseType;
 import org.karmaexchange.dao.Event;
 import org.karmaexchange.dao.Event.EventParticipant;
 import org.karmaexchange.dao.Event.ParticipantType;
 import org.karmaexchange.dao.Event.UpsertParticipantTxn;
 import org.karmaexchange.dao.Organization.AutoMembershipRule;
 import org.karmaexchange.dao.OrganizationNamedKeyWrapper;
 import org.karmaexchange.dao.User.KarmaGoal;
 import org.karmaexchange.dao.User.RegisteredEmail;
 import org.karmaexchange.dao.GeoPtWrapper;
 import org.karmaexchange.dao.ImageProviderType;
 import org.karmaexchange.dao.KeyWrapper;
 import org.karmaexchange.dao.Location;
 import org.karmaexchange.dao.OAuthCredential;
 import org.karmaexchange.dao.Organization;
 import org.karmaexchange.dao.PageRef;
 import org.karmaexchange.dao.Rating;
 import org.karmaexchange.dao.Review;
 import org.karmaexchange.dao.SuitableForType;
 import org.karmaexchange.dao.User;
 import org.karmaexchange.dao.UserUsage;
 import org.karmaexchange.dao.Waiver;
 import org.karmaexchange.provider.FacebookSocialNetworkProvider;
 import org.karmaexchange.provider.SocialNetworkProvider.SocialNetworkProviderType;
 import org.karmaexchange.resources.msg.ErrorResponseMsg;
 import org.karmaexchange.resources.msg.ErrorResponseMsg.ErrorInfo;
 import org.karmaexchange.task.ComputeLeaderboardServlet;
 import org.karmaexchange.util.AdminUtil;
 import org.karmaexchange.util.ServletUtil;
 import org.karmaexchange.util.AdminUtil.AdminSubtask;
 
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.VoidWork;
 
 public class TestResourcesBootstrapTask extends BootstrapTask {
 
   private static int eventNum = 0;
 
   private final String baseUrl;
   private final ServletContext servletCtx;
 
   public enum TestUser {
     USER1("100006074376957", "Katie", "Hilary", null,
       Long.valueOf(6 * 60), false),
     USER2("100006058506752", "Alan", "Franjo", "alan.franjo@KidsClub.org"),
     USER3("100006051787601", "Kenneth", "Gilbert", "kenneth.gilbert@kidsclub.org"),
     USER4("100006076592978", "Eddie", "Kenelm"),
     USER5("100006052237443", "Will", "Lorne"),
     USER6("100006093303024", "Courtney", "Arden"),
     USER7("100006045731576", "Leslie", "Lavern"),
     USER8("100006080162988", "Darryl", "Teo"),
     USER9("100006054577389", "Johnny", "Lev", "lev@fakekidsclub.org"),
     USER10("100006053377578", "Cassidy", "Sadie"),
     USER11("100006084302920", "Michelle", "Dailey", "michelle.daileyl@kidsclub.org"),
     USER12("100006069696646", "Erica", "Wade"),
     USER13("100006083973038", "Todd", "Angelos"),
     AMIR("1111368160", "Amir", "Valiani", null,
       Long.valueOf(8 * 60), true),
     HARISH("537854733", "Harish", "Balijepalli", null,
       Long.valueOf(4 * 60), true),
     POONUM("3205292", "Poonum", "Kaberwal", null,
       Long.valueOf(4 * 60), true);
 
     private final String fbId;
     @Getter
     private final String firstName;
     @Getter
     private final String lastName;
     @Getter
     @Nullable
     private final String email;
     @Getter
     private final Long monthlyKarmaPtsGoal;
     @Getter
     private final boolean saveUsageInfo;
 
     private TestUser(String fbId, String firstName, String lastName) {
       this(fbId, firstName, lastName, null);
     }
 
     private TestUser(String fbId, String firstName, String lastName, @Nullable String email) {
       this(fbId, firstName, lastName, email, null, false);
     }
 
     private TestUser(String fbId, String firstName, String lastName, @Nullable String email,
         Long monthlyKarmaPtsGoal, boolean saveUsageInfo) {
       this.fbId = fbId;
       this.firstName = firstName;
       this.lastName = lastName;
       this.email = email;
       this.monthlyKarmaPtsGoal = monthlyKarmaPtsGoal;
       this.saveUsageInfo = saveUsageInfo;
     }
 
     public Key<User> getKey() {
       return User.getKey(createOAuthCredential());
     }
 
     public User createUser() {
       User user = User.create(createOAuthCredential());
       user.setFirstName(firstName);
       user.setLastName(lastName);
       if ((this != AMIR) && (this != HARISH) && (this != POONUM) &&
           ((Long.valueOf(fbId) % 2) != 0)) {
         user.setAbout("I'm looking forward to making a difference!");
       }
       if (email != null) {
         user.getRegisteredEmails().add(new RegisteredEmail(email, true));
       }
       if (monthlyKarmaPtsGoal != null) {
         KarmaGoal goal = new KarmaGoal();
         user.setKarmaGoal(goal);
         goal.setMonthlyGoal(monthlyKarmaPtsGoal);
       }
       return user;
     }
 
     private OAuthCredential createOAuthCredential() {
       return OAuthCredential.create(getSocialNetworkProviderType().getOAuthProviderName(), fbId,
         "invalid_token");
     }
 
     public SocialNetworkProviderType getSocialNetworkProviderType() {
       return SocialNetworkProviderType.FACEBOOK;
     }
   }
 
   public enum TestOrganization {
     BGCSF("BGCSF",
       "https://bgcsf.thankyou4caring.org/page.aspx?pid=298",
       AMIR,
       asList(
         TestOrgMembership.of(USER7, Organization.Role.MEMBER, Organization.Role.ORGANIZER),
         TestOrgMembership.of(USER6, Organization.Role.MEMBER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER5, Organization.Role.ORGANIZER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER1, Organization.Role.MEMBER, null),
         TestOrgMembership.of(USER9, null, Organization.Role.MEMBER), // fake email, not granted
         TestOrgMembership.of(USER10, null, Organization.Role.ADMIN),
         TestOrgMembership.of(USER2, null, Organization.Role.MEMBER),  // auto-grant
         TestOrgMembership.of(USER11, null, Organization.Role.ORGANIZER),  // auto-grant
         TestOrgMembership.of(USER3, null, Organization.Role.ADMIN)), // not-auto-granted
       null,
       asList(new AutoMembershipRule("kidsclub.org", Organization.Role.ORGANIZER)),
       asList(createWaiver("Soccer clinic waiver",
                 "If you are injured while volunteering BGCSF is not liable.\n\n" +
                 "If your property is stolen while volunteering that is your responsibility."),
              createWaiver("After school tutoring waiver",
                 "You are responsible for any property you bring."))),
 
     BGCSF_COLUMBIA_PARK("columbia.park",
       "https://bgcsf.thankyou4caring.org/page.aspx?pid=298",
       AMIR,
       asList(
         TestOrgMembership.of(USER1, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(USER8, null, Organization.Role.ORGANIZER),
         TestOrgMembership.of(HARISH, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(POONUM, Organization.Role.ORGANIZER, null)),
       BGCSF,
       ImmutableList.<AutoMembershipRule>of(),
       asList(createWaiver("Soccer clinic waiver",
         "If you are injured while volunteering Columbia Park BGCSF is not liable.\n\n" +
         "If your property is stolen while volunteering that is your responsibility."))),
 
     BGCSF_TENDERLOIN("Tenderloin.clubhouse",
       "https://bgcsf.thankyou4caring.org/page.aspx?pid=298",
       HARISH,
       asList(
         TestOrgMembership.of(POONUM, Organization.Role.ADMIN, null),
         TestOrgMembership.of(USER1, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(USER2, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(USER8, null, Organization.Role.ORGANIZER)),
       BGCSF),
 
     BENEVOLENT("benevolent.net",
       "https://www.benevolent.net/about/donate.html",
       HARISH,
       asList(
         TestOrgMembership.of(POONUM, Organization.Role.ADMIN, null),
         TestOrgMembership.of(USER7, Organization.Role.MEMBER, Organization.Role.ORGANIZER),
         TestOrgMembership.of(USER6, Organization.Role.MEMBER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER5, Organization.Role.ORGANIZER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER1, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(USER2, null, Organization.Role.MEMBER),
         TestOrgMembership.of(USER3, null, Organization.Role.ADMIN))),
 
     UNITED_WAY("UnitedWay",
       "https://give.liveunited.org/page/contribute/support-us",
       AMIR,
       asList(
         TestOrgMembership.of(USER7, Organization.Role.MEMBER, Organization.Role.ORGANIZER),
         TestOrgMembership.of(USER6, Organization.Role.MEMBER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER5, Organization.Role.ORGANIZER, Organization.Role.ADMIN),
         TestOrgMembership.of(USER1, Organization.Role.ORGANIZER, null),
         TestOrgMembership.of(USER2, null, Organization.Role.MEMBER),
         TestOrgMembership.of(USER3, null, Organization.Role.ADMIN)));
 
     @Getter
     private final String pageName;
     @Getter
     private final String donationUrl;
     @Getter
     private final TestUser initialAdmin;
     @Getter
     private final List<TestOrgMembership> memberships;
     @Getter
     private final TestOrganization parentOrg;
     @Getter
     private final List<AutoMembershipRule> autoMembershipRules;
     @Getter
     private final List<Waiver> waivers;
 
 
     private TestOrganization(String pageName, String donationUrl, TestUser initialAdmin,
         List<TestOrgMembership> memberships) {
       this(pageName, donationUrl, initialAdmin, memberships, null);
     }
 
     private TestOrganization(String pageName,  String donationUrl, TestUser initialAdmin,
         List<TestOrgMembership> memberships, @Nullable TestOrganization parentOrg) {
       this(pageName, donationUrl, initialAdmin, memberships, parentOrg,
         ImmutableList.<AutoMembershipRule>of(), ImmutableList.<Waiver>of());
     }
 
     private TestOrganization(String pageName, String donationUrl, TestUser initialAdmin,
         List<TestOrgMembership> memberships, @Nullable TestOrganization parentOrg,
         @Nullable List<AutoMembershipRule> autoMembershipRules,
         @Nullable List<Waiver> waivers) {
       this.pageName = pageName;
       this.donationUrl = donationUrl;
       this.initialAdmin = initialAdmin;
       this.memberships = memberships;
       this.parentOrg = parentOrg;
       this.autoMembershipRules = autoMembershipRules;
       this.waivers = waivers;
     }
 
     public PageRef getPageRef() {
       return PageRef.create(pageName, FacebookSocialNetworkProvider.PAGE_BASE_URL + pageName,
         SocialNetworkProviderType.FACEBOOK);
     }
 
     public Key<Organization> getKey() {
       return Key.create(Organization.class, Organization.getNameFromPageName(pageName));
     }
 
     private static Waiver createWaiver(String description, String content) {
       Waiver waiver = new Waiver();
       waiver.setDescription(description);
       waiver.setEmbeddedContent(content);
       return waiver;
     }
   }
 
   @Data(staticConstructor="of")
   private static class TestOrgMembership {
     private final TestUser user;
     private final Organization.Role grantedRole;
     private final Organization.Role requestedRole;
   }
 
   public TestResourcesBootstrapTask(HttpServletRequest req, ServletContext servletCtx,
       PrintWriter statusWriter) {
     super(statusWriter);
     this.servletCtx = servletCtx;
     baseUrl = ServletUtil.getBaseUri(req);
   }
 
   @Override
   protected void performTask() {
     statusWriter.println("About to persist test users...");
     for (TestUser testUser : TestUser.values()) {
       User user = ofy().load().key(testUser.getKey()).now();
       if (user != null) {
         throw new RuntimeException("DB has not been purged");
       }
       BaseDao.upsert(testUser.createUser());
       User.updateProfileImage(testUser.getKey(), testUser.getSocialNetworkProviderType());
       if (!testUser.saveUsageInfo) {
         UserUsage.deleteHistory(testUser.getKey());
       }
     }
     statusWriter.println("About to persist organizations...");
     persistOrganizations();
     statusWriter.println("About to persist test events...");
     Date now = new Date();
     CreateEventsResult createEventsResult = createEvents();
     for (Event event : createEventsResult.getEvents()) {
       BaseDao.upsert(event);
       // Process any completed events.
       if (event.getEndTime().before(now)) {
         Event.processEventCompletionTasks(Key.create(event));
       }
     }
     for (EventNoShowInfo noShowInfo : createEventsResult.eventNoShowInfo) {
       noShowInfo.persist();
       checkState(noShowInfo.event.getEndTime().before(now));
       Event.processEventCompletionTasks(Key.create(noShowInfo.event));
     }
     for (PendingReview pendingReview : createEventsResult.getPendingReviews()) {
       pendingReview.persistReview();
     }
 
     statusWriter.println("About to award badges...");
     awardBadges(createEventsResult.getEvents());
 
//    statusWriter.println("About to update organization leaderboards...");
//    String leaderboardJobId = ComputeLeaderboardServlet.startComputeLeaderboardMapReduce();
//    statusWriter.println("Leaderboard update initiated. View status at: " +
//        ComputeLeaderboardServlet.getMapReduceStatusUrl(baseUrl, leaderboardJobId));
 
     statusWriter.println("Test resources persisted.");
   }
 
   private static CreateEventsResult createEvents() {
     List<Event> events = Lists.newArrayList();
     Date now = new Date();
 
     Key<Waiver> bgcsfSoccerWaiverKey = Key.create(BGCSF.waivers.get(0));
     Key<Waiver> bgcsfTutoringWaiverKey = Key.create(BGCSF.waivers.get(1));
     Key<Waiver> columbiaParkSoccerWaiverKey = Key.create(BGCSF_COLUMBIA_PARK.waivers.get(0));
 
     Location ferryBuilding = new Location(
       "Ferry building",
       "The San Francisco Ferry Building is a terminal for ferries that " +
         "travel across the San Francisco Bay, a marketplace, and also has offices",
       new Address(
         "1 Sausalito - San Francisco Ferry Bldg",
         "San Francisco",
         "CA",
         "USA",
         "94111",
         GeoPtWrapper.create(new GeoPt(37.7955f, -122.3937f))));
     Location soccerField = new Location(
       "Youngblood-Coleman Playground",
       null,
       new Address(
         "1398 Hudson Avenue",
         "San Francisco",
         "CA",
         "USA",
         "94124",
         GeoPtWrapper.create(new GeoPt(37.738905f,-122.384654f))));
     Location bgcsfClubhouse = new Location(
       "BGCSF Clubhouse",
       null,
       new Address(
         "450 Guerrero St",
         "San Francisco",
         "CA",
         "USA",
         "94110",
         GeoPtWrapper.create(new GeoPt(37.763762f,-122.424233f))));
     Location unitedWayParkmoorOffice = new Location(
       "United Way Silicon Valley Office",
       null,
       new Address(
         "1400 Parkmoor Avenue #250",
         "San Jose",
         "CA",
         "USA",
         "95126",
         GeoPtWrapper.create(new GeoPt(37.316094f,-121.912575f))));
     Location benevolentParkmoorOffice = new Location(
       "Benevolent San Jose Office",
       null,
       new Address(
         "1400 Parkmoor Avenue",
         "San Jose",
         "CA",
         "USA",
         "95126",
         GeoPtWrapper.create(new GeoPt(37.316094f,-121.912575f))));
 
     List<Key<User>> organizers = asList(USER1.getKey(), AMIR.getKey(), HARISH.getKey(),
       POONUM.getKey());
     List<Key<User>> registeredUsers = asList(USER2.getKey(), USER4.getKey(), USER5.getKey(),
       USER6.getKey(), USER7.getKey(), USER8.getKey(), USER9.getKey(), USER10.getKey(),
       USER11.getKey(), USER12.getKey(), USER13.getKey());
     List<Key<User>> waitListedUsers = asList();
     Event event = createEvent("Youth Soccer Clinic", BGCSF_COLUMBIA_PARK, soccerField,
       computeEventDate(now, 2, 4), 1, organizers, registeredUsers,
       waitListedUsers, 100, "502904489789649", columbiaParkSoccerWaiverKey);
     event.setSuitableForTypes(Lists.newArrayList(EnumSet.allOf(SuitableForType.class)));
     events.add(event);
 
     organizers = asList(USER2.getKey());
     registeredUsers = asList(USER1.getKey(), USER4.getKey());
     waitListedUsers = asList(USER3.getKey(), USER5.getKey());
     event = createEvent("After School Tutoring", BGCSF_TENDERLOIN, bgcsfClubhouse,
       computeEventDate(now, 5, 3), 3, organizers, registeredUsers,
       waitListedUsers, 2, "502905379789560", bgcsfTutoringWaiverKey);
     event.setSuitableForTypes(Lists.newArrayList(SuitableForType.AGE_55_PLUS));
     events.add(event);
 
     organizers = asList(AMIR.getKey());
     registeredUsers = asList();
     waitListedUsers = asList();
     event = createEvent("Credit Coaching", UNITED_WAY, unitedWayParkmoorOffice,
       computeEventDate(now, 12, 7), 1, organizers, registeredUsers,
       waitListedUsers, 5, null);
     event.setSuitableForTypes(Lists.newArrayList(SuitableForType.GROUPS));
     events.add(event);
 
     organizers = asList(HARISH.getKey(), POONUM.getKey());
     registeredUsers = asList();
     waitListedUsers = asList();
     event = createEvent("Resume Workshop", BENEVOLENT, benevolentParkmoorOffice,
       computeEventDate(now, 12, 7), 1, organizers, registeredUsers,
       waitListedUsers, 5, null);
     event.setSuitableForTypes(Lists.newArrayList(SuitableForType.GROUPS));
     events.add(event);
 
     Date extraEventStartDate = computeEventDate(now, 20, 0);
     final int numDaysToAddPerIteration = 15;
     for (int extraEventCnt = 0; extraEventCnt < 8; extraEventCnt += 2) {
 
       organizers = asList(USER5.getKey());
       registeredUsers = asList(AMIR.getKey());
       waitListedUsers = asList();
       event = createEvent("Credit Coaching", UNITED_WAY, unitedWayParkmoorOffice,
         computeEventDate(extraEventStartDate, 0, 5), 1, organizers, registeredUsers,
         waitListedUsers, 5, null);
       events.add(event);
 
       if ((extraEventCnt/2) % 2 == 0) {
         organizers = asList(USER5.getKey());
         registeredUsers = asList(HARISH.getKey(), POONUM.getKey());
         waitListedUsers = asList();
         event = createEvent("Resume Workshop", BENEVOLENT, benevolentParkmoorOffice,
           computeEventDate(extraEventStartDate, 5, 7), 2, organizers, registeredUsers,
           waitListedUsers, 5, null);
         events.add(event);
       } else {
         organizers = asList(USER1.getKey());
         registeredUsers = asList(USER2.getKey(), USER4.getKey(), USER5.getKey(),
           USER6.getKey(), USER7.getKey(), USER8.getKey(), USER9.getKey(), USER10.getKey(),
           USER11.getKey(), USER12.getKey(), USER13.getKey(),
           HARISH.getKey(), POONUM.getKey());
         waitListedUsers = asList();
         event = createEvent("Youth Soccer Clinic", BGCSF_COLUMBIA_PARK, soccerField,
           computeEventDate(extraEventStartDate, 5, 4), 1, organizers, registeredUsers,
           waitListedUsers, 100, null, columbiaParkSoccerWaiverKey);
         event.setSuitableForTypes(Lists.newArrayList(EnumSet.allOf(SuitableForType.class)));
         events.add(event);
       }
 
       extraEventStartDate = computeEventDate(extraEventStartDate, numDaysToAddPerIteration, 0);
     }
 
     // Past events.
     List<PendingReview> pendingReviews = Lists.newArrayList();
     List<EventNoShowInfo> eventNoShowInfo = Lists.newArrayList();
 
     organizers = asList(USER1.getKey(), HARISH.getKey(), POONUM.getKey());
     registeredUsers = asList(USER2.getKey(), USER4.getKey(), USER5.getKey(),
       USER6.getKey(), USER7.getKey(), USER8.getKey(), USER9.getKey(), USER10.getKey(),
       USER11.getKey(), USER12.getKey(), AMIR.getKey());
     waitListedUsers = asList(USER3.getKey());
     event = createEvent("Youth Soccer Clinic", BGCSF_TENDERLOIN, soccerField,
       computeEventDate(now, -2, 4), 1,
       organizers, registeredUsers, waitListedUsers, registeredUsers.size(), "502904833122948",
       bgcsfSoccerWaiverKey);
     event.setSuitableForTypes(Lists.newArrayList(EnumSet.allOf(SuitableForType.class)));
     event.setImpactSummary(
       "Thanks to all the volunteers that came by to help teach the kids soccer." +
       " The kids really enjoyed learning from everyone and they're eagerly anticipating the" +
       " next event!");
     events.add(event);
     pendingReviews.add(PendingReview.create(event, USER4.getKey(),
       "I had a great time teaching soccer to the kids.\n\n" +
       "Thanks to Harish and Poonum for organizing such a wonderful event!", 5));
     pendingReviews.add(PendingReview.create(event, USER8.getKey(),
       "Parking was a bit difficult to find...", 3));
     pendingReviews.add(PendingReview.create(event, USER12.getKey(), null, 3));
     eventNoShowInfo.add(new EventNoShowInfo(event, asList(USER2.getKey())));
 
     organizers = asList(USER1.getKey(), AMIR.getKey());
     registeredUsers = asList(USER2.getKey(), USER4.getKey(), USER5.getKey(),
       USER6.getKey(), USER7.getKey(), USER8.getKey(), HARISH.getKey(), POONUM.getKey());
     waitListedUsers = asList(USER3.getKey());
     event = createEvent("Youth Soccer Clinic", BGCSF_COLUMBIA_PARK, soccerField,
       computeEventDate(now, -5, 4), 1,
       organizers, registeredUsers, waitListedUsers, registeredUsers.size(), "502904726456292",
       columbiaParkSoccerWaiverKey);
     event.setSuitableForTypes(Lists.newArrayList(EnumSet.allOf(SuitableForType.class)));
     event.setImpactSummary(
       "The soccer clinic was a huge success! Thank you to everyone who stopped by.");
     events.add(event);
     pendingReviews.add(PendingReview.create(event, USER7.getKey(), null, 4));
     eventNoShowInfo.add(new EventNoShowInfo(event, asList(USER2.getKey())));
 
     organizers = asList(USER1.getKey(), HARISH.getKey(), POONUM.getKey());
     registeredUsers = asList(USER2.getKey(), USER5.getKey());
     waitListedUsers = asList();
     event = createEvent("San Francisco Street Cleanup", BENEVOLENT, ferryBuilding,
       computeEventDate(now, -20, 1), 1, organizers, registeredUsers, waitListedUsers, 100,
       "502906933122738");
     event.setImpactSummary(
         "78 Square folks & neighbors brought in 52 bags, a mattress & a chair for a total of " +
         "353 pounds of trash.");
     events.add(event);
 
     organizers = asList(USER1.getKey(), AMIR.getKey());
     registeredUsers = asList(USER2.getKey(), USER5.getKey());
     waitListedUsers = asList();
     event = createEvent("San Jose Street Cleanup", UNITED_WAY, unitedWayParkmoorOffice,
       computeEventDate(now, -27, 1), 1, organizers, registeredUsers, waitListedUsers, 100,
       "502906759789422");
     event.setImpactSummary(
       "107 pounds of trash off the street. 7 syringes. 7 thank yous from local residents.");
     events.add(event);
 
     organizers = asList(USER1.getKey(), HARISH.getKey(), POONUM.getKey());
     registeredUsers = asList(USER2.getKey(), USER5.getKey(), AMIR.getKey());
     waitListedUsers = asList();
     event = createEvent("San Francisco Street Cleanup", BENEVOLENT, ferryBuilding,
       computeEventDate(now, -31, 1), 1, organizers, registeredUsers, waitListedUsers, 100,
       "502906933122738");
     events.add(event);
     eventNoShowInfo.add(new EventNoShowInfo(event,
       asList(AMIR.getKey(), USER2.getKey())));
 
     organizers = asList(USER1.getKey(), AMIR.getKey());
     registeredUsers = asList(USER2.getKey(), USER5.getKey(), HARISH.getKey(), POONUM.getKey());
     waitListedUsers = asList();
     event = createEvent("San Jose Street Cleanup", UNITED_WAY, unitedWayParkmoorOffice,
       computeEventDate(now, -61, 0), 1, organizers, registeredUsers, waitListedUsers, 100,
       "502906759789422");
     events.add(event);
     eventNoShowInfo.add(new EventNoShowInfo(event,
       asList(HARISH.getKey(), POONUM.getKey(), USER2.getKey())));
 
     return new CreateEventsResult(events, eventNoShowInfo, pendingReviews);
   }
 
   private static Date computeEventDate(Date now, int days, int timePm) {
     Date result = DateUtils.truncate(now, Calendar.DATE);
     result = DateUtils.addHours(result, timePm + 12);
     return DateUtils.addDays(result, days);
   }
 
   private static Event createEvent(String title, TestOrganization testOrg, Location location,
       Date startTime, int numHours, List<Key<User>> organizers, List<Key<User>> registeredUsers,
       List<Key<User>> waitListedUsers, int maxRegistrations, @Nullable String albumId) {
     return createEvent(title, testOrg, location, startTime, numHours, organizers, registeredUsers,
       waitListedUsers, maxRegistrations, albumId, null);
   }
 
   private static Event createEvent(String title, TestOrganization testOrg, Location location,
       Date startTime, int numHours, List<Key<User>> organizers, List<Key<User>> registeredUsers,
       List<Key<User>> waitListedUsers, int maxRegistrations, @Nullable String albumId,
       @Nullable Key<Waiver> waiverKey) {
     eventNum++;
     Event event = new Event();
     event.setTitle(title);
     event.setOrganization(KeyWrapper.create(testOrg.getKey()));
     String eventDescriptionIntro =
         "This is a fabricated event for our online demo. Feel free to register, wait list, etc." +
         " Example event description: ";
     if (eventNum % 2 == 0) {
       event.setDescription(
         eventDescriptionIntro +
         "The Eastmont Garden of Hope receives and distributes donated clothing, hygiene products, household items, canned goods, and other necessities to Social Services Agency clients in urgent need year-round. This program serves as many as 400 households monthly. Whether providing food for a struggling family, a warm coat for a shivering child, or diapers for a desperate mother’s newborn, the Eastmont Garden of Hope seeks to ensure that those in greatest need are served expeditiously and with utmost compassion. Volunteers play a vital role in the program's operations by managing donations and keeping the service area open and available to customers in need throughout the week.\n\n" +
         "Volunteers are requested to participate for at least one shift (3-4 hours) per week, for at least 3 months. Scheduling is flexible, Monday-Friday, between the hours of 9:00am-12:00pm and 1:00-5:00pm.");
       event.setSpecialInstructions("A mandatory Orientation is scheduled for Saturday, June 29, 2013 from 10:00-12:00 at the San Lorenzo Public Library (community meeting room): 395 Paseo Grande, San Lorenzo. Please contact Axxxx Wxxx, coordinator at XXX-XXX-XXXX or xxx@xxx.org to inquire about availability of this position and confirm attendance at orientation.");
       event.setCauses(asList(CauseType.SENIORS));
     } else {
       event.setDescription(
         eventDescriptionIntro +
         "We are seeking volunteers willing to provide in-home respite care for people with terminally ill companion animals. Ancillary services would involve providing some comfort care for the pets themselves as well as pet loss counseling for those who have lost a pet. Risk management and liability issues are still being explored vis-a-vis a possible partnership with our local Humane Society of the North Bay, but we hope to begin training sessions for volunteers in 2014. In the meantime, volunteers will undergo initial screening through The NHFP. Please be patient if you do not hear from us right away. We are generally inundated with requests for emergency pet hospice care as well as involved with training seminars or our biennial symposium. We would appreciate a telephone call from you if you have not heard back from us within four weeks. Please call (XXX) XXX-XXXX and/or leave a message explaining you are a potential volunteer.");
       event.setCauses(asList(CauseType.ANIMALS));
     }
     event.setLocation(location);
     event.setStartTime(startTime);
     event.setEndTime(DateUtils.addHours(startTime, numHours));
     event.setParticipants(createParticpantsList(organizers, registeredUsers, waitListedUsers));
     event.setMaxRegistrations(maxRegistrations);
     if (albumId != null) {
       event.setAlbum(new AlbumRef(albumId, ImageProviderType.FACEBOOK));
     }
     if (waiverKey != null) {
       event.setWaiver(KeyWrapper.create(waiverKey));
     }
     return event;
   }
 
   private static List<EventParticipant> createParticpantsList(List<Key<User>> organizers,
       List<Key<User>> registeredUsers, List<Key<User>> waitListedUsers) {
     List<EventParticipant> participants = Lists.newArrayList();
     for (Key<User> organizer : organizers) {
       participants.add(EventParticipant.create(organizer, ParticipantType.ORGANIZER));
     }
     for (Key<User> registeredUser : registeredUsers) {
       participants.add(EventParticipant.create(registeredUser, ParticipantType.REGISTERED));
     }
     for (Key<User> waitListedUser : waitListedUsers) {
       participants.add(EventParticipant.create(waitListedUser, ParticipantType.WAIT_LISTED));
     }
     return participants;
   }
 
   @Data
   private static class CreateEventsResult {
     private final Collection<Event> events;
     private final List<EventNoShowInfo> eventNoShowInfo;
     // Reviews are persisted after the events the belong to are persisted because reviews are
     // child entities of the events. Meaning the events must be persisted so their event keys
     // can be generated. The event keys will be used as the parent keys for the reviews.
     private final Collection<PendingReview> pendingReviews;
   }
 
   @Data
   private static class EventNoShowInfo {
     private final Event event;
     private final List<Key<User>> noShowUsers;
 
     public void persist() {
       for (Key<User> noShowUserKey : noShowUsers) {
         ofy().transact(new UpsertParticipantTxn(
           Key.create(event), noShowUserKey, ParticipantType.REGISTERED_NO_SHOW));
       }
     }
   }
 
   @Data
   private static class PendingReview {
     private final Event event;
     private final Key<User> userKey;
     private final Review review;
 
     public static PendingReview create(Event event, Key<User> userKey, @Nullable String comment,
       double ratingValue) {
       return new PendingReview(event, userKey, createReview(comment, ratingValue));
     }
 
     private static Review createReview(@Nullable String comment, double ratingValue) {
       Review review = new Review();
       if (comment != null) {
         review.setComment(comment);
       }
       review.setRating(Rating.create(ratingValue));
       return review;
     }
 
     public void persistReview() {
       AdminUtil.executeSubtaskAsUser(userKey, null, new AdminSubtask() {
         @Override
         public void execute() {
           Event.mutateEventReviewForCurrentUser(Key.create(event), review);
         }
       });
     }
   }
 
   private void persistOrganizations() {
     for (final TestOrganization testOrg : TestOrganization.values()) {
       BaseDao.upsert(createOrganization(testOrg));
     }
     // The org is created without any memberships to start with.
     for (TestOrganization testOrg : TestOrganization.values()) {
       User.updateMembership(testOrg.initialAdmin.getKey(), testOrg.getKey(),
         Organization.Role.ADMIN);
       for (TestOrgMembership membership : testOrg.memberships) {
         if (membership.grantedRole != null) {
           User.updateMembership(membership.user.getKey(), testOrg.getKey(), membership.grantedRole);
         }
       }
       for (Waiver waiver : testOrg.waivers) {
         Waiver.insert(testOrg.getKey(), waiver);
       }
     }
 
     for (final TestOrganization testOrg : TestOrganization.values()) {
       for (final TestOrgMembership membership : testOrg.memberships) {
         AdminUtil.executeSubtaskAsUser(membership.user.getKey(),
           null,
           new AdminSubtask() {
             @Override
             public void execute() {
               if (membership.requestedRole != null) {
                 User.updateMembership(membership.user.getKey(), testOrg.getKey(),
                   membership.requestedRole);
               }
             }
           });
       }
     }
   }
 
   public Organization createOrganization(TestOrganization testOrg) {
     Organization org = new Organization();
     org.setPage(testOrg.getPageRef());
     org.setDonationUrl(testOrg.getDonationUrl());
     if (testOrg.parentOrg != null) {
       org.setParentOrg(new OrganizationNamedKeyWrapper(testOrg.parentOrg.getKey()));
     }
     org.getAutoMembershipRules().addAll(testOrg.autoMembershipRules);
     try {
       org.initFromPage(servletCtx, new URI(baseUrl));
     } catch (URISyntaxException e) {
       throw new RuntimeException(e);
     }
     return org;
   }
 
   private void awardBadges(Collection<Event> events) {
     // TODO(avaliani): when organization badges are awarded Badge -> BadgeSummary will not
     // be a sufficient mapping.
     Map<Key<User>, Map<Badge, BadgeSummary>> badgesToAward = Maps.newHashMap();
 
     List<Key<Event>> pastEventKeys = Lists.newArrayList();
     Date now = new Date();
     for (Event initialEvent : events) {
       if (initialEvent.getStartTime().before(now)) {
         pastEventKeys.add(Key.create(initialEvent));
       }
     }
 
     Map<Key<Event>, Event> upToDateEvents = ofy().load().keys(pastEventKeys);
     for (Event upToDateEvent : upToDateEvents.values()) {
       for (EventParticipant participant : upToDateEvent.getParticipants()) {
 
         // Award Padawan badges if the a user has attended at least one event.
         if ((participant.getType() == ParticipantType.ORGANIZER) ||
             (participant.getType() == ParticipantType.REGISTERED) ) {
           Key<User> userKey =
               Key.<User>create(participant.getUser().getKey());
           Map<Badge, BadgeSummary> userBadges =
               badgesToAward.get(userKey);
           if (userBadges == null) {
             userBadges = Maps.newHashMap();
             badgesToAward.put(userKey, userBadges);
           }
           if (!userBadges.containsKey(Badge.KARMA_PADAWAN)) {
             userBadges.put(Badge.KARMA_PADAWAN,
               new BadgeSummary(1, Badge.KARMA_PADAWAN, null));
           }
         }
 
       }
     }
 
     // For now award the monthly zen recipient badge to a fixed set of users - history
     // independent. To do this accurately we would need to know the per month karma goal
     // per user.
     List<Key<User>> monthlyZenRecipients = Arrays.asList(
       AMIR.getKey(), HARISH.getKey(), POONUM.getKey(), USER1.getKey());
     for (Key<User> monthlyZenRecipient : monthlyZenRecipients) {
       Map<Badge, BadgeSummary> userBadges =
           badgesToAward.get(monthlyZenRecipient);
       if (userBadges == null) {
         userBadges = Maps.newHashMap();
         badgesToAward.put(monthlyZenRecipient, userBadges);
       }
       userBadges.put(Badge.MONTHLY_ZEN,
         new BadgeSummary(2, Badge.MONTHLY_ZEN, null));
     }
 
     for (Map.Entry<Key<User>, Map<Badge, BadgeSummary>> entry : badgesToAward.entrySet()) {
       ofy().transact(new UpdateBadgesTxn(
         entry.getKey(), entry.getValue().values()));
     }
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class UpdateBadgesTxn extends VoidWork {
     private final Key<User> userToUpdateKey;
     private final Collection<BadgeSummary> badges;
 
     public void vrun() {
       User user = ofy().load().key(userToUpdateKey).now();
       if (user == null) {
         throw ErrorResponseMsg.createException("user not found", ErrorInfo.Type.BAD_REQUEST);
       }
       user.setBadges(Lists.newArrayList(badges));
       BaseDao.partialUpdate(user);
     }
   }
 }
