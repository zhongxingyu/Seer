 package com.artivisi.aplikasi.controller;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.velocity.runtime.directive.Foreach;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.artivisi.aplikasi.internal.MasterGroupService;
 import com.artivisi.aplikasi.internal.MasterPegawaiService;
 import com.artivisi.aplikasi.internal.MasterUserService;
 import com.artivisi.aplikasi.internal.SaldoKasbonService;
 import com.artivisi.aplikasi.internal.TransaksiKasbonService;
 import com.artivisi.aplikasi.internal.entity.MasterGroup;
 import com.artivisi.aplikasi.internal.entity.MasterPegawai;
 import com.artivisi.aplikasi.internal.entity.MasterUser;
 import com.artivisi.aplikasi.internal.entity.SaldoKasbon;
 import com.artivisi.aplikasi.internal.entity.TrKasbon;
 
 @Controller
 public class KasbonController {
 
 	@Autowired
 	private TransaksiKasbonService kasbonService;
 	
 	@Autowired
 	private MasterPegawaiService pegawaiService;
 	
 	@Autowired
 	private SaldoKasbonService saldoKasbonService;
 	
 	@Autowired
 	private MasterUserService userService;
 	
 	@Autowired
 	private MasterGroupService groupService;
 	
 	//@Autowired
 	//private BayarKasbonService bayarKasbon;
 	
 	private String tanggal="yyyy-MM-dd";
 	
 	@InitBinder
 	public void initBinder(WebDataBinder binder){
 		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(tanggal), true));
 		binder.registerCustomEditor(MasterPegawai.class, new PegawaiEditor(pegawaiService));
 	}
 	
 	public List<MasterPegawai> pegawai() throws Exception{
 		return pegawaiService.semuaPegawai();
 	}
 	
 	@RequestMapping(value="/transaksi/formKasbon", method=RequestMethod.GET)
 	public ModelMap displayFormKasbon(){
 		return new ModelMap().addAttribute("daftarPegawai",pegawaiService.semuaPegawai());
 	}
 	
 	@RequestMapping(value="/transaksi/formKasbon", method=RequestMethod.POST)
 	public String saveFormKasbon(@ModelAttribute TrKasbon kasbon){
 		MasterPegawai mp = pegawaiService.findById(kasbon.getMasterPegawai().getId());
 		if (mp!=null){
 			SaldoKasbon sk=saldoKasbonService.findByPegawai(mp);
 			kasbon.setSaldoAwal(sk.getSaldoAkhir());
 			kasbon.setSaldoAkhir(sk.getSaldoAkhir().add(kasbon.getNilai()));
 			kasbon.setType("kasbon");
 			kasbonService.saveKasbon(kasbon);
 			sk.setSaldoAkhir(sk.getSaldoAkhir().add(kasbon.getNilai()));
 			sk.setTanggal(kasbon.getTanggal());
 			saldoKasbonService.saveSaldoKasbon(sk);
 		}
 		return "redirect:saldoKasbon";
 	}
 	
 	@RequestMapping(value="/transaksi/formBayarKasbon", method=RequestMethod.GET)
 	public ModelMap displayFormBayarKasbon(@RequestParam MasterPegawai idPeg){
 		ModelMap mm = new ModelMap();
 		SaldoKasbon saldo=saldoKasbonService.findByPegawai(idPeg);
 		mm.addAttribute("dataSaldo",saldo);
 		return mm;
 	}
 	
 	@RequestMapping(value="/transaksi/formBayarKasbon", method=RequestMethod.POST)
 	public String saveFormBayarKasbon(@ModelAttribute TrKasbon bayar){
 		bayar.setSaldoAkhir(bayar.getSaldoAwal().subtract(bayar.getNilai()));
 		bayar.setType("bayar");
 		kasbonService.saveKasbon(bayar);
 		MasterPegawai mp=pegawaiService.findById(bayar.getMasterPegawai().getId());
 		if (mp!=null){
 			SaldoKasbon saldo=saldoKasbonService.findByPegawai(mp);
 			saldo.setTanggal(bayar.getTanggal());
 			saldo.setSaldoAkhir(bayar.getSaldoAwal().subtract(bayar.getNilai()));
 			saldoKasbonService.saveSaldoKasbon(saldo);
 		}
 		return "redirect:saldoKasbon";
 	}
 	
 	@RequestMapping(value="/transaksi/saldoKasbon", method=RequestMethod.GET)
 	public ModelMap saldoKasbonKeseluruhan(){
 		ModelMap mm=new ModelMap();
 		mm.addAttribute("saldo", saldoKasbonService.listSaldoKasbon());
 		BigDecimal jumlah=new BigDecimal(0);
 		for (SaldoKasbon s : saldoKasbonService.listSaldoKasbon()) {
 			jumlah=jumlah.add(s.getSaldoAkhir());
 		}
 		mm.addAttribute("jumlah", jumlah);
 		return mm;
 	}
 	
 	@RequestMapping(value="/transaksi/laporanKasbon", method=RequestMethod.GET)
 	public ModelMap formLapKasbon(){
 		MasterUser mu=userService.findByUsername(SecurityHelper.getCurrentUsername());
 		Long id=new Long(2);
 		MasterGroup mg=groupService.findById(id);
 		if (mu.getMasterGroup().getId()!=mg.getId()){
 			ModelMap mm=new ModelMap();
 			List<MasterPegawai> a=pegawaiService.findByUser(mu.getMasterPegawai().getId());
 			mm.addAttribute("daftarPegawai", a);
 			return mm;
 		}else{
 			ModelMap mm=new ModelMap();
 			List<MasterPegawai> a=pegawaiService.semuaPegawai();
 			mm.addAttribute("daftarPegawai", a);
 			return mm;
 		}
 	}
 	
 	@RequestMapping(value="/transaksi/lapKasbon", method=RequestMethod.POST)
 	public ModelMap lapKasbonPerPegawai(@RequestParam MasterPegawai masterPegawai, Date mulai, Date sampai){
 		ModelMap mm = new ModelMap();
 		List<TrKasbon> kasbon=kasbonService.findAllKasbonByPegawai(masterPegawai, mulai, sampai);
 		BigDecimal jumlah1=new BigDecimal(0);
 		for (TrKasbon ksbn : kasbon) {
 			jumlah1=jumlah1.add(ksbn.getNilai());
 		}	
 		List<TrKasbon> bayar=kasbonService.findAllBayarByPegawai(masterPegawai, mulai, sampai);
 		BigDecimal jumlah2=new BigDecimal(0);
 		for (TrKasbon ksb : bayar) {
 			jumlah2=jumlah2.add(ksb.getNilai());
 		}
 		mm.addAttribute("kasbon",kasbonService.findAllKasbonByPegawai(masterPegawai, mulai, sampai));
 		mm.addAttribute("bayar",kasbonService.findAllBayarByPegawai(masterPegawai, mulai, sampai));
 		mm.addAttribute("all",kasbonService.findAllByPegawai(masterPegawai,mulai,sampai));
 		mm.addAttribute("saldoAwal", kasbonService.findSaldoAwalByDate(masterPegawai, mulai, sampai));
 		mm.addAttribute("jumlahKasbon",jumlah1);
 		mm.addAttribute("jumlahBayar",jumlah2);
 		mm.addAttribute("mulai",new SimpleDateFormat("yyyy-MM-dd").format(mulai));
 		mm.addAttribute("sampai",new SimpleDateFormat("yyyy-MM-dd").format(sampai));
 		mm.addAttribute("header",saldoKasbonService.findByPegawai(masterPegawai));
 		return mm;
 	}
 	
	@RequestMapping("/transaksi/currentUsername")
	@ResponseBody
	public Map<String, Object> infoUser(){
 
 		Map<String, Object> info = new HashMap<String, Object>();
 		info.put("user", SecurityHelper.getCurrentUsername());
 		info.put("role", userService.findByUsername(SecurityHelper.getCurrentUsername()).getMasterGroup().getNamaGroup());
		
 		return info;
 	}
 	
 }
