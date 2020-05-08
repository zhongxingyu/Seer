 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.iknition.micutecake.controller;
 
 import com.iknition.micutecake.controller.renderers.ProductTypeListRenderer;
 import com.iknition.micutecake.model.beans.ProductType;
 import com.iknition.micutecake.services.ProductTypeService;
 import com.iknition.micutecake.services.ServiceLocator;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.Page;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.select.SelectorComposer;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.VariableResolver;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zk.ui.select.annotation.WireVariable;
 import org.zkoss.zk.ui.util.GenericForwardComposer;
 import org.zkoss.zkplus.databind.AnnotateDataBinder;
 import org.zkoss.zkplus.databind.BindingListModelList;
 import org.zkoss.zkplus.spring.SpringUtil;
 import org.zkoss.zul.*;
 
 /**
  *
  * @author Yichao
  */
 //@Controller
 public class ProductTypeController extends SelectorComposer{
 
     
     @WireVariable
     private ProductTypeService productTypeService;
    // private AnnotateDataBinder binder;
     @Wire
     private Listbox typeList;
 
    
    @WireVariable 
     private ProductType productType;
     
     @Wire
     private Window modalProductType;
     
     private AnnotateDataBinder binder;
     
     private int selectedIdx=-1;
     
     @Override
     public void doAfterCompose(Component comp) throws Exception {
         super.doAfterCompose(comp); 
         if(typeList!=null){
            typeList.setModel(new ListModelList(productTypeService.getAll()));
           typeList.setItemRenderer(new ProductTypeListRenderer(this.typeList,this.productTypeService));
         }
         binder = new AnnotateDataBinder(comp);
         binder.bindBean("vm", this);
         System.out.println("-----------------composed---------------");
       //  productType = new ProductType();
     }
 
     @Listen("onClick = button#load")
     public void loadAll(){
         BindingListModelList model = new BindingListModelList(productTypeService.getAll(), false);
         typeList.setItemRenderer(new ProductTypeListRenderer(this.typeList, this.productTypeService));
         typeList.setModel(model);              
     }
     
     @Listen("onClick = #new")
     public void showModal(Event e) {
         this.selectedIdx = -1;
         this.productType = new ProductType();
         binder.loadAll();
         this.modalProductType.setVisible(true); 
       /*  Component comp = Executions.createComponents(
                 "/new.zul", null, null);
         if(comp instanceof Window) {
             ((Window)comp).doModal();
         }*/
     }
      
      @Listen("onClick = window button#save")
      public void save() throws InterruptedException {
                 if(this.getProductType().getId()==null){
                     ((ListModelList)this.typeList.getModel()).add(getProductType());
                     System.out.println("creacion");
                 }else{
                     ((ListModelList)typeList.getModel()).set(this.selectedIdx, this.getProductType());
                     System.out.println("edicion");
                 }
                 System.out.println("- ---- > "+ this.productType.getName());
                 
                 productTypeService.save(this.getProductType());
                 modalProductType.setVisible(false);
                 
 	}
      
      @Listen("onDoubleClick=#typeList")
      public void getSelected(){
         ProductType p =(ProductType)this.getTypeList().getSelectedItem().getValue();
         Integer id = p.getId();
         this.productType = this.productTypeService.getById(id);
         System.out.println(this.productType.getName());
         this.selectedIdx= typeList.getSelectedIndex();
         binder.loadAll();
         this.modalProductType.setVisible(true);
        /* Component comp = Executions.createComponents(
                 "/new.zul", null, null);
         if(comp instanceof Window) {
             ((Window)comp).doModal();
         }*/
      }
 
      @Listen("onClose = window")
      public void closewin(Event e){
          e.stopPropagation();
          ((Window)e.getTarget()).setVisible(false);
      }
      
      @Listen("onClick = window button#cancel")
      public void cancel(Event e){
          e.stopPropagation();
          this.modalProductType.setVisible(false);
      }
      
     public Listbox getTypeList() {
         return typeList;
     }
 
     public void setTypeList(Listbox typeList) {
         this.typeList = typeList;
     }
 
     public ProductType getProductType() {
         return productType;
     }
 
     public void setProductType(ProductType productType) {
         this.productType = productType;
     }
     
     
     
     
 }
