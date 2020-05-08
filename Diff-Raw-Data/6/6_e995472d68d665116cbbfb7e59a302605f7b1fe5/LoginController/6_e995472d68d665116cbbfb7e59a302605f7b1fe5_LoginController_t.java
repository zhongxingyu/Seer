 package com.project.form;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import com.project.dao.PobierzGrupyDAO;
 import com.project.data.GrupyZajeciowe;
 import com.project.data.Kursy;
 import com.project.data.Prowadzacy;
 import com.project.service.LoginService;
 import com.project.service.PobierzGrupyService;
 import com.project.service.PobranieGrupZajService;
 import com.project.Utils.Encryption;
 import com.project.Utils.ImportCSV;
 import com.project.Utils.RandomPassword;
 import com.project.Utils.SendMail;
 
 import com.project.Json.*;
 
 @Controller
 public class LoginController extends SendMail {
 
 	@Autowired
 	private LoginService loginService;
 
 	@Autowired
 	private PobierzGrupyService pobierzGrupyService;
 
 	@Autowired
 	private PobranieGrupZajService pobierzGrupyZajService;
 
 	@Autowired
 	private HttpServletRequest context;
 
 	@Autowired
 	private ImportCSV importcsv;
 
 	@RequestMapping("/")
 	public String przelacz() {
 		return "*/index";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public @ResponseBody
 	Integer addProwadzacy(
 			@RequestParam(value = "firstname", required = true) String imie,
 			@RequestParam(value = "surname", required = true) String nazwisko,
 			@RequestParam(value = "email", required = true) String mail,
 			@RequestParam(value = "user", required = true) String login,
 			Model model) {
 		String from = "grupy.pwr.wroc@gmail.com";
 		String subject = "Przesłanie hasła do logowania!";
 		Date data = new Date();
 		Prowadzacy prowadzacy = new Prowadzacy();
 		prowadzacy.setImiona(imie);
 		prowadzacy.setNazwisko(nazwisko);
 		prowadzacy.setEmail(mail);
 		prowadzacy.setLogin(login);
 		prowadzacy.setDataDodania(data);
 		prowadzacy.setWaznosc(true);
 		prowadzacy.setAktywowany(false);
 		String haslo = RandomPassword.Random();
 		prowadzacy.setHaslo(Encryption.encrypt(haslo));
 		loginService.addProwadzacy(prowadzacy);
 		Wyslij_maila(mail, subject, haslo, from);
 		List<Prowadzacy> registerlist = loginService.validateRegister(imie,
 				nazwisko, mail, login);
 		if (registerlist.size() > 0) {
 
 			return 1;
 		} else {
 
 			return 0;
 		}
 
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public @ResponseBody
 	String loginAuthentication(
 			@RequestParam(value = "user", required = true) String login,
 			@RequestParam(value = "pass", required = true) String haslo,
 			Model model) {
 
 		List<Prowadzacy> loginlist = loginService.validateLogin(login, haslo);
 
 		System.out.println("Hasło: " + haslo + " Login: " + login);
 		System.out.println("\nRozmiar loginlist: " + loginlist.size());
 
 		if (loginlist.size() > 0) {
 			return loginlist.get(0).getIdProwadzacy().toString();
 
 		} else {
 			return "-1";
 		}
 	}
 
 	/*@RequestMapping(value = "/getgroups", method = RequestMethod.POST, produces="application/json")
 	public @ResponseBody
 	JsonGroupWyzej wyslijGrupy(
 			@RequestParam(value = "dateid", required = true) int idGrupyZaj,
 			Model model) {
 
 		JsonMarksAndPresence jsonmarks = new JsonMarksAndPresence();
 
 		JsonStudents jsonstudent = new JsonStudents();
 		JsonSpotkania jsonspotkania = new JsonSpotkania();
 		JsonNotes jsonnotatki = new JsonNotes();
 
 		JsonGrupyProjektowe jsongrup = new JsonGrupyProjektowe();
 		JsonNieWGrupie jsonniewgrupie = new JsonNieWGrupie();
 
 		JsonGroupZajeciowe jsonGroupZajeciowe = new JsonGroupZajeciowe();
 
 		JsonGroupWyzej jsonGroupWyzej = new JsonGroupWyzej();
 		
 		List<GrupyZajeciowe> grupzajeciowe = pobierzGrupyZajService.pobierzGrupyZajeciowe(idGrupyZaj);
 		int i;
 		int idgrupyZaj;
 		
 		List<JsonGroupZajeciowe> dates = new ArrayList<JsonGroupZajeciowe>();
 		for(i=0; i<grupzajeciowe.size(); i++) {
 			idgrupyZaj = grupzajeciowe.get(i).getIdGrupyZajeciowe();
 			
 			
 			for(){
 				for() {
 					for(){
 						for() {
 							
 						}
 					}
 					for() {
 						
 					}
 					for() {
 						
 					}
 				}
 				for() {
 					
 				}
 			}
 			jsonGroupWyzej.setDates(dates);
 		}
 		return jsonGroupWyzej;
 		/*
 		 * List<GrupyZajeciowe> grupyzajeciowe = pobierzGrupyService
 		 * .pobierzGrupyZajeciowe(login);
 		 */
 		/*
 		 * int idKursu ; int i; JsonKursy jsonKursy = new JsonKursy(); JsonGrupy
 		 * jsonGrupy = new JsonGrupy(); JsonGrupyZajeciowe jsonGrupyZajeciowe =
 		 * new JsonGrupyZajeciowe(); List<JsonGrupy> courses = new
 		 * ArrayList<JsonGrupy>(); 
 		 * for( i = 0; i<= grupyzajeciowe.size()-1;
 		 * i++){ 
 		 * idKursu = grupyzajeciowe.get(i).getIdKursu().getIdKursy();
 		 * 
 		 * List<Kursy> kurslist = pobierzGrupyService.pobierzKursy(idKursu);
 		 * 
 		 * for(Kursy kurs : kurslist) { List<JsonGrupyZajeciowe> name = new
 		 * ArrayList<JsonGrupyZajeciowe>(); for(GrupyZajeciowe grupa :
 		 * grupyzajeciowe) { // set dla JsonGrupyZajeciowe
 		 * jsonGrupyZajeciowe.setId(grupa.getIdKursu().getIdKursy());
 		 * jsonGrupyZajeciowe.setCode(grupa.getKodGrupy());
 		 * jsonGrupyZajeciowe.setName(grupa.getNazwa()); }
 		 * jsonGrupy.setName(kurs.getNazwaKursu());
 		 * jsonGrupy.setId(kurs.getIdKursy()); jsonGrupy.setDates(name); }
 		 * jsonKursy.setCourses(courses); }
 		 * 
 		 * return jsonKursy;
 		 */
 
 //	}*/
 
 	@RequestMapping(value = "/getcourses", method = RequestMethod.POST,  produces="application/json")
 	public @ResponseBody
 	JsonKursy wyslijKursy(
 			@RequestParam(value = "userid", required = true) int login,
 			Model model) {
 
 		List<GrupyZajeciowe> grupyzajeciowe = pobierzGrupyService
 				.pobierzGrupyZajeciowe(login);
 		int idKursu;
 		int i;
 		JsonKursy jsonKursy = new JsonKursy();
 		JsonGrupy jsonGrupy = new JsonGrupy();
 		JsonGrupyZajeciowe jsonGrupyZajeciowe = new JsonGrupyZajeciowe();
 		List<JsonGrupy> courses = new ArrayList<JsonGrupy>();
 		for (i = 0; i < grupyzajeciowe.size(); i++) {
 			idKursu = grupyzajeciowe.get(i).getIdKursu().getIdKursy();
 
 			List<Kursy> kurslist = pobierzGrupyService.pobierzKursy(idKursu);
 
 			for (Kursy kurs : kurslist) {
 				List<JsonGrupyZajeciowe> name = new ArrayList<JsonGrupyZajeciowe>();
 			
 				for(GrupyZajeciowe grupa : grupyzajeciowe) {				
 					//  set dla JsonGrupyZajeciowe
 					
 					jsonGrupyZajeciowe.setId(grupa.getIdKursu().getIdKursy());
 					jsonGrupyZajeciowe.setCode(grupa.getKodGrupy());
					jsonGrupyZajeciowe.setName(grupa.getTermin());
 				}
				name.add(jsonGrupyZajeciowe);
 				jsonGrupy.setName(kurs.getNazwaKursu());
 				jsonGrupy.setId(kurs.getIdKursy());
 				jsonGrupy.setDates(name);
 			}
			courses.add(jsonGrupy);
 			jsonKursy.setCourses(courses);
 			System.out.println("Test wydruk"+ jsonKursy);
 		}
 
 		return jsonKursy;
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.POST)
 	public @ResponseBody
 	int logout(@RequestParam(value = "userid", required = true) int login,
 			Model model) {
 		List<Prowadzacy> loglist = loginService.logout(login);
 		if (loglist.size() > 0) {
 			return 1;
 		}
 		return 0;
 	}
 
 	@RequestMapping(value = "/changepass", method = RequestMethod.POST)
 	public @ResponseBody
 	int zmien_haslo(
 			@RequestParam(value = "userid", required = true) int login,
 			@RequestParam(value = "oldpass", required = true) String stareHaslo,
 			@RequestParam(value = "newpass", required = true) String noweHaslo,
 			Model model) {
 		List<Prowadzacy> passlist = loginService
 				.validatePass(login, stareHaslo);
 		String zmienHaslo;
 		if (passlist.size() > 0) {
 			zmienHaslo = passlist.get(0).getHaslo();
 			if (zmienHaslo == stareHaslo) {
 				loginService.zmienPass(login, noweHaslo);
 				return 1;
 			}
 		}
 		return 0;
 	}
 
 	@RequestMapping(value = "/importcsv", method = RequestMethod.POST)
 	public @ResponseBody
 	String upload(
 			@RequestParam(value = "filecontent", required = false) CommonsMultipartFile file,
 			@RequestParam(value = "userid", required = true) int login,
 			Model model) {
 
 		InputStream is = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			is = file.getFileItem().getInputStream();
 			Reader reader = new InputStreamReader(is);
 			int data = reader.read();
 			while (data != -1) {
 				char theChar = (char) data;
 				sb.append(theChar);
 				data = reader.read();
 			}
 			reader.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		System.out.println(file.getFileItem().getFieldName());
 
 		if (file.isEmpty()) {
 			return "0";
 		} else {
 			importcsv.do_import(sb.toString(), login);
 			return "1";
 		}
 	}
 
 }
