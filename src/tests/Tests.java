package tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.junit.Test;

import ElevatorScheduling.ElevatorModel;

public class Tests {
	@Test
	public void testElevatorIdle(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		floorList.add(1);
		floorList.add(5);
		floorList.add(11);
		ElevatorModel.Movement mv = ElevatorModel.Movement.NONE;
		int result = ElevatorModel.searchNextObjective(floorList, 10, mv);
		assertEquals(result, 11);
		//assertEquals(mv, BasicElevatorModel.Movement.UP);
	}
	
	@Test
	public void testElevatorNewCall(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		floorList.add(15);
		floorList.add(9);
		ElevatorModel.Movement mv = ElevatorModel.Movement.DOWN;
		int result = ElevatorModel.searchNextObjective(floorList, 10, mv);
		assertEquals(result, 9);
	}
	
	@Test
	public void testNothingToDo(){
		LinkedHashSet<Integer> floorList = new LinkedHashSet<Integer>();
		
		
		ElevatorModel.Movement mv = ElevatorModel.Movement.DOWN;
		int result = ElevatorModel.searchNextObjective(floorList, 10, mv);
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
		
		assertEquals(ElevatorModel.getOrderOfRequest(testList, 1, ElevatorModel.Movement.UP, 17), 5);
	}
}
