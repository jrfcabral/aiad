package elevatorManagement;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.domain.DFService;

public class BasicElevatorModel extends Agent{
	private int currentFloor = 0;
	private int timeBetweenFloors = 1000; //millis
	private int maxLoad = 500; //kg
	private int[] floorStack;

	private MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			MessageTemplate.MatchOntology("make-stop"));

	
	public BasicElevatorModel(int timeBetweenFloors, int maxLoad){
		this.timeBetweenFloors = timeBetweenFloors;
		this.maxLoad = maxLoad;
		
	}
	
	public void setup(){
		System.out.println("Elevator " + getLocalName() + " coming online");
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
					ACLMessage msg = myAgent.receive(template);
					if(msg != null){
						ACLMessage reply = msg.createReply();
						int floorToStop = Integer.parseInt(msg.getContent());
						double heuristicScore = calculateScore(floorToStop);
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(getLocalName() + " " + heuristicScore);
						myAgent.send(reply);
					}
				}
	  		});
	  		
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
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
