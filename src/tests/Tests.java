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
	
	
	@Test
	public void testOrderingFunction(){
		LinkedHashSet<Integer> testList = new LinkedHashSet<Integer>();
		testList.add(18);
		testList.add(17);
		testList.add(6);
		testList.add(5);
		testList.add(3);
		testList.add(0);
		
		assertEquals(BasicElevatorModel.getOrderOfRequest(testList, 1, BasicElevatorModel.Movement.UP, 17), 5);
	}
}
