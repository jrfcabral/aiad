package elevatorManagement;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import sajas.core.Runtime;

public class LeModel extends RepastSLauncher{
	
	private ContainerController mainContainer;
	
	public static void main(String[] args) {
		LeModel model = new LeModel();
		//Context<?> context = model.build(new Context<Object>());
		
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile pl = new ProfileImpl();
		
		mainContainer = rt.createMainContainer(pl);
		launchAgents();
		
	}
	
	
	public void launchAgents(){
		for(int i = 0; i < 3; i++){
			BasicElevatorModel elevator = new BasicElevatorModel(1000, 500);
			try {
				mainContainer.acceptNewAgent("Elevator"+i, elevator);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Context<Object> build(Context<Object> context){
		return super.build(context);
	}
}
