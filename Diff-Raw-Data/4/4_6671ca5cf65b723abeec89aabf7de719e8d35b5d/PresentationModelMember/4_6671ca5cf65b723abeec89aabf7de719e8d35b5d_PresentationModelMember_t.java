 package ch.raffael.util.binding;
 
 /**
  * Common base class for presentation model members. This is actually used for the fluent
  * API only, for tracking the presentation model. You may choose not to extend this class,
  * however, you'll then have to find your own solutions for realising a fluent API. ;)
  *
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class PresentationModelMember {
 
     PresentationModel presentationModel;
 
     protected <T extends PresentationModelMember> T add(T member) {
         member.presentationModel = presentationModel;
        if ( presentationModel != null ) {
            presentationModel.add(member);
        }
         return member;
     }
 
 }
