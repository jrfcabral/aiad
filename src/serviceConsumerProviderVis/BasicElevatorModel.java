package serviceConsumerProviderVis;

import java.util.LinkedHashSet;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;

public class BasicElevatorModel extends Agent{
	
	private enum Movement{
		NONE, UP, DOWN
	}
	
	private int currentFloor = 10;
	private int timeBetweenFloors = 1000; //millis
	private int maxLoad = 500; //kg
	private LinkedHashSet<Integer> floors;
	private double currLoad;
	public Movement state;
	public int currentObjective;
	Context<Object> context; 
	public ContinuousSpace space;
	public Grid grid;

	private MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);


	
	public BasicElevatorModel(int timeBetweenFloors, int maxLoad){
		this.timeBetweenFloors = timeBetweenFloors;
		this.maxLoad = maxLoad;
		this.floors = new LinkedHashSet<Integer>();
		this.state = Movement.NONE;

	}
	
	public void setup(){
		System.out.println("Elevator " + getLocalName() + " coming online");
		this.context = ContextUtils.getContext((Object)this);
		this.space = (ContinuousSpace) context.getProjection("space");
		this.grid =  (Grid) context.getProjection("grid");
		this.currentObjective = -1;
		space.moveTo(this, 25, 10);
		this.currentFloor = 10;
		try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName("Elevator");
	  		sd.setType("elevator");
	  		sd.addOntologies("elevator-ontology");
	  		dfd.addServices(sd);
	  		DFService.register(this, dfd);
	  		
	  		addBehaviour(new TickerBehaviour(this, this.timeBetweenFloors){

				@Override
				protected void onTick() {
					NdPoint currPos = space.getLocation(this.myAgent);
					System.out.println("Objectivo: " + BasicElevatorModel.this.currentObjective);
					System.out.println("Andar atual: "+ currPos.getY());
					System.out.print("[");
					for(int val: BasicElevatorModel.this.floors){
						System.out.print(val+", ");
					}
					System.out.print("]\n");
					if(BasicElevatorModel.this.currentObjective >= 0){
						if(currPos.getY() == currentObjective){
							System.out.println("objectivo atingido");
							BasicElevatorModel.this.floors.remove(currentObjective);
							searchNextObjective();
							System.out.println("Novo Objectivo: " + BasicElevatorModel.this.currentObjective);

						}
						if(BasicElevatorModel.this.state == Movement.UP){	
							space.moveTo(this.myAgent, currPos.getX(), currPos.getY()+1);
							BasicElevatorModel.this.currentFloor++;
						}
						else if(BasicElevatorModel.this.state == Movement.DOWN){
							space.moveTo(this.myAgent, currPos.getX(), currPos.getY()-1);
							BasicElevatorModel.this.currentFloor--;
						}
					}
					else{
						searchNextObjective();
						System.out.println("Novo Objectivo: " + BasicElevatorModel.this.currentObjective);
					}
						
					System.out.print("\n\n\n\n\n\n\n");
				}
	  			
	  		});
	  		
	  		addBehaviour(new CyclicBehaviour(this){

				@Override
				public void action() {
					ACLMessage msg = myAgent.receive(template);
					if(msg != null){
						
						floors.add(Integer.parseInt(msg.getContent()));
						
						
						
						
						/*ACLMessage reply = msg.createReply();
						int floorToStop = Integer.parseInt(msg.getContent());
						double heuristicScore = calculateScore(floorToStop);
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(getLocalName() + " " + heuristicScore);
						myAgent.send(reply);*/
					}
				}
	  		});
	  		
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	}
	
	public void searchNextObjective(){
		int result = -1;
		for(int val: this.floors){
			if(this.state == Movement.UP && val > this.currentFloor && (val < result || result == -1)){
				result = val;
			}
			else if(this.state == Movement.DOWN && val > this.currentFloor && (val > result || result == -1)){
				result = val;
			}
			else if (this.state == Movement.NONE && (result == -1 || Math.abs(this.currentFloor-val) < Math.abs(this.currentFloor - result))){
				result = val;
			}			
		}
		
		if (result != -1 && result != this.currentFloor){//this.state == Movement.NONE && result != -1){
			if(result < this.currentFloor){
				this.state = Movement.DOWN;
			}
			else {
				this.state = Movement.UP;
			}
			
			System.out.println("estou no " + this.currentObjective + " e vou passar a ir para " + result + " pelo que vou deslocar-me no sentido " + this.state);
		}
		else if (result == -1){
			System.out.println("nao tenho mais nada para fazer");
			this.state = Movement.NONE;
		}
		
		this.currentObjective = result;
	}
	
	
	public void takeDown(){
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		System.out.println("Elevator " + getLocalName() + " going offline");
	}
	
	public double calculateScore(int intendedFloor){
		return 0;
	}
}
