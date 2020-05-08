 package org.openmrs.module.hr.web.controller;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrCertificate;
 import org.openmrs.module.hr.HrPostHistory;
 import org.openmrs.module.hr.HrStaff;
 import org.openmrs.module.hr.HrStaffCert;
 import org.openmrs.module.hr.api.HRQualificationService;
 import org.openmrs.module.hr.api.propertyEditor.HrCertificateEditor;
 import org.openmrs.module.hr.api.propertyEditor.HrPostHistoryEditor;
 import org.openmrs.module.hr.api.validator.CertificateValidator;
 import org.openmrs.module.hr.api.validator.StaffCertificateValidator;
 import org.openmrs.propertyeditor.ConceptEditor;
 import org.openmrs.propertyeditor.LocationEditor;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.web.WebConstants;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.imageio.ImageIO;
 import javax.servlet.http.HttpServletRequest;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 @SessionAttributes("staff")
 public class StaffCertificateController {
 
     private Log log = LogFactory.getLog(this.getClass());
 
     private static final String SUCCESS_LIST_VIEW = "/module/hr/manager/staffCertificates";
     private static final String SUCCESS_FORM_VIEW = "/module/hr/manager/staffCertificate";
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
         binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
         binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
         binder.registerCustomEditor(HrCertificate.class, new HrCertificateEditor());
     }
 
     @RequestMapping(value="module/hr/manager/staffCertificate.form")
     @ModelAttribute("staffCertificate")
     public HrStaffCert showForm(@RequestParam(value="staffCertId",required=false) Integer staffCertificateId,ModelMap model){
         HrStaffCert hrStaffCert;
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(staffCertificateId != null){
             hrStaffCert = hrQualificationService.getStaffCertificateById(staffCertificateId);
             Date today = getTodaysDate();
             if(hrStaffCert.getCertExpirationDate().before(today))
                 model.addAttribute("expired",true);
         }
         else{
             hrStaffCert = new HrStaffCert();
             model.addAttribute("expired",false);
         }
         model.addAttribute("allCertificatesList",hrQualificationService.getCertificates());
         return hrStaffCert;
     }
 
     private Date getTodaysDate() {
         Calendar calendar = Calendar.getInstance();
         return calendar.getTime();
     }
 
     @RequestMapping(value = "module/hr/manager/staffCertificates.list")
     public String showList(ModelMap model ,@ModelAttribute("staff") HrStaff staff){
         HRQualificationService hrQualificationService=Context.getService(HRQualificationService.class);
         List<HrStaffCert> hrStaffCertList = hrQualificationService.getCertificatesForStaff(staff);
         model.addAttribute("staffCertificates",hrStaffCertList);
         return SUCCESS_LIST_VIEW;
     }
 
     @RequestMapping(value ="module/hr/manager/staffCertificate.form", method = RequestMethod.POST)
     public ModelAndView createOrUpdateStaffCertificate(HttpServletRequest request,@ModelAttribute("staffCertificate")  HrStaffCert hrStaffCert, BindingResult errors,@ModelAttribute("staff") HrStaff staff){
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(request.getParameter("action").equalsIgnoreCase("Delete Staff Certificate"))
             deleteStaffCertificate(hrQualificationService,hrStaffCert,request);
 
         else{
         new StaffCertificateValidator().validate(hrStaffCert,errors);
         if(errors.hasErrors())
             return new ModelAndView(SUCCESS_FORM_VIEW).addObject("allCertificatesList",hrQualificationService.getCertificates());
 
        if(request.getParameter("cancelStaffCertificate") != null)
             return cancelStaffCertificate(request, hrStaffCert, hrQualificationService, errors,staff);
 
 
         hrStaffCert.setImagePresent(false);
         hrStaffCert.setHrStaff(staff);
         hrQualificationService.saveStaffCertificate(hrStaffCert);
 
         if(request instanceof MultipartHttpServletRequest)
             addImage((MultipartHttpServletRequest)request,hrStaffCert,errors);
 
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Staff Certificate Saved Successfully");
         }
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("staffCertificates",hrQualificationService.getCertificatesForStaff(staff));
     }
 
     private void deleteStaffCertificate(HRQualificationService hrQualificationService, HrStaffCert hrStaffCert, HttpServletRequest request) {
         hrQualificationService.deleteStaffCertificate(hrQualificationService.getStaffCertificateById(hrStaffCert.getStaffCertId()));
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Staff Certificate Deleted Successfully");
     }
 
     private ModelAndView cancelStaffCertificate(HttpServletRequest request, HrStaffCert hrStaffCert, HRQualificationService hrQualificationService, BindingResult errors, HrStaff staff) {
         String cancelReason = request.getParameter("certCancel");
         if(cancelReason == null || cancelReason.length() ==0){
             errors.reject("certCancel","Cancel Reason can not be empty");
             ModelAndView modelAndView = new ModelAndView(SUCCESS_FORM_VIEW);
             modelAndView.addObject("staffCertificate",hrQualificationService.getStaffCertificateById(hrStaffCert.getStaffCertId()));
             modelAndView.addObject("expired",false);
             modelAndView.addObject("allCertificatesList",hrQualificationService.getCertificates());
             return modelAndView;
         }
 
         HrStaffCert staffCert = hrQualificationService.getStaffCertificateById(hrStaffCert.getStaffCertId());
         staffCert.setCancelDate(getTodaysDate());
         staffCert.setCertCancel(cancelReason);
         hrQualificationService.saveStaffCertificate(staffCert);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Staff Certificate Cancelled Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("staffCertificates",hrQualificationService.getCertificatesForStaff(staff));
     }
 
     private void addImage(MultipartHttpServletRequest request, HrStaffCert hrStaffCert, BindingResult errors) {
         MultipartFile image = request.getFile("image");
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(!image.isEmpty()){
             if(image.getOriginalFilename().endsWith(".jpeg") ||
                image.getOriginalFilename().endsWith(".jpg")  ||
                image.getOriginalFilename().endsWith(".png")  ||
                image.getOriginalFilename().endsWith(".gif"))
             {
 
                 hrStaffCert.setImagePresent(true);
                 File imageFolder = new File(OpenmrsUtil.getApplicationDataDirectory(),"hr_certificates");
                 File origImageToSave = new File(imageFolder,"staff_cert_orig_"+hrStaffCert.getUuid());
 
                 try {
                     log.info("Saving image");
                     image.transferTo(origImageToSave);
                     BufferedImage originalImage = ImageIO.read(new File(imageFolder,"staff_cert_orig_"+hrStaffCert.getUuid()));
                     int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                     BufferedImage resizedImage = new BufferedImage(100,100,type);
                     Graphics2D graphics2D = resizedImage.createGraphics();
                     graphics2D.drawImage(originalImage,0,0,100,100,null);
                     graphics2D.dispose();
                     ImageIO.write(resizedImage,"jpg",new File(imageFolder,"staff_cert_display_"+hrStaffCert.getUuid()));
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
 
             }
             else
             {
                 errors.reject("fileFormat");
                 return;
             }
 
 
         }
         else
             hrStaffCert.setImagePresent(false);
         hrQualificationService.saveStaffCertificate(hrStaffCert);
     }
 
 }
