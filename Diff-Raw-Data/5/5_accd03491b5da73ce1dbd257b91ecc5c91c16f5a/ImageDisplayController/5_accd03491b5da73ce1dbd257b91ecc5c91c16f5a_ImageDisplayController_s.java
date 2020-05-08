 package com.trailmagic.image.ui;
 
 import com.trailmagic.image.Image;
 import com.trailmagic.image.ImageFrame;
 import com.trailmagic.image.ImageGroup;
 import com.trailmagic.image.ImageGroupRepository;
 import com.trailmagic.image.ImageRepository;
 import com.trailmagic.image.security.ImageSecurityFactory;
 import com.trailmagic.web.util.ImageRequestInfo;
 import com.trailmagic.web.util.MalformedUrlException;
 import com.trailmagic.web.util.WebRequestTools;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 public class ImageDisplayController extends SimpleFormController {
     private ImageRepository imageRepository;
     private ImageGroupRepository imageGroupRepository;
     private ImageSecurityFactory imageSecurityFactory;
     private WebRequestTools webRequestTools;
     
     public ImageDisplayController(ImageRepository imageRepository,
                                   ImageGroupRepository imageGroupRepository,
                                   ImageSecurityFactory imageSecurityFactory,
                                   WebRequestTools webRequestTools) {
         super();
         this.imageRepository = imageRepository;
         this.imageGroupRepository = imageGroupRepository;
         this.imageSecurityFactory = imageSecurityFactory;
         this.webRequestTools = webRequestTools;
     }
 
     @Override
     protected void initBinder(HttpServletRequest request,
                               ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         
         binder.registerCustomEditor(Date.class, "captureDate",
                                    new CustomDateEditor(SimpleDateFormat.getDateInstance(), true));
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request)
             throws Exception {
         ImageRequestInfo iri = webRequestTools.getImageRequestInfo(request);
         Image image = imageRepository.getById(iri.getImageId());
         if (image == null) {
             throw new Exception("no such image");
         }
         return image;
     }
     
     private boolean isEditMode(HttpServletRequest request) throws ServletRequestBindingException {
         String mode = ServletRequestUtils.getStringParameter(request, "mode");
         if ("edit".equals(mode)) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     protected ModelAndView showForm(HttpServletRequest request,
                                     HttpServletResponse response,
                                     BindException errors) throws Exception {
         @SuppressWarnings("unchecked")
         Map<String, Object> model = errors.getModel();
         model.put("isEditView", errors.hasErrors() ? true : isEditMode(request));
         return setupModel(request, model);
     }
     
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object command, BindException errors)
             throws Exception {
         
         Image image = (Image) command;
         
         imageRepository.save(image);
 
         @SuppressWarnings("unchecked")
         Map<String, Object> model = errors.getModel();
         model.put("isEditView", false);
         return setupModel(request, model);
     }
 
     
     private ModelAndView setupModel(HttpServletRequest request, Map<String,Object> model) throws MalformedUrlException {
         ImageRequestInfo iri = webRequestTools.getImageRequestInfo(request);
         ImageFrame frame =
             imageGroupRepository.getImageFrameByGroupNameAndImageId(iri.getImageGroupName(),
                                                                     iri.getImageId());
         model.put("frame", frame);
         model.put("image", frame.getImage());
         model.put("group", frame.getImageGroup());
         model.put("imageIsPublic",
                   imageSecurityFactory.isPublic(frame.getImage()));
         List<ImageGroup> groupsContainingImage =
             imageGroupRepository.getByImage(frame.getImage());
         List<ImageGroup> otherGroups = new ArrayList<ImageGroup>();
         Iterator<ImageGroup> iter = groupsContainingImage.iterator();
         ImageGroup containingGroup;
         while (iter.hasNext()) {
             containingGroup = iter.next();
             if ( !frame.getImageGroup().equals(containingGroup) ) {
                 otherGroups.add(containingGroup);
             }
         }
         model.put("groupsContainingImage", otherGroups);
 
         SortedSet<ImageFrame> frames = frame.getImageGroup().getFrames();
         SortedSet<ImageFrame> tmpSet = frames.headSet(frame);
 
 //             Iterator iter = tmpSet.iterator();
 //             iter.next();
 //             if ( iter.hasNext() ) {
 //                 ImageFrame prevFrame = (ImageFrame)iter.next();
 //             }
 
 
         if ( !tmpSet.isEmpty() ) {
             ImageFrame prevFrame = tmpSet.last();
             model.put("prevFrame", prevFrame);
         }
 
         tmpSet = frames.tailSet(frame);
         Iterator<ImageFrame> framesIter = tmpSet.iterator();
         framesIter.next();
         if ( framesIter.hasNext() ) {
             ImageFrame nextFrame = framesIter.next();
             model.put("nextFrame", nextFrame);
         }
 
         // got user, group, and frame number: show that frame
         return new ModelAndView(getFormView(), model);
     }
 }
