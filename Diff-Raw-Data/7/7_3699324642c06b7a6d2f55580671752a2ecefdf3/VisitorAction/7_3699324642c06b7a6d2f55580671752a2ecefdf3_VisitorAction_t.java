 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.screen.iface.jsf.session;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import zeroth.actor.service.app.actor.MemberApplication;
 import zeroth.actor.service.domain.ActorFilterFactory;
 import zeroth.actor.service.domain.Member;
 import zeroth.framework.screen.iface.jsf.FacesHelper;
 /**
  * Visitor controller.
  * @author nilcy
  */
 @Named("visitorAction")
 @SessionScoped
 // @ServletSecurity(value = @HttpConstraint(rolesAllowed = { SPONSOR, DIRECTOR,
 // CREATOR, EDITOR,
 // AUDITOR, READER }))
 public class VisitorAction implements Serializable {
     /** 製品番号 */
     private static final long serialVersionUID = -6272066593466089850L;
     /** member. */
     private Member member;
     /** logged in date. */
     private Date loggedInDate;
     /** member service Local-I/F. */
     @EJB
     private MemberApplication memberApplication;
     /** conversation. */
     @Inject
     private Conversation conversation;
     /** ロガー */
    // @Inject
    // private Logger log;
    private static Logger log = Logger.getGlobal();
     /** コンストラクタ */
     public VisitorAction() {
         super();
     }
     /**
      * Determine if logged-in.
      * @return true if logged-in
      */
    @SuppressWarnings("static-method")
     public boolean isLoggedIn() {
         log.info("getUserPrincipal = " + FacesHelper.getExternalContext().getUserPrincipal());
         return FacesHelper.getExternalContext().getUserPrincipal() != null;
     }
     /**
      * {@link #member}.
      * @return {@link #member}
      */
     public Member getMember() {
         if ((member == null) && isLoggedIn()) {
             final String account = FacesHelper.getExternalContext().getRemoteUser();
             log.info("account = " + account);
             final Collection<Member> members = memberApplication.findMany(ActorFilterFactory
                 .createMemberFilter(account));
             member = members.iterator().next();
             loggedInDate = new Date();
             // InfraHelper.addSuccessBundleMessage("LoggedIn");
             // InfraHelper.keepMessage();
         } else if (!isLoggedIn()) {
             member = null;
             loggedInDate = null;
         }
         return member;
     }
     /**
      * {@link #loggedInDate}.
      * @return {@link #loggedInDate}
      */
     public Date getLoggedInDate() {
         return loggedInDate;
     }
     /**
      * Determine if user in role.
      * @param aRole role
      * @return true if user in role
      */
     @SuppressWarnings("static-method")
     public boolean hasRole(final String aRole) {
         return FacesHelper.getExternalContext().isUserInRole(aRole);
     }
     /**
      * stack-trace.
      * @return stack-trace
      */
     @SuppressWarnings("static-method")
     public String getStackTrace() {
         final FacesContext context = FacesContext.getCurrentInstance();
         final Map<String, Object> map = context.getExternalContext().getRequestMap();
         final Throwable throwable = (Throwable) map.get("javax.servlet.error.exception");
         final StringBuilder builder = new StringBuilder();
         final String lf = "<br/>";
         builder.append(throwable.getMessage()).append(lf);
         for (final StackTraceElement element : throwable.getStackTrace()) {
             builder.append(element).append(lf);
         }
         return builder.toString();
     }
     /**
      * {@link #conversation}.
      * @return {@link #conversation}
      */
     public Conversation getConversation() {
         return conversation;
     }
 }
