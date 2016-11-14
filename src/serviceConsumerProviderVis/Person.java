package serviceConsumerProviderVis;

import repast.simphony.random.RandomHelper;

public class Person {
	private double weight;
	private int destination;
	
	public Person(int destination){
		this.destination = destination;
		this.weight = RandomHelper.nextDoubleFromTo(40, 200);
	}

	public double getWeight() {
		return weight;
	}

	public int getDestination() {
		return destination;
	}	
	
}
