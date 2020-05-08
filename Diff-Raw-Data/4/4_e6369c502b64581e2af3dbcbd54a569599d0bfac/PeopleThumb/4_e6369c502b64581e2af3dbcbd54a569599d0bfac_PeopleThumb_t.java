 package ai.ilikeplaces.logic.Listeners.widgets.people;
 
 import ai.ilikeplaces.logic.Listeners.widgets.UserProperty;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.AbstractWidgetListener;
 import ai.ilikeplaces.util.MarkupTag;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.Element;
 import org.w3c.dom.html.HTMLDocument;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ravindranath Akila
  * Date: 1/1/12
  * Time: 12:22 PM
  */
 public class PeopleThumb extends AbstractWidgetListener<PeopleThumbCriteria> {
 
     private static final String PROFILE_PHOTOS = "PROFILE_PHOTOS";
 
 
     public static enum PeopleThumbIds implements WidgetIds {
         PeopleThumbImage
     }
 
     /**
      * @param request__
      * @param appendToElement__
      */
     public PeopleThumb(final ItsNatServletRequest request__, final PeopleThumbCriteria peopleThumbCriteria, final Element appendToElement__) {
         super(request__, Controller.Page.PeopleThumb, peopleThumbCriteria, appendToElement__);
     }
 
 
     /**
      * @param peopleThumbCriteria
      */
     @Override
     protected void init(final PeopleThumbCriteria peopleThumbCriteria) {
         final String profilePhotoURLPath = UserProperty.formatProfilePhotoUrl(peopleThumbCriteria.getProfilePhoto());
        $$(PeopleThumbIds.PeopleThumbImage).setAttribute(MarkupTag.IMG.title(), profilePhotoURLPath);
     }
 
     /**
      * Use ItsNatHTMLDocument variable stored in the AbstractListener class
      * Do not call this method anywhere, just implement it, as it will be
      * automatically called by the constructor
      *
      * @param itsNatHTMLDocument_
      * @param hTMLDocument_
      */
     @Override
     protected void registerEventListeners(ItsNatHTMLDocument itsNatHTMLDocument_, HTMLDocument hTMLDocument_) {
     }
 
 }
