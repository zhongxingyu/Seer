 package de.unihalle.sim.main;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 import de.unihalle.sim.entities.Flower;
 import de.unihalle.sim.entities.PositionedEntity;
 
 public class ReportEventListener implements EventListener {
 
 	double _lastNotificationTime = -1;
 	PrintWriter _out;
 
 	private static class SimulationState {
 
 		private double _time;
 		private int _numberOfHives;
 		private int _numberOfFlowers;
 		private int _numberOfBees;
 		private int _numberOfInfectedBees;
 		private double _beeInfectionRatio;
 		private double _averageFlowerNectarRatio;
 		private double _collapsedHiveRatio;
 
 		private SimulationState(double time) {
 			_time = time;
 		}
 
 		private static class Builder {
 
 			private SimulationState _state;
 
 			private Builder(double time) {
 				_state = new SimulationState(time);
 			}
 
 			public static Builder time(double time) {
 				return new Builder(time);
 			}
 
 			public Builder numberOfHives(int numberOfHives) {
 				_state.setNumberOfHives(numberOfHives);
 				return this;
 			}
 
 			public Builder numberOfBees(int numberOfBees) {
 				_state.setNumberOfBees(numberOfBees);
 				return this;
 			}
 
 			public Builder numberOfFlowers(int numberOfFlowers) {
 				_state.setNumberOfFlowers(numberOfFlowers);
 				return this;
 			}
 
 			public Builder numberOfInfectedBees(int numberOfInfectedBees) {
 				_state.setNumberOfInfectedBees(numberOfInfectedBees);
 				return this;
 			}
 
 			public Builder beeInfectionRatio(double infectionRatio) {
 				_state.setBeeInfectionRatio(infectionRatio);
 				return this;
 			}
 
 			public Builder averageFlowerNectarRatio(double nectarRatio) {
 				_state.setAverageFlowerNectarRatio(nectarRatio);
 				return this;
 			}
 
 			public Builder collapsedHiveRatio(double ratio) {
 				_state.setCollapsedHiveRatio(ratio);
 				return this;
 			}
 
 			public SimulationState build() {
 				return _state;
 			}
 
 		}
 
 		private void setNumberOfHives(int numberOfHives) {
 			_numberOfHives = numberOfHives;
 		}
 
 		private void setNumberOfFlowers(int numberOfFlowers) {
 			_numberOfFlowers = numberOfFlowers;
 		}
 
 		private void setNumberOfBees(int numberOfBees) {
 			_numberOfBees = numberOfBees;
 		}
 
 		private void setNumberOfInfectedBees(int numberOfInfectedBees) {
 			_numberOfInfectedBees = numberOfInfectedBees;
 		}
 
 		private void setBeeInfectionRatio(double infectionRatio) {
 			_beeInfectionRatio = infectionRatio;
 		}
 
 		private void setAverageFlowerNectarRatio(double averageFlowerNectarRatio) {
 			_averageFlowerNectarRatio = averageFlowerNectarRatio;
 		}
 
 		private void setCollapsedHiveRatio(double ratio) {
 			_collapsedHiveRatio = ratio;
 		}
 
 		@Override
 		public String toString() {
 			return _time + ";" + _numberOfHives + ";" + _numberOfFlowers + ";" + _numberOfBees + ";"
 					+ _numberOfInfectedBees + ";" + _beeInfectionRatio + ";" + _averageFlowerNectarRatio + ";"
 					+ _collapsedHiveRatio;
 		}
 
 		public static String getSchema() {
 			return "time;numberOfHives;numberOfFlowers;numberOfBees;numberOfInfectedBees;beeInfectionRatio;averageFlowerNectarRatio;collapsedHiveRatio";
 		}
 
 	}
 
 	private List<SimulationState> _states;
 	private BeeSimulation _simulation;
 
 	public ReportEventListener(PrintWriter out, BeeSimulation simulation) {
 		_simulation = simulation;
 		_out = out;
 		_states = Lists.newArrayList();
 		_states = Collections.synchronizedList(_states);
 	}
 
 	public ReportEventListener(String fileName, BeeSimulation simulation) throws FileNotFoundException {
 		this(new PrintWriter(fileName), simulation);
 	}
 
 	@Override
 	public void notify(PositionedEntity e) {
 		Environment environment = _simulation.environment();
 		double currentTime = (Math.round(e.getTimeNow() * 1000)) / 1000d;
 		int numBees = environment.getNumberOfBees();
 		int numHives = environment.getNumberOfHives();
 		int numFlowers = environment.getNumberOfFlowers();
 		int numInfected = environment.getNumberOfInfectedBees();
 		double infectionRatio = (double) numInfected / (double) numBees;
 		List<Flower> flowers = environment.getFlowers();
 		double averageFlowerNectarRatio = 0;
 		for (Flower f : flowers) {
 			averageFlowerNectarRatio += (double) f.getNectarAmount() / (double) f.getMaxNectarAmount();
 		}
 		averageFlowerNectarRatio /= flowers.size();
 		double collapsedHiveRatio = (double) environment.getNumberOfCollapsedHives()
 				/ (double) environment.getNumberOfHives();
 
 		if (_lastNotificationTime == currentTime) {
 			_states.remove(_states.size() - 1);
 		}
 		_states.add(SimulationState.Builder.time(currentTime).numberOfBees(numBees).numberOfFlowers(numFlowers)
 				.numberOfHives(numHives).numberOfInfectedBees(numInfected).beeInfectionRatio(infectionRatio)
 				.averageFlowerNectarRatio(averageFlowerNectarRatio).collapsedHiveRatio(collapsedHiveRatio).build());
 		_lastNotificationTime = currentTime;
 	}
 
 	public void close() {
 		_out.write(SimulationState.getSchema() + "\n");
 		for (SimulationState s : _states) {
 			_out.write(s.toString() + "\n");
 		}
 		_out.close();
 	}
 }
