 package at.ac.tuwien.complang.carfactory.ui.xvsm;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.mozartspaces.core.Entry;
 import org.mozartspaces.notifications.Notification;
 import org.mozartspaces.notifications.NotificationListener;
 import org.mozartspaces.notifications.Operation;
 
 import at.ac.tuwien.complang.carfactory.domain.Car;
 import at.ac.tuwien.complang.carfactory.domain.ICarPart;
 import at.ac.tuwien.complang.carfactory.domain.Task;
 import at.ac.tuwien.complang.carfactory.ui.IFactoryData;
 
 public class SpaceListener implements NotificationListener {
 	IFactoryData data;
 
 	public SpaceListener(IFactoryData data) {
 		this.data = data;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void entryOperationFinished(
 		Notification source,
 		Operation operation,
 		List<? extends Serializable> entries)
 	{
 		System.out.println("[XVSM Notification: " + operation.name() + "]");
 		if(operation.name().equals("WRITE")) {
 			for(Entry entry : (List<Entry>) entries) {
 				if (entry.getValue() instanceof Car) {
 					System.out.println("[GUI_Notification] New Car written");
 					Car car = (Car) entry.getValue();
					boolean isTested = (car.getCompletenessTesterId() != -1 || car.getDefectTesterId() != -1);
					boolean hasProblem = (!car.isComplete() || car.isDefect());
					if(isTested && hasProblem) {
 						data.removeCar(car);
 						if(!data.updateDefectCar(car)) {
 							data.addDefectCar(car);
 						}
 					} else {
 						if(!data.updateCar(car)) {
 							data.addCar(car); //we add the car only if we could not update it, because that means its not in the data set yet.
 						}
 					}
 				} else if (entry.getValue() instanceof ICarPart) { //its not a car but still a carpart
 					ICarPart part = (ICarPart) entry.getValue();
 					data.addPart(part);
 				} else if (entry.getValue() instanceof Task) {
 					Task task = (Task) entry.getValue();
 					if(!data.updateTask(task)) {
 						data.addTask(task);
 					}
 				}
 			}
 		} else if(operation.name().equals("TAKE")) {
 			for(ICarPart part: (List<ICarPart>) entries) {
 				if(!(part instanceof Car)) {
 					data.removePart(part);
 				}
 			}
 		}else if(operation.name().equals("DELETE")) {
 			for(Task t: (List<Task>) entries) {
 					data.removeTask(t);
 			}
 		}
 	}
 }
