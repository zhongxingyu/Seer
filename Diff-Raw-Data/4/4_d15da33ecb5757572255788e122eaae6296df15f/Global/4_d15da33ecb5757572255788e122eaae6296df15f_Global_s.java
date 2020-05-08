 import java.lang.reflect.Method;
 import java.util.Arrays;
 import models.SecurityRole;
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.PlayAuthenticate.Resolver;
 import com.feth.play.module.pa.exceptions.AccessDeniedException;
 import com.feth.play.module.pa.exceptions.AuthException;
 import controllers.routes;
 import models.SessionToken;
 import play.Application;
 import play.GlobalSettings;
 import play.mvc.Action;
 import play.mvc.Call;
 import java.net.UnknownHostException;
 import play.Logger;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
 import controllers.MorphiaObject;
 import play.mvc.Http.Request;
 import play.mvc.Result;
 
 import static play.mvc.Results.ok;
 
 public class Global extends GlobalSettings {
 
 	public void onStart(Application app) {
 
         super.beforeStart(app);
         Logger.debug("** Starting Application **");
         try {
            MorphiaObject.mongo = new Mongo("10.172.104.17", 27017);
        	//MorphiaObject.mongo = new Mongo("127.0.0.1", 27017);
 
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
         MorphiaObject.morphia = new Morphia();
         MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(MorphiaObject.mongo, "test");
         MorphiaObject.datastore.ensureIndexes();
         MorphiaObject.datastore.ensureCaps();
 
 		PlayAuthenticate.setResolver(new Resolver() {
 
 			@Override
 			public Call login() {
 				// Your login page
 				return routes.Application.index();//return routes.Templates.login();
 			}
 
 			@Override
 			public Call afterAuth() {
 				// The user will be redirected to this page after authentication
 				// if no original URL was saved
 				return routes.Application.index();
 			}
 
 			@Override
 			public Call afterLogout() {
 				return routes.Application.index();//routes.Templates.logout();
 			}
 
 			@Override
 			public Call auth(final String provider) {
 				// You can provide your own authentication implementation,
 				// however the default should be sufficient for most cases
 				return com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);
 			}
 
 			@Override
 			public Call askMerge() {
 				return routes.Account.askMerge();
 			}
 
 			@Override
 			public Call askLink() {
 				return routes.Account.askLink();
 			}
 
 			@Override
 			public Call onException(final AuthException e) {
 				if (e instanceof AccessDeniedException) {
 					return routes.Signup.oAuthDenied(((AccessDeniedException) e).getProviderKey());
 				}
 				// more custom problem handling here...
 				return super.onException(e);
 			}
 		});
 
 		initialData();
 	}
 
 	private void initialData() {
         if (SecurityRole.all(SecurityRole.class).size() == 0) {
             for (final String roleName : Arrays.asList(controllers.Application.USER_ROLE)) {
                 final SecurityRole role = new SecurityRole();
                 role.roleName = roleName;
                 role.save();
             }
         }
 	}
 
     @Override
     public Action onRequest(Request request, Method actionMethod) {
         if (request.cookie("PLAY_SESSION") == null && 
             !actionMethod.getName().equals("doLogin") && 
             !actionMethod.getName().equals("doSignup")) {
             if (request.getHeader("token") != null) {
                 return new Action.Simple() {
                     @Override
                     public Result call(play.mvc.Http.Context ctx) throws Throwable {
                         SessionToken st = SessionToken.findByToken(ctx.request().getHeader("token"));
                         if (st != null && !st.expired()) {
                             ctx.session().put(PlayAuthenticate.ORIGINAL_URL, ctx.request().uri());
                             ctx.session().put(PlayAuthenticate.USER_KEY, st.getUserId());
                             ctx.session().put(PlayAuthenticate.PROVIDER_KEY, st.getProviderId());
                             ctx.session().put(PlayAuthenticate.EXPIRES_KEY, Long.toString(st.getExpires().getTime()));
                             ctx.session().put(PlayAuthenticate.SESSION_ID_KEY, st.getToken());
                             return delegate.call(ctx);
                         }
                         return unauthorized("Unauthorized operation");
                     }
                 };
             }
             return new Action.Simple() {
                 @Override
                 public Result call(play.mvc.Http.Context ctx) throws Throwable {
                     return unauthorized("Unauthorized operation");
                 }
             };
         }
         return super.onRequest(request, actionMethod);
     }
 }
