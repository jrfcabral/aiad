package serviceConsumerProviderVis;

public class RequestInformation {
	private int targetFloor;
	private int destinationFloor;
	private String direction;
	private boolean passengerStop;
	private int requestScore;
	
	public RequestInformation(String request, boolean passengerStop, int reqScore){
		String[] processedRequest = request.split(" ");
		if(processedRequest.length == 3){
			this.targetFloor = Integer.parseInt(processedRequest[1]);
			this.destinationFloor = Integer.parseInt(processedRequest[2]);
			this.direction = (this.targetFloor < this.destinationFloor)?"UP":"DOWN";
		}
		else if(processedRequest.length == 2){
			this.targetFloor = Integer.parseInt(processedRequest[1]);
			this.direction = processedRequest[0];
		}
		
		this.passengerStop = passengerStop;
		this.requestScore = reqScore;
	}
	
	public RequestInformation(int destination){
		this.destinationFloor = destination;
		this.passengerStop = true;
		this.direction = "N/A";
	}

	public int getTargetFloor() {
		return targetFloor;
	}

	public void setTargetFloor(int targetFloor) {
		this.targetFloor = targetFloor;
	}

	public int getDestinationFloor() {
		return destinationFloor;
	}

	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
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
	
	
}
