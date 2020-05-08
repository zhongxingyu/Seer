 package org.mm;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.Action;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Names;
 import org.mm.action.LoginAction;
 
 /**
  * User: Mustafa Motiwala
  * Date: May 18, 2010
  * Time: 6:11:04 PM
  */
 public class ApplicationModule extends AbstractModule {
     @Override
     protected void configure() {
         bind(Action.class).annotatedWith(Names.named("loginAction")).to(LoginAction.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<List<ApplicationTab>>() {}).to(new TypeLiteral<ArrayList<ApplicationTab>>(){}).in(Scopes.SINGLETON);
     }
 }
