package tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.junit.Test;

import serviceConsumerProviderVis.BasicElevatorModel;

public class Tests {
	@Test
	public void testElevatorIdle(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		floorList.add(1);
		floorList.add(5);
		floorList.add(11);
		BasicElevatorModel.Movement mv = BasicElevatorModel.Movement.NONE;
		int result = BasicElevatorModel.searchNextObjective(floorList, 10, mv);
		assertEquals(result, 11);
		//assertEquals(mv, BasicElevatorModel.Movement.UP);
	}
	
	@Test
	public void testElevatorNewCall(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		floorList.add(15);
		floorList.add(9);
		BasicElevatorModel.Movement mv = BasicElevatorModel.Movement.DOWN;
		int result = BasicElevatorModel.searchNextObjective(floorList, 10, mv);
		assertEquals(result, 9);
	}
	
	@Test
	public void testNothingToDo(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		
		
		BasicElevatorModel.Movement mv = BasicElevatorModel.Movement.DOWN;
		int result = BasicElevatorModel.searchNextObjective(floorList, 10, mv);
		assertEquals(result, -1);
	}
}
