 package com.zarcillo.service;
 
 import com.zarcillo.dto.compra.ActualizarExistencia;
 import com.zarcillo.dto.compra.ExistenciaValorizada;
 import com.zarcillo.dao.CrudDAO;
 import com.zarcillo.dao.ExistenciaDAO;
 import com.zarcillo.dao.LineaDAO;
 import com.zarcillo.dao.ListadoExistenciaDAO;
 import com.zarcillo.dao.LoteDAO;
 import com.zarcillo.dao.PeriodoDAO;
 import com.zarcillo.domain.Existencia;
 import com.zarcillo.domain.Linea;
 import com.zarcillo.domain.Periodo;
 import com.zarcillo.dto.almacen.Inventario;
 import com.zarcillo.dto.almacen.InventarioLote;
 import com.zarcillo.dto.almacen.InventarioValorizado;
 import com.zarcillo.dto.venta.ListadoPrecio;
 import com.zarcillo.negocio.Igv;
 import com.zarcillo.negocio.Numero;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author saisa
  */
 @Service("listadoExistenciaService")
 @Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
 public class ListadoExistenciaServiceImpl implements ListadoExistenciaService {
 
     @Autowired
     private CrudDAO cruddao;
     @Autowired
     private ExistenciaDAO existenciadao;
     @Autowired
     private ListadoExistenciaDAO listadoexistenciadao;
     @Autowired
     private PeriodoDAO periododao;
     @Autowired
     private LineaDAO lineadao;
 
     @Override
     public List<ListadoPrecio> listadoPrecio(Integer idalmacen, List<Integer> lista) {
         List<ListadoPrecio> listaPrecio = new ArrayList<>();
         Periodo periodo = periododao.buscarPorFecha(new Date());
 
         List<Existencia> listaExistencia = existenciadao.listaPorIdalmacenPorLineasConStock(idalmacen, lista);
         ListadoPrecio listado;
         for (Existencia e : listaExistencia) {
             listado = new ListadoPrecio();
             listado.setIdproducto(e.getIdproducto());
             listado.setNcosuni(e.getNcosuni());
             listado.setNstock(e.getNstock());
             listado.setNstockm(e.getNstockm());
 
             listado.setNincremento(e.getIdproducto().getIdsublinea().getIdlinea().getNincremento());
 
 
             if (!Numero.isCero(e.getNvalven())) {
                 listado.setNvaluni(e.getNvalven());
                 BigDecimal nvalorunitariofraccion = listado.getNvaluni().divide(new BigDecimal(e.getIdproducto().getNmenudeo()), 4, BigDecimal.ROUND_HALF_UP);
                 listado.setNvalunim(nvalorunitariofraccion);
             } else {
                 listado.setNvaluni(e.getNcosuni().add(listado.getNcosuni().multiply(e.getIdproducto().getIdsublinea().getIdlinea().getNincremento().divide(Numero.cien))));
                 BigDecimal nvalorunitariofraccion = listado.getNvaluni().divide(new BigDecimal(e.getIdproducto().getNmenudeo()), 4, BigDecimal.ROUND_HALF_UP);
                 listado.setNvalunim(nvalorunitariofraccion);
                 listado.setNincremento(new BigDecimal("0"));
             }
 
             listado.setNprecos(Igv.importeDetalleVenta(listado.getNcosuni(), e.getIdproducto().getBinafecto()));
             listado.setNpreuni(Igv.importeDetalleVenta(listado.getNvaluni(), e.getIdproducto().getBinafecto()));
             listado.setNpreunim(Igv.importeDetalleVenta(listado.getNvalunim(), e.getIdproducto().getBinafecto()));
             listado.calculaUtilidad();
             listaPrecio.add(listado);
         }
 
         return listaPrecio;
     }
 
     @Override
     public List<Inventario> listaInventarioPorIdalmacenPorLineas(Integer idalmacen, List<Integer> lista) {
         List<Existencia> listaExistencia = existenciadao.listaPorIdalmacenPorLineasConStock(idalmacen, lista);
         List<Inventario> listaInventario = new ArrayList<>();
 
         Inventario inventario;
         BigDecimal nstockfraccion;
         BigDecimal nstockentero;
         BigDecimal nstocktotal;
         for (Existencia e : listaExistencia) {
             inventario = new Inventario();
             nstocktotal = new BigDecimal("0");
             inventario.setIdproducto(e.getIdproducto());
             inventario.setCubicacion(e.getCubicacion());
             inventario.setNstock(e.getNstock());
             inventario.setNstockm(e.getNstockm());
             inventario.setNcosuni(e.getNcosuni());
 
             nstockentero = new BigDecimal(e.getNstock());
             nstockfraccion = new BigDecimal(e.getNstockm()).divide(new BigDecimal(e.getIdproducto().getNmenudeo()), 2, BigDecimal.ROUND_HALF_EVEN);
             nstocktotal = nstockentero.add(nstockfraccion);
             inventario.setNsubcos(inventario.getNcosuni().multiply(nstocktotal));
             inventario.setNprecos(Igv.importeDetalleVenta(inventario.getNsubcos(), e.getIdproducto().getBinafecto()));
 
 
             listaInventario.add(inventario);
         }
         return listaInventario;
     }
 
     @Override
     public List<InventarioLote> listaInventarioLotePorIdalmacenPorLineas(Integer idalmacen, List<Integer> lista) {
         return listadoexistenciadao.listaPorIdalmacenPorLineasConStock(idalmacen, lista);
     }
 
     @Override
    public List<InventarioValorizado> listaInventarioValorizadoPorIdalmacen(Integer idalmacen) {      
         List<Linea> listaLinea = lineadao.listaConStock(idalmacen);
 
         List<InventarioValorizado> listaRetorno = new ArrayList<>();
 
         BigDecimal ntotal = new BigDecimal("0");
        
         InventarioValorizado inventario;
         for (Linea l : listaLinea) {           
             inventario=costoPorIdalmacenPorIdlinea(idalmacen, l);
             listaRetorno.add(inventario);            
             
             ntotal=ntotal.add(inventario.getNcosto());
             
         }
         
         
 
         BigDecimal nparticipacion;
         for (InventarioValorizado i : listaRetorno) {
             if (Numero.isCero(i.getNcosto())) {
                 nparticipacion = i.getNcosto().divide(ntotal, 4, BigDecimal.ROUND_HALF_UP);
             } else {
                 nparticipacion = Numero.cero;
             }
             nparticipacion = nparticipacion.multiply(Numero.cien);
             i.setNporcentaje(nparticipacion);
         }
 
         return listaRetorno;
 
     }
 
     private InventarioValorizado costoPorIdalmacenPorIdlinea(Integer idalmacen, Linea linea) {
 
         List<Existencia> listaExistencia = existenciadao.listaPorIdalmacenPorIdlinea(idalmacen, linea.getIdlinea());
         
        
         BigDecimal ncosuni ;
         BigDecimal ncosto = new BigDecimal("0");
         BigDecimal nprecos = new BigDecimal("0");
         
         BigDecimal nstockentero;
         BigDecimal nstocfraccion;
         BigDecimal nstocktotalactual;
         for (Existencia e : listaExistencia) {
             ncosuni=new BigDecimal("0");
             nstockentero = new BigDecimal(e.getNstock());
             nstocfraccion = new BigDecimal(e.getNstockm()).divide(new BigDecimal(e.getIdproducto().getNmenudeo()), 2, BigDecimal.ROUND_HALF_UP);
             nstocktotalactual = nstockentero.add(nstocfraccion);
             ncosuni = (e.getNcosuni().multiply(nstocktotalactual));
             ncosto=ncosto.add(ncosuni);
            nprecos=nprecos.add(Igv.importeDetalleVenta(ncosuni, e.getIdproducto().getBinafecto()));
             
         }
 
         InventarioValorizado inventario=new InventarioValorizado();
         inventario.setIdlinea(linea);
         inventario.setNcosto(ncosto);
         inventario.setPcosto(nprecos);
         
         return inventario;
 
     }
 
     @Override
     public List<ActualizarExistencia> listaActualizarPrecioPorDescripcion(Integer idalmacen, String criterio) {
         List<Existencia> listaExistencias = existenciadao.busquedaListaPorIdalmacenPorDescripcion(idalmacen, criterio);
         List<ActualizarExistencia> listaRetorno = new ArrayList<>();
         ActualizarExistencia a;
         for (Existencia e : listaExistencias) {
             a = new ActualizarExistencia();
             a.setIdproducto(e.getIdproducto());
             a.setIdalmacen(e.getIdalmacen());
             a.setNstock(e.getNstock());
             a.setNstockm(e.getNstockm());
             a.setNcosuni(e.getNcosuni());
             a.setNcospre(Igv.importeDetalleVenta(e.getNcosuni(), e.getIdproducto().getBinafecto()));
             a.setNincremento(e.getIdproducto().getIdsublinea().getIdlinea().getNincremento());
 
             if (!Numero.isCero(e.getNvalven())) {
                 a.setNvaluni(e.getNvalven());
             } else {
                 a.setNvaluni(e.getNcosuni().add(a.getNcosuni().multiply(e.getIdproducto().getIdsublinea().getIdlinea().getNincremento().divide(Numero.cien))));
                 a.setNincremento(new BigDecimal("0"));
             }
 
             a.setNpreuni(Igv.importeDetalleVenta(a.getNvaluni(), e.getIdproducto().getBinafecto()));
             a.calculaUtilidad();
 
             listaRetorno.add(a);
         }
 
         return listaRetorno;
 
 
     }
 
     @Override
     @Transactional
     public void actualizarPrecio(ActualizarExistencia actualizar) {
         try {
             Existencia existencia = existenciadao.buscarPorIdalmacenPorIdproducto(actualizar.getIdalmacen().getIdalmacen(), actualizar.getIdproducto().getIdproducto());
             BigDecimal nvaluni = Igv.valorVentaDetalleVenta(actualizar.getNpreuni(), existencia.getIdproducto().getBinafecto());
             existencia.setNvalven(nvaluni);
             cruddao.actualizar(existencia);
         } catch (Exception e) {
             e.printStackTrace();
             throw new ExceptionZarcillo(e.getMessage());
         }
     }
 
     @Override
     public List<ExistenciaValorizada> listaExistenciaValorizadaPorIdalmacenPorIdlinea(Integer idalmacen, Integer idlinea) {
         List<Existencia> listaExistencia = existenciadao.listaPorIdalmacenPorIdlineaConStock(idalmacen, idlinea);
         List<ExistenciaValorizada> listaRetorno = new ArrayList<>();
         ExistenciaValorizada existencia;
         BigDecimal nstockentero;
         BigDecimal nstocfraccion;
         BigDecimal nstocktotalactual;
         for (Existencia e : listaExistencia) {
             existencia = new ExistenciaValorizada();
             existencia.setIdalmacen(e.getIdalmacen());
             existencia.setIdproducto(e.getIdproducto());
             existencia.setNstock(e.getNstock());
             existencia.setNstockm(e.getNstockm());
             existencia.setNcosuni(e.getNcosuni());
 
             nstockentero = new BigDecimal(existencia.getNstock());
             nstocfraccion = new BigDecimal(existencia.getNstockm()).divide(new BigDecimal(e.getIdproducto().getNmenudeo()), 2, BigDecimal.ROUND_HALF_UP);
             nstocktotalactual = nstockentero.add(nstocfraccion);
             existencia.setNsubcos(e.getNcosuni().multiply(nstocktotalactual));
             existencia.setNprecos(Igv.importeDetalleVenta(existencia.getNsubcos(), existencia.getIdproducto().getBinafecto()));
 
             listaRetorno.add(existencia);
         }
 
         return listaRetorno;
     }
 }
