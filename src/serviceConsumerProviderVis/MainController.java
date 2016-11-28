package serviceConsumerProviderVis;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import repast.simphony.random.RandomHelper;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import sajas.proto.ContractNetInitiator;

public class MainController extends Agent {
	public static final int FLOORNUM = 21;
	
	
	public static ArrayList<ArrayList<Person>> peopleAtFloors = new ArrayList< ArrayList<Person>>(FLOORNUM);	
	
	public MainController(){
		System.out.println("Constructing");
		for(int i = 0; i < FLOORNUM; i++){
			MainController.peopleAtFloors.add(new ArrayList<Person>());
		}
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
				int prob = RandomHelper.nextIntFromTo(0, 100);
				if (prob <= 20) {
					
					int floor = RandomHelper.nextIntFromTo(0, FLOORNUM+9 );
					if (floor < 10) {
						MainController.peopleAtFloors.get(0).add(new Person(randomDestination(0)));
						createRequest(0);
					} else {
						int destination = floor -10;
						MainController.peopleAtFloors.get(destination).add(new Person(randomDestination(destination)));						
						createRequest(destination);
					}
				}
			}

		});
	}

	public String randomStr(){
		SecureRandom rand = new SecureRandom();
		return new BigInteger(130, rand).toString(64);
	}
	
	
	public void createRequest(int floor) {
		try {
			ACLMessage request = new ACLMessage(ACLMessage.CFP);
			request.setContent(Integer.toString(floor));
			request.setConversationId(randomStr());
			request.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			request.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
			
			DFAgentDescription agentDesc = new DFAgentDescription();
			ServiceDescription serviceDesc = new ServiceDescription();

			serviceDesc.setType("Elevator");
			agentDesc.addServices(serviceDesc);

			DFAgentDescription[] elevators = DFService.search(this, agentDesc, null);
			
			if(elevators.length > 0){
				for(DFAgentDescription dfd: elevators){
					System.out.println("Found " + dfd.getName());
					request.addReceiver(dfd.getName());
				}
			}
			else{
				System.out.println("Catastrophic failure!!");
			}
			
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
			
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void takeDown() {
		System.out.println("VAI TUDO ABAIXOOOOOO");
	}

}
