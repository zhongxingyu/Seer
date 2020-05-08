 package v.facade;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 
 import util.SimpleFilter;
 import v.eao.CajaEaoLocal;
 import v.eao.FacturaVentaEaoLocal;
 import v.eao.PagoEaoLocal;
 import v.eao.RegistroPagoEaoLocal;
 import v.eao.UsuarioEaoLocal;
 import v.excepciones.GuardarException;
 import v.modelo.Caja;
 import v.modelo.FacturaVenta;
 import v.modelo.Pago;
 import v.modelo.RegistroPago;
 import v.modelo.Usuario;
 import v.ws.PagoWs;
 
 /**
  * Session Bean implementation class CajeroFacade
  */
 @Stateless
 public class CajeroFacade implements CajeroFacadeLocal {
 
 	@EJB
 	CajaEaoLocal cajaEao;
 	
 	@EJB
 	PagoEaoLocal pagoEao;
 	
 	@EJB
 	FacturaVentaEaoLocal ventaEao;
 	
 	@EJB
 	UsuarioEaoLocal usuarioEao;
 	
 	@EJB
 	RegistroPagoEaoLocal registroEao;
 
     public CajeroFacade() {
     
     }
     
     @Override
     public void cierredeCaja(Long idCajero) throws GuardarException{
     	
     	List<Pago> pagosDelDia = cajaEao.pagosNoCerrados(idCajero);
     	
     	if(pagosDelDia.isEmpty()){
     		throw new GuardarException("No existen pagos para cerrar");
     	}
     	
     	for (Pago pagoCierre : pagosDelDia){
     		Pago pago = pagoEao.getById(pagoCierre.getId());
     		pago.setEstado("cerrado");
     		pagoEao.modificar(pago);
     	}
     }
 
 	@Override
 	public int getTotalFacturasPendientes() {
 		return ventaEao.getTotalPendientes();
 	}
 
 	@Override
 	public List<FacturaVenta> listarFacturasPendientes(
 			List<SimpleFilter> plainFilters, int start, int limit) {
 		return ventaEao.listar(plainFilters, start, limit);
 	}
 	
 	@Override
 	public List<FacturaVenta> listarFacturas(List<SimpleFilter> plainFilters, int start, int limit) {
 		return ventaEao.listar(plainFilters, start, limit);
 	}	
 
 	@Override
 	public Pago registrarPago(Pago pago) throws GuardarException {
 		FacturaVenta factura = 	ventaEao.findById(pago.getFactura().getNumeroFactura());
 		Usuario cajero = usuarioEao.findByUsername(pago.getUsuario().getUsername());
 		Caja caja = cajero.getCaja();
 		
 		if(pago.getMonto() > factura.getSaldo()){
 			throw new GuardarException("El monto a pagar excede el saldo");
 		}
 		
 		pago.setFactura(factura);
 		pago.setUsuario(cajero);
 		pago.setCaja(caja);
 		pago.setFecha(new Date());
 		factura.setSaldo(factura.getSaldo() - pago.getMonto());
 		if(factura.getSaldo() == 0.0){
 			factura.setEstado("pagada");
 		}
 		return pagoEao.agregar(pago);
 	}
 	
 	@Override
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public Pago registrarPagoWebService(Pago pago) throws GuardarException {
 		FacturaVenta factura = 	ventaEao.findById(pago.getFactura().getNumeroFactura());
 		Usuario cajero = usuarioEao.findByUsername(pago.getUsuario().getUsername());
 		Caja caja = cajero.getCaja();
 		
 		if(pago.getMonto() > factura.getSaldo()){
 			throw new GuardarException("El monto a pagar excede el saldo");
 		}
 		
 		pago.setFactura(factura);
 		pago.setUsuario(cajero);
 		pago.setCaja(caja);
 		pago.setFecha(new Date());
 		factura.setSaldo(factura.getSaldo() - pago.getMonto());
 		if(factura.getSaldo() == 0.0){
 			factura.setEstado("pagada");
 		}
 		return pagoEao.agregar(pago);
 	}
     
 	@Override
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public List<PagoWs> registroPagosWebService(List<PagoWs> pagos) {
 		
 		List<PagoWs> pagosRegistrados = new ArrayList<PagoWs>();
 		
 		for (PagoWs pagoWs : pagos) {
 			
 			RegistroPago registro = new RegistroPago();
 			
 			Usuario usuario = usuarioEao.findById(pagoWs.getIdCajero());
 	    	FacturaVenta facturaVenta = ventaEao.findById(pagoWs.getIdFactura());
 	    	
 	    	Pago pago = new Pago();
 	    	
 	    	pago.setUsuario(usuario);
 	    	pago.setFactura(facturaVenta);
 	    	pago.setMonto(pagoWs.getMonto());
 	    	pago.setEstado("cerrado");
 	    	
 	    	try {
 				pago = registrarPagoWebService(pago);
 				registro.setRealizado(true);
 				pagoWs.setIdPago(pago.getId());
 				pagosRegistrados.add(pagoWs);
 				registro.setIdPago(pago.getId());
 	    	} catch (Exception e) {
 				registro.setRealizado(false);
 				registro.setMensajeError("Error de componente pago");
 	    	}
 	    	
     		registro.setFecha(new Date());
    		
    		//if (pago != null){
    		//	}	
    		
     		try {
 				registroEao.agregar(registro);
 			} catch (GuardarException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 	    	
 		}
 		return pagosRegistrados;	
 	}
 
 	@Override
 	public int getTotalPagos() {
 		return pagoEao.getTotalPagos();
 	}
 
 	@Override
 	public int getTotalPagosFactura(Integer numeroFactura) {
 		return pagoEao.getTotalPagosFactura(numeroFactura);
 	}
 
 	@Override
 	public List<Pago> listarPagos(List<SimpleFilter> filters, int start, int limit) {
 		return pagoEao.listar(filters, start, limit);
 	}
 
 	@Override
 	public int getTotalFacturas() {
 		return ventaEao.getTotalFacturas();
 	}
 
 	@Override
 	public int getTotalFacturasFilters(List<SimpleFilter> filters) {
 		return ventaEao.getTotalFacturasFilters(filters);
 	}
 
 	@Override
 	public int getTotalPagosFilters(List<SimpleFilter> plainFilters) {
 		return pagoEao.getTotalPagosFilters(plainFilters);
 	}
 }
