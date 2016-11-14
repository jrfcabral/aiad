package serviceConsumerProviderVis;

import java.util.ArrayList;
import java.util.Random;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import repast.simphony.random.RandomHelper;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;

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

	public void createRequest(int floor) {
		try {
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.setContent(Integer.toString(floor));

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
			
			
			send(request);
			
			
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void takeDown() {
		System.out.println("VAI TUDO ABAIXOOOOOO");
	}

}
