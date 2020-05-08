 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package perpus.service;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import org.hibernate.SessionFactory;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import perpus.domain.LaporanPeminjamanDto;
 import perpus.domain.PeminjamanDetail;
 import perpus.domain.PengembalianDetail;
 
 /**
  *
  * @author adi
  */
 @Service("reportService")
 public class ReportServiceImpl implements ReportService{
     
     @Autowired private SessionFactory sessionFactory;
     @Autowired private TransaksiService transaksiService;
     
     public List<LaporanPeminjamanDto> dataLaporanPeminjaman(String mode, Date mulai, Date sampai){
         List<LaporanPeminjamanDto> result = new ArrayList<LaporanPeminjamanDto>();
         
         if(mode.equalsIgnoreCase("PEMINJAMAN")){
             List<PeminjamanDetail> detailPeminjamans = transaksiService.getTransaksiBelumKembali(mulai, sampai);
             for(PeminjamanDetail p : detailPeminjamans){
                 PengembalianDetail kembali = 
                         transaksiService.getPengembalianByIdPinjamAndKodeBuku(
                             p.getHeader().getId(), p.getBuku().getId());
 
                 LaporanPeminjamanDto lpd = new LaporanPeminjamanDto();
                lpd.setId(p.getHeader().getId().toString());
                 lpd.setKodeAnggota(p.getHeader().getAnggota().getKodeAnggota());
                 lpd.setNamaAnggota(p.getHeader().getAnggota().getNamaAnggota());
                 lpd.setTglPinjam(p.getHeader().getTglPinjam());
                 lpd.setTglKembali(p.getHeader().getTglKembali());
                 lpd.setKodeBuku(p.getBuku().getKodeBuku());
                 lpd.setJudul(p.getBuku().getJudulBuku());
 
                 if(kembali != null){
                     lpd.setTglKembaliSebenarnya(new DateTime(lpd.getTglKembali())
                             .plusDays(kembali.getTelat()).toDate());
                     lpd.setTelat(kembali.getTelat());
                     lpd.setDenda(kembali.getDenda());
                     lpd.setStatus("Sudah Kembali");
                 } else {
                     lpd.setStatus("Belum Kembali");
                 }
                 result.add(lpd);
             }
         } else {
             List<PengembalianDetail> detailpengembalians = 
                     transaksiService.getTransaksiPengembalian(mulai, sampai);
             for (PengembalianDetail p : detailpengembalians) {
                 LaporanPeminjamanDto lpd = new LaporanPeminjamanDto();
                lpd.setId(p.getHeader().getId().toString());
                 lpd.setKodeAnggota(p.getHeader().getTransaksiPeminjaman().getAnggota().getKodeAnggota());
                 lpd.setNamaAnggota(p.getHeader().getTransaksiPeminjaman().getAnggota().getNamaAnggota());
                 lpd.setTglPinjam(p.getHeader().getTransaksiPeminjaman().getTglPinjam());
                 lpd.setTglKembali(p.getHeader().getTransaksiPeminjaman().getTglKembali());
                 lpd.setKodeBuku(p.getBuku().getKodeBuku());
                 lpd.setJudul(p.getBuku().getJudulBuku());
                 lpd.setTglKembaliSebenarnya(p.getCreatedDate());
                 lpd.setTelat(p.getTelat());
                 lpd.setDenda(p.getDenda());
                 lpd.setStatus("Sudah Kembali");
                 result.add(lpd);
             }
         }
         return result;
     }
     
     @Override
     public JasperPrint printLaporanPeminjaman(String mode, Date mulai, Date sampai){
         try {
             InputStream inputStream = 
                     getClass().getResourceAsStream("/perpus/jrxml/LaporanPeminjaman.jasper");
             List<LaporanPeminjamanDto> listWrappers = dataLaporanPeminjaman(mode, mulai, sampai);
             Map<String, Object> parameters = new HashMap<String, Object>();
             parameters.put("tglMulai", mulai);
             parameters.put("tglSampai", sampai);
             
             if(mode.equalsIgnoreCase("PEMINJAMAN")){
                 parameters.put("title", "LAPORAN PEMINJAMAN BUKU");
             } else {
                 parameters.put("title", "LAPORAN PENGEMBALIAN BUKU");
             }
 
             JasperPrint j =
                     JasperFillManager.fillReport(
                     inputStream, parameters, new JRBeanCollectionDataSource(listWrappers));
 
             return j;
         } catch (JRException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 }
