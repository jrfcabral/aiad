package serviceConsumerProviderVis;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.random.RandomHelper;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import sajas.proto.ContractNetInitiator;

public class MainController extends Agent {
	public static int FLOORNUM = 21;
	public static String REQTYPE = "SIMPLE"; //SIMPLE, DIRECTIONAL or SPECIFIC
	public static int ELEVATORNUM = 2;
	public static int REQPROBABILITY = 15;
	public static String SECTORIZATION	= "DYNAMIC";
	public static String REALLOCATION = "NONE";
	
	public static ArrayList<ArrayList<Person>> peopleAtFloors = new ArrayList< ArrayList<Person>>(FLOORNUM);	
	
	public MainController(){
		System.out.println("Constructing");
		for(int i = 0; i < FLOORNUM; i++){
			MainController.peopleAtFloors.add(new ArrayList<Person>());
		}
	}
	
	public int waitingTime(){
		int total = 0;
		int people = 0;
		for (ArrayList<Person> floor: this.peopleAtFloors){
			for (Person person: floor){
				total += person.getTimeWaiting();
				people++;
			}
		}
		int result = people == 0 ?  0 : total/people;
		return result;
	}
	
	private int randomDestination(int origin){
		int destination;
		do{
			destination = RandomHelper.nextIntFromTo(0, FLOORNUM-1);
		}while(destination == origin);
		return destination;
	}
	
	public void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Main");
		sd.setType("main");
		sd.addOntologies("elevator-ontology");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		addBehaviour(new TickerBehaviour(this, 1000) {

			@Override
			protected void onTick() {
				
				increaseTimeWaiting();
				
				int prob = RandomHelper.nextIntFromTo(0, 100);
				if (prob <= REQPROBABILITY) {
					
					int floor = RandomHelper.nextIntFromTo(0, FLOORNUM+9 );
					int min = FLOORNUM, max = 0;
					LinkedList<Integer> targets = new LinkedList<Integer>();
					
					if (floor < 10) {
						for(int i = 0; i < RandomHelper.nextIntFromTo(1, 2); i++){
							int personDestination = randomDestination(0);
							MainController.peopleAtFloors.get(0).add(new Person(personDestination));
							if(min > personDestination){
								min = personDestination;
							}
							if(max < personDestination){
								max = personDestination;
							}
							targets.add(personDestination);
						}
						createRequest(0, min, max, targets);
					} else {
						int destination = floor -10;
						for(int i = 0; i < RandomHelper.nextIntFromTo(1, 5); i++){
							int personDestination = randomDestination(destination);
							MainController.peopleAtFloors.get(destination).add(new Person(personDestination));
							if(min > personDestination){
								min = personDestination;
							}
							if(max < personDestination){
								max = personDestination;
							}
							targets.add(personDestination);
						}
						createRequest(destination, min, max, targets);	
					}
				}
			}

		});
	}

	public static String randomStr(){
		SecureRandom rand = new SecureRandom();
		return new BigInteger(130, rand).toString(64);
	}
	
	public void increaseTimeWaiting(){
		for(int i = 0; i < FLOORNUM; i++){
			ArrayList<Person> people = this.peopleAtFloors.get(i);
			for(Person p: people){
				p.timeWaiting++;
			}
		}
	}
	
	
	public void createRequest(int floor, int targetFloorMin, int targetFloorMax, LinkedList<Integer> targets) {
			if(REQTYPE.equals("SIMPLE")){
				ACLMessage request = new ACLMessage(ACLMessage.CFP);
				request.setContent( "SIMPLE" + " " + Integer.toString(floor));
				completeMessageAndSend(request);
			}
			else if(REQTYPE.equals("DIRECTIONAL")){
				
				if(targetFloorMin > floor){
					ACLMessage request = new ACLMessage(ACLMessage.CFP);
					request.setContent( "UP" + " " + Integer.toString(floor));
					completeMessageAndSend(request);
				}
				else if(targetFloorMax < floor){
					ACLMessage request = new ACLMessage(ACLMessage.CFP);
					request.setContent("DOWN" + " " + Integer.toString(floor));
					completeMessageAndSend(request);
					
				}
				else if(targetFloorMax > floor && targetFloorMin < floor){
					ACLMessage request = new ACLMessage(ACLMessage.CFP);
					request.setContent("UP" + " " + Integer.toString(floor));
					completeMessageAndSend(request);
					
					ACLMessage request2 = new ACLMessage(ACLMessage.CFP);
					request2.setContent("DOWN" + " " + Integer.toString(floor));
					completeMessageAndSend(request2);
				}
			}
			else if(REQTYPE.equals("SPECIFIC")){
				LinkedHashSet<Integer> targetList = new LinkedHashSet<Integer>();
				for(Integer i: targets){
					if(!targetList.contains(i)){
						ACLMessage request = new ACLMessage(ACLMessage.CFP);
						request.setContent("SPECIFIC" + " " + Integer.toString(floor) + " " + i.intValue());
						targetList.add(i);
						completeMessageAndSend(request);
					}
					
				}
			}
	}
	
	
	public void completeMessageAndSend(ACLMessage request){
		try {
			request.setConversationId(randomStr());
			request.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			request.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
		
			DFAgentDescription agentDesc = new DFAgentDescription();
			ServiceDescription serviceDesc = new ServiceDescription();
			
			serviceDesc.setType("Elevator");
			agentDesc.addServices(serviceDesc);

			DFAgentDescription[] elevators;
		
			elevators = DFService.search(this, agentDesc, null);
		
		
			if(elevators.length > 0){
				for(DFAgentDescription dfd: elevators){
					System.out.println("Found " + dfd.getName());
					request.addReceiver(dfd.getName());
				}
			}
			else{
				System.out.println("Catastrophic failure!!");
			}
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startProtocol(request);
		
	}
	
	public void startProtocol(ACLMessage request){
		addBehaviour(new ContractNetInitiator(this, request){
			protected void handlePropose(ACLMessage propose, Vector v) {
				System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent "+refuse.getSender().getName()+" refused");
			}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed");
				}
				// Immediate failure --> we will not receive a response from this agent
			}
			
			protected void handleAllResponses(Vector responses, Vector acceptances) {
				// Evaluate proposals.
				int bestProposal = Integer.MAX_VALUE;
				jade.core.AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements()) {
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						acceptances.addElement(reply);
						int proposal = Integer.parseInt(msg.getContent());
						if (proposal < bestProposal) {
							bestProposal = proposal;
							bestProposer = (jade.core.AID) msg.getSender();
							accept = reply;
						}
					}
				}
				// Accept the proposal of the best proposer
				if (accept != null) {
					System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}						
			}
			
		});
	}
	
	public void takeDown() {
		System.out.println("VAI TUDO ABAIXOOOOOO");
	}

}
