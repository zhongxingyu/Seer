 /**
  *  Copyright 2011 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of SDMXDataExport module.
  *
  *  SDMXDataExport module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  SDMXDataExport module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with SDMXDataExport module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 package org.openmrs.module.sdmxhddataexport.web.controller.report;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.sdmxhddataexport.SDMXHDDataExportService;
 import org.openmrs.module.sdmxhddataexport.model.DataElement;
 import org.openmrs.module.sdmxhddataexport.model.Query;
 import org.openmrs.module.sdmxhddataexport.model.Report;
 import org.openmrs.module.sdmxhddataexport.model.ReportDataElement;
 import org.openmrs.module.sdmxhddataexport.model.ReportDataElementResult;
 import org.openmrs.module.sdmxhddataexport.web.controller.editor.DataElementEditor;
 import org.openmrs.module.sdmxhddataexport.web.controller.editor.QueryEditor;
 import org.openmrs.module.sdmxhddataexport.web.controller.editor.ReportEditor;
 import org.openmrs.module.sdmxhddataexport.web.controller.utils.SDMXHDataExportUtils;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 
 @Controller("SDMXHDDataExportReportDataElementController")
 public class ReportDataElementController {
 
     Log log = LogFactory.getLog(this.getClass());
     private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 
     @RequestMapping(value = "/module/sdmxhddataexport/reportDataElement.form", method = RequestMethod.GET)
     public String view(
             @RequestParam(value = "reportDataElementId", required = false) Integer reportDataElementId,
             @RequestParam(value = "reportId", required = false) Integer reportId,
             @ModelAttribute("reportDataElement") ReportDataElement reportDataElement,
             Model model) {
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         if (reportDataElementId != null) {
             reportDataElement = sDMXHDDataExportService.getReportDataElementById(reportDataElementId);
             reportId = reportDataElement.getReport().getId();
             model.addAttribute("reportDataElement", reportDataElement);
 
         }
         model.addAttribute("reportId", reportId);
         return "/module/sdmxhddataexport/report/reportDataElement";
     }
 
     @RequestMapping(value = "/module/sdmxhddataexport/reportDataElement.form", method = RequestMethod.POST)
     public String post(
             @ModelAttribute("reportDataElement") ReportDataElement reportDataElement,
             BindingResult bindingResult, SessionStatus status, Model model) {
         new ReportDataElementValidator().validate(reportDataElement,
                 bindingResult);
         if (bindingResult.hasErrors()) {
             return "/module/sdmxhddataexport/report/reportDataElement";
         } else {
             SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
             reportDataElement.setCreatedOn(new java.util.Date());
             sDMXHDDataExportService.saveReportDataElement(reportDataElement);
             status.setComplete();
             return "redirect:/module/sdmxhddataexport/reportDataElement.form?reportId="
                     + reportDataElement.getReport().getId();
         }
         // return "/module/sdmxhddataexport/report/reportDataElement";
     }
 
     @RequestMapping(value = "/module/sdmxhddataexport/resultExecuteReport.form", method = RequestMethod.GET)
     public String executeReport(
             @RequestParam(value = "reportId", required = false) Integer reportId,
             @RequestParam(value = "startDate", required = false) String startDate,
             @RequestParam(value = "endDate", required = false) String endDate,
             Model model) throws ParseException {
 
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         List<ReportDataElement> list = sDMXHDDataExportService.listReportDataElement(reportId, null, null, 0, 0);
         String dataSetCode = "";
         String reportName = "";
         List<String> periods = new ArrayList<String>();
         Map<String, List<ReportDataElementResult>> periodResults = new HashMap<String, List<ReportDataElementResult>>();
         
         if (CollectionUtils.isNotEmpty(list)) {
             dataSetCode = list.get(0).getReport().getCode();
             reportName = list.get(0).getReport().getName();
             Date begin = SDMXHDataExportUtils.getFirstDate(sdf.parse(startDate));
             Date end = SDMXHDataExportUtils.getLastDate(begin);
             SimpleDateFormat periodFormatter = new SimpleDateFormat("yyyyMM");
             Date finalDate = SDMXHDataExportUtils.getLastDate(sdf.parse(endDate));
             while (begin.before(finalDate)) {
                 periods.add(periodFormatter.format(begin));
                 List<ReportDataElementResult> results = new ArrayList<ReportDataElementResult>();
                 for (ReportDataElement reportDataElement : list) {
                     ReportDataElementResult result = new ReportDataElementResult();
                     result.setDataElement(reportDataElement.getDataElement());
                     result.setId(reportDataElement.getId());
                     result.setReport(reportDataElement.getReport());
                     result.setQuery(reportDataElement.getQuery());
                     result.setResult(sDMXHDDataExportService.executeQuery(
                             reportDataElement.getQuery().getSqlQuery(),
                             sdf.format(begin), sdf.format(end)));
                     results.add(result);
                 }
 
                 periodResults.put(periodFormatter.format(begin), results);
                 begin = SDMXHDataExportUtils.nextMonth(begin);
                 end = SDMXHDataExportUtils.getLastDate(begin);
             }
             String orgunitCode = Context.getAdministrationService().getGlobalProperty("sdmxhddataexport.organisationUnit");
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
             model.addAttribute("DATASET_CODE", dataSetCode);
             model.addAttribute("reportName", reportName);
             model.addAttribute("periods", periods);
             model.addAttribute("periodResults", periodResults);
             model.addAttribute("orgunit", orgunitCode);
             model.addAttribute("prepared", formatter.format(new Date()));
         }
         return "/module/sdmxhddataexport/report/result";
     }
 
     @RequestMapping(value = "/module/sdmxhddataexport/downloadExecutedReport.form", method = RequestMethod.GET)
     public void downloadExecutedReport(
             @RequestParam(value = "reportId", required = false) Integer reportId,
             @RequestParam(value = "startDate", required = false) String startDate,
             @RequestParam(value = "endDate", required = false) String endDate,
             HttpServletRequest request, HttpServletResponse response)
             throws ParseException, IOException {
 
         String urlToRead = "http://" + request.getLocalAddr() + ":"
                 + request.getLocalPort() + request.getContextPath()
                 + "/module/sdmxhddataexport/resultExecuteReport.form?reportId="
                 + reportId + "&startDate=" + startDate + "&endDate=" + endDate;
         URL url;
         HttpURLConnection conn;
         response.setContentType("application/download");
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
         response.setHeader("Content-Disposition", "attachment; filename=\""
                 + "sdmxhd-" + formatter.format(new Date()) + ".xml" + "\"");
 
         try {
             url = new URL(urlToRead);
             conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             InputStream rd = conn.getInputStream();
             byte[] bytes = new byte[1024];
             int bytesRead;
             while ((bytesRead = rd.read(bytes)) != -1) {
                 String str = new String(bytes);
                str = str.substring(0, bytesRead).trim();
                 response.getOutputStream().write(str.getBytes(), 0, str.getBytes().length);
             }
             rd.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @RequestMapping(value = "/module/sdmxhddataexport/extractMonth.form", method = RequestMethod.GET)
     public void extractMonth(@RequestParam(value = "date") String dateStr,
             HttpServletResponse response) throws IOException, ParseException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         Date date = sdf.parse(dateStr);
         SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
         out.print(formatter.format(date));
     }
 
     @RequestMapping(value = "/module/sdmxhddataexport/deleteReportDataElement.form", method = RequestMethod.POST)
     public String deleteReportDataElement(@RequestParam("ids") String[] ids,
             HttpServletRequest request) {
         String temp = "";
         HttpSession httpSession = request.getSession();
         Integer reportDataElementId = null;
         Integer reportId = null;
         try {
             SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
             if (ids != null && ids.length > 0) {
                 for (String sId : ids) {
                     reportDataElementId = Integer.parseInt(sId);
                     if (reportDataElementId != null && reportDataElementId > 0) {
                         ReportDataElement reportDataElement = sDMXHDDataExportService.getReportDataElementById(reportDataElementId);
                         sDMXHDDataExportService.deleteReportDataElement(reportDataElement);
                         reportId = reportDataElement.getReport().getId();
                     } else {
                         // temp +=
                         // "We can't delete store="+store.getName()+" because that store is using please check <br/>";
                         temp = "This reportDataElement cannot be deleted as it is in use";
                     }
                 }
             }
         } catch (Exception e) {
             httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
                     "Can not delete reportDataElement ");
             log.error(e);
         }
         httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, StringUtils.isBlank(temp) ? "sdmxhddataexport.reportDataElement.deleted"
                 : temp);
 
         return "redirect:/module/sdmxhddataexport/reportDataElement.form?reportId="
                 + reportId;
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.registerCustomEditor(Report.class, new ReportEditor());
         binder.registerCustomEditor(Query.class, new QueryEditor());
         binder.registerCustomEditor(DataElement.class, new DataElementEditor());
     }
 
     @ModelAttribute("reportDataElements")
     public List<ReportDataElement> reportDataElements(HttpServletRequest request) {
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         Integer reportId = NumberUtils.toInt(request.getParameter("reportId"),
                 0);
         List<ReportDataElement> reportDataElements = sDMXHDDataExportService.listReportDataElement(reportId, null, null, 0, 0);
         return reportDataElements;
     }
 
     @ModelAttribute("reports")
     public List<Report> reports() {
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         List<Report> reports = sDMXHDDataExportService.listReport("", 0, 0);
         return reports;
     }
 
     @ModelAttribute("queries")
     public List<Query> queries() {
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         List<Query> queries = sDMXHDDataExportService.listQuery("", 0, 0);
         return queries;
     }
 
     @ModelAttribute("dataElements")
     public List<DataElement> dataelements() {
         SDMXHDDataExportService sDMXHDDataExportService = Context.getService(SDMXHDDataExportService.class);
         List<DataElement> dataelements = sDMXHDDataExportService.listDataElement("", 0, 0);
         return dataelements;
     }
 }
