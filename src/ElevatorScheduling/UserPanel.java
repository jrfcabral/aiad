package ElevatorScheduling;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class UserPanel implements repast.simphony.userpanel.ui.UserPanelCreator{

	@Override
	public JPanel createPanel() {
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new GridLayout(9,1));
		
		JLabel floorNumLbl = new JLabel("Number of floors"); 
		JLabel elevatorNumLbl = new JLabel("Number of elevators");
		JLabel reqTypeLbl = new JLabel("Type of request");
		JLabel reqProbabilityLbl = new JLabel("Probability of generating requests (per second)");
		JLabel weightModelLbl = new JLabel("Weight Model");
		JLabel sectorizationLbl = new JLabel("Sectorization Model");
		JLabel maxLoadLbl = new JLabel("Elevator max load");
		JLabel tickPerSecondLbl = new JLabel("Time between ticks (in milliseconds)");
		
		JTextField elevatorNum = new JTextField(Integer.toString(MainController.ELEVATORNUM));
		JTextField floorNum = new JTextField(Integer.toString(MainController.FLOORNUM));
		
		String[] reqTypeOptions = {"SIMPLE", "DIRECTIONAL", "SPECIFIC"};
		JComboBox<String> reqType = new JComboBox<String>(reqTypeOptions);
		
		JSlider reqProbability = new JSlider();
		reqProbability.setMinimum(0);
		reqProbability.setMaximum(100);
		reqProbability.setValue(MainController.REQPROBABILITY);
		
		String[] weightModelOptions = {"NONE", "STEP", "INCREMENTAL"};
		JComboBox<String> weightModel = new JComboBox<String>(weightModelOptions);
		
		String[] sectorizationModelOptions = {"NONE", "RIGID", "DYNAMIC"};
		JComboBox<String> sectorizationModel = new JComboBox<String>(weightModelOptions);
		
		JTextField maxLoad = new JTextField(Integer.toString(ElevatorModel.MAXLOAD));
		JTextField tickPerSecond = new JTextField(Integer.toString(ElevatorModel.TIMEBETWEENFLOORS));
		
		
		JPanel floorPanel = new JPanel(new GridLayout(1, 2));
		floorPanel.add(floorNumLbl);
		floorPanel.add(floorNum);
		
		JPanel elevatorPanel = new JPanel(new GridLayout(1, 2));
		elevatorPanel.add(elevatorNumLbl);
		elevatorPanel.add(elevatorNum);
		
		JPanel maxLoadPanel = new JPanel(new GridLayout(1, 2));
		maxLoadPanel.add(maxLoadLbl);
		maxLoadPanel.add(maxLoad);
		
		JPanel tickPanel = new JPanel(new GridLayout(1, 2));
		tickPanel.add(tickPerSecondLbl);
		tickPanel.add(tickPerSecond);
		
		JPanel reqTPanel = new JPanel(new GridLayout(1, 2));
		reqTPanel.add(reqTypeLbl);
		reqTPanel.add(reqType);
		
		JPanel reqPPanel = new JPanel(new GridLayout(2, 1));
		reqPPanel.add(reqProbabilityLbl);
		reqPPanel.add(reqProbability);
		
		JPanel weightModelPanel = new JPanel(new GridLayout(1, 2));
		weightModelPanel.add(weightModelLbl);
		weightModelPanel.add(weightModel);
		
		JPanel sectorizationModelPanel = new JPanel(new GridLayout(1, 2));
		sectorizationModelPanel.add(sectorizationLbl);
		sectorizationModelPanel.add(sectorizationModel);
		
		
		JButton saveButton = new JButton("Save settings");
		
		settingsPanel.add(floorPanel);
		settingsPanel.add(elevatorPanel);
		settingsPanel.add(maxLoadPanel);
		settingsPanel.add(tickPanel);
		settingsPanel.add(reqTPanel);
		settingsPanel.add(reqPPanel);
		settingsPanel.add(weightModelPanel);
		settingsPanel.add(sectorizationModelPanel);
		settingsPanel.add(saveButton);
		
		saveButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					MainController.ELEVATORNUM = Integer.parseInt(elevatorNum.getText());
				}
				catch(NumberFormatException e){
					elevatorNum.setText(Integer.toString(MainController.ELEVATORNUM));
				}
				
				try{
					MainController.FLOORNUM = Integer.parseInt(floorNum.getText());
				}
				catch(NumberFormatException e){
					floorNum.setText(Integer.toString(MainController.FLOORNUM));
				}
				
				MainController.REQTYPE = (String) reqType.getSelectedItem();
				MainController.REQPROBABILITY = reqProbability.getValue();
				
			}
			
		});
		
		
		return settingsPanel;
	}

}
