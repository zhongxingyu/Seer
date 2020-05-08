 /**
  *   Copyright 2013 Nekorp
  *
  *Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License
  */
 
 package org.nekorp.workflow.desktop.control.imp;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.nekorp.workflow.desktop.control.MensajesControl;
 import org.nekorp.workflow.desktop.control.NuevoServicioWizard;
 import org.nekorp.workflow.desktop.control.WorkflowApp;
 import org.nekorp.workflow.desktop.data.access.AutoDAO;
 import org.nekorp.workflow.desktop.data.access.BitacoraDAO;
 import org.nekorp.workflow.desktop.data.access.ClienteDAO;
 import org.nekorp.workflow.desktop.data.access.ServicioDAO;
 import org.nekorp.workflow.desktop.modelo.auto.Auto;
 import org.nekorp.workflow.desktop.modelo.bitacora.Evento;
 import org.nekorp.workflow.desktop.modelo.cliente.Cliente;
 import org.nekorp.workflow.desktop.modelo.servicio.Servicio;
 import org.nekorp.workflow.desktop.rest.util.Callback;
 import org.nekorp.workflow.desktop.servicio.EventoServicioFactory;
 import org.nekorp.workflow.desktop.servicio.bridge.AutoBridge;
 import org.nekorp.workflow.desktop.servicio.bridge.BitacoraBridge;
 import org.nekorp.workflow.desktop.servicio.bridge.ClienteBridge;
 import org.nekorp.workflow.desktop.servicio.bridge.ServicioBridge;
 import org.nekorp.workflow.desktop.view.model.ServicioVB;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoSistemaVB;
 import org.nekorp.workflow.desktop.view.model.bitacora.EventoVB;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.springframework.web.client.ResourceAccessException;
 
 /**
  *
  */
 @Service("nuevoServicioWizard")
 public class NuevoServicioWizardImp implements NuevoServicioWizard {
     private static final Logger LOGGER = Logger.getLogger(NuevoServicioWizardImp.class);
     
     @Autowired
     private WorkflowApp worflowApp;
     @Autowired
     @Qualifier(value="w-servicio")
     private ServicioVB servicio;
     @Autowired
     private ServicioDAO servicioDAO;
     @Autowired
     private ServicioBridge servicioBridge;
     @Autowired
     private ClienteDAO clienteDAO;
     @Autowired
     private ClienteBridge clienteBridge;
     @Autowired
     private AutoDAO autoDAO;
     @Autowired
     private AutoBridge autoBridge;
     @Autowired
     private BitacoraDAO bitacoraDAO;
     @Autowired
     private BitacoraBridge bitacoraBridge;
     @Autowired
     private EventoServicioFactory eventoFactory;
     @Autowired
     private MensajesControl mensajesControl;
     
     @Override
     public void loadCliente(Cliente origen) {
         try {
             clienteBridge.load(origen, servicio.getCliente());
         } catch(ResourceAccessException e) {
             NuevoServicioWizardImp.LOGGER.error("error al cargar clientes" + e.getMessage());
             this.mensajesControl.reportaError("Error de comunicacion con el servidor");
         }
     }
 
     @Override
     public List<Cliente> getClientes() {
         try {
             return clienteDAO.consultaTodos();
         } catch(ResourceAccessException e) {
             NuevoServicioWizardImp.LOGGER.error("error al cargar todos los clientes" + e.getMessage());
             this.mensajesControl.reportaError("Error de comunicacion con el servidor");
             return new LinkedList<>();
         }
     }
 
     @Override
     public void buscarCliente(final String name, final Callback<List<Cliente>> cmd) {
         try {
             clienteDAO.buscar(name, cmd);
         } catch(ResourceAccessException e) {
             NuevoServicioWizardImp.LOGGER.error("error al buscar un cliente por nombre" + e.getMessage());
             this.mensajesControl.reportaError("Error de comunicacion con el servidor");
         }
     }
 
     @Override
     public void inicia() {
         //Se inician todos los datos en blanco
         //datos del servicio
         Servicio nuevo = new Servicio();
         servicioBridge.load(nuevo, servicio);
        List<Evento> bitacora = new LinkedList<>();
        bitacoraBridge.load(bitacora, servicio.getBitacora());
        //los datos de costo no se editan en el wizard
         //cliente
         Cliente cliente = new Cliente();
         clienteBridge.load(cliente, servicio.getCliente());
         //auto
         Auto auto = new Auto();
         autoBridge.load(auto, servicio.getDatosAuto());
     }
     
     @Override
     public void nuevoServicio() {
         try {
             //como es parte del wizard se asume que el servicio es nuevo.
             Servicio nuevoServicio = new Servicio();
             servicioBridge.unload(servicio, nuevoServicio);
             //primero se tratan de guardar los datos del cliente y auto.
             Cliente nuevoCliente = new Cliente();
             clienteBridge.unload(servicio.getCliente(), nuevoCliente);
             clienteDAO.guardar(nuevoCliente);
             //el cliente nuevo o no, al terminar ya debe tener id
             nuevoServicio.setIdCliente(nuevoCliente.getId());
             //datos del auto
             Auto nuevoAuto = new Auto();
             autoBridge.unload(servicio.getDatosAuto(), nuevoAuto);
             autoDAO.guardar(nuevoAuto);
             nuevoServicio.setIdAuto(nuevoAuto.getNumeroSerie());
             //se guarda el nuevo servicio
             servicioDAO.guardar(nuevoServicio);
             registrarInicioServicio();
             //la bitacora del servicio esta vacia por que es nuevo.
             List<Evento> bitacora = new LinkedList<>();
             //se copian los datos de la bitacora
             bitacoraBridge.unload(servicio.getBitacora(), bitacora);
             bitacoraDAO.guardar(nuevoServicio.getId(), bitacora);
             worflowApp.cargaServicio(nuevoServicio.getId());
         } catch(ResourceAccessException e) {
             NuevoServicioWizardImp.LOGGER.error("error al guardar el nuevo servicio" + e.getMessage());
             this.mensajesControl.reportaError("Error de comunicacion con el servidor");
         }
     }
     /**
      * genera una nueva entrada en la bitacora indicando el inicio del servicio.
      */
     private void registrarInicioServicio() {
         EventoSistemaVB eventoDeCreacion = eventoFactory.creaEvento(EventoSistemaVB.class);
         eventoDeCreacion.setNombre("Inicio del Servicio");
         List<EventoVB> eventosVB = servicio.getBitacora().getEventos();
         eventosVB.add(eventoDeCreacion);
         servicio.getBitacora().setEventos(eventosVB);
     }
 }
