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
 package org.nekorp.workflow.desktop.view;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.nekorp.workflow.desktop.view.binding.Bindable;
 import org.nekorp.workflow.desktop.view.binding.BindingManager;
 import org.nekorp.workflow.desktop.view.model.inventario.damage.DamageDetailsVB;
 import org.nekorp.workflow.desktop.view.model.servicio.ServicioVB;
 import org.nekorp.workflow.desktop.view.resource.ShapeView;
 import org.nekorp.workflow.desktop.view.resource.imp.DetailDamageCaptureListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  *
  */
 @Component("inventarioDamageView")
 @Aspect
 public class InventarioDamageView extends ApplicationView implements DetailDamageCaptureListener, Bindable {
     private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(InventarioDamageView.class);
     private LinkedList<Object> ignore;
     private Object target;
     private String property;
     @Autowired
     private BindingManager<Bindable> bindingManager;
     @Autowired
     @Qualifier(value = "servicio")
     private ServicioVB servicioVB;
     @Autowired
     private DamageDetailsVB damageCaptura;
     @Autowired
     @Qualifier("autoCuatroRightView")
     private ShapeView autoRightView;
     @Autowired
     @Qualifier("autoCuatroLeftView")
     private ShapeView autoLeftView;
     @Autowired
     @Qualifier("autoCuatroFrontView")
     private ShapeView autoFrontView;
     @Autowired
     @Qualifier("autoCuatroRearView")
     private ShapeView autoRearView;
     @Autowired
     private AutoDamageView damageView;
     private String lastSie;
     /**
      * Creates new form InventarioDamage
      */
     public InventarioDamageView() {
         ignore = new LinkedList();
         
     }
     
     @Pointcut("execution(* org.nekorp.workflow.desktop.control.WorkflowApp.cargaServicio(..))")  
     public void loadServicioPointCut() {
     }
     
     @AfterReturning("loadServicioPointCut()")
     public void cargarServicio() {
         this.setBindings("derecha");
     }
     
     @Override
     public void iniciaVista() {
         initComponents();
         damageView.iniciaVista();
         this.content.add(damageView);
     }
 
     @Override
     public void setEditableStatus(boolean value) {
         //aun nada
     }
 
     @Override
     public ViewValidIndicator getValidInidicator() {
         return null;
     }
     
     public void setBindings(String side) {
         lastSie = side;
         bindingManager.clearBindings(this);
         derecha.setSelected(false);
         izquierda.setSelected(false);
         frontal.setSelected(false);
         trasera.setSelected(false);
         if (StringUtils.equals(side, "derecha")) {
             damageView.changeView(autoRightView);
             derecha.setSelected(true);
             bindingManager.registerBind(servicioVB.getDatosAuto().getDamage(), side, this);
         }
         if (StringUtils.equals(side, "izquierda")) {
             damageView.changeView(autoLeftView);
             izquierda.setSelected(true);
             bindingManager.registerBind(servicioVB.getDatosAuto().getDamage(), side, this);
         }
         if (StringUtils.equals(side, "frontal")) {
             damageView.changeView(autoFrontView);
             frontal.setSelected(true);
             bindingManager.registerBind(servicioVB.getDatosAuto().getDamage(), side, this);
         }
         if (StringUtils.equals(side, "trasera")) {
             damageView.changeView(autoRearView);
             trasera.setSelected(true);
             bindingManager.registerBind(servicioVB.getDatosAuto().getDamage(), side, this);
         }
     }
 
     @Override
     public void agregar() {
         try {
             List<DamageDetailsVB> value = damageView.getModelo();
             DamageDetailsVB nuevo = new DamageDetailsVB();
             BeanUtils.copyProperties(nuevo, damageCaptura);
             value.add(nuevo);
             BeanUtils.setProperty(target, property, value);
         } catch (IllegalAccessException | InvocationTargetException ex) {
             InventarioDamageView.LOGGER.error(ex);
         }
     }
     
     @Override
     public void borrar(DamageDetailsVB borrado) {
         try {
             List<DamageDetailsVB> value = damageView.getModelo();
             value.remove(borrado);
             BeanUtils.setProperty(target, property, value);
         } catch (IllegalAccessException | InvocationTargetException ex) {
             InventarioDamageView.LOGGER.error(ex);
         }
     }
     
     @Override
     public void updateModel(Object origen, String property, Object value) {
         if(!ignore.remove(value)){
             if (StringUtils.equals(lastSie, property)) {
                 damageView.setModelo((List<DamageDetailsVB>) value);
             }
         }
     }
 
     @Override
     public void ignoreUpdate(Object value) {
         ignore.add(value);
     }
 
     @Override
     public Object getModelValue() {
         return damageView.getModelo();
     }
 
     @Override
     public void bindListener(Object target, String property) {
         this.target = target;
         this.property = property;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jToolBar1 = new javax.swing.JToolBar();
         derecha = new javax.swing.JToggleButton();
         izquierda = new javax.swing.JToggleButton();
         frontal = new javax.swing.JToggleButton();
         trasera = new javax.swing.JToggleButton();
         content = new javax.swing.JPanel();
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
 
        derecha.setText("Derecha");
         derecha.setFocusable(false);
         derecha.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         derecha.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         derecha.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 derechaActionPerformed(evt);
             }
         });
         jToolBar1.add(derecha);
 
        izquierda.setText("Izquierda");
         izquierda.setFocusable(false);
         izquierda.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         izquierda.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         izquierda.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 izquierdaActionPerformed(evt);
             }
         });
         jToolBar1.add(izquierda);
 
         frontal.setText("Frente");
         frontal.setFocusable(false);
         frontal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         frontal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         frontal.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 frontalActionPerformed(evt);
             }
         });
         jToolBar1.add(frontal);
 
         trasera.setText("Atrás");
         trasera.setFocusable(false);
         trasera.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         trasera.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         trasera.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 traseraActionPerformed(evt);
             }
         });
         jToolBar1.add(trasera);
 
         content.setLayout(new java.awt.BorderLayout());
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void derechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derechaActionPerformed
         this.setBindings("derecha");
     }//GEN-LAST:event_derechaActionPerformed
 
     private void izquierdaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_izquierdaActionPerformed
         this.setBindings("izquierda");
     }//GEN-LAST:event_izquierdaActionPerformed
 
     private void frontalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frontalActionPerformed
         this.setBindings("frontal");
     }//GEN-LAST:event_frontalActionPerformed
 
     private void traseraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traseraActionPerformed
         this.setBindings("trasera");
     }//GEN-LAST:event_traseraActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel content;
     private javax.swing.JToggleButton derecha;
     private javax.swing.JToggleButton frontal;
     private javax.swing.JToggleButton izquierda;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JToggleButton trasera;
     // End of variables declaration//GEN-END:variables
 
 }
