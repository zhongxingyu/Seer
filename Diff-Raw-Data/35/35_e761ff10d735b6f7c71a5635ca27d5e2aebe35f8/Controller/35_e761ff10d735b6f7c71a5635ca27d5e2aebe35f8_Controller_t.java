 package ai.ilikeplaces.servlets;
 
 import ai.ilikeplaces.doc.*;
 import ai.ilikeplaces.logic.Listeners.*;
 import ai.ilikeplaces.logic.Listeners.templates.TemplateGeneric;
 import ai.ilikeplaces.logic.Listeners.widgets.*;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.util.Loggers;
 import org.itsnat.core.*;
 import org.itsnat.core.event.ItsNatServletRequestListener;
 import org.itsnat.core.http.HttpServletWrapper;
 import org.itsnat.core.http.ItsNatHttpServlet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author Ravindranath Akila
  */
 
 @TODO(task = "Code to disable url calls with itsnat_doc_name=### type urls if possible")
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 final public class
         Controller extends HttpServletWrapper {
 // ------------------------------ FIELDS ------------------------------
 
     public static final String LOCATION_HUB = "/page/Earth_of_Earth?WOEID=1";
     public static final String WEB_INF_PAGES = "WEB-INF/pages/";
     public static String USER_PROPERTY_EMAIL_XHTML = "ai/ilikeplaces/widgets/UserProperty_email.xhtml";
     public static String PEOPLE_EMAIL_XHTML = "ai/ilikeplaces/widgets/people/people_thumb_email.xhtml";
 
 
     /**
      * This Map is static as Id's in html documents should be universally identical, i.e. as htmldocname_elementId
      */
     public final static Map<String, String> GlobalHTMLIdRegistry = new IdentityHashMap<String, String>();
 
     /**
      * Retrievable list of all element Ids by Page
      */
     public final static Map<PageFace, HashSet<String>> GlobalPageIdRegistry = new IdentityHashMap<PageFace, HashSet<String>>();
     public static String REAL_PATH;//Weak implementation but suffices
 
     private final static Map<PageFace, String> PrettyURLMap_ = new IdentityHashMap<PageFace, String>();//Please read javadoc before making any changes to this implementation
     final static private Logger staticLogger = LoggerFactory.getLogger(Controller.class.getName());
     private static final String ITSNAT_DOC_NAME = "itsnat_doc_name";
     private static final String DT = "dt";
     private static final String DOWNTOWN = "downtown";
     private static final String UNDERSCORE = "_";
     private static final String HASH = "#";
     private static final String _SO = "_so";
     private static final String _PHOTO_ = "_photo_";
     private static final String EMPTY = "";
     private static final String _ME = "_me";
     private static final String _ORG = "_org";
     private static final String _FRIENDS = "_friends";
     private static final String _BOOK = "_book";
     private static final String _TRIBES = "_tribes";
     private static final String _PROFILE = "_profile";
     private static final String _I = "_i";
     private static final String _ACTIVATE = "_activate";
     private static final String _SHARE = "_share";
     private static final String _GEOBUSINESS = "_geobusiness";
     private static final String _PUBLIC = "_public";
     private static final String _LEGAL = "_legal";
     private static final String _MUSTER = "_muster";
     private static final String _HELP = "_help";
 
     final PageFace locationMain = Page.LocationMain;
     final PageFace aarrr = Page.Aarrr;
     //    final PageFace photoCRUD = Page.PhotoCRUD;
     final PageFace photo$Description = Page.Photo$Description;
     final PageFace signInOn = Page.SignInOn;
     final PageFace notification = Page.Notification;
     final PageFace downTownFlow = Page.DownTownFlow;
     final PageFace tribeHome = Page.TribeHome;
     final PageFace tribeCreateHome = Page.TribeCreateHome;
 
     final PageFace privateLocationCreate = Page.PrivateLocationCreate;
     final PageFace privateLocationView = Page.PrivateLocationView;
     final PageFace privateLocationDelete = Page.PrivateLocationDelete;
     final PageFace tribeSidebar = Page.TribeSidebar;
 
     final PageFace privateEventCreate = Page.PrivateEventCreate;
     final PageFace privateEventView = Page.PrivateEventView;
     final PageFace privateEventDelete = Page.PrivateEventDelete;
     final PageFace privateEventViewSidebar = Page.PrivateEventViewSidebar;
 
     final PageFace wOIEDGrabber = Page.WOEIDGrabber;
 
     final PageFace downTownHeatMap = Page.DownTownHeatMap;
 
     final PageFace skeleton = Page.Skeleton;
     final PageFace organize = Page.Organize;
     final PageFace findFriend = Page.Friends;
     final PageFace book = Page.Bookings;
     final PageFace tribes = Page.Tribes;
     final PageFace profile = Page.Profile;
     final PageFace i = Page.I;
     final PageFace activate = Page.Activate;
     final PageFace share = Page.Share;
     final PageFace geoBusiness = Page.GeoBusiness;
     final PageFace photos = Page.Photos;
     final PageFace legal = Page.Legal;
     final PageFace muster = Page.Muster;
     final PageFace helpPage = Page.HelpPage;
 
     final PageFace findFriendWidget = Page.FindFriend;
     final PageFace friendAdd = Page.FriendAdd;
     final PageFace friendDelete = Page.FriendDelete;
     final PageFace friendList = Page.FriendList;
 
     final PageFace genericButton = Page.GenericButton;
 
     final PageFace templateGeneric = Page.TemplateGeneric;
 
     final PageFace displayName = Page.DisplayName;
 
     final PageFace wallHanlder = Page.WallHandler;
 
     final PageFace passwordChange = Page.PasswordChange;
     final PageFace forgotPasswordChange = Page.ForgotPasswordChange;
     final PageFace profileWidget = Page.ProfileWidget;
 
     final PageFace teachTribe = Page.TeachTribe;
     final PageFace teachMoment = Page.TeachMoment;
 
     final PageFace carousel = Page.Carousel;
     final PageFace carouselThumb = Page.CarouselThumb;
 
     final PageFace people = Page.People;
     final PageFace peopleThumb = Page.PeopleThumb;
 
     final PageFace autoplayControls = Page.AutoplayControls;
 
     final PageFace album = Page.Album;
     final PageFace albumTribe = Page.AlbumTribe;
 
     final PageFace userProperty = Page.UserProperty;
     final PageFace userPropertySidebar = Page.UserPropertySidebar;
 
     final PageFace termsOfServices = Page.TermsOfServices;
     final PageFace privacyPolicy = Page.PrivacyPolicy;
     final PageFace bate = Page.Bate;
     final PageFace help = Page.Help;
 
     final PageFace juice = Page.Juice;
 
     final PageFace adaptableSignup = Page.AdaptableSignup;
 
 // -------------------------- ENUMERATIONS --------------------------
 
     @NOTE(note = "Inner Enums are static. Therefore, the lists shall be populated only once.")
     public enum Page implements PageFace {
         TermsOfServices("ai/ilikeplaces/widgets/legal/terms_of_services.xhtml"
         ) {
             @Override
             public String toString() {
                 return DocTermsOfServices;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         PrivacyPolicy("ai/ilikeplaces/widgets/legal/privacy_policy.xhtml"
         ) {
             @Override
             public String toString() {
                 return DocPrivacyPolicy;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         Bate("ai/ilikeplaces/widgets/bate.xhtml",
                 Controller.Page.BateSignup,
                 Controller.Page.BateSignupEmail,
                 Controller.Page.BateSignupPassword,
                 Controller.Page.BateSignupNotifications,
                 Controller.Page.BateSignupButton,
                 Controller.Page.BateImportResults,
                 Controller.Page.BateIntroduction,
                 Controller.Page.BateOmg,
                 Controller.Page.BateOmgSuccessMsg
         ) {
             @Override
             public String toString() {
                 return DocBate;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         Help("ai/ilikeplaces/widgets/help.xhtml"
         ) {
             @Override
             public String toString() {
                 return DocHelp;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         TeachTribe("ai/ilikeplaces/widgets/teach/teach_tribe.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.teach.TeachTribe.TeachTribeIds.values()
         ) {
             @Override
             public String toString() {
                 return DocTeachTribe;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         TeachMoment("ai/ilikeplaces/widgets/teach/teach_moment.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.teach.TeachMoment.TeachMomentIds.values()
         ) {
             @Override
             public String toString() {
                 return DocTeachMoment;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         Carousel("ai/ilikeplaces/widgets/carousel/carousel.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.carousel.Carousel.CarouselIds.values()
         ) {
             @Override
             public String toString() {
                 return DocCarousel;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         CarouselThumb("ai/ilikeplaces/widgets/carousel/carousel_thumb.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.carousel.CarouselThumb.CarouselThumbIds.values()
         ) {
             @Override
             public String toString() {
                 return DocCarouselThumb;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         People("ai/ilikeplaces/widgets/people/people.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.people.People.PeopleIds.values()
         ) {
             @Override
             public String toString() {
                 return DocPeople;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         PeopleThumb("ai/ilikeplaces/widgets/people/people_thumb.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.people.PeopleThumb.PeopleThumbIds.values()
         ) {
             @Override
             public String toString() {
                 return DocPeopleThumb;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         AutoplayControls("ai/ilikeplaces/widgets/autoplay/controls.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.autoplay.AutoplayControls.AutoplayControlsIds.values()
         ) {
             @Override
             public String toString() {
                 return DocAutoplayControls;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         Album("ai/ilikeplaces/widgets/Album.xhtml",
                 AlbumManager.AlbumManagerIds.values()
         ) {
             @Override
             public String toString() {
                 return DocAlbum;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
         AlbumTribe("ai/ilikeplaces/widgets/AlbumTribe.xhtml",
                 AlbumManagerTribe.AlbumManagerTribeIds.values()
         ) {
             @Override
             public String toString() {
                 return DocAlbumTribe;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         UserProperty("ai/ilikeplaces/widgets/UserProperty.xhtml",
                 Controller.Page.user_property_profile_photo,
                 Controller.Page.user_property_name,
                 Controller.Page.user_property_widget,
                 Controller.Page.user_property_content
 
         ) {
             @Override
             public String toString() {
                 return DocUserProperty;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         UserPropertySidebar("ai/ilikeplaces/widgets/UserProperty_sidebar.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.UserPropertySidebar.UserPropertySidebarIds.values()
 
         ) {
             @Override
             public String toString() {
                 return DocUserPropertySidebar;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         ProfileWidget("ai/ilikeplaces/widgets/profile.xhtml",
                 Controller.Page.ProfileNotice,
                 Controller.Page.ProfileURLChange,
                 Controller.Page.ProfileURL,
                 Controller.Page.ProfileURLUpdate
         ) {
             @Override
             public String toString() {
                 return DocProfileWidget;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         ForgotPasswordChange("ai/ilikeplaces/widgets/password.xhtml",
                 ForgotPasswordManager.ForgotPasswordManagerIds.values()
 
         ) {
             @Override
             public String toString() {
                 return DocForgotPasswordChange;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         PasswordChange("ai/ilikeplaces/widgets/password.xhtml",
                 Controller.Page.ProfilePasswordWidget,
                 Controller.Page.ProfilePasswordNotice,
                 Controller.Page.ProfilePasswordCurrent,
                 Controller.Page.ProfilePasswordNewConfirm,
                 Controller.Page.ProfilePasswordNew,
                 Controller.Page.ProfilePasswordSave
         ) {
             @Override
             public String toString() {
                 return DocPasswordChange;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         WallHandler("ai/ilikeplaces/widgets/wall.xhtml",
                 WallWidget.WallWidgetIds.values()
         ) {
             @Override
             public String toString() {
                 return DocWall;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         DisplayName("ai/ilikeplaces/widgets/DisplayName.xhtml",
                 Controller.Page.DisplayNameDisplay,
                 Controller.Page.DisplayNameInput,
                 Controller.Page.DisplayNameSave,
                 Controller.Page.DisplayNameNotice) {
             @Override
             public String toString() {
                 return DocDisplayName;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         PrivateEventCreate("ai/ilikeplaces/widgets/privateevent/private_event_create.xhtml",
                 Controller.Page.privateEventCreateName,
                 Controller.Page.privateEventCreateInfo,
                 Controller.Page.privateEventCreateSave,
                 Controller.Page.privateEventCreateNotice
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventCreate;
             }
         },
 
         PrivateEventView("ai/ilikeplaces/widgets/privateevent/private_event_view.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.privateevent.PrivateEventView.PrivateEventViewIds.values()
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventView;
             }
         },
 
 
         PrivateEventDelete("ai/ilikeplaces/widgets/privateevent/private_event_delete.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.privateevent.PrivateEventDelete.PrivateEventDeleteIds.values()
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventDelete;
             }
         },
 
 
         PrivateEventViewSidebar("ai/ilikeplaces/widgets/privateevent/private_event_view_sidebar.xhtml",
                 Controller.Page.private_event_view_sidebar_name,
                 Controller.Page.private_event_view_sidebar_content,
                 Controller.Page.private_event_view_sidebar_profile_photo
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventViewSidebar;
             }
         },
 
         TribeSidebar("ai/ilikeplaces/widgets/tribe_sidebar.xhtml",
                 Controller.Page.tribe_sidebar_name,
                 Controller.Page.tribe_sidebar_content,
                 Controller.Page.tribe_sidebar_profile_photo
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocTribeSidebar;
             }
         },
 
 
         WOEIDGrabber("ai/ilikeplaces/widgets/WOEIDGrabber.xhtml",
                 Controller.Page.WOEIDGrabberWOEID
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocWOEIDGrabber;
             }
         },
 
 
         DownTownHeatMap("ai/ilikeplaces/widgets/DownTownHeatMap.xhtml",
                 Controller.Page.DownTownHeatMapWOEID,
                 Controller.Page.DownTownHeatMapBB,
                 Controller.Page.DownTownHeatMapSignupWidget,
                 Controller.Page.DownTownHeatMapSignupEmail,
                 Controller.Page.DownTownHeatMapSignupPassword,
                 Controller.Page.DownTownHeatMapSignupNotifications,
                 Controller.Page.DownTownHeatMapSignupButton
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocDownTownHeatMap;
             }
         },
 
         FindFriend("ai/ilikeplaces/widgets/friend/friend_find.xhtml",
                 Controller.Page.friendFindSearchTextInput,
                 Controller.Page.friendFindSearchButtonInput,
                 Controller.Page.friendFindSearchResults,
                 Controller.Page.friendFindSearchNotice,
                 Controller.Page.friendFindSearchInvites
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFindFriend;
             }
         },
 
         FriendAdd("ai/ilikeplaces/widgets/friend/friend_add.xhtml",
                 Controller.Page.friendAddAddButton,
                 Controller.Page.friendAddDisplayNameLabel
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendAdd;
             }
         },
 
         FriendDelete("ai/ilikeplaces/widgets/friend/friend_delete.xhtml",
                 Controller.Page.friendDeleteAddButton,
                 Controller.Page.friendDeleteDisplayNameLabel
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendDelete;
             }
         },
 
         FriendList("ai/ilikeplaces/widgets/friend/friend_list.xhtml",
                 Controller.Page.FriendListList
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendList;
             }
         },
 
         GenericButton("ai/ilikeplaces/widgets/button.xhtml",
                 Controller.Page.GenericButtonLink,
                 Controller.Page.GenericButtonText,
                 Controller.Page.GenericButtonImage,
                 Controller.Page.GenericButtonWidth
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocGenericButton;
             }
         },
 
         Organize(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_org";
             }
 
             @Override
             public String toString() {
                 return DocOrganize;
             }
         },
 
         Legal(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_legal";
             }
 
             @Override
             public String toString() {
                 return DocLegal;
             }
         },
 
         Muster(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_muster";
             }
 
             @Override
             public String toString() {
                 return DocMuster;
             }
         },
 
 
         HelpPage(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_help";
             }
 
             @Override
             public String toString() {
                 return DocHelpPage;
             }
         },
 
         Activate(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_activate";
             }
 
             @Override
             public String toString() {
                 return DocActivate;
             }
         },
 
         Share(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_share";
             }
 
             @Override
             public String toString() {
                 return DocShare;
             }
         },
 
         GeoBusiness(null) {
             public String getURL() {
                 return APP_ROOT + "page/_geobusiness";
             }
 
             @Override
             public String toString() {
                 return DocGeoBusiness;
             }
         },
 
         TemplateGeneric(null) {
             public String getURL() {
                 return APP_ROOT + "page/_public";
             }
 
             @Override
             public String toString() {
                 return DocPublic;
             }
         },
 
         Profile(null,
                 Controller.Page.ProfilePhotoChange
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_profile";
             }
 
             @Override
             public String toString() {
                 return DocProfile;
             }
         },
 
         I(null) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_i";
             }
 
             @Override
             public String toString() {
                 return DocI;
             }
         },
 
         Photos(null) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_me";
             }
 
             @Override
             public String toString() {
                 return DocPhotos;
             }
         },
 
         Friends(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_friends";
             }
 
             @Override
             public String toString() {
                 return DocFriends;
             }
         },
         Bookings(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_book";
             }
 
             @Override
             public String toString() {
                 return DocBook;
             }
         },
         Tribes(null
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/_tribes";
             }
 
             @Override
             public String toString() {
                 return DocTribes;
             }
         },
 
         Skeleton("ai/ilikeplaces/Skeleton.xhtml",
                 Controller.Page.skeletonTitle,
                 Controller.Page.SkeletonCPageTitle,
                 Controller.Page.SkeletonCPageIntro,
                 Controller.Page.SkeletonCPageNotice,
                 Controller.Page.Skeleton_center,
                 Controller.Page.Skeleton_center_content,
                 Controller.Page.Skeleton_center_skeleton,
                 Controller.Page.Skeleton_file_list,
                 Controller.Page.Skeleton_left_column,
                 Controller.Page.Skeleton_login_widget,
                 Controller.Page.Skeleton_notice,
                 Controller.Page.Skeleton_notice_sh,
                 Controller.Page.Skeleton_othersidebar,
                 Controller.Page.Skeleton_profile_photo,
                 Controller.Page.Skeleton_othersidebar_identity,
                 Controller.Page.Skeleton_othersidebar_organizer_link,
                 Controller.Page.Skeleton_othersidebar_photo_manager_link,
                 Controller.Page.Skeleton_othersidebar_places_link,
                 Controller.Page.Skeleton_othersidebar_profile_link,
                 Controller.Page.Skeleton_othersidebar_upload_file_sh,
                 Controller.Page.Skeleton_right_column,
                 Controller.Page.Skeleton_sidebar,
                Controller.Page.Skeleton_notifications
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocSkeleton;
             }
         },
         PrivateLocationCreate("ai/ilikeplaces/widgets/privatelocation/private_location_create.xhtml",
                 Controller.Page.privateLocationCreateName,
                 Controller.Page.privateLocationCreateInfo,
                 Controller.Page.privateLocationCreateWOEID,
                 Controller.Page.privateLocationCreateWOEIDGrabber,
                 Controller.Page.privateLocationCreateSave,
                 Controller.Page.PrivateLocationCreateCNotice,
                 Controller.Page.PrivateLocaionCreateCTitle,
                 Controller.Page.PrivateLocaionCreateCIntro
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationCreate;
             }
         },
 
         PrivateLocationView("ai/ilikeplaces/widgets/privatelocation/private_location_view.xhtml",
                 Controller.Page.privateLocationViewNotice,
                 Controller.Page.privateLocationViewName,
                 Controller.Page.privateLocationViewInfo,
                 Controller.Page.privateLocationViewLocationMap,
                 Controller.Page.privateLocationViewOwners,
                 Controller.Page.privateLocationViewVisitors,
                 Controller.Page.privateLocationViewLink,
                 Controller.Page.privateLocationViewEventList
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationView;
             }
         },
 
 
         PrivateLocationDelete("ai/ilikeplaces/widgets/privatelocation/private_location_delete.xhtml",
                 Controller.Page.privateLocationDeleteName,
                 Controller.Page.privateLocationDeleteInfo,
                 Controller.Page.privateLocationDeleteLocationMap,
                 Controller.Page.privateLocationDeleteNotice,
                 Controller.Page.privateLocationDelete,
                 Controller.Page.privateLocationDeleteOwners,
                 Controller.Page.privateLocationDeleteVisitors,
                 Controller.Page.privateLocationDeleteEventList
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationDelete;
             }
         },
 
         home(
                 null) {
             @Override
             public String getURL() {
                 return APP_ROOT + "";
             }
 
             @Override
             public String toString() {
                 return "";
             }
         },
         LocationMain(
                 "ai/ilikeplaces/Main.xhtml",
                 Controller.Page.body,
                 Controller.Page.mainTitle,
                 Controller.Page.mainMetaDesc,
                 Controller.Page.Main_ICBM,
                 Controller.Page.Main_geoposition,
                 Controller.Page.Main_geoplacename,
                 Controller.Page.Main_georegion,
                 Controller.Page.Main_othersidebar_identity,
                 Controller.Page.Main_location_photo,
                 Controller.Page.Main_profile_photo,
                 Controller.Page.Main_othersidebar_profile_link,
                 Controller.Page.Main_othersidebar_upload_file_sh,
                 Controller.Page.Main_notice_sh,
                 Controller.Page.Main_loading_hotels_link,
                 Controller.Page.Main_hotels_link,
                 Controller.Page.Main_home_page_link,
                 Controller.Page.Main_flight_page_link,
                 Controller.Page.Main_car_page_link,
                 Controller.Page.Main_cruise_page_link,
                 Controller.Page.Main_center_main,
                 Controller.Page.Main_notice,
                 Controller.Page.Main_center_main_location_title,
                 Controller.Page.Main_center_content,
                 Controller.Page.Main_yox,
                 Controller.Page.Main_left_column,
                 Controller.Page.Main_right_column,
                 Controller.Page.Main_sidebar,
                 Controller.Page.Main_login_widget,
                 Controller.Page.Main_location_backlink,
                 Controller.Page.Main_location_list_header,
                 Controller.Page.Main_location_list,
                 Controller.Page.Main_flickr,
                 Controller.Page.Main_disqus_thread_data
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/main";
             }
 
             @Override
             public String toString() {
                 return DocLocation;
             }
         },
         Aarrr(
                 "ai/ilikeplaces/AARRR.xhtml",
                 Controller.Page.AarrrDownTownHeatMap,
                 Controller.Page.AarrrJuice,
                 Controller.Page.AarrrWOEID,
                Controller.Page.AarrrHeader
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/";
             }
 
             @Override
             public String toString() {
                 return DocAarrr;
             }
        }, Global(
                "",
                Page.CPageType
        ) {
            @Override
            public String getURL() {
                throw new IllegalAccessError("SORRY! THIS IS THE ENUM FOR GLOBAL KEYS ONLY");
            }

            @Override
            public String toString() {
                throw new IllegalAccessError("SORRY! THIS IS THE ENUM FOR GLOBAL KEYS ONLY");
            }
         },

         Photo$Description(
                 "ai/ilikeplaces/widgets/Photo-Description.xhtml",
                 Controller.Page.pd,
                 Controller.Page.close,
                 Controller.Page.pd_photo_permalink,
                 Controller.Page.pd_photo,
                 Controller.Page.pd_photo_description,
                 Controller.Page.pd_photo_delete,
                 Controller.Page.pd_photo_wall,
                 Controller.Page.pd_photo_sequence_number
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return "Photo-Description";
             }
         },
         PhotoUpload(
                 "ai/ilikeplaces/widgets/PhotoUpload.xhtml") {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/main";
             }
 
             @Override
             public String toString() {
                 return "PhotoUpload";
             }
         },
         signup(
                 null) {
             @Override
             public String getURL() {
                 return APP_ROOT + "signup";
             }
 
             @Override
             public String toString() {
                 return APP_ROOT + "signup";
             }
         },
         login(
                 null) {
             @Override
             public String getURL() {
                 return APP_ROOT + "login";
             }
 
             @Override
             public String toString() {
                 return APP_ROOT + "login";
             }
         },
         SignInOn(
                 "ai/ilikeplaces/widgets/SignInOn.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.SignInOn.SignInOnIds.values()
         ) {
             @Override
             public String getURL() {
                 return APP_ROOT + "page/main";
             }
 
             @Override
             public String toString() {
                 return DocSignInOn;
             }
         },
         Notification(
                 "ai/ilikeplaces/widgets/notification.xhtml",
                 Controller.Page.notification_simple
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocNotification;
             }
         },
         DownTownFlow(
                 "ai/ilikeplaces/widgets/DownTownFlow.xhtml",
                 Controller.Page.DownTownFlowWidget,
                 Controller.Page.DownTownFlowTalks,
                 Controller.Page.DownTownFlowMoments,
                 Controller.Page.DownTownFlowMomentsMoments,
                 Controller.Page.DownTownFlowTribes,
                 Controller.Page.DownTownFlowTribesTribes,
                 Controller.Page.DownTownFlowTalksFriends,
                 Controller.Page.DownTownFlowInviteNoti,
                 Controller.Page.DownTownFlowInviteEmail,
                 Controller.Page.DownTownFlowInviteClick
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocDownTownFlow;
             }
         },
         TribeHome(
                 "ai/ilikeplaces/widgets/TribeHome.xhtml",
                 TribeWidget.TribeWidgetIds.values()
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocTribeHome;
             }
         },
         TribeCreateHome(
                 "ai/ilikeplaces/widgets/TribeHomeCreate.xhtml",
                 Page.tribeHomeCreateWidget,
                 Page.tribeHomeCreateName,
                 Page.tribeHomeCreateStory,
                 Page.tribeHomeCreateSave,
                 Page.tribeHomeCreateNoti
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocTribeCreateHome;
             }
         }, Juice(
                 "ai/ilikeplaces/widgets/Juice.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.Juice.JuiceIds.values()) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocJuice;
             }
         }, AdaptableSignup(
                 "ai/ilikeplaces/widgets/AdaptableSignup.xhtml",
                 ai.ilikeplaces.logic.Listeners.widgets.AdaptableSignup.AdaptableSignupIds.values()) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocAdaptableSignup;
             }
         };
 
         private static final String APP_ROOT = RBGet.getGlobalConfigKey("AppRoot");
 
         /*TermsOfServices Page*/
         final static public String DocTermsOfServices = "DocTermsOfServices";
         /*DocTermsOfServices IDs*/
 
         /*PrivacyPolicy Page*/
         final static public String DocPrivacyPolicy = "DocPrivacyPolicy";
         /*PrivacyPolicy IDs*/
         //Nothing here
 
         /*Bate Page*/
         final static public String DocBate = "DocBate";
         /*Bate IDs*/
         final static public String BateSignup = "BateSignup";
         final static public String BateSignupEmail = "BateSignupEmail";
         final static public String BateSignupPassword = "BateSignupPassword";
         final static public String BateSignupNotifications = "BateSignupNotifications";
         final static public String BateSignupButton = "BateSignupButton";
         final static public String BateImportResults = "BateImportResults";
         final static public String BateIntroduction = "BateIntroduction";
         final static public String BateOmg = "BateOmg";
         final static public String BateOmgSuccessMsg = "BateOmgSuccessMsg";
 
         /*TeachTribe Page*/
         final static public String DocTeachTribe = "DocTeachTribe";
         /*TeachTribe IDs*/
         //Nothing here
 
         /*TeachTribe Page*/
         final static public String DocTeachMoment = "DocTeachMoment";
         /*TeachTribe IDs*/
         //Nothing here
 
         /*Help Page*/
         final static public String DocHelp = "DocHelp";
         /*Help IDs*/
         //Nothing here
 
         /*Carousel*/
         final static public String DocCarousel = "DocCarousel";
 
         /*PeopleThumb*/
         final static public String DocPeopleThumb = "DocPeopleThumb";
 
         /*AutoplayControls*/
         final static public String DocAutoplayControls = "DocAutoplayControls";
 
         /*People*/
         final static public String DocPeople = "DocPeople";
 
         /*CarouselThumb*/
         final static public String DocCarouselThumb = "DocCarouselThumb";
 
         /*Album Page*/
         final static public String DocAlbum = "DocAlbum";
 
         /*AlbumTribe Page*/
         final static public String DocAlbumTribe = "DocAlbumTribe";
 
         /*ProfileWidget Page*/
         final static public String DocUserProperty = "DocUserProperty";
         /*ProfileWidget IDs*/
         final static public String user_property_profile_photo = "user_property_profile_photo";
         final static public String user_property_name = "user_property_name";
         final static public String user_property_widget = "user_property_widget";
         final static public String user_property_content = "user_property_content";
 
         /*UserPropertySidebar Page*/
         final static public String DocUserPropertySidebar = "DocUserPropertySidebar";
 
 
         /*ProfileWidget Page*/
         final static public String DocProfileWidget = "DocProfileWidget";
         /*ProfileWidget IDs*/
         final static public String ProfileNotice = "ProfileNotice";
         final static public String ProfileURLChange = "ProfileURLChange";
         final static public String ProfileURL = "ProfileURL";
         final static public String ProfileURLUpdate = "ProfileURLUpdate";
 
         /*Forgot Password Page*/
         final static public String DocForgotPasswordChange = "DocForgotPasswordChange";
 
 
         /*Password Page*/
         final static public String DocPasswordChange = "DocPasswordChange";
         /*Password IDs*/
         final static public String ProfilePasswordWidget = "ProfilePasswordWidget";
         final static public String ProfilePasswordNotice = "ProfilePasswordNotice";
         final static public String ProfilePasswordCurrent = "ProfilePasswordCurrent";
         final static public String ProfilePasswordNewConfirm = "ProfilePasswordNewConfirm";
         final static public String ProfilePasswordNew = "ProfilePasswordNew";
         final static public String ProfilePasswordSave = "ProfilePasswordSave";
 
         /*WallHandler Page*/
         final static public String DocWall = "DocWall";
 
         /*DisplayName Page*/
         final static public String DocDisplayName = "DocDisplayName";
         /*DisplayName IDs*/
         final static public String DisplayNameDisplay = "DisplayNameDisplay";
         final static public String DisplayNameInput = "DisplayNameInput";
         final static public String DisplayNameSave = "DisplayNameSave";
         final static public String DisplayNameNotice = "DisplayNameNotice";
 
         /*Private Event Page*/
         final static public String DocPrivateEventView = "PrivateEventView";
 
         /*Private Event Page*/
         final static public String DocPrivateEventCreate = "PrivateEventCreate";
         /*Private Event Create IDs*/
         final static public String privateEventCreateName = "privateEventCreateName";
         final static public String privateEventCreateInfo = "privateEventCreateInfo";
         final static public String privateEventCreateSave = "privateEventCreateSave";
         final static public String privateEventCreateNotice = "privateEventCreateNotice";
 
         /*Private Event Page*/
         final static public String DocPrivateEventDelete = "PrivateEventDelete";
 
 
         /*Private Event View Sidebar Page*/
         final static public String DocPrivateEventViewSidebar = "DocPrivateEventViewSidebar";
         /*Private Event View Sidebar IDs*/
         final static public String private_event_view_sidebar_name = "private_event_view_sidebar_name";
         final static public String private_event_view_sidebar_content = "private_event_view_sidebar_content";
         final static public String private_event_view_sidebar_profile_photo = "private_event_view_sidebar_profile_photo";
 
         /*Tribe Sidebar Page*/
         final static public String DocTribeSidebar = "DocTribeSidebar";
         /*Tribe Sidebar IDs*/
         final static public String tribe_sidebar_name = "tribe_sidebar_name";
         final static public String tribe_sidebar_content = "tribe_sidebar_content";
         final static public String tribe_sidebar_profile_photo = "tribe_sidebar_profile_photo";
 
         /*WOIEDGrabber Page*/
         final static public String DocWOEIDGrabber = "DocWOEIDGrabber";
         /*WOIEDGrabber IDs*/
         final static public String WOEIDGrabberWOEID = "WOEIDGrabberWOEID";
 
         /*DownTownHeatMap Page*/
         final static public String DocDownTownHeatMap = "DocDownTownHeatMap";
         /*DownTownHeatMap IDs*/
         final static public String DownTownHeatMapWOEID = "DownTownHeatMapWOEID";
         final static public String DownTownHeatMapBB = "DownTownHeatMapBB";
         final static public String DownTownHeatMapSignupWidget = "DownTownHeatMapSignupWidget";
         final static public String DownTownHeatMapSignupEmail = "DownTownHeatMapSignupEmail";
         final static public String DownTownHeatMapSignupPassword = "DownTownHeatMapSignupPassword";
         final static public String DownTownHeatMapSignupNotifications = "DownTownHeatMapSignupNotifications";
         final static public String DownTownHeatMapSignupButton = "DownTownHeatMapSignupButton";
 
         /*FindFriend Page*/
         final static public String DocFindFriend = "DocFindFriend";
         /*FindFriend IDs*/
         final static public String friendFindSearchTextInput = "friendFindSearchTextInput";
         final static public String friendFindSearchButtonInput = "friendFindSearchButtonInput";
         final static public String friendFindSearchResults = "friendFindSearchResults";
         final static public String friendFindSearchNotice = "friendFindSearchNotice";
         final static public String friendFindSearchInvites = "friendFindSearchInvites";
 
         /*AddFriend Page*/
         final static public String DocFriendAdd = "DocFriendAdd";
         /*AddFriend IDs*/
         final static public String friendAddDisplayNameLabel = "friendAddDisplayNameLabel";
         final static public String friendAddAddButton = "friendAddAddButton";
 
         /*DeleteFriend Page*/
         final static public String DocFriendDelete = "DocFriendDelete";
         /*DeleteFriend IDs*/
         final static public String friendDeleteDisplayNameLabel = "friendDeleteDisplayNameLabel";
         final static public String friendDeleteAddButton = "friendDeleteDeleteButton";
 
         /*FriendList Page*/
         final static public String DocFriendList = "DocFriendList";
         /*FriendList IDs*/
         final static public String FriendListList = "FriendListList";
 
         /*FriendList Page*/
         final static public String DocGenericButton = "DocGenericButton";
         /*FriendList IDs*/
         final static public String GenericButtonLink = "GenericButtonLink";
         final static public String GenericButtonText = "GenericButtonText";
         final static public String GenericButtonImage = "GenericButtonImage";
         final static public String GenericButtonWidth = "GenericButtonWidth";
 
         /*Profile Page*/
         final static public String DocProfile = "DocProfile";
         /*Profile IDs*/
         final static public String ProfilePhotoChange = "ProfilePhotoChange";
 
         /*I Page*/
         final static public String DocI = "DocI";
 
         /*Photos Page*/
         final static public String DocPhotos = "DocPhotos";
 
         /*Actiavte Page*/
         final static public String DocActivate = "DocActivate";
 
         /*Share Page*/
         final static public String DocShare = "DocShare";
 
         /*GeoBusiness Page*/
         final static public String DocGeoBusiness = "DocGeoBusiness";
 
         /*GeoBusiness Page*/
         final static public String DocLegal = "DocLegal";
 
         /*Muster Page*/
         final static public String DocMuster = "DocMuster";
 
         /*Help Page*/
         final static public String DocHelpPage = "DocHelpPage";
 
         /*TemplateGeneric Pages*/
         final static public String DocPublic = "DocPublic";
 
         /*Organize Page*/
         final static public String DocOrganize = "DocOrganize";
         /*Organize Attributes*/
         final static public String DocOrganizeCategory = "category";
         final static public int DocOrganizeModeOrganize = 0;
         final static public String DocOrganizeLocation = "location";
         final static public int DocOrganizeModeLocation = 1;
         final static public String DocOrganizeEvent = "event";
         final static public int DocOrganizeModePrivateLocation = 2;
         final static public String DocOrganizeAlbum = "album";
         final static public int DocOrganizeModeEvent = 3;
 
         /*FriendFind Page*/
         final static public String DocFriends = "DocFriends";
 
         /*Book Page*/
         final static public String DocBook = "DocBook";
 
         /*Tribes Page*/
         final static public String DocTribes = "DocTribes";
         /*Organize Attributes*/
         final static public String DocTribesMode = "mode";
         final static public int DocTribesModeCreate = 0;
         final static public int DocTribesModeView = 1;
         final static public String DocTribesWhich = "which";
 
 
         /*Skeleton Page*/
         final static public String DocSkeleton = "DocPrivateLocationCreate";
         /*Skeleton Create IDs*/
         final static public String skeletonTitle = "skeletonTitle";
         final static public String Skeleton_login_widget = "Skeleton_login_widget";
         final static public String Skeleton_othersidebar = "Skeleton_othersidebar";
         final static public String Skeleton_profile_photo = "Skeleton_profile_photo";
         final static public String Skeleton_othersidebar_identity = "Skeleton_othersidebar_identity";
         final static public String Skeleton_othersidebar_places_link = "Skeleton_othersidebar_places_link";
         final static public String Skeleton_othersidebar_profile_link = "Skeleton_othersidebar_profile_link";
         final static public String Skeleton_othersidebar_photo_manager_link = "Skeleton_othersidebar_photo_manager_link";
         final static public String Skeleton_othersidebar_organizer_link = "Skeleton_othersidebar_organizer_link";
         final static public String Skeleton_othersidebar_upload_file_sh = "Skeleton_othersidebar_upload_file_sh";
         final static public String Skeleton_file_list = "Skeleton_file_list";
         final static public String Skeleton_center = "Skeleton_center";
         final static public String Skeleton_center_skeleton = "Skeleton_center_skeleton";
         final static public String Skeleton_notice_sh = "Skeleton_notice_sh";
         final static public String Skeleton_notice = "Skeleton_notice";
         final static public String Skeleton_center_content = "Skeleton_center_content";
         final static public String Skeleton_left_column = "Skeleton_left_column";
         final static public String Skeleton_right_column = "Skeleton_right_column";
         final static public String Skeleton_sidebar = "Skeleton_sidebar";
         final static public String Skeleton_notifications = "Skeleton_notifications";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationView = "PrivateLocationView";
         /*Private Location Create IDs*/
         final static public String privateLocationViewNotice = "privateLocationViewNotice";
         final static public String privateLocationViewName = "privateLocationViewName";
         final static public String privateLocationViewInfo = "privateLocationViewInfo";
         final static public String privateLocationViewLocationMap = "privateLocationViewLocationMap";
         final static public String privateLocationViewOwners = "privateLocationViewOwners";
         final static public String privateLocationViewVisitors = "privateLocationViewVisitor";
         final static public String privateLocationViewLink = "privateLocationViewLink";
         final static public String privateLocationViewEventList = "privateLocationViewEventList";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationCreate = "PrivateLocationCreate";
         /*Private Location Create IDs*/
         final static public String privateLocationCreateName = "privateLocationCreateName";
         final static public String privateLocationCreateInfo = "privateLocationCreateInfo";
         final static public String privateLocationCreateWOEID = "privateLocationCreateWOEID";
         final static public String privateLocationCreateWOEIDGrabber = "privateLocationCreateWOEIDGrabber";
         final static public String privateLocationCreateSave = "privateLocationCreateSave";
         final static public String privateLocationDeleteOwners = "privateLocationDeleteOwners";
         final static public String privateLocationDeleteVisitors = "privateLocationDeleteVisitors";
         final static public String privateLocationDeleteEventList = "privateLocationDeleteEventList";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationDelete = "PrivateLocationDelete";
         /*Private Location Create IDs*/
         final static public String privateLocationDeleteName = "privateLocationDeleteName";
         final static public String privateLocationDeleteInfo = "privateLocationDeleteInfo";
         final static public String privateLocationDeleteLocationMap = "privateLocationDeleteLocationMap";
         final static public String privateLocationDeleteNotice = "privateLocationDeleteNotice";
         final static public String privateLocationDelete = "privateLocationDelete";
 
         /*Photo Descrition Specific IDs*/
         final static public String pd = "pd";
         final static public String close = "close";
         final static public String pd_photo_permalink = "pd_photo_permalink";
         final static public String pd_photo = "pd_photo";
         final static public String pd_photo_description = "pd_photo_description";
         final static public String pd_photo_delete = "pd_photo_delete";
         final static public String pd_photo_wall = "pd_photo_wall";
         final static public String pd_photo_sequence_number = "pd_photo_sequence_number";
 
 
         /*Aarrr Page*/
         final static public String DocAarrr = "Aarrr";
         /*Aarrr Specific IDs*/
         final static public String AarrrDownTownHeatMap = "AarrrDownTownHeatMap";
         final static public String AarrrJuice = "AarrrJuice";
         final static public String AarrrWOEID = "AarrrWOEID";
         final static public String AarrrHeader = "AarrrHeader";
 
 
         /*DocLocation Page*/
         final static public String DocLocation = "DocLocation";
 
         /*Main Specific IDs*/
         final static public String body = "body";
         final static public String mainTitle = "mainTitle";
         final static public String mainMetaDesc = "mainMetaDesc";
         final static public String Main_ICBM = "Main_ICBM";
         final static public String Main_geoposition = "Main_geoposition";
         final static public String Main_geoplacename = "Main_geoplacename";
         final static public String Main_georegion = "Main_georegion";
         final static public String Main_othersidebar_identity = "Main_othersidebar_identity";
         final static public String Main_location_photo = "Main_location_photo";
         final static public String Main_profile_photo = "Main_profile_photo";
         final static public String Main_othersidebar_profile_link = "Main_othersidebar_profile_link";
         final static public String Main_othersidebar_upload_file_sh = "Main_othersidebar_upload_file_sh";
         final static public String Main_loading_hotels_link = "Main_loading_hotels_link";
         final static public String Main_hotels_link = "Main_hotels_link";
         final static public String Main_home_page_link = "Main_home_page_link";
         final static public String Main_flight_page_link = "Main_flight_page_link";
         final static public String Main_car_page_link = "Main_car_page_link";
         final static public String Main_cruise_page_link = "Main_cruise_page_link";
 
         final static public String Main_center_main = "Main_center_main";
         final static public String Main_notice = "Main_notice";
         final static public String Main_notice_sh = "Main_notice_sh";
         final static public String Main_center_main_location_title = "Main_center_main_location_title";
         final static public String Main_center_content = "Main_center_content";
         final static public String Main_yox = "Main_yox";
         final static public String Main_left_column = "Main_left_column";
         final static public String Main_right_column = "Main_right_column";
         final static public String Main_sidebar = "Main_sidebar";
         final static public String Main_login_widget = "Main_login_widget";
         final static public String Main_location_backlink = "Main_location_backlink";
         final static public String Main_location_list_header = "Main_location_list_header";
         final static public String Main_location_list = "Main_location_list";
         final static public String Main_flickr = "Main_flickr";
         final static public String Main_disqus_thread_data = "Main_disqus_thread_data";
 
         /*PhotoCRUD Specific IDs*/
         final static public String pc_photo_title = "pc_photo_title";
         final static public String pc_close = "pc_close";
         final static public String pc = "pc";
         final static public String pc_photo = "pc_photo";
         final static public String pc_photo_permalink = "pc_photo_permalink";
         final static public String pc_photo_name = "pc_photo_name";
         final static public String pc_update_name = "pc_update_name";
         final static public String pc_photo_description = "pc_photo_description";
         final static public String pc_delete = "pc_delete";
         final static public String pc_update_description = "pc_update_description";
 
         /*SignInOn Page*/
         final static public String DocSignInOn = "SignInOn";
 
         /*Notification Page*/
         final static public String DocNotification = "DocNotification";
         /*Notification Specific IDs*/
         final static public String notification_simple = "notification_simple";
 
         /*DocDownTownFlow Page*/
         final static public String DocDownTownFlow = "DocDownTownFlow";
         /*DocDownTownFlow Specific IDs*/
         final static public String DownTownFlowWidget = "DownTownFlowWidget";
         final static public String DownTownFlowTalks = "DownTownFlowTalks";
         final static public String DownTownFlowMoments = "DownTownFlowMoments";
         final static public String DownTownFlowMomentsMoments = "DownTownFlowMomentsMoments";
         final static public String DownTownFlowTribes = "DownTownFlowTribes";
         final static public String DownTownFlowTribesTribes = "DownTownFlowTribesTribes";
         final static public String DownTownFlowTalksFriends = "DownTownFlowTalksFriends";
         final static public String DownTownFlowInviteNoti = "DownTownFlowInviteNoti";
         final static public String DownTownFlowInviteEmail = "DownTownFlowInviteEmail";
         final static public String DownTownFlowInviteClick = "DownTownFlowInviteClick";
 
         /*TribeHome Page*/
         final static public String DocTribeHome = "DocTribeHome";
 
         /*Juice Page*/
         final static public String DocJuice = "DocJuice";
 
         /*AdaptableSignup Page*/
         final static public String DocAdaptableSignup = "DocAdaptableSignup";
 
         /*TribeCreateHome Page*/
         final static public String DocTribeCreateHome = "DocTribeCreateHome";
         /*TribeCreateHome Specific IDs*/
         final static public String tribeHomeCreateWidget = "tribeHomeCreateWidget";
         final static public String tribeHomeCreateName = "tribeHomeCreateName";
         final static public String tribeHomeCreateStory = "tribeHomeCreateStory";
         final static public String tribeHomeCreateSave = "tribeHomeCreateSave";
         final static public String tribeHomeCreateNoti = "tribeHomeCreateNoti";
 
 
         /*Common IDs that should be present in any page*/
         final static public String CPageTitle = "PageTitle";
         final static public String CPageIntro = "PageIntro";
         final static public String CPageNotice = "PageNotice";
        final static public String CPageType = "pageType";
 
 
         final static public String SkeletonCPageTitle = CPageTitle;
         final static public String MainCPageTitle = CPageTitle;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocaionCreateCTitle = "PrivateLocationCreateTitle";
 
         final static public String SkeletonCPageIntro = CPageIntro;
         final static public String MainCPageIntro = CPageIntro;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocaionCreateCIntro = "PrivateLocationCreateIntro";
 
         final static public String SkeletonCPageNotice = CPageNotice;
         final static public String MainCPageNotice = CPageNotice;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocationCreateCNotice = "PrivateLocationCreateNotice";
 
         final static public String Skeleton_pageType = CPageType;
         final static public String AARRR_pageType = CPageType;
        final static public String Main_pageType = CPageType;
 
 /*End of common definitions*/
 
 
         private Page(final String path__, final Object... ids__) {
             try {
                 Loggers.DEBUG.debug("HELLO, ENUM VAL:" + this.name());
                 Loggers.DEBUG.debug("HELLO, ENUM PATH:" + path__);
                 Loggers.DEBUG.debug("HELLO, ENUM IDS:" + Arrays.toString(ids__));
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO PRETTYURL MAP");
                 PrettyURLMap_.put(this, path__);
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO ALLPAGEELEMENTS");
                 PutAllPageElementIds(ids__);
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO ALLPAGEELEMENTS BY PAGE");
                 PutAllPageElementIdsByPage(this, ids__);
             } catch (
                     @WARNING(warning = "Don't remove this catch. You'll not see any duplicate id exceptions etc. anywhere if you do so.")
                     final Exception e) {
                 Loggers.EXCEPTION.error("SORRY! SOMETHING WENT WRONG DURING INITIALIZATION. THIS SHOULD EXPECTED TO BE FATAL.", e);
             }
         }
 
         abstract public String toString();
 
         abstract public String getURL();
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Servlet ---------------------
 
     /**
      * @param serveletConfig__
      * @throws ServletException
      */
     @Override
     public void init(final ServletConfig serveletConfig__) throws ServletException {
         super.init(serveletConfig__);
 
         final ItsNatHttpServlet inhs__ = getItsNatHttpServlet();
 
         final ItsNatServletConfig itsNatServletConfig = inhs__.getItsNatServletConfig();
 
         itsNatServletConfig.setDebugMode(false);
         itsNatServletConfig.setClientErrorMode(ClientErrorMode.NOT_SHOW_ERRORS);
         itsNatServletConfig.setLoadScriptInline(true);
         itsNatServletConfig.setUseGZip(UseGZip.MARKUP);
         itsNatServletConfig.setCommMode(CommMode.XHR_ASYNC_HOLD);
         itsNatServletConfig.setAutoCleanEventListeners(true);
         itsNatServletConfig.setDefaultEncoding("UTF-8");
         itsNatServletConfig.setReferrerEnabled(true);
 
         setDefaultLocale:
         {
             Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0001"));
             Locale.setDefault(new Locale("en", "US"));
             Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0002"), Locale.getDefault().toString());
         }
 
         Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0003"), Locale.getDefault().toString());
 
 
         /*Add a listner to convert pretty urls to proper urls*/
         inhs__.addItsNatServletRequestListener(new ItsNatServletRequestListener() {
             @Override
             public void processRequest(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
                 final ItsNatDocument itsNatDocument__ = request__.getItsNatDocument();
                 /*if(itsNatDocument != null && ((HttpServletRequest) request__.getServletRequest()).getPathInfo().contains("itsnat_doc_name")){
                 throw new java.lang.RuntimeException("INVALID URL");//This code does not seem to work, please verify.
                 }*/
                 if (itsNatDocument__ == null && request__.getServletRequest().getAttribute(ITSNAT_DOC_NAME) == null) {
                     final HttpServletRequest httpServletRequest = (HttpServletRequest) request__.getServletRequest();
                     pathResolver(request__, response__);
                     parameterResolver(request__);
                     request__.getItsNatServlet().processRequest(httpServletRequest, response__.getServletResponse());
                 }
             }
         });
 
         final String realPath__ = getServletContext().getRealPath("/");
         REAL_PATH = realPath__;
         /*final String pathPrefix__ = realPath__ + "WEB-INF/pages/";*/
 
         final String pathPrefix__ = RBGet.getGlobalConfigKey("PAGEFILES") != null ? RBGet.getGlobalConfigKey("PAGEFILES") : realPath__ + WEB_INF_PAGES;
 
         registerDocumentTemplates:
         {
             inhs__.registerItsNatDocumentTemplate(aarrr.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(aarrr)).addItsNatServletRequestListener(new ListenerAarrr());
 
             inhs__.registerItsNatDocumentTemplate(locationMain.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(locationMain)).addItsNatServletRequestListener(new ListenerMain());
 
             inhs__.registerItsNatDocumentTemplate(photos.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerPhoto());
 
             inhs__.registerItsNatDocumentTemplate(organize.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerOrganize());
 
             inhs__.registerItsNatDocumentTemplate(findFriend.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerFriends());
 
             inhs__.registerItsNatDocumentTemplate(book.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerBookings());
 
             inhs__.registerItsNatDocumentTemplate(tribes.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerTribes());
 
             inhs__.registerItsNatDocumentTemplate(profile.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerProfile());
 
             inhs__.registerItsNatDocumentTemplate(i.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerI());
 
             inhs__.registerItsNatDocumentTemplate(activate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerActivate());
 
             inhs__.registerItsNatDocumentTemplate(share.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerShare());
 
             inhs__.registerItsNatDocumentTemplate(legal.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerLegal());
 
             inhs__.registerItsNatDocumentTemplate(muster.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerMuster());
 
             inhs__.registerItsNatDocumentTemplate(helpPage.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerHelp());
 
             inhs__.registerItsNatDocumentTemplate(geoBusiness.toString(), "text/html", new TemplateSourceGeoBusiness()).addItsNatServletRequestListener(new ListenerGeoBusiness());
 
             inhs__.registerItsNatDocumentTemplate(templateGeneric.toString(), "text/html", new TemplateGeneric(true));
         }
 
         registerDocumentFragmentTemplatesAKAWidgets:
         {
 //            inhs__.registerItsNatDocFragmentTemplate(photoCRUD.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(photoCRUD));
 
             inhs__.registerItsNatDocFragmentTemplate(photo$Description.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(photo$Description));
 
             inhs__.registerItsNatDocFragmentTemplate(signInOn.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(signInOn));
 
             inhs__.registerItsNatDocFragmentTemplate(notification.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(notification));
 
             inhs__.registerItsNatDocFragmentTemplate(downTownFlow.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(downTownFlow));
 
             inhs__.registerItsNatDocFragmentTemplate(tribeHome.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(tribeHome));
 
             inhs__.registerItsNatDocFragmentTemplate(tribeCreateHome.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(tribeCreateHome));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationCreate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationCreate));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationView.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationView));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventCreate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventCreate));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventView.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventView));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventViewSidebar.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventViewSidebar));
 
             inhs__.registerItsNatDocFragmentTemplate(tribeSidebar.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(tribeSidebar));
 
             inhs__.registerItsNatDocFragmentTemplate(wOIEDGrabber.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(wOIEDGrabber));
 
             inhs__.registerItsNatDocFragmentTemplate(downTownHeatMap.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(downTownHeatMap));
 
             inhs__.registerItsNatDocFragmentTemplate(findFriendWidget.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(findFriendWidget));
 
             inhs__.registerItsNatDocFragmentTemplate(friendAdd.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendAdd));
 
             inhs__.registerItsNatDocFragmentTemplate(friendDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(friendList.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendList));
 
             inhs__.registerItsNatDocFragmentTemplate(genericButton.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(genericButton));
 
             inhs__.registerItsNatDocFragmentTemplate(displayName.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(displayName));
 
             inhs__.registerItsNatDocFragmentTemplate(wallHanlder.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(wallHanlder));
 
             inhs__.registerItsNatDocFragmentTemplate(passwordChange.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(passwordChange));
 
             inhs__.registerItsNatDocFragmentTemplate(forgotPasswordChange.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(forgotPasswordChange));
 
             inhs__.registerItsNatDocFragmentTemplate(profileWidget.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(profileWidget));
 
             inhs__.registerItsNatDocFragmentTemplate(teachTribe.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(teachTribe));
 
             inhs__.registerItsNatDocFragmentTemplate(teachMoment.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(teachMoment));
 
             inhs__.registerItsNatDocFragmentTemplate(album.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(album));
 
             inhs__.registerItsNatDocFragmentTemplate(carousel.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(carousel));
 
             inhs__.registerItsNatDocFragmentTemplate(carouselThumb.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(carouselThumb));
 
             inhs__.registerItsNatDocFragmentTemplate(people.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(people));
 
             inhs__.registerItsNatDocFragmentTemplate(peopleThumb.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(peopleThumb));
 
             inhs__.registerItsNatDocFragmentTemplate(autoplayControls.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(autoplayControls));
 
             inhs__.registerItsNatDocFragmentTemplate(albumTribe.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(albumTribe));
 
             inhs__.registerItsNatDocFragmentTemplate(userProperty.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(userProperty));
 
             inhs__.registerItsNatDocFragmentTemplate(userPropertySidebar.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(userPropertySidebar));
 
             inhs__.registerItsNatDocFragmentTemplate(termsOfServices.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(termsOfServices));
 
             inhs__.registerItsNatDocFragmentTemplate(privacyPolicy.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privacyPolicy));
 
             inhs__.registerItsNatDocFragmentTemplate(bate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(bate));
 
             inhs__.registerItsNatDocFragmentTemplate(help.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(help));
 
             inhs__.registerItsNatDocFragmentTemplate(juice.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(juice));
 
             inhs__.registerItsNatDocFragmentTemplate(adaptableSignup.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(adaptableSignup));
         }
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
 
 // -------------------------- STATIC METHODS --------------------------
 
     private static void PutAllPageElementIdsByPage(final PageFace page__, final Object... ids__) {
         final HashSet<String> ids_ = new HashSet<String>();
 
         for (final Object id__ : ids__) {
             ids_.add(id__.toString());
         }
 
         GlobalPageIdRegistry.put(page__, ids_);
     }
 
     /**
      * Some nested widgets have no access to the request. instead of passing the request along the widget chain,
      * we store it in the users session.
      * <p/>
      * While this implementation may consume a bit more memory(could be an issue), it allows the widget to set the
      * value to null on a per request basis.
      *
      * @param request__
      */
     private static void parameterResolver(final ItsNatServletRequest request__) {
         wallEntryRelated:
         {
             final String wall_entry = request__.getServletRequest().getParameter(WallWidget.PARAM_WALL_ENTRY);
             if (wall_entry != null) {
                 request__.getItsNatSession().setAttribute(WallWidget.PARAM_WALL_ENTRY, wall_entry);
             }
         }
 
         others:
         {
         }
     }
 
     /**
      * We have a URL of type say www.ilikeplaces.com/page/Egypt
      * where we are supposed to receive the /Egypt part. Most requests WILL be
      * <p/>
      * location requests so we go optimistic on that first.
      *
      * @param request__
      * @param response__
      */
     private static void pathResolver(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
         final String pathInfo = ((HttpServletRequest) request__.getServletRequest()).getPathInfo();
         String URL__ = pathInfo == null ? "" : ((HttpServletRequest) request__.getServletRequest()).getPathInfo().substring(1);//Removes preceding slash
 
         URL__ = URL__.split("\\?")[0].split("#")[0];
 
         if (isHomePage(URL__)) {
             Loggers.DEBUG.debug(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0012"));
             Loggers.DEBUG.debug(((HttpServletRequest) request__.getServletRequest()).getRequestURL().toString()
                     + (((HttpServletRequest) request__.getServletRequest()).getQueryString() != null
                     ? ((HttpServletRequest) request__.getServletRequest()).getQueryString()
                     : ""));
             request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
 
 //            final ItsNatHttpSession itsNatHttpSession = (ItsNatHttpSession) request__.getItsNatSession();
 //            final Object attribute__ = itsNatHttpSession.getAttribute(ServletLogin.HumanUser);
 //            final SessionBoundBadRefWrapper<HumanUserLocal> sessionBoundBadRefWrapper = attribute__ == null ? null : (SessionBoundBadRefWrapper<HumanUserLocal>) attribute__;
 //
 //            if (sessionBoundBadRefWrapper != null && sessionBoundBadRefWrapper.boundInstance.getHumanUserId() != null) {
 //                try {
 //                    ((HttpServletResponse) response__.getServletResponse()).sendRedirect(LOCATION_HUB);
 //                } catch (final IOException e) {
 //                    Loggers.EXCEPTION.error("", e);
 //                }
 //            }
         } else if (isDownTownPage(URL__)) {
             response__.addCodeToSend(JSCodeToSend.redirectPageWithURL("/page/_org"));
             return;//let's be paranoid
         } else {
             if (isNonLocationPage(URL__)) {/*i.e. starts with underscore*/
                 final HttpSession httpSession = ((HttpServletRequest) request__.getServletRequest()).getSession(false);
                 if (isSignOut(URL__)) {//This can never happen, as there is a filter in place for this
                     if (httpSession != null) {
                         try {
                             ((HttpServletRequest) request__.getServletRequest()).getSession(false).invalidate();
                         } finally {
                             request__.getServletRequest().setAttribute("location", "");
                             request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
                             try {
                                 ((HttpServletResponse) response__.getServletResponse()).sendRedirect(LOCATION_HUB);
                             } catch (final IOException e) {
                                 Loggers.EXCEPTION.error(Loggers.EMBED, e);
                             }
                         }
                     }
                 } else if (isPhotoPage(URL__)) {
                     request__.getServletRequest().setAttribute(RBGet.globalConfig.getString("HttpSessionAttr.location"), getPhotoLocation(URL__));
                     request__.getServletRequest().setAttribute("photoURL", getPhotoURL(URL__));
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, "photo");/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0005") + getPhotoLocation(URL__));
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0006") + getPhotoURL(URL__));
                 } else if (isHumanPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocPhotos);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0007"));
                 } else if (isOrganizePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocOrganize);/*Framework specific*/
                     request__.getServletRequest().setAttribute(Page.DocOrganizeCategory, request__.getServletRequest().getParameter(Page.DocOrganizeCategory));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeLocation, request__.getServletRequest().getParameter(Page.DocOrganizeLocation));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeEvent, request__.getServletRequest().getParameter(Page.DocOrganizeEvent));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeAlbum, request__.getServletRequest().getParameter(Page.DocOrganizeAlbum));
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0013"));
                 } else if (isFriendsPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocFriends);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0014"));
                 } else if (isBookingsPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocBook);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0015"));
                 } else if (isTribesPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocTribes);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0025"));
                 } else if (isProfilePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocProfile);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0016"));
                 } else if (isIPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocI);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0017"));
                 } else if (isActivatePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocActivate);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0018"));
                 } else if (isSharePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocShare);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0019"));
                 } else if (isGeoBusinessPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocGeoBusiness);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0020"));
                 } else if (isTemplateGenericPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocPublic);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0021"));
                 } else if (isLegalPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocLegal);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0022"));
                 } else if (isMusterPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocMuster);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0023"));
                 } else if (isHelpPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocHelpPage);/*Framework specific*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0024"));
                 } else {/*Divert to home page*/
                     Loggers.DEBUG.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0008"));
                     request__.getServletRequest().setAttribute("location", "");
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
                 }
             } else if (isCorrectLocationFormat(URL__)) {
                 request__.getServletRequest().setAttribute("location", URL__);
                 request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocLocation);/*Framework specific*/
                 Loggers.DEBUG.debug(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0009") + URL__);
             } else {/*Divert to home page*/
                 Loggers.DEBUG.debug(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0026"));
                 request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
                 request__.getServletRequest().setAttribute("location", "");
                 try {
                     ((HttpServletResponse) response__.getServletResponse()).sendError(HttpServletResponse.SC_NOT_FOUND);
                 } catch (final IOException e) {
                     Loggers.EXCEPTION.error(Loggers.EMBED, e);
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     /**
      * Check if the place contains any odd/special characters that are not valid
      * Also check if the url is of itsnat?docblablabla format with "?"
      * Home page will not have any parameters. e.g. ilikeplaces.com/ilikeplaces/page/[nothing here]
      *
      * @param URL__
      * @return boolean
      */
     static private boolean isHomePage(final String URL__) {
         return URL__.length() == 0;
     }
 
     static private boolean isDownTownPage(final String URL_) {
         return (URL_.startsWith(DT) || URL_.startsWith(DOWNTOWN));
     }
 
     static private boolean isNonLocationPage(final String URL_) {
         return (URL_.startsWith(UNDERSCORE) || URL_.startsWith(HASH));
     }
 
     static private boolean isSignOut(final String URL_) {
         return (URL_.equals(_SO));
     }
 
     static private boolean isPhotoPage(final String URL_) {
         return (URL_.startsWith(_PHOTO_) && URL_.split(UNDERSCORE).length == 4);
     }
 
     static private String getPhotoLocation(final String URL_) {
         return URL_.replace(_PHOTO_, EMPTY).split(UNDERSCORE)[0];
     }
 
     static private String getPhotoURL(final String URL_) {
         return URL_.replace(_PHOTO_, EMPTY).split(UNDERSCORE)[1];
     }
 
     static private boolean isHumanPage(final String URL_) {
         return (URL_.startsWith(_ME));
     }
 
     static private boolean isOrganizePage(final String URL_) {
         return (URL_.startsWith(_ORG));
     }
 
     static private boolean isFriendsPage(final String URL_) {
         return (URL_.startsWith(_FRIENDS));
     }
 
     static private boolean isBookingsPage(final String URL_) {
         return (URL_.startsWith(_BOOK));
     }
 
     static private boolean isTribesPage(final String URL_) {
         return (URL_.startsWith(_TRIBES));
     }
 
     static private boolean isProfilePage(final String URL_) {
         return (URL_.startsWith(_PROFILE));
     }
 
     static private boolean isIPage(final String URL_) {
         return (URL_.startsWith(_I));
     }
 
     static private boolean isActivatePage(final String URL_) {
         return (URL_.startsWith(_ACTIVATE));
     }
 
     static private boolean isSharePage(final String URL_) {
         return (URL_.startsWith(_SHARE));
     }
 
     static private boolean isGeoBusinessPage(final String URL_) {
         return (URL_.startsWith(_GEOBUSINESS));
     }
 
     static private boolean isTemplateGenericPage(final String URL_) {
         return (URL_.startsWith(_PUBLIC));
     }
 
     static private boolean isLegalPage(final String URL_) {
         return (URL_.startsWith(_LEGAL));
     }
 
     static private boolean isMusterPage(final String URL_) {
         return (URL_.startsWith(_MUSTER));
     }
 
     static private boolean isHelpPage(final String URL_) {
         return (URL_.startsWith(_HELP));
     }
 
     /**
      * Check if the place contains any odd/special characters that are not valid
      * Also check if the url is of itsnat?docblablabla format with "?"
      *
      * @param URL__
      * @return boolean
      */
     @FIXME(issue = "Location should not contain underscores")
     static private boolean isCorrectLocationFormat(final String URL__) {
         /*"_" (underscore) check first is vital as the photo and me urls might have "/"*/
         return !(URL__.startsWith("_") || URL__.contains(","));
     }
 
     /**
      * Register all your document keys before using. Accepts variable argument length.
      * Usage: putAllPageElementIds("id1","id2","id3");
      *
      * @param ids__
      */
     public final static void PutAllPageElementIds(final Object... ids__) {
         for (final Object id_ : ids__) {
             if (!GlobalHTMLIdRegistry.containsKey(id_.toString())) {
                 /**
                  * Do not verify if element exists in "document" here as elements
                  * can be dynamically created
                  */
                 GlobalHTMLIdRegistry.put(id_.toString(), id_.toString());
             } else {
                 throw new SecurityException("SORRY! THIS KEY IS ALREADY IN REGISTRY:" + id_);
             }
         }
     }
 }
