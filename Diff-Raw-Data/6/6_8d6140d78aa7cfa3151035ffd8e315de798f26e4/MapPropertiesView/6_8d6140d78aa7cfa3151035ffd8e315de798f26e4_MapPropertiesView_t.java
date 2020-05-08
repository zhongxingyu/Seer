 /*
  * MapPropertiesView.java
  * 
  * Copyright (C) 2013  Pavel Prokhorov (pavelvpster@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package map.ui;
 
 import common.ui.mvc.Controller;
 import common.ui.mvc.View;
 
 import javax.swing.SwingUtilities;
 
 import map.navigation.Action;
 
 /**
  * Представление свойств карты.
  * 
  * @author pavelvpster
  * 
  */
 public final class MapPropertiesView extends javax.swing.JPanel implements View {
 
 	/**
 	 * Конструктор по умолчанию (параметризованный).
 	 * 
 	 * @param controller контроллер.
 	 * 
 	 */
 	public MapPropertiesView(Controller controller) {
 		
 		this.controller = controller;
 		
 		// Инициализируем представление
 		
 		initialize();
 	}
 	
 	
 	/**
 	 * Контроллер.
 	 * 
 	 */
 	private final Controller controller;
 	
 	
 	/**
 	 * Этот метод выполняет инициализацию.
 	 * 
 	 */
 	private void initialize() {
 
 		// Регистрируем представление в контроллере
 		
 		controller.addView(this);
 		
 		// Инициализируем элементы управления
 		
 		initComponents();
		
		// Масштаб
		
		double scale = (Double)controller.getProperty("Scale");
		
		onScaleChanged(scale);
 	}
 	
 
 	/**
 	 * Этот метод инициализирует элементы управления.
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         label_NextAction = new javax.swing.JLabel();
         textField_NextAction = new javax.swing.JTextField();
         label_Scale = new javax.swing.JLabel();
         comboBox_Scale = new javax.swing.JComboBox();
 
         label_NextAction.setText("Следующее действие:");
 
         textField_NextAction.setEditable(false);
         textField_NextAction.setMaximumSize(new java.awt.Dimension(1000, 30));
         textField_NextAction.setMinimumSize(new java.awt.Dimension(50, 30));
         textField_NextAction.setPreferredSize(new java.awt.Dimension(150, 30));
 
         label_Scale.setText("Масштаб:");
 
         comboBox_Scale.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "300%", "200%", "100%", "50%" }));
         comboBox_Scale.setSelectedIndex(2);
         comboBox_Scale.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 onSetScale(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(label_NextAction)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(textField_NextAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(label_Scale)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(comboBox_Scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(193, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(label_NextAction)
                     .addComponent(textField_NextAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(label_Scale)
                     .addComponent(comboBox_Scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 	
     private void onSetScale(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSetScale
         
 		// Получаем масштаб
 		
 		String item = (String)comboBox_Scale.getSelectedItem();
 		
 		// Удаляем знак процента
 		
 		String scaleString = item.replace("%", "");
 		
 		double scale;
 		
 		try {
 			
 			scale = Double.parseDouble(scaleString);
 			
 		} catch (NumberFormatException E) {
 			
 			return ;
 		}
 		
 		// Устанавливаем масштаб
 		
 		controller.setProperty("Scale", scale / 100.0);
     }//GEN-LAST:event_onSetScale
 
 	
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox comboBox_Scale;
     private javax.swing.JLabel label_NextAction;
     private javax.swing.JLabel label_Scale;
     private javax.swing.JTextField textField_NextAction;
     // End of variables declaration//GEN-END:variables
 
 	
 	/**
 	 * @see View
 	 * 
 	 */
 	@Override
 	public void updateContent() {
 	}
 
 	/**
 	 * @see View
 	 * 
 	 */
 	@Override
 	public void updateContent(String propertyName, Object value) {
 
 		final String p = propertyName;
 		
 		final Object x = value;
 
 		Runnable r = new Runnable() {
 
 			@Override
 			public void run() {
 
 				doUpdateContent(p, x);
 			}
 		};
 
 		SwingUtilities.invokeLater(r);
 	}
 	
 	
 	/**
 	 * Этот метод выполняет обновление представления.
 	 * 
 	 * @param propertyName имя параметра,
 	 * 
 	 * @param value значение.
 	 * 
 	 */
 	private void doUpdateContent(String propertyName, Object value) {
 		
 		switch (propertyName) {
 			
 			case "NextAction":
 				
 				Action action = (Action)value;
 				
 				onNextActionChanged(action);
 				
 				break;
 				
 				
 			case "Scale":
 				
 				double scale = (Double)value;
 
 				onScaleChanged(scale);
 				
 				break;
 		}
 	}
 
 
 	/**
 	 * Этот метод будет вызван при изменении следующего действия на пути к цели.
 	 * 
 	 */
 	private void onNextActionChanged(Action action) {
 		
 		if (action == null) {
 			
 			textField_NextAction.setText("Осмотреться...");
 			
 			return ;
 		}
 		
 		switch (action) {
 			
 			case IDLE:
 
 				textField_NextAction.setText("Цель достигнута!");
 				
 				break;
 				
 			case GO_UP:
 
 				textField_NextAction.setText("Вверх");
 				
 				break;
 				
 			case GO_DOWN:
 
 				textField_NextAction.setText("Вниз");
 				
 				break;
 				
 			case GO_LEFT:
 
 				textField_NextAction.setText("Влево");
 				
 				break;
 				
 			case GO_RIGHT:
 
 				textField_NextAction.setText("Вправо");
 				
 				break;
 		}
 	}
 
 	/**
 	 * Этот метод будет вызван при изменении масштаба карты.
 	 * 
 	 */
 	private void onScaleChanged(double scale) {
 
 		String scaleString = String.format("%.0f", scale * 100.0) + "%";
 
 		if (scaleString.equals((String)comboBox_Scale.getSelectedItem())) return ;
 
 		comboBox_Scale.setSelectedItem(scaleString);
 	}
 	
 }
