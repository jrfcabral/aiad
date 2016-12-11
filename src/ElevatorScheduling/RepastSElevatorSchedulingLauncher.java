package ElevatorScheduling;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.ui.RSApplication;
import repast.simphony.util.ContextUtils;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class RepastSElevatorSchedulingLauncher extends RepastSLauncher {

	private ContainerController mainContainer;

	@Override
	public String getName() {
		return "Service Consumer/Provider -- SAJaS RepastS Test";
	}

	@Override
	protected void launchJADE() {

		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		launchAgents();
	}

	private void launchAgents() {
		try {
			MainController main = new MainController();
			mainContainer.acceptNewAgent("Main", main).start();
			for(int i = 0;  i < MainController.ELEVATORNUM; i++){
				ElevatorModel elevator;
				if(MainController.SECTORIZATION.equals("NONE")){
					elevator = new ElevatorModel(ElevatorModel.MAXLOAD, ElevatorModel.TIMEBETWEENFLOORS, (50/MainController.ELEVATORNUM)*(i+1));
				}
				else{
					elevator = new ElevatorModel(ElevatorModel.MAXLOAD, ElevatorModel.TIMEBETWEENFLOORS, (50/MainController.ELEVATORNUM)*(i+1), (MainController.FLOORNUM/MainController.ELEVATORNUM)*i, (MainController.FLOORNUM/MainController.ELEVATORNUM)*(i+1));
				}
				mainContainer.acceptNewAgent("Elevator"+i, elevator).start();
			} 
		}
		catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Context build(Context<Object> context) {
		UserPanel up = new UserPanel();
		RSApplication.getRSApplicationInstance().addCustomUserPanel(up.createPanel());
		// http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 50, MainController.FLOORNUM);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		// Correct import: import repast.simphony.space.grid.WrapAroundBorders;

		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50, MainController.FLOORNUM));

		return super.build(context);
	}

}
