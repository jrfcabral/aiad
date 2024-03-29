package ElevatorScheduling;

import repast.simphony.random.RandomHelper;

public class Person {
	private double weight;
	private int destination;
	public int timeWaiting = 0;
	public int timeInElevator = 0;
	
	public Person(int destination){
		this.destination = destination;
		this.weight = generateWeight();
	}

	public double getWeight() {
		return weight;
	}

	public int getDestination() {
		return destination;
	}
	
	private double generateWeight(){
		double[] weightArray = new double[10];
		weightArray[0] = RandomHelper.nextDoubleFromTo(40, 60);
		for (int i = 1; i < 6; i++){
			weightArray[i] = RandomHelper.nextDoubleFromTo(60, 80);
		}
		for (int i = 6; i < 9; i++){
			weightArray[i] = RandomHelper.nextDoubleFromTo(80, 100);
		}
		weightArray[9] = RandomHelper.nextDoubleFromTo(100, 150);
		return weightArray[RandomHelper.nextIntFromTo(0, 9)];
	}

	public int getTimeWaiting() {
		return timeWaiting;
	}

	public void setTimeWaiting(int timeWaiting) {
		this.timeWaiting = timeWaiting;
	}

	public int getTimeInElevator() {
		return timeInElevator;
	}

	public void setTimeInElevator(int timeInElevator) {
		this.timeInElevator = timeInElevator;
	}
	
}
