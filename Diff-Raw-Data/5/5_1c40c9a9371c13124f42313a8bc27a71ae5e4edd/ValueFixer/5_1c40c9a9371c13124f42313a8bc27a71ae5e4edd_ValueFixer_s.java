 /*
  * Created on Nov 1, 2005
  */
 package uk.org.ponder.rsf.componentprocessor;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.BeanResolver;
 import uk.org.ponder.mapping.ShellInfo;
 import uk.org.ponder.mapping.support.ConverterConverter;
 import uk.org.ponder.mapping.support.DataConverterRegistry;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.ELReference;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.request.EarlyRequestParser;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.request.RequestSubmittedValueCache;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.state.support.ErrorStateManager;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * Fetches values from the request bean model that are referenced via EL value
  * bindings, if such have not already been set. Will also compute the fossilized
  * binding for this component (not a completely cohesively coupled set of
  * functions, but we are accumulating quite a lot of little processors).
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class ValueFixer implements ComponentProcessor {
   private BeanLocator beanlocator;
   private BeanModelAlterer alterer;
   private RequestSubmittedValueCache rsvc;
   private boolean renderfossilized;
   private DataConverterRegistry dataConverterRegistry;
   private MessageLocator messagelocator;
 
   public void setMessageLocator(MessageLocator messagelocator) {
     this.messagelocator = messagelocator;
   }
 
   public void setBeanLocator(BeanLocator beanlocator) {
     this.beanlocator = beanlocator;
   }
 
   public void setModelAlterer(BeanModelAlterer alterer) {
     this.alterer = alterer;
   }
 
   public void setRenderFossilizedForms(boolean renderfossilized) {
     this.renderfossilized = renderfossilized;
   }
 
   public void setDataConverterRegistry(
       DataConverterRegistry dataConverterRegistry) {
     this.dataConverterRegistry = dataConverterRegistry;
   }
 
   private FormModel formModel;
 
   public void setFormModel(FormModel formModel) {
     this.formModel = formModel;
   }
 
   public void setErrorStateManager(ErrorStateManager errorStateManager) {
     // this.errorStateManager = errorStateManager;
     if (errorStateManager.errorstate.rsvc != null) {
       rsvc = errorStateManager.errorstate.rsvc;
     }
     else
       rsvc = new RequestSubmittedValueCache();
   }
 
   // This dependency is here so we can free FC from instance wiring cycle on
   // RenderSystem. A slight loss of efficiency since this component may never
   // be rendered - we might think about "lazy processors" at some point...
   private FossilizedConverter fossilizedconverter;
 
   public void setFossilizedConverter(FossilizedConverter fossilizedconverter) {
     this.fossilizedconverter = fossilizedconverter;
   }
 
   public void processComponent(UIComponent toprocesso) {
     if (toprocesso instanceof UIMessage) {
       UIMessage toprocess = (UIMessage) toprocesso;
       if (toprocess.arguments != null) {
         for (int i = 0; i < toprocess.arguments.length; ++ i) {
           if (toprocess.arguments[i] instanceof ELReference) {
             ELReference elref = (ELReference) toprocess.arguments[i];
             String flatvalue = (String) alterer.getFlattenedValue(elref.value, beanlocator, String.class, null);
             toprocess.arguments[i] = flatvalue;
           }
         }
        toprocess.setValue(messagelocator.getMessage(toprocess.messagekeys,
            toprocess.arguments));
       }
     }
     if (toprocesso instanceof UIBound) {
       UIBound toprocess = (UIBound) toprocesso;
       // If there is a value in the SVE, return it to the control.
       SubmittedValueEntry sve = rsvc.byID(toprocess.getFullID());
       boolean hadcached = false;
       Object modelvalue = null;
       if (sve != null && sve.newvalue != null) {
         toprocess.updateValue(sve.newvalue);
         hadcached = true;
       }
       UIForm form = formModel.formForComponent(toprocess);
       boolean getform = form == null ? false
           : form.type.equals(EarlyRequestParser.RENDER_REQUEST);
       Object root = getform ? (Object) form.viewparams
           : beanlocator;
       if (toprocess.valuebinding != null
           && (toprocess.acquireValue() == null
               || UITypes.isPlaceholder(toprocess.acquireValue()) || hadcached)) {
         // a bound component ALWAYS contains a value of the correct type.
         Object oldvalue = toprocess.acquireValue();
         String stripbinding = toprocess.valuebinding.value;
         BeanResolver resolver = computeResolver(toprocess, root);
         Object flatvalue = null;
         try {
           flatvalue = alterer.getFlattenedValue(stripbinding, root, oldvalue
               .getClass(), resolver);
         }
         catch (Exception e) {
           // don't let a bad bean model prevent the correct reference being
           // encoded
           Logger.log.info("Error resolving EL reference " + stripbinding
               + " for component with full ID " + toprocess.getFullID(), e);
         }
         // If it was cached, we want to propagate the old "oldvalue" to the next
         // request
         if (hadcached) {
           modelvalue = sve.oldvalue;
         }
         else if (flatvalue != null) {
           modelvalue = flatvalue;
           toprocess.updateValue(flatvalue);
         }
       }
       else if (toprocess.resolver != null) {
         Object oldvalue = toprocess.acquireValue();
         // User may have directly supplied raw value + resolver, for example
         // MessageKeys. Note that this function of ValueFixer is non-idempotent.
         BeanResolver resolver = computeResolver(toprocess, root);
         Object flatvalue = alterer.getFlattenedValue(null, oldvalue, oldvalue
             .getClass(), resolver);
         if (flatvalue != null) {
           toprocess.updateValue(flatvalue);
         }
       }
       if (toprocess.submittingname == null) {
         toprocess.submittingname = toprocess.getFullID();
       }
       if (toprocess.valuebinding != null && !getform) {
         // TODO: Think carefully whether we want these "encoded" bindings to
         // EVER appear in the component tree. Tradeoffs - we would need to
         // create
         // more classes that renderer could recognise to compute bindings, and
         // increase its knowledge about the rest of RSF.
         if (toprocess.fossilize && toprocess.fossilizedbinding == null
             && renderfossilized) {
           UIParameter fossilized = fossilizedconverter
               .computeFossilizedBinding(toprocess, modelvalue);
           toprocess.fossilizedbinding = fossilized;
         }
         if (toprocess.darreshaper != null) {
           toprocess.fossilizedshaper = fossilizedconverter
               .computeReshaperBinding(toprocess);
         }
       }
       if (toprocess.acquireValue() == null) {
         throw new IllegalArgumentException(
             "Error following value fixup: null bound value found in component "
                 + toprocess + " with full ID " + toprocess.getFullID());
       }
     }
     else if (toprocesso instanceof UIVerbatim) {
       UIVerbatim toprocess = (UIVerbatim) toprocesso;
       if (toprocess.markup instanceof ELReference) {
         ELReference ref = (ELReference) toprocess.markup;
         toprocess.markup = alterer.getBeanValue(ref.value, beanlocator, null);
       }
       if (toprocess.resolver != null) {
         // User may have directly supplied raw value + resolver, for example
         // MessageKeys. Note that this function of ValueFixer is non-idempotent.
         BeanResolver resolver = computeResolver(toprocess, beanlocator);
         Object flatvalue = alterer.getFlattenedValue(null, toprocess.markup, toprocess.markup
             .getClass(), resolver);
         if (flatvalue != null) {
           toprocess.markup = flatvalue;
         }
       }
     }
   }
 
   /**
    * As well as resolving any reference to a BeanResolver in the
    * <code>resolver</code> field, this method will also copy it across to the
    * <code>darreshaper</code> field if a) it refers to a LeafObjectParser, and
    * b) the field is currently empty. This is a courtesy to allow compactly
    * encoded things like DateParsers to be used first class, although we REALLY
    * expect users to make transit beans.
    */
   private BeanResolver computeResolver(UIComponent toprocess, Object root) {
     Object renderer = toprocess instanceof UIBound ? ((UIBound) toprocess).resolver
         : ((UIVerbatim) toprocess).resolver;
     ELReference valuebinding = null;
     UIBound bound = null;
     if (toprocess instanceof UIBound) {
       bound = (UIBound) toprocess;
       valuebinding = bound.valuebinding;
     }
     else {
       UIVerbatim toprocessv = (UIVerbatim) toprocess;
       if (toprocessv.markup instanceof ELReference) {
         valuebinding = (ELReference) toprocessv.markup;
       }
     }
 
     if (renderer == null) {
       if (valuebinding != null) {
         ShellInfo shells = alterer.fetchShells(valuebinding.value, root, false);
 
         renderer = dataConverterRegistry.fetchConverter(shells);
       }
     }
 
     if (renderer instanceof ELReference) {
       renderer = alterer.getBeanValue(((ELReference) renderer).value,
           beanlocator, null);
       if (bound != null && bound.darreshaper == null) {
         bound.darreshaper = (ELReference) bound.resolver;
       }
     }
     if (renderer == null)
       return null;
     BeanResolver resolver = ConverterConverter.toResolver(renderer);
     if (resolver == null) {
       throw UniversalRuntimeException.accumulate(
           new IllegalArgumentException(), "Renderer object for "
               + toprocess.getFullID() + " of unrecognised "
               + renderer.getClass()
               + " (expected BeanResolver or LeafObjectParser)");
     }
     return resolver;
   }
 
 }
