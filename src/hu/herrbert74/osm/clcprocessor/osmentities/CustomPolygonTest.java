package hu.herrbert74.osm.clcprocessor.osmentities;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.*;

public class CustomPolygonTest {
	
	
	CustomPolygon cp;
	Intersections is;
	
	@Test
	public void test_startA() {
		setup();
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startB() {
		setup();
		Collections.rotate(cp.villageNodes, -1);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startC() {
		setup();
		Collections.rotate(cp.villageNodes, -2);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startD() {
		setup();
		Collections.rotate(cp.villageNodes, -3);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startE() {
		setup();
		Collections.rotate(cp.villageNodes, -4);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startF() {
		setup();
		Collections.rotate(cp.villageNodes, -5);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startG() {
		setup();
		Collections.rotate(cp.villageNodes, -6);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startH() {
		setup();
		Collections.rotate(cp.villageNodes, -7);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}

	@Test
	public void test_startI() {
		setup();
		Collections.rotate(cp.villageNodes, -8);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	
	@Test
	public void test_startJ() {
		setup();
		Collections.rotate(cp.villageNodes, -9);
		cp.addVillageNode(cp.getVillageNodes().get(0));
		ArrayList<CustomWay> cways = cp.getBorderWays(is);
		assertTrue(cways.size() == 2);
		assertTrue(cways.get(0).getMembers().get(0) == 3 || cways.get(0).getMembers().get(0) == 7);
		assertTrue(cways.get(0).getMembers().size() == 4);
		assertTrue(cways.get(1).getMembers().get(0) == 3 || cways.get(1).getMembers().get(0) == 7);
		assertTrue(cways.get(1).getMembers().size() == 4);
	}
	//Create villagenodes for polygon (10 nodes)
	//Intersections: 1, 2
	//TwoNodeIntersections: 6,7
	private void setup() {
		cp = new CustomPolygon();
		ArrayList<CustomNode> villageNodes = new ArrayList<CustomNode>();
		for(int i = 0; i < 10; i++) {
			CustomNode cn = new CustomNode((double)0, (double)0);
			cn.setNodeId(i);
			villageNodes.add(cn);
		}
		cp.setVillageNodes(villageNodes);
		is = new Intersections();
		HashSet<CustomNode> iss = new HashSet<CustomNode>();
		HashSet<CustomNode> twoNodeIntersections = new HashSet<CustomNode>();
		iss.add(villageNodes.get(1));
		iss.add(villageNodes.get(2));
		twoNodeIntersections.add(villageNodes.get(6));
		twoNodeIntersections.add(villageNodes.get(7));
		is.setIntersections(iss);
		is.setTwoNodeIntersections(twoNodeIntersections);
	}

	private ArrayList<CustomNode> getVillageNodes() {
		// TODO Auto-generated method stub
		return null;
	}

}
