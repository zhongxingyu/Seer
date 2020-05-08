 import java.util.ArrayList;
 
 public class AdminCRM {
 
     private ArrayList<Cliente> pro;
     private ArrayList<Cliente> cli;
     private ArrayList<Ventas> ven;
 
     public AdminCRM() {
         pro = new ArrayList<Cliente>();
         cli = new ArrayList<Cliente>();
         ven = new ArrayList<Ventas>();
     }
 
     // Validar datos incompletos de Ventas
     public void validarDatosIncompletosVentas(String numero, String fecha_emision, String fecha_vencimiento, String empresa, String fecha_pago, String estado, String concepto, double subtotal, double igv, double total, String moneda) throws BusinessException {
         String msg = "";
         if (numero == null || numero.isEmpty()) {
             msg = "El numero de documento no puede ser vacio o nulo";
         }
         if (fecha_emision == null || fecha_emision.isEmpty()) {
             msg += "\nFecha de Emision no pueder ser vacio o nulo";
         }
         if (fecha_vencimiento == null || fecha_vencimiento.isEmpty()) {
             msg += "\nFecha de vencimiento no pueder ser vacio o nulo";
         }
 
         if (!msg.isEmpty()) {
             throw new BusinessException(msg);
         }
     }
 
     // Validar datos incompletos de Clientes/Prospectos
     public void validarDatosIncompletosClientes(String nombres, String apellidopat, String apellidomat, String email, String dni, String telefono, String FechaContacto) throws BusinessException {
 
         String msg = "";
 
         if (nombres == null || nombres.isEmpty()) {
             msg = "El nombre del cliente no puede estar vacio o nulo";
         }
 
         if (apellidopat == null || apellidopat.isEmpty()) {
             msg = "El apellido paterno del cliente no puede estar vacio o nulo";
         }
 
         if (apellidomat == null || apellidomat.isEmpty()) {
             msg = "El apellido materno del cliente no puede estar vacio o nulo";
         }
 
         if (email == null || email.isEmpty()) {
             msg = "El email del cliente no puede estar vacio o nulo";
         }
 
         if (dni == null || dni.isEmpty()) {
             msg = "El DNI del cliente no puede estar vacio o nulo";
         }
 
         if (telefono == null || telefono.isEmpty()) {
             msg = "El número de teléfono o celular del cliente no puede estar vacio o nulo";
         }
 
         if (FechaContacto == null || FechaContacto.isEmpty()) {
             msg = "La fecha de contacto del cliente no puede estar vacio o nulo";
         }
 
 
         if (!msg.isEmpty()) {
             throw new BusinessException(msg);
         }
     }
 
     // Validar que la venta a realizar no este duplicada
     public void validarDuplicidadVentas(String numero) throws BusinessException {
         if (Fn_buscar_nro_documento(numero) != null) {
             String msg = "Documento " + numero + " ya existe.";
             throw new BusinessException(msg);
         }
     }
 
     // Validar que el cliente a ingresar no exista
     public void validarDuplicidadClientes(String nombres) throws BusinessException {
         if (Fn_buscar_Cliente(nombres) != null) {
             String msg = "El cliente " + nombres + " ya existe.";
             throw new BusinessException(msg);
         }
     }
 
     // Validar que el prospecto a ingresar no exista
     public void validarDuplicidadProspecto(String nombres) throws BusinessException {
         if (Fn_buscar_Prospecto(nombres) != null) {
             String msg = "El prospecto " + nombres + " ya existe.";
             throw new BusinessException(msg);
         }
     }
 
     public void ValidarEsNumerico(String numero) throws BusinessException {
         if (!numero.matches("\\d*")) {
             String mensaje = "Debe de ingresar solo números.";
             throw new BusinessException(mensaje);
         }
     }
 
     // Función para buscar cliente
     public Cliente Fn_buscar_Cliente(String nombres) {
         for (Cliente cli : fn_getCliente()) {
             if (cli.getNombres().trim().equals(nombres)) {
                 return cli;
             }
         }
         return null;
     }
 
     // Función para buscar prospecto
     public Cliente Fn_buscar_Prospecto(String nombres) {
         for (Cliente cli : fn_getProspecto()) {
             if (cli.getNombres().trim().equals(nombres)) {
                 return cli;
             }
         }
         return null;
     }
 
     // Función para buscar ventas por numero de documento
     public Ventas Fn_buscar_nro_documento(String numero) {
         for (Ventas doc : fn_getVentas()) {
             if (doc.getNumero().trim().equals(numero)) {
                 return doc;
             }
         }
         return null;
     }
 
     // Función para buscar ventas por nombre de cliente
     public Ventas Fn_buscar_Venta_x_Cliente(String nombre) {
         for (Ventas doc : fn_getVentas()) {
             if (doc.getEmpresa().trim().equals(nombre)) {
                 return doc;
             }
         }
         return null;
     }
 
     // Validar que la ventas exista
     private void validarExistenciaVenta(String numero)
             throws BusinessException {
         if (Fn_buscar_nro_documento(numero) == null) {
             String msg = "Numero de documento " + numero + " no existe.";
             throw new BusinessException(msg);
         }
     }
 
     // Validar que el cliente exista
     private void validarExistenciaCliente(String nombres) throws BusinessException {
         if (Fn_buscar_Cliente(nombres) == null) {
             String msg = "El cliente " + nombres + " no existe";
             throw new BusinessException(msg);
         }
     }
 
     // Validar que el cliente no tenga ventas registradas
     public void validarVentas_x_Clientes(String nombres) throws BusinessException {
         if (Fn_buscar_Venta_x_Cliente(nombres) != null) {
             String msg = "El cliente " + nombres + " ya tiene transacciones creadas.";
             throw new BusinessException(msg);
         }
     }
 
     // Registar ventas
     public void registrarVenta(String numero, String fecha_emision, String fecha_vencimiento, String empresa, String fecha_pago, String estado, String concepto, double subtotal, double igv, double total, String moneda) throws BusinessException {
         // Validar datos incompletos
         validarDatosIncompletosVentas(numero, fecha_emision, fecha_vencimiento, empresa, fecha_pago, estado, concepto, subtotal, igv, total, moneda);
         // Validar que exista documento
         validarDuplicidadVentas(numero);
         fn_getVentas().add(new Ventas(numero, fecha_emision, fecha_vencimiento, empresa, fecha_pago, estado, concepto, subtotal, igv, total, moneda));
     }
 
     //Registrar cliente
     public void registrarCliente(String nombres, String apellidopat, String apellidomat, String email, String dni, String telefono, String FechaContacto, int estado) throws BusinessException {
         validarDatosIncompletosClientes(nombres, apellidopat, apellidomat, email, dni, telefono, FechaContacto);
         validarDuplicidadClientes(nombres);
         fn_getCliente().add(new Cliente(nombres, apellidopat, apellidomat, email, dni, telefono, FechaContacto, estado));
     }
 
     // Registrar prospecto
     public void registrarProspecto(String nombres, String apellidopat, String apellidomat, String email, String dni, String telefono, String FechaContacto, int estado) throws BusinessException {
         validarDatosIncompletosClientes(nombres, apellidopat, apellidomat, email, dni, telefono, FechaContacto);
         validarDuplicidadClientes(nombres);
         fn_getProspecto().add(new Cliente(nombres, apellidopat, apellidomat, email, dni, telefono, FechaContacto, estado));
     }
 
     // Eliminar venta
     public void eliminarVenta(String numero) throws BusinessException {
         validarExistenciaVenta(numero);
         fn_getVentas().remove(Fn_buscar_nro_documento(numero));
     }
 
     // Eliminar cliente
     public void eliminarCliente(String nombres) throws BusinessException {
         validarExistenciaCliente(nombres);
         fn_getCliente().remove(Fn_buscar_Cliente(nombres));
     }
 
     // Eliminar prospecto
     public void eliminarProspecto(String nombres) throws BusinessException {
         validarExistenciaCliente(nombres);
        fn_getProspecto().remove(Fn_buscar_Cliente(nombres));
     }
 
     // Editar venta
     public void editarVenta(String numero, String fecha_emision, String fecha_vencimiento, String empresa, String fecha_pago, String estado, String concepto, double subtotal, double igv, double total, String moneda) throws BusinessException {
         Ventas oven = Fn_buscar_nro_documento(numero);
         int index = 0;
 
         oven.setFecha_emision(fecha_emision);
         oven.setFecha_vencimiento(fecha_vencimiento);
         oven.setEmpresa(empresa);
         oven.setFecha_pago(fecha_pago);
         oven.setEstado(estado);
         oven.setConcepto(concepto);
         oven.setSubtotal(subtotal);
         oven.setIgv(igv);
         oven.setTotal(total);
         oven.setMoneda(moneda);
 
         index = ven.indexOf(oven);
         ven.set(index, oven);
     }
 
     // Editar cliente
     public void editarCliente(String nombres, String apellidopat, String apellidomat, String email, String dni, String telefono, String FechaContacto, int estado) throws BusinessException {
         Cliente oven = Fn_buscar_Cliente(nombres);
         int index = 0;
 
         oven.setApellidopat(apellidopat);
         oven.setApellidomat(apellidomat);
         oven.setEmail(email);
         oven.setDni(dni);
         oven.setTelefono(telefono);
         oven.setFechaContacto(FechaContacto);
         oven.setEstado(estado);
 
         index = cli.indexOf(oven);
         cli.set(index, oven);
     }
 
     // Editar prospecto
     public void editarProspecto(String nombres, String apellidopat, String apellidomat, String email, String dni, String telefono, String FechaContacto, int estado) throws BusinessException {
        Cliente oven = Fn_buscar_Cliente(nombres);
         int index = 0;
 
         oven.setApellidopat(apellidopat);
         oven.setApellidomat(apellidomat);
         oven.setEmail(email);
         oven.setDni(dni);
         oven.setTelefono(telefono);
         oven.setFechaContacto(FechaContacto);
         oven.setEstado(estado);
 
         index = pro.indexOf(oven);
         pro.set(index, oven);
     }
 
     public ArrayList<Ventas> fn_getVentas() {
         return ven;
     }
 
     public ArrayList<Cliente> fn_getCliente() {
         return cli;
     }
 
     public ArrayList<Cliente> fn_getProspecto() {
         return pro;
     }
 }
