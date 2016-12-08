package serviceConsumerProviderVis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.NoSuchElementException;

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
import sajas.core.behaviours.OneShotBehaviour;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import sajas.proto.ContractNetResponder;

public class BasicElevatorModel extends Agent{
	
	public	 enum Movement{
		NONE, UP, DOWN
	}
	
	public static String WEIGHTMODEL="NONE";
	
	
	private int currentFloor;
	private int timeBetweenFloors = 1000; //millis
	private int maxLoad = 500; //kg
	private LinkedHashSet<Integer> floors;
	private HashMap<Integer, RequestInformation> floorInfo;
	private int idleTime; //time the elevator needs to stay at a floor for everyone to get in/out
	public ArrayList<ArrayList<Person>> people = new ArrayList< ArrayList<Person>>(MainController.FLOORNUM);
	private double currLoad;
	public Movement state;
	public int currentObjective;
	Context<Object> context; 
	public ContinuousSpace space;
	public Grid grid;
	public int startingX;
	public int[] sectorBounds = null;
	
	
	public int emptyTime = 0; //ticks/seconds 
	public int withPeopleTime = 0; //ticks/seconds
	public int stoppedTime = 0; // ticks/seconds
	public int courseTime = 0; // ticks/seconds
	public int totalTime = 0; // ticks/seconds
	  

	private MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
	
	public BasicElevatorModel(int timeBetweenFloors, int maxLoad, int startingX){
		this.timeBetweenFloors = timeBetweenFloors;
		this.maxLoad = maxLoad;
		this.startingX = startingX;
		this.floors = new LinkedHashSet<Integer>();
		this.floorInfo = new HashMap<Integer, RequestInformation>();
		this.idleTime = 0;
		this.state = Movement.NONE;
		for(int i = 0; i < MainController.FLOORNUM; i++){
			people.add(new ArrayList<Person>());
		}

	}
	
	public BasicElevatorModel(int timeBetweenFloors, int maxLoad, int startingX, int lowerSecBound, int upperSecBound){
		this.timeBetweenFloors = timeBetweenFloors;
		this.maxLoad = maxLoad;
		this.startingX = startingX;
		this.sectorBounds = new int[2];
		this.sectorBounds[0] = lowerSecBound; this.sectorBounds[1] = upperSecBound;
		
		this.floors = new LinkedHashSet<Integer>();
		this.floorInfo = new HashMap<Integer, RequestInformation>();
		this.idleTime = 0;
		this.state = Movement.NONE;
		for(int i = 0; i < MainController.FLOORNUM; i++){
			people.add(new ArrayList<Person>());
		}

	}
	
