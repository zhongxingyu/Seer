 package edu.hust.k54.controller;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.ui.Model;
 import edu.hust.k54.persistence.Baocao;
 import edu.hust.k54.persistence.BaocaoHome;
 import edu.hust.k54.persistence.Soyeulylich;
 import edu.hust.k54.persistence.SoyeulylichHome;
 import edu.hust.k54.persistence.Taikhoandangnhap;
 import edu.hust.k54.persistence.TaikhoandangnhapHome;
 
 @Controller
 public class ReportController {
 	private static String NOT_ENOUGH_PERMISSION = "Không đủ quyền xem file này";
 	private static String NOT_ENOUGH_PERMISSION_TO_VIEW = "Không đủ quyền xem trang nà";
 
 	@RequestMapping(value = "/showreport.spms", method = RequestMethod.GET)
 	public String getReport(@RequestParam("id") int id,
 			HttpServletRequest request, HttpServletResponse response,
 			Model model) throws IOException {
 		Taikhoandangnhap account = (Taikhoandangnhap) request.getSession()
 				.getAttribute("user");
 		if (account == null) {
 			model.addAttribute("err", NOT_ENOUGH_PERMISSION);
 			return "report";
 		}
 
 		BaocaoHome ds = new BaocaoHome();
 		Baocao report = ds.findById(id);
 
 		if (report == null) {
 			model.addAttribute("err", "File được yêu cầu không tồn tạ");
 			return "report";
 		}
		
 		try {
 			String file_path = request.getRealPath("")
 					+ "/uploadContent/reports/" + report.getNoidung();
 			System.err.println(file_path);
 			FileInputStream is = new FileInputStream(file_path);
 
 			OutputStream os = response.getOutputStream();
			response.addHeader("Content-Disposition", "attachment; filename=\"" + report.getNoidung() + "\"");
 			IOUtils.copy(is, os);
 			response.flushBuffer();
 			return null;
 		} catch (IOException ex) {
 			model.addAttribute("err", "File được yêu cầu không tồn tại");
 			return "report";
 		}
 	}
	
 	@RequestMapping(value = "/deletereport.spms", method = RequestMethod.GET)
 	public String deleteReport(@RequestParam("id") int id,
 			HttpServletRequest request, HttpServletResponse response,
 			Model model) throws IOException {
 		Taikhoandangnhap account = (Taikhoandangnhap) request.getSession()
 				.getAttribute("user");
		
 		if (account == null) {
 			model.addAttribute("err", NOT_ENOUGH_PERMISSION);
 			return "report";
 		}
 
 		BaocaoHome ds = new BaocaoHome();
 		Baocao report = ds.findById(id);
 
 		if (report == null) {
 			model.addAttribute("err", "File được yêu cầu không tồn tại");
 			return "report";
 		}
 
 		try {
 			String file_path = request.getRealPath("")
 					+ "/uploadContent/reports/" + report.getNoidung();
 			File f = new File(file_path);
 			boolean re = f.delete();
 			String flash = "";
 			if (re) {
 				flash = "Xóa file thành công";
 			} else {
 				flash = "Xóa file không thành công";
 			}
 			ds.delete(report);
 			ds.getSessionFactory().getCurrentSession().flush();
 			request.getSession().setAttribute("flash", flash);
 			return "redirect:/report.spms";
 		} catch (Exception ex) {
 			model.addAttribute("err", "File được yêu cầu không tồn tại");
 			return "report";
 		}
 
 	}
 
 	@RequestMapping(value = "/report.spms", method = RequestMethod.GET)
 	public String getAllReadableReport(HttpServletRequest request, Model model) {
 		Manager man = new Manager();
 		Taikhoandangnhap account = (Taikhoandangnhap) request.getSession()
 				.getAttribute("user");
		if (account == null && account.getPermission() < 2) {
 			return "errorPage";
 		}
 		String flash = (String) request.getSession().getAttribute("flash");
 		if (flash != null) {
 			model.addAttribute("flash", flash);
 			request.getSession().removeAttribute("flash");
 		}
 		Integer idcanbo = account.getIduser();
		System.err.println("idcanbo: " + idcanbo);
		List <Baocao> baocaos = new ArrayList<Baocao>();
 		Soyeulylich soyeulylich = account.getSoyeulylich();
		(new SoyeulylichHome()).attachDirty(soyeulylich);
		List <Baocao> list = man.getLowerPermission(idcanbo);
 		baocaos.addAll(list);
 		BaocaoHome ds = new BaocaoHome();
		System.out.println(baocaos.size());
 		model.addAttribute("report_list", baocaos);
 
 		return "report";
 	}
 
 }
