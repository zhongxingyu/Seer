 package com.bluespot.collections.observable;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 
import com.bluespot.collections.observable.list.ObservableList;
 import com.bluespot.demonstration.AbstractDemonstration;
 import com.bluespot.demonstration.Runner;
 
 public class CellRendererDemonstration extends AbstractDemonstration {
 
 	public class MyCellRenderer extends JLabel implements ListCellRenderer {
 
 		public Component getListCellRendererComponent(final JList sourceList, final Object value, final int index,
 				final boolean isSelected, final boolean cellHasFocus) {
 			this.setOpaque(true);
 			this.setText(String.valueOf(value.toString().length()));
 			if (isSelected) {
 				this.setForeground(Color.WHITE);
 				this.setBackground(Color.BLUE);
 				this.setText(value.toString());
 			} else {
 				this.setForeground(Color.BLACK);
 				this.setBackground(Color.WHITE);
 			}
 			return this;
 		}
 
 	}
 
 	protected JList list;
 
 	protected List<String> stringList;
 
 	@Override
 	public void initializeFrame(final JFrame frame) {
 		frame.setLayout(new BorderLayout());
 		frame.setSize(400, 400);
 		frame.getContentPane().add(new JScrollPane(this.constructList()), BorderLayout.CENTER);
 
 		final JPanel panel = new JPanel();
 
 		final JButton addButton = new JButton("Add");
 		final JButton removeButton = new JButton("Remove");
 		removeButton.setEnabled(false);
 
 		addButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent arg0) {
 				final List<String> listModelAdapter = CellRendererDemonstration.this.stringList;
 				listModelAdapter.add("Hello, world! This is element " + listModelAdapter.size());
 				removeButton.setEnabled(true);
 			}
 		});
 		panel.add(addButton);
 
 		removeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent arg0) {
 				final List<String> listModelAdapter = CellRendererDemonstration.this.stringList;
 				listModelAdapter.remove(0);
 				if (listModelAdapter.isEmpty()) {
 					removeButton.setEnabled(false);
 				}
 			}
 		});
 		panel.add(removeButton);
 
 		frame.getContentPane().add(panel, BorderLayout.SOUTH);
 	}
 
 	private JList constructList() {
		final ObservableList<String> listModelAdapter = new ObservableList<String>();
 		this.list = new JList(listModelAdapter);
 		this.stringList = listModelAdapter;
 
 		this.list.setCellRenderer(new MyCellRenderer());
 
 		this.populateList(this.stringList);
 
 		return this.list;
 	}
 
 	private void populateList(final List<String> targetList) {
 		final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		final char[] charArray = alphabet.toCharArray();
 		final String[] stringArray = new String[26];
 		int index = 0;
 		for (final char c : charArray) {
 			stringArray[index++] = String.valueOf(c);
 		}
 		Collections.addAll(targetList, stringArray);
 	}
 
 	public static void main(final String[] args) {
 		Runner.run(new CellRendererDemonstration(), true);
 	}
 
 	protected static int counter;
 
 }
