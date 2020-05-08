 package net.sprauer.sitzplaner.EA.operations;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import net.sprauer.sitzplaner.EA.EAOperation;
 import net.sprauer.sitzplaner.EA.GeneString;
 import net.sprauer.sitzplaner.exceptions.ClassNotLoadedException;
 import net.sprauer.sitzplaner.exceptions.ClassRoomToSmallException;
 import net.sprauer.sitzplaner.model.Student;
 import net.sprauer.sitzplaner.view.Parameter;
 
 public class OperationGreedy implements EAOperation {
 
 	private GeneString model;
 	private List<Integer> listOfStudents;
 
 	@Override
 	public void invoke(GeneString gene) throws ClassRoomToSmallException, ClassNotLoadedException {
 		model = gene;
 		model.resetPositions();
 		checkClassRoomSize();
 		loadAndSortStudents();
 
 		Student studentB4 = null;
 		do {
 			Point nextFreePosition = gene.freePositionNextTo(studentB4);
 			Student firstStudent = getStudentWithLowestPriority();
			Student secondStudent = findBestMatchTo(studentB4);
			Student choice = selectBetterChoice(firstStudent, secondStudent, nextFreePosition, studentB4);
 
			removeFromListOfStudents(choice);
			choice.position = nextFreePosition;
			studentB4 = choice;
 			System.out.println(listOfStudents);
 		} while (!listOfStudents.isEmpty());
 		gene = model;
 	}
 
 	private void removeFromListOfStudents(Student choice) {
 		listOfStudents.remove(model.getStudents().indexOf(choice));
 	}
 
 	private Student selectBetterChoice(Student firstStudent, Student secondStudent, Point position, Student lastStudent) {
 		if (firstStudent == null) {
 			return secondStudent;
 		}
 		if (secondStudent == null) {
 			return firstStudent;
 		}
 		if (position == null) {
 			System.out.println("ffuuuuu pos");
 		}
 		double firstStudentFitness = firstStudent.getFirstRowFactor(position.y);
 		double secondStudentFitness = secondStudent.relationTo(lastStudent);
 		if (firstStudentFitness < secondStudentFitness) {
 			return firstStudent;
 		} else {
 			return secondStudent;
 		}
 	}
 
 	private void checkClassRoomSize() throws ClassRoomToSmallException, ClassNotLoadedException {
 		if (model.size() > (Parameter.numRows * Parameter.numCols)) {
 			throw new ClassRoomToSmallException("Der Raum ist zu klein fr diese Klasse");
 		} else if (model.size() <= 0) {
 			throw new ClassNotLoadedException("Keine Klasse geladen");
 		}
 	}
 
 	private void loadAndSortStudents() {
 		listOfStudents = new ArrayList<Integer>();
 		Collections.sort(model.getStudents(), new Comparator<Student>() {
 
 			@Override
 			public int compare(Student o1, Student o2) {
 				return Double.compare(o1.getFirstRowFactor(), o2.getFirstRowFactor());
 			}
 
 		});
 		for (int i = 0; i < model.size(); i++) {
 			listOfStudents.add(i);
 		}
 	}
 
 	private Student getStudentWithLowestPriority() {
 		return model.get(listOfStudents.size() - 1);
 	}
 
 	private Student findBestMatchTo(Student student) {
 		if (student == null) {
 			return null;
 		}
 		double bestRelation = 1;
 		Student bestStud = null;
 		List<Student> students = model.getStudents();
 		for (int i = 0; i < students.size(); i++) {
 			if (!listOfStudents.contains(i)) {
 				continue;
 			}
 			Student stud = students.get(i);
 			Double currentRelation = stud.relationTo(student);
 			if (currentRelation < bestRelation) {
 				bestStud = stud;
 				bestRelation = currentRelation;
 			}
 		}
 		return bestStud;
 	}
 
 }
