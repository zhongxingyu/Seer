 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.absensi.controller;
 
 import com.artivisi.absensi.converter.PesertaConverter;
 import com.artivisi.absensi.domain.Kehadiran;
 import com.artivisi.absensi.domain.Peserta;
 import com.artivisi.absensi.service.AplikasiAbsenService;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author endy
  */
 @Controller
 public class KehadiranController {
     @Autowired
     private AplikasiAbsenService service;
     
     @InitBinder
     public void initBinder(WebDataBinder binder){
         
         // converter untuk tipe data tanggal (Date)
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         binder.registerCustomEditor(Date.class, new CustomDateEditor(formatter, true));
         
         // konverter untuk tipe data Peserta
         PesertaConverter px = new PesertaConverter(service);
         binder.registerCustomEditor(Peserta.class, px);
     }
     
     @RequestMapping("/kehadiran/list")
     public ModelMap daftarKehadiran(){
         ModelMap mm = new ModelMap();
         
         List<Kehadiran> data = service.cariSemuaKehadiran();
         mm.addAttribute("daftarKehadiran", data);
         System.out.println("Jumlah data : "+data.size());
         
         return mm;
     }
     
     @RequestMapping(value="/kehadiran/form", method= RequestMethod.GET)
     public ModelMap tampilkanForm(){
         System.out.println("Tampilkan form kehadiran");
         ModelMap mm = new ModelMap();
         Kehadiran k = new Kehadiran();
         mm.addAttribute("kehadiran", k);
         return mm;
     }
     
     @RequestMapping(value="/kehadiran/form", method= RequestMethod.POST)
     public String prosesForm(@ModelAttribute @Valid Kehadiran x, BindingResult errors){
         System.out.println("Memproses form kehadiran");
        if(x.getPeserta() != null){
            System.out.println("Peserta : "+x.getPeserta().getNomor());
        }
         System.out.println("Jam Masuk : "+x.getJamMasuk());
         System.out.println("Jam Pulang : "+x.getJamPulang());
         
         if(errors.hasErrors()) {
             return "/kehadiran/form";
         } 
         
         service.simpan(x);
         return "redirect:list";
     }
 }
