 package cn.mobiledaily.web.controller;
 
 import cn.mobiledaily.common.exception.InternalServerError;
 import cn.mobiledaily.common.exception.InvalidValueException;
 import cn.mobiledaily.common.exception.ValidationException;
 import cn.mobiledaily.common.service.FileService;
 import cn.mobiledaily.common.validation.ExchangePassword;
 import cn.mobiledaily.common.validation.NotBlank;
 import cn.mobiledaily.module.applicant.domain.Applicant;
 import cn.mobiledaily.module.applicant.domain.ApplyStatus;
 import cn.mobiledaily.module.applicant.domain.ApplyType;
 import cn.mobiledaily.module.applicant.event.UpdateApplyStatusEvent;
 import cn.mobiledaily.module.applicant.service.ApplicantService;
 import cn.mobiledaily.module.exhibition.domain.Exhibition;
 import cn.mobiledaily.module.exhibition.service.ExhibitionService;
 import cn.mobiledaily.module.exhibition.validation.ExKey;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedInputStream;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Controller
@Validated
 @RequestMapping("applies")
public class ApplyController {
     private Logger logger = LoggerFactory.getLogger(ApplyController.class);
    @Autowired
     ApplicationEventPublisher eventPublisher;
     @Autowired
     private ExhibitionService exhibitionService;
     @Autowired
     private ApplicantService applicantService;
     @Autowired
     FileService fileService;
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "put", method = RequestMethod.POST)
     public void putApply(
             @ExKey String exKey,
             @NotBlank String token,
             @NotBlank String name,
             @NotBlank String mobile,
            String email,
             String type
     ) {
         try {
             if (StringUtils.isBlank(type) || !StringUtils.containsOnly(type, "AE")) {
                 throw new InvalidValueException("type", "[A|E]", type);
             }
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             if (exhibition == null) {
                 throw new InvalidValueException("exKey", "existed exhibition", exKey);
             }
             Applicant applicant = applicantService.findByToken(token, exhibition);
             if (applicant == null) {
                 applicant = new Applicant();
                 applicant.setExhibition(exhibition);
             }
             applicant.setToken(token);
             applicant.setName(name);
             applicant.setMobile(mobile);
             applicant.setEmail(email);
             applicant.getReasons().add(new DateTime().toString("您在yyyy年M月d日 HH:mm提交申请"));
             applicant.setApplyType(ApplyType.fromString(type));
             applicantService.save(applicant);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/applies/put", e);
             throw new InternalServerError("/applies/put", e);
         }
     }
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "update", method = RequestMethod.POST)
     public void updateApply(
             @ExchangePassword String pwd,
             @ExKey String exKey,
             @NotBlank String token,
             String status,
             String reason
     ) {
         try {
             if (StringUtils.isBlank(status) || !StringUtils.containsOnly(status, "AD")) {
                 throw new InvalidValueException("status", "[A|D]", status);
             }
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             Applicant applicant = applicantService.findByToken(token, exhibition);
             if (applicant == null) {
                 throw new InvalidValueException("token", "existed token", token);
             }
             ApplyStatus applyStatus = ApplyStatus.fromCode(status);
             applicant.setApplyStatus(applyStatus);
             StringBuilder sb = new StringBuilder()
                     .append("您的申请在")
                     .append(new DateTime().toString("yyyy年M月d日 HH:mm"))
                     .append("完成审核")
                     .append("\n审核结果：")
                     .append(ApplyStatus.APPROVED.equals(applyStatus) ? "审核通过" : "审核未通过")
                     ;
             if (StringUtils.isNotBlank(reason)) {
                 sb.append("\n").append(reason);
             }
             applicant.getReasons().add(sb.toString());
             applicantService.save(applicant);
             eventPublisher.publishEvent(new UpdateApplyStatusEvent(applicant));
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/applies/update", e);
             throw new InternalServerError("/applies/update", e);
         }
     }
 
     @RequestMapping(value = "get", method = RequestMethod.GET)
     @ResponseBody
     public ApplyWrapper getApply(
             @ExKey String exKey,
             @NotBlank String token
     ) {
         try {
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             Applicant applicant = applicantService.findByToken(token, exhibition);
             ApplyWrapper wrapper = new ApplyWrapper(applicant);
             wrapper.setExKey(exKey);
             return wrapper;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/applies/get", e);
             throw new InternalServerError("/applies/get", e);
         }
     }
 
     @RequestMapping(value = "all", method = RequestMethod.GET)
     @ResponseBody
     public Object getApplies() {
         try {
             List<Applicant> applicants = applicantService.findAll();
             Collections.sort(applicants, new Comparator<Applicant>() {
                 @Override
                 public int compare(Applicant o1, Applicant o2) {
                     return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                 }
             });
             List<Map<String, Object>> list = new ArrayList<>(applicants.size());
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             for (Applicant applicant : applicants) {
                 Map<String, Object> o = new HashMap<>();
                 list.add(o);
                 o.put("createdAt", dateFormat.format(applicant.getCreatedAt()));
                 o.put("type", applicant.getApplyType());
                 o.put("status", applicant.getApplyStatus());
                 o.put("exhibition", applicant.getExhibition().getName());
                 o.put("name", applicant.getName());
                 o.put("mobile", applicant.getMobile());
                 o.put("email", applicant.getEmail());
                 o.put("exKey", applicant.getExhibition().getExKey());
                 o.put("token", applicant.getToken());
             }
             return list;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/applies/all", e);
             throw new InternalServerError("/applies/all", e);
         }
     }
 
     public static class ApplyWrapper {
         private String exKey;
         private String status = "N";
         private List<String> logs = new LinkedList<>();
 
         public ApplyWrapper(Applicant applicant) {
             if (applicant != null) {
                 status = applicant.getApplyStatus().getCode();
                 logs.addAll(applicant.getReasons());
             }
         }
 
         public void setExKey(String exKey) {
             this.exKey = exKey;
         }
 
         public String getExKey() {
             return exKey;
         }
 
         public String getStatus() {
             return status;
         }
 
         public List<String> getLogs() {
             return logs;
         }
     }
 
     @RequestMapping(value = "export", method = RequestMethod.GET)
     public void exportApplies(HttpServletResponse resp) {
         try {
             String filename = new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date()) + ".xls";
             OutputStream out = fileService.openOutputStream(filename);
             exportExcel(out);
             BufferedInputStream in = new BufferedInputStream(fileService.openInputStream(filename));
             resp.setContentType("application/vnd.ms-excel");
             resp.addHeader("Content-Disposition", "attachment; filename=applies.xls");
             resp.setContentLength(in.available());
             out = resp.getOutputStream();
             byte[] bs = new byte[4096];
             int len = 0;
             while ((len = in.read(bs)) > 0) {
                 out.write(bs, 0, len);
             }
         } catch (Exception e) {
             throw new InternalServerError("/applies/export", e);
         }
     }
 
     private void exportExcel(OutputStream out) throws Exception {
         List<Applicant> applicants = applicantService.findAll();
         Workbook wb;
 //        if (filename.toUpperCase().endsWith(".XLS")) {
             wb = new HSSFWorkbook();
 //        } else {
 //            wb = new XSSFWorkbook();
 //        }
         Sheet sheet = wb.createSheet();
         Row titleRow = sheet.createRow(0);
         titleRow.createCell(0).setCellValue("E_Title");
         titleRow.createCell(1).setCellValue("U_Name");
         titleRow.createCell(2).setCellValue("U_Telephone");
         titleRow.createCell(3).setCellValue("U_Email");
         titleRow.createCell(4).setCellValue("U_Company");
         int rowNum = 0;
         for (int i = 0, n = applicants.size(); i < n; i++) {
             Applicant applicant = applicants.get(i);
             if (ApplyStatus.APPROVED == applicant.getApplyStatus()) {
                 Row row = sheet.createRow(++rowNum);
                 row.createCell(0).setCellValue(applicant.getExhibition().getName());
                 row.createCell(1).setCellValue(applicant.getName());
                 row.createCell(2).setCellValue(applicant.getMobile());
                 row.createCell(3).setCellValue(applicant.getEmail());
             }
         }
 //        FileOutputStream out = new FileOutputStream(filename);
         wb.write(out);
         out.close();
     }
 }
