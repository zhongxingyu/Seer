 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 15, 2012
  * Author(s): SebastianN
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JLabel;
 
 import edu.dhbw.sos.gui.right.AddBtn;
 import edu.dhbw.sos.gui.right.DelBtn;
 import edu.dhbw.sos.gui.right.EditBtn;
 
 
 /**
  * Manages the stored courses
  * @author SebastianN
  * 
  */
 public class CourseController implements ActionListener, MouseListener, ItemListener {
 	// private static final Logger logger = Logger.getLogger(CourseController.class);
 	private Courses	courses;
 	
 	
 	public CourseController(Courses courses) {
 		this.courses = courses;
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// handle add, modify, delete course
 		if (e.getSource() instanceof AddBtn) {
			Course newC = new Course("New Course " + courses.size());
 			courses.add(newC);
 			courses.setCurrentCourse(newC);
 		} else if (e.getSource() instanceof EditBtn) {
 			// TODO trigger edit mode
 		} else if (e.getSource() instanceof DelBtn) {
 			courses.remove(courses.getCurrentCourse());
 		}
 	}
 	
 	
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.SELECTED) {
 			if (e.getItem() instanceof String) {
 				courses.getCurrentCourse().setName((String) e.getItem());
 				courses.notifyCoursesListObservers();
 			}
 			courses.setCurrentCourse(e.getItem());
 		}
 	}
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (e.getSource() instanceof JLabel) {
 			JLabel editBtn = (JLabel) e.getSource();
 			// the action command contains left, right, top or bottom. Give it directly to the next method
 			if (e.getButton() == MouseEvent.BUTTON3) {
 				courses.getCurrentCourse().subStudents(editBtn.getName());
 			} else {
 				courses.getCurrentCourse().addStudents(editBtn.getName());
 			}
 		}
 	}
 	
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO NicolaiO Auto-generated method stub
 		
 	}
 	
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO NicolaiO Auto-generated method stub
 		
 	}
 	
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO NicolaiO Auto-generated method stub
 		
 	}
 	
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO NicolaiO Auto-generated method stub
 		
 	}
 }
