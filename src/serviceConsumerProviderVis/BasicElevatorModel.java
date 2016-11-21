package serviceConsumerProviderVis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.ListIterator;

import Util.Logger;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
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
import sajas.proto.ContractNetResponder;

public class BasicElevatorModel extends Agent{
	
	public	 enum Movement{
		NONE, UP, DOWN
	}
	
	private int currentFloor = 10;
	private int timeBetweenFloors = 1000; //millis
	private int maxLoad = 500; //kg
	private LinkedHashSet<Integer> floors;
	public ArrayList<ArrayList<Person>> people = new ArrayList< ArrayList<Person>>(MainController.FLOORNUM);
	private double currLoad;
	public Movement state;
	public int currentObjective;
	Context<Object> context; 
	public ContinuousSpace space;
	public Grid grid;
	// Statistics
	public int emptyTime = 0; //ticks/seconds 
	public int withPeopleTime = 0; //ticks/seconds
	public int stoppedTime = 0; // ticks/seconds
	public int courseTime = 0; // ticks/seconds
	public int totalTime = 0; // ticks/seconds

	private MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);


	
	public BasicElevatorModel(int timeBetweenFloors, int maxLoad){
		this.timeBetweenFloors = timeBetweenFloors;
		this.maxLoad = maxLoad;
		this.floors = new LinkedHashSet<Integer>();
		this.state = Movement.NONE;
		for(int i = 0; i < MainController.FLOORNUM; i++){
			people.add(new ArrayList<Person>());
		}

	}
	
	public void setup(){
		System.out.println("Elevator " + getLocalName() + " coming online");
		
		Logger.writeToLog(getLocalName() + " coming online");
		
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
					System.out.println("Peso atual: " + BasicElevatorModel.this.currLoad);
					System.out.print("[");
					for(int val: BasicElevatorModel.this.floors){
						System.out.print(val+", ");
					}
					System.out.print("]\n");
					System.out.println("Numero de pessoas dentro: " + BasicElevatorModel.this.getNumPeople());
					System.out.println("Tempo total: " + totalTime);
					System.out.println("Tempo em que esteve vazio: " + emptyTime);
					System.out.println("Tempo com pessoas: " + withPeopleTime);
					System.out.println("Tempo parado: " + stoppedTime);
					System.out.println("Tempo em andamento: " + courseTime);
					
					totalTime++;
					if (BasicElevatorModel.this.getNumPeople() == 0)
						emptyTime++;
					else
						withPeopleTime++;
					
					//TODO confirmar calculo					
					if (currPos.getY() == currentObjective || currentObjective < 0)
						stoppedTime++;
					else
						courseTime++;
					
					if(BasicElevatorModel.this.currentObjective >= 0){
						if(currPos.getY() == currentObjective){
							
							Logger.writeToLog(getLocalName() + " Reached objective floor: " + BasicElevatorModel.this.currentFloor + " with " 
							+ BasicElevatorModel.this.getNumPeople() + " people");
							
							BasicElevatorModel.this.floors.remove(currentObjective);
							
							ejectPassengers(BasicElevatorModel.this.currentFloor);
							getNewPassengers(BasicElevatorModel.this.currentFloor);
							
							searchNextObjective();
							Logger.writeToLog(getLocalName() + " leaving floor with " + BasicElevatorModel.this.getNumPeople() + " peoples");
							

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
		}
	  		
//	  		addBehaviour(new CyclicBehaviour(this){
//
//				@Override
//				public void action() {
//					ACLMessage msg = myAgent.receive(template);
//					if(msg != null){
//						
//						/*floors.add(Integer.parseInt(msg.getContent()));
//						Logger.writeToLog(getLocalName() + " Received request to go to " + msg.getContent()); 
//						searchNextObjective();*/
//
//						ACLMessage reply = msg.createReply();
//						int floorToStop = Integer.parseInt(msg.getContent());
//						double heuristicScore = calculateScore(floorToStop);
//						reply.setPerformative(ACLMessage.PROPOSE);
//						reply.setContent(getLocalName() + " " + heuristicScore);
//						myAgent.send(reply);
//					}
//				}
//	  		});
//	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
		
		addBehaviour(new ContractNetResponder(this, template) {
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
				int proposal = calculateScore(Integer.parseInt(cfp.getContent()));
				if (proposal > 2) {
					// We provide a proposal
					System.out.println("Agent "+getLocalName()+": Proposing "+proposal);
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(proposal));
					return propose;
				}
				else {
					// We refuse to provide a proposal
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("evaluation-failed");
				}
			}
			
			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				System.out.println("Agent "+getLocalName()+": Proposal accepted");
				if (BasicElevatorModel.this.floors.add(Integer.parseInt(propose.getContent()))) {
					System.out.println("Agent "+getLocalName()+": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Action execution failed");
					throw new FailureException("unexpected-error");
				}	
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Agent "+getLocalName()+": Proposal rejected");
			}
		});
	}
	
	
	public int calculateScore(int target){
		if((this.currentFloor < target && this.state.equals(Movement.UP)) || (this.currentFloor > target && this.state.equals(Movement.DOWN))){
			return Integer.MAX_VALUE;
		}
		
		if(target == this.currentFloor){
			return 0;
		}
		
		return Math.abs(target - this.currentFloor);
		
		
	}
	
	
	
	public void searchNextObjective(){
		int result = -1;
		for(int val: this.floors){
			if(this.state.equals(Movement.UP) && val > this.currentFloor && (val < result || result == -1)){
				result = val;
			}
			else if(this.state.equals(Movement.DOWN) && val < this.currentFloor && (val > result || result == -1)){
				result = val;
			}
			else if (this.state.equals(Movement.NONE) && (result == -1 || Math.abs(this.currentFloor-val) < Math.abs(this.currentFloor - result))){
				result = val;
			}			
		}
		
		if (result != -1 && result != this.currentFloor){
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
		System.out.println("Novo Objectivo: " + this.currentObjective);
		Logger.writeToLog("Calculated new Objective: " + this.currentObjective);
	}
	
	//for testing purposes only
	public static int searchNextObjective(LinkedHashSet<Integer> objectiveList, int startingFloor, Movement state){
		int result = -1;
		int currentFloor = startingFloor;
		Movement currentState = state;
		for(int val: objectiveList){
			if(currentState.equals(Movement.UP) && val > currentFloor && (val < result || result == -1)){
				result = val;
			}
			else if(currentState.equals(Movement.DOWN) && val < currentFloor && (val > result || result == -1)){
				result = val;
			}
			else if (currentState.equals(Movement.NONE) && (result == -1 || Math.abs(currentFloor-val) < Math.abs(currentFloor - result))){
				result = val;
			}			
		}
		
		if (result != -1 && result != currentFloor){
			if(result < currentFloor){
				state = Movement.DOWN;
			}
			else {
				state = Movement.UP;
			}
			
			System.out.println("estou no " + startingFloor + " e vou passar a ir para " + result + " pelo que vou deslocar-me no sentido " + state);
		}
		else if (result == -1){
			System.out.println("nao tenho mais nada para fazer");
			state = Movement.NONE;
		}
		
		System.out.println("Novo Objectivo: " + result);
		//Logger.writeToLog("Calculated new Objective: " + this.currentObjective);
		return result;
	}
	
	private void ejectPassengers(int floor){
		ListIterator<Person> it = this.people.get(floor).listIterator();
		while(it.hasNext()){
			System.out.println("VAI SAIR UM MANO");			
			this.currLoad -= it.next().getWeight();
			it.remove();
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	private void getNewPassengers(int floor){
		ListIterator<Person> it = MainController.peopleAtFloors.get(floor).listIterator();
		while(it.hasNext()){
			Person p = it.next();
			if(this.currLoad + p.getWeight() >= this.maxLoad){
				continue;
			}
			System.out.println("VAI ENTRAR UM MANO");
			this.currLoad += p.getWeight();
			
			this.people.get(p.getDestination()).add(p);
			
			this.floors.add(p.getDestination());
			it.remove();
			Logger.writeToLog("Person came in with objective: " + p.getDestination());
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	private int getNumPeople(){
		int totalSize = 0;
		for(int i = 0; i < MainController.FLOORNUM; i++){
			totalSize += this.people.get(i).size();
		}
		return totalSize;
	}
	
	
	public void takeDown(){
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		System.out.println("Elevator " + getLocalName() + " going offline");
	}
}
