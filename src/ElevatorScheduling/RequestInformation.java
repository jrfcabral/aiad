package ElevatorScheduling;

import java.util.LinkedHashSet;

public class RequestInformation {
	private int targetFloor;
	private LinkedHashSet<Integer> destinationFloor;
	private String direction;
	private boolean passengerStop;
	private int requestScore = -1;
	public String getActualRequest() {
		return actualRequest;
	}

	public void setActualRequest(String actualRequest) {
		this.actualRequest = actualRequest;
	}

	private String actualRequest;
	
	public RequestInformation(String request, boolean passengerStop, int reqScore){
		this.actualRequest = request;
		String[] processedRequest = request.split(" ");
		if(processedRequest.length == 3){
			this.targetFloor = Integer.parseInt(processedRequest[1]);
			this.destinationFloor = new LinkedHashSet<Integer>(); 
			this.destinationFloor.add(Integer.parseInt(processedRequest[2]));
			this.direction = "N/A";
		}
		else if(processedRequest.length == 2){
			this.targetFloor = Integer.parseInt(processedRequest[1]);
			this.direction = processedRequest[0];
		}
		
		this.passengerStop = passengerStop;
		this.requestScore = reqScore;
	}
	
	public RequestInformation(int destination){
		this.targetFloor = destination;
		this.passengerStop = true;
		this.direction = "N/A";
		this.destinationFloor = new LinkedHashSet<Integer>();
	}

	public int getTargetFloor() {
		return targetFloor;
	}

	public void setTargetFloor(int targetFloor) {
		this.targetFloor = targetFloor;
	}

	public LinkedHashSet<Integer> getDestinationFloor() {
		return destinationFloor;
	}

	

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public boolean isPassengerStop() {
		return passengerStop;
	}

	public void setPassengerStop(boolean passengerStop) {
		this.passengerStop = passengerStop;
	}

	public int getRequestScore() {
		return requestScore;
	}

	public void setRequestScore(int requestScore) {
		this.requestScore = requestScore;
	}
	
	
}
