 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aplikasi.peserta.service.impl.dummy;
 
 import aplikasi.peserta.domain.Peserta;
 import aplikasi.peserta.service.ManajemenPesertaService;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *
  * @author Student-03
  */
 public class ManajemenPesertaServiceDummy implements ManajemenPesertaService {
 
     private Map<Integer, Peserta> dbPalsu = new TreeMap<Integer, Peserta>();
     
     @Override
     public void simpan(Peserta p) {
         if(p.getId() == null){
             p.setId(dbPalsu.size());
         }
        
        // fix bug kalau id dikosongkan di WS, ternyata diset jadi 0
        Peserta px = dbPalsu.get(p.getId());
        if(px == null){
            p.setId(dbPalsu.size());
        }
         dbPalsu.put(p.getId(), p);
     }
 
     @Override
     public Peserta findPesertaById(Integer id) {
         return dbPalsu.get(id);
     }
 
     @Override
     public Peserta findPesertaByNomerPeserta(String no) {
         for(Integer i : dbPalsu.keySet()){
             Peserta p = dbPalsu.get(i);
             if(p.getNomerPeserta().equals(no)) {
                 return p;
             }
         }
         return null;
     }
 
     @Override
     public List<Peserta> findSemuaPeserta(Integer start, Integer rows) {
         List<Peserta> hasil = new ArrayList<Peserta>();
         for(Integer i : dbPalsu.keySet()){
             Peserta p = dbPalsu.get(i);
             hasil.add(p);
         }
         return hasil;
     }
 
     @Override
     public Long countSemuaPeserta() {
        return new Long(dbPalsu.size());
     }
     
 }
