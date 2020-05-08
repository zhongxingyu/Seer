 package at.ac.tuwien.swag.webapp.in;
 
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
import at.ac.tuwien.swag.webapp.SwagWebSession;

 @AuthorizeInstantiation({ Roles.ADMIN, Roles.USER })
 public abstract class InPage extends WebPage {
 	private static final long serialVersionUID = 7723393016017265570L;
 	
 	public InPage(PageParameters parameters) {
 		add(new InTopNavigation("topNavigation"));
 		add(new InNavigation("mainNavigation"));

		SwagWebSession session = (SwagWebSession) getSession();

		if ( session.getRoles().hasRole( Roles.ADMIN ) ) {
			add(new InSideNavigation("sideNavigation"));			
		} else {
			add( new Label( "sideNavigation", "" ) );
		}
 	}
 }
