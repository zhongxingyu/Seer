 /**
  * 
  */
 package com.tokogame.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 
 import com.tokogame.domain.Kategori;
 import com.tokogame.domain.Koleksi;
 import com.tokogame.domain.KoleksiRelation;
 import com.tokogame.service.KategoriMaintenanceService;
 
 /**
  * @author willyam surya
  *
  */
 @UrlBinding("/action/kategoriMaintenance")
 public class KategoriMaintenanceActionBean extends BaseActionBean{
 
 	public int size;
 	public int fklk;
 	public List<Kategori> listKategori;
 	public List<Koleksi> listKoleksi;
 	public List<Integer> listBoxKoleksi;
 	public List<KoleksiRelation> listKoleksiRelation;
 	public Kategori kategori;
 	public Koleksi koleksi;
 	public KoleksiRelation koleksiRelation;
 	
 	@SpringBean
 	private KategoriMaintenanceService kategoriMaintenanceService;
 	
	//function to create offset position paging of the table
	private void makeOffset(String param) {
		try { 
			this.setOffset(Integer.parseInt(this.context.getRequest()
					.getParameter(param)));
		} catch (Exception e) {
		}
	} 
	
 	@Override
 	@DefaultHandler
 	public Resolution show() {
 		// TODO Auto-generated method stub
 		return this.search();
 	}
 	
 	public Resolution search(){
 		Kategori kategori = new Kategori();
 		
 		//show item list with paging
 		makeOffset("kategori_list.offset");
 		
 		listKategori = kategoriMaintenanceService.getKategoriList(kategori);
 		setSize(listKategori.size());
 		return new ForwardResolution("/WEB-INF/pages/admin/kategori_maintenance.jsp");
 	}
 	
 	public Resolution test() {
 		// TODO Auto-generated method stub
 		Koleksi koleksi = new Koleksi();
 		listKoleksi = kategoriMaintenanceService.getKoleksiList(koleksi);
 		
 		return new ForwardResolution("/WEB-INF/pages/admin/kategori_maintenance_field.jsp");
 	}
 	
 	public Resolution add(){
 		Koleksi koleksi = new Koleksi();
 		listKoleksi = kategoriMaintenanceService.getKoleksiList(koleksi);
 		
 		return new ForwardResolution("/WEB-INF/pages/admin/kategori_maintenance_field.jsp");
 	}
 	
 	public Resolution edit(){
 		listKategori = kategoriMaintenanceService.getKategoriList(kategori);
 		if(listKategori.size()>0){
 			kategori = listKategori.get(0);
 		}
 		
 		Koleksi koleksi = new Koleksi();
 		listKoleksi = kategoriMaintenanceService.getKoleksiList(koleksi);
 		listBoxKoleksi = new ArrayList<Integer>();
 		
 		listKoleksiRelation = kategoriMaintenanceService.selectSelectiveKoleksiRelation(kategori.getPkKategori());
 		for(int i=0; i < listKoleksiRelation.size(); i++){
 			int val = listKoleksiRelation.get(i).getFkKoleksi();
 			listBoxKoleksi.add(val);
 		}
 		return new ForwardResolution("/WEB-INF/pages/admin/kategori_maintenance_field.jsp");
 	}
 	
 	public Resolution delete(){
 		kategoriMaintenanceService.deleteKategori(kategori);
 
 		kategoriMaintenanceService.deleteKoleksiRelation(kategori.getPkKategori());
 		return this.show();
 	}
 	
 	public Resolution save(){
 		if(kategori.getPkKategori()==null){
 			if(kategoriMaintenanceService.countKategoriName(kategori.getKategoriName()) > 0){
 				this.addGlobalError("validation.kategori.kategoriname");
 				return this.test();
 			}
 			else if(kategoriMaintenanceService.countKategoriName(kategori.getKategoriName()) <= 0){
 				kategoriMaintenanceService.insertKategori(kategori);
 				if (listBoxKoleksi.size() != 0) {
 					for(int i = 0; i < listBoxKoleksi.size(); i++){
 						fklk = listBoxKoleksi.get(i).intValue();
 						KoleksiRelation koleksiRelation = new KoleksiRelation();
 						koleksiRelation.setFkKoleksi(fklk);
 						kategori.setPkKategori(kategori.getPkKategori());
 						koleksiRelation.setFkKategori(kategori.getPkKategori());
 						kategoriMaintenanceService.insertKoleksiRelation(koleksiRelation);
 					}		
 				}
 			}
 		}
 		else if(kategori.getPkKategori()!=null){
 			kategoriMaintenanceService.updateKategori(kategori);
 			kategoriMaintenanceService.deleteKoleksiRelation(kategori.getPkKategori());
 			if (listBoxKoleksi.size() != 0) {
 				for(int i = 0; i < listBoxKoleksi.size(); i++){
 					fklk = listBoxKoleksi.get(i).intValue();
 					KoleksiRelation koleksiRelation = new KoleksiRelation();
 					koleksiRelation.setFkKoleksi(fklk);
 					kategori.setPkKategori(kategori.getPkKategori());
 					koleksiRelation.setFkKategori(kategori.getPkKategori());
 					kategoriMaintenanceService.insertKoleksiRelation(koleksiRelation);
 				}		
 			}
 		}
 		
 //		if (listBoxKoleksi.size() != 0) {
 //			for(int i = 0; i < listBoxKoleksi.size(); i++){
 //				fklk = listBoxKoleksi.get(i).intValue();
 //				KoleksiRelation koleksiRelation = new KoleksiRelation();
 //				koleksiRelation.setFkKoleksi(fklk);
 //				kategori.setPkKategori(kategori.getPkKategori());
 //				koleksiRelation.setFkKategori(kategori.getPkKategori());
 //				kategoriMaintenanceService.insertKoleksiRelation(koleksiRelation);
 //			}		
 //		}
 		
 		return this.show();
 	}
 	
 	public Resolution cancel(){
 		
 		return this.show();
 	}
 
 	
 	/* Setter & Getter     */
 	
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public List<Kategori> getListKategori() {
 		return listKategori;
 	}
 
 	public void setListKategori(List<Kategori> listKategori) {
 		this.listKategori = listKategori;
 	}
 
 	public Kategori getKategori() {
 		return kategori;
 	}
 
 	public void setKategori(Kategori kategori) {
 		this.kategori = kategori;
 	}
 	
 	public List<Koleksi> getListKoleksi() {
 		return listKoleksi;
 	}
 
 	public void setListKoleksi(List<Koleksi> listKoleksi) {
 		this.listKoleksi = listKoleksi;
 	}
 	
 	public Koleksi getKoleksi() {
 		return koleksi;
 	}
 
 	public void setKoleksi(Koleksi koleksi) {
 		this.koleksi = koleksi;
 	}
 	
 	public KoleksiRelation getKoleksiRelation() {
 		return koleksiRelation;
 	}
 
 	public void setKoleksiRelation(KoleksiRelation koleksiRelation) {
 		this.koleksiRelation = koleksiRelation;
 	}
 
 	public List<Integer> getListBoxKoleksi() {
 		return listBoxKoleksi;
 	}
 
 	public void setListBoxKoleksi(List<Integer> listBoxKoleksi) {
 		this.listBoxKoleksi = listBoxKoleksi;
 	}
 
 	
 }