	public void setup(){
		
		
		Logger.writeAndPrint(getLocalName() + " coming online");
		if(BasicElevatorModel.this.sectorBounds != null){
			Logger.writeToLog(getLocalName() + " sector: " + BasicElevatorModel.this.sectorBounds[0] + " - " + BasicElevatorModel.this.sectorBounds[1]);
		}
		
		
		this.context = ContextUtils.getContext((Object)this);
		this.space = (ContinuousSpace) context.getProjection("space");
		this.grid =  (Grid) context.getProjection("grid");
		this.currentObjective = -1;
		space.moveTo(this, BasicElevatorModel.this.startingX, (!MainController.SECTORIZATION.equals("NONE"))?((BasicElevatorModel.this.sectorBounds[1] - BasicElevatorModel.this.sectorBounds[0])/2 + BasicElevatorModel.this.sectorBounds[0]):(MainController.FLOORNUM/2));
		this.currentFloor = (!MainController.SECTORIZATION.equals("NONE"))?((BasicElevatorModel.this.sectorBounds[1] - BasicElevatorModel.this.sectorBounds[0])/2 + BasicElevatorModel.this.sectorBounds[0]):(MainController.FLOORNUM/2);
		try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName("Elevator");
	  		sd.setType("elevator");
	  		sd.addOntologies("elevator-ontology");
	  		dfd.addServices(sd);
	  		DFService.register(this, dfd);
	  		
	  		
	  		
			addBehaviour(new CyclicBehaviour(this){

				@Override
				public void action() {
					MessageTemplate tmplt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					
					ACLMessage msg = myAgent.receive(tmplt);
					if(msg != null){
						String[] content = msg.getContent().split(" ");
						if(BasicElevatorModel.this.sectorBounds[0] < Integer.parseInt(content[0]) && BasicElevatorModel.this.sectorBounds[1] > Integer.parseInt(content[0])){
							String[] newSector = content[1].split("-");
							BasicElevatorModel.this.sectorBounds[0] = Integer.parseInt(newSector[0]);
							BasicElevatorModel.this.sectorBounds[1] = Integer.parseInt(newSector[1]);
							Logger.writeAndPrint("Got a message from " + msg.getSender() + ": " + msg.getContent());
						}
					}					
				}
				
			});
	  		
	  		
	  		addBehaviour(new TickerBehaviour(this, this.timeBetweenFloors){

				@Override
				protected void onTick() {
					NdPoint currPos = space.getLocation(this.myAgent);
					String taskList = "";
					for(int val: BasicElevatorModel.this.floors){
						taskList += val+", ";
					}
					Logger.writeAndPrint(getLocalName() + ": \n" + 
										"Objetivo: " +  BasicElevatorModel.this.currentObjective + "\n" +
										"Direcao atual: " + BasicElevatorModel.this.state + "\n" +
										"Andar atual: " + currPos.getY() + ", " + BasicElevatorModel.this.currentFloor + "\n" + 
										"Peso atual: " + BasicElevatorModel.this.currLoad + "\n"+
										"Lista de tarefas: " + "\n[" + taskList +  "]\n" +
										"Passageiros: " + BasicElevatorModel.this.getNumPeople() + "\n\n\n");
		
					/*System.out.println("Tempo total: " + totalTime);
					System.out.println("Tempo em que esteve vazio: " + emptyTime);
					System.out.println("Tempo com pessoas: " + withPeopleTime);
					System.out.println("Tempo parado: " + stoppedTime);
					System.out.println("Tempo em andamento: " + courseTime);*/
					 					
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
					
					if(BasicElevatorModel.this.idleTime != 0){
						BasicElevatorModel.this.idleTime--;
						Logger.writeAndPrint(getLocalName() + ": Parado por haver pessoas a entrar e sair");
						return;
					}
					
					
					if(BasicElevatorModel.this.currentObjective >= 0){
						if(currPos.getY() == currentObjective && BasicElevatorModel.this.floors.contains(currentObjective)){
							
							ejectPassengers(BasicElevatorModel.this.currentFloor);
							
							increaseTimeInElevator();
							
							getNewPassengers(BasicElevatorModel.this.currentFloor);
							
							
							BasicElevatorModel.this.floors.remove(currentObjective);
							BasicElevatorModel.this.floorInfo.remove(currentObjective);
							
							searchNextObjective();
						}
						if(BasicElevatorModel.this.idleTime != 0){
							BasicElevatorModel.this.idleTime--;
							Logger.writeAndPrint(getLocalName() + ": Parado por haver pessoas a entrar e sair");
							return;
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
						Logger.writeAndPrint(getLocalName() + " - Novo Objectivo: " + BasicElevatorModel.this.currentObjective);
					}
						
					System.out.print("\n\n\n\n\n\n\n");
				}
	  			
	  		});
		}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
		
		addBehaviour(new ContractNetResponder(this, template) {
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				Logger.writeAndPrint(getLocalName()+": Recebi este pedido: "+cfp.getContent());
				int proposal = calculateScore(cfp.getContent());
				// We provide a proposal
				Logger.writeAndPrint(getLocalName()+":  propoe " +proposal);
				ACLMessage propose = cfp.createReply();
				propose.setPerformative(ACLMessage.PROPOSE);
				propose.setContent(String.valueOf(proposal));
				return propose;
			}
			
			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				Logger.writeAndPrint(getLocalName()+": A proposta foi aceite");
				if (BasicElevatorModel.this.floors.add(Integer.parseInt(cfp.getContent().split(" ")[1]))) {
					BasicElevatorModel.this.floorInfo.put(Integer.parseInt(cfp.getContent().split(" ")[1]), new RequestInformation(cfp.getContent(), false, Integer.parseInt(propose.getContent())));
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					searchNextObjective();
					return inform;
				}
				else {
					Logger.writeAndPrint(getLocalName()+": Nao conseguiu acrescentar o pedido � lista");
					throw new FailureException("unexpected-error");
				}	
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				Logger.writeAndPrint(getLocalName()+": A proposta foi rejeitada");
			}
		});
	}
	
	public void increaseTimeInElevator(){
		for(int i = 0; i < MainController.FLOORNUM; i++){
			ArrayList<Person> persons = this.people.get(i);
			for(Person p: persons){
				p.timeInElevator++;
			}
		}
	}
	
	
	public int calculateScore(String request){
		String[] processedReq = request.split(" ");
		int target = Integer.parseInt(processedReq[1]);
		int simpleScore = 0;
		switch(processedReq[0]){
			case "SIMPLE":	
				if((this.currentFloor <= target && this.state.equals(Movement.UP)) || (this.currentFloor >= target && this.state.equals(Movement.DOWN)) || this.state.equals(Movement.NONE)){
					simpleScore = Math.abs(this.currentFloor - target);
				}
				else if(this.state.equals(Movement.UP)){
					int maxObject = Collections.max(this.floors);
					simpleScore = Math.abs(this.currentFloor - maxObject) + Math.abs(maxObject - target);
				}
				else if(this.state.equals(Movement.DOWN)){
					int minObject = Collections.min(this.floors);
					simpleScore = Math.abs(this.currentFloor - minObject) + Math.abs(minObject - target);
				}
				
				
				if(!MainController.SECTORIZATION.equals("NONE") && (target > this.sectorBounds[1] || target < this.sectorBounds[0])){
					simpleScore += 5;
				}
				
				
				if(BasicElevatorModel.WEIGHTMODEL.equals("STEP")){
					if(this.currLoad > this.maxLoad - 40){
						simpleScore += 10;
					}
				}
				else if(BasicElevatorModel.WEIGHTMODEL.equals("INCREMENTAL")){
					simpleScore += (this.currLoad/this.maxLoad)*10;
				}
				return simpleScore;
			case "UP": 
				try{
					int maxObject = Collections.max(this.floors);
					int minObject = Collections.min(this.floors);
					if(this.floorInfo.containsKey(target) && this.floorInfo.get(target).equals("DOWN")){
						simpleScore += MainController.FLOORNUM + 10;
					}
					else if(this.state.equals(Movement.UP) && this.currentFloor > target){ // CASO MAIS CHATO
						simpleScore += Math.abs(this.currentFloor - maxObject) + Math.abs(maxObject - minObject) + Math.abs(minObject - target);
					}
					else if(this.state.equals(Movement.DOWN)){
						simpleScore += Math.abs(minObject - this.currentFloor) + Math.abs(target - minObject);
					}
					else{
						simpleScore += Math.abs(target - this.currentFloor);
					}
				}
				catch(NoSuchElementException e ){ //NONE CASE
					simpleScore += Math.abs(this.currentFloor - target);
				}
				
				if(!MainController.SECTORIZATION.equals("NONE")){
					if(target > this.sectorBounds[1]){
						simpleScore += 7;
					}
					else if(target < this.sectorBounds[0]){
						simpleScore += 2;
					}
					
				}
				
				if(BasicElevatorModel.WEIGHTMODEL.equals("STEP")){
					if(this.currLoad > this.maxLoad - 40){
						simpleScore += 10;
					}
				}
				else if(BasicElevatorModel.WEIGHTMODEL.equals("INCREMENTAL")){
					simpleScore += (this.currLoad/this.maxLoad)*10;
				}
				return simpleScore;
			case "DOWN":
				try{
					int maxObject1 = Collections.max(this.floors);
					int minObject1 = Collections.min(this.floors);
					if(this.floorInfo.containsKey(target) && this.floorInfo.get(target).equals("UP")){
						simpleScore += MainController.FLOORNUM + 10;
					}
					else if(this.state.equals(Movement.DOWN) && this.currentFloor < target){
						 simpleScore += Math.abs(this.currentFloor - minObject1) + Math.abs(maxObject1 - minObject1) + Math.abs(maxObject1 - target);
					}
					else if(this.state.equals(Movement.UP)){
						simpleScore += Math.abs(maxObject1 - this.currentFloor) + Math.abs(target - maxObject1);
					}
					else{
						simpleScore += Math.abs(target - this.currentFloor);
					}
					
				}
				catch(NoSuchElementException e){
					simpleScore += Math.abs(this.currentFloor - target);
				}
				
				if(!MainController.SECTORIZATION.equals("NONE")){
					if(target > this.sectorBounds[1]){
						simpleScore += 2;
					}
					else if(target < this.sectorBounds[0]){
						simpleScore += 7;
					}
					
				}
				
				if(BasicElevatorModel.WEIGHTMODEL.equals("STEP")){
					if(this.currLoad >= this.maxLoad - 40){
						simpleScore += 10;
					}
				}
				else if(BasicElevatorModel.WEIGHTMODEL.equals("INCREMENTAL")){
					simpleScore += (this.currLoad/this.maxLoad)*10;
				}
				
				return simpleScore;
			case "SPECIFIC":
				int destination = Integer.parseInt(processedReq[2]);
				int maxObj;
				int minObj;
				try{
					 maxObj = Collections.max(this.floors);
					 minObj = Collections.min(this.floors);
					
					if(this.state.equals(Movement.UP)){
						if( target >= this.currentFloor && destination > target){
							simpleScore = Math.abs(this.currentFloor - target) + Math.abs(target - destination);
						}
						
						else if(target <= this.currentFloor && destination < target){
							simpleScore =  Math.abs(this.currentFloor - maxObj) + Math.abs(maxObj - target) + Math.abs(target - destination);
						}
						
						else if (target <= this.currentFloor && destination > target){
							simpleScore = Math.abs(this.currentFloor - maxObj) + Math.abs(maxObj - target) + Math.abs(target - Math.min(target, minObj)) + Math.abs(target - Math.min(target,minObj));
						}
						else if (target >= this.currentFloor && destination < target){
							//return dist(elevator, call) + [ dist(call, max(objectives)) + dist(max(objectives), destination) ]
							simpleScore = Math.abs(this.currentFloor - target) + Math.abs(target - maxObj) + Math.abs(maxObj - destination);
						}
					}
					else if(this.state.equals(Movement.DOWN)){
						if(target < this.currentFloor && destination < target){
							simpleScore = Math.abs(this.currentFloor - target) + Math.abs(target - destination);
						}
						else if (target > this.currentFloor && destination > target){
							simpleScore = Math.abs(minObj - this.currentFloor) + Math.abs(minObj - target) + Math.abs(target - destination);
						}
						else if (target > this.currentFloor && destination < target){
							simpleScore = Math.abs(minObj - this.currentFloor) + Math.abs(minObj - target) + Math.abs(Math.max(maxObj, target) - target) + Math.abs(Math.max(maxObj, target) - destination); 
						}
						else if (target < this.currentFloor && destination > target){
							simpleScore = Math.abs(this.currentFloor - target) + Math.abs(minObj - target) + Math.abs(minObj - destination);
						}
					}
					
					else{
						return 1;
					}
				}
				catch(NoSuchElementException e){
					simpleScore = Math.abs(this.currentFloor - target) + Math.abs(target - destination);
				}
				return simpleScore;
			default:
				break;
		}
		return -1;
	}
	
	
	public void searchNextObjective(){
		int result = -1;
		for(int val: this.floors){
			RequestInformation reqInfo = this.floorInfo.get(val);
			if(this.state.equals(Movement.UP) && val > this.currentFloor && (val < result || result == -1)){
				if(reqInfo.getDirection().equals("DOWN") && !reqInfo.isPassengerStop()){
					continue;
				}
				result = val;
			}
			else if(this.state.equals(Movement.DOWN) && val < this.currentFloor && (val > result || result == -1)){
				if(reqInfo.getDirection().equals("UP") && !reqInfo.isPassengerStop()){
					continue;
				}
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
			
		}
		else if (result == -1){
			this.state = Movement.NONE;
			if(!MainController.SECTORIZATION.equals("NONE") && (this.currentFloor > this.sectorBounds[1] || this.currentFloor < this.sectorBounds[0])){
				result = (BasicElevatorModel.this.sectorBounds[1] - BasicElevatorModel.this.sectorBounds[0])/2;
			}
		}
		
		this.currentObjective = result;
		Logger.writeAndPrint(getLocalName() + ": novo objetivo: " + this.currentObjective);
		
		if(MainController.SECTORIZATION.equals("DYNAMIC") && (this.currentObjective > this.sectorBounds[1] || this.currentObjective < this.sectorBounds[0])){
			addBehaviour(new OneShotBehaviour(){

				@Override
				public void action() {
					// TODO Auto-generated method stub
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent(BasicElevatorModel.this.currentObjective + " " + BasicElevatorModel.this.sectorBounds[0] + "-" + BasicElevatorModel.this.sectorBounds[1]);
					msg.setConversationId(MainController.randomStr());
					DFAgentDescription agentDesc = new DFAgentDescription();
					ServiceDescription serviceDesc = new ServiceDescription();
					
					serviceDesc.setType("Elevator");
					agentDesc.addServices(serviceDesc);

					DFAgentDescription[] elevators;
				
					try {
						elevators = DFService.search(myAgent, agentDesc, null);
					
				
						if(elevators.length > 0){
							for(DFAgentDescription dfd: elevators){
								if(!dfd.getName().equals(getLocalName())){
									msg.addReceiver(dfd.getName());
								}
							}
							
						}
						
						myAgent.send(msg);
					} catch (FIPAException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
		}
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
			Person p = it.next();
			Logger.writeToStat("Time waiting for the elevator: " + p.timeWaiting + "\nTime in elevator: " + p.timeInElevator + "\n");
			this.currLoad -= p.getWeight();
			this.idleTime++;
			it.remove();
		}
	}
	
	
	private void getNewPassengers(int floor){
		ListIterator<Person> it = MainController.peopleAtFloors.get(floor).listIterator();
		RequestInformation reqInfo = this.floorInfo.get(floor);
		
		while(it.hasNext()){
			Person p = it.next();
			if(this.currLoad + p.getWeight() >= this.maxLoad){
				continue;
			}
			
			if(reqInfo.getDirection().equals("SIMPLE") || (reqInfo.getDirection().equals("UP") && p.getDestination() > floor) || (reqInfo.getDirection().equals("DOWN") && p.getDestination() < floor)){
				this.currLoad += p.getWeight();
				this.people.get(p.getDestination()).add(p);
				this.floors.add(p.getDestination());
				if(this.floorInfo.containsKey(p.getDestination())){
					RequestInformation req = this.floorInfo.get(p.getDestination());
					req.setPassengerStop(true);
				}
				else{
					this.floorInfo.put(p.getDestination(), new RequestInformation(p.getDestination()));
				}
				this.idleTime++;
				Logger.writeAndPrint(getLocalName() + ": Entrou uma pessoa com objetivo: " + p.getDestination());
				it.remove();
			}
			
			
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
