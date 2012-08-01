package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;

public class WayPair {

	int first;
	int second;
	ArrayList<Integer> startA = new ArrayList<Integer>();
	ArrayList<Integer> endA = new ArrayList<Integer>();
	ArrayList<Integer> startB = new ArrayList<Integer>();
	ArrayList<Integer> endB = new ArrayList<Integer>();
	ArrayList<CustomWay> newWay;
	int numberOfWays;

	public int getNumberOfWays() {
		return numberOfWays;
	}

	public void setNumberOfWays(int numberOfWays) {
		this.numberOfWays = numberOfWays;
	}

	public WayPair(int a, int b) {
		numberOfWays = 0;
		first = a;
		second = b;
		newWay = new ArrayList<CustomWay>();
	}

	public int getStartA(int pos) {
		return startA.get(pos);
	}
	
	public void addStartB(int value) {
		startB.add(value);
	}

	public void addEndA(int value) {
		endA.add(value);
	}
	
	public int getEndASize(){
		return endA.size();
	}
	
	public int getEndBSize(){
		return endB.size();
	}
	
	public void addEndB(int value) {
		endB.add(value);
	}
	
	public void addStartA(int value) {
		startA.add(value);
	}
	public void setStartA(int value, int pos) {
		startA.set(pos, value);
	}

	public void setEndA(int value, int pos) {
		endA.set(pos, value);
	}

	public void setStartB(int value, int pos) {
		startB.set(pos, value);
	}

	public void setEndB(int value, int pos) {
		endB.set(pos, value);
	}

	public int getEndA(int pos) {
		return endA.get(pos);
	}

	public int getStartB(int pos) {
		return startB.get(pos);
	}

	public int getEndB(int pos) {
		return endB.get(pos);
	}

	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}

	public CustomWay getNewWay(int pos) {
		return newWay.get(pos);
	}

	public void setNewWayId(int newWayId, int pos) {
		newWay.get(pos).setWayId(newWayId);
	}

	public void addNewWay(CustomWay otherWay) {
		newWay.add(otherWay);
	}

	public int hashCode() {
		return (Math.max(first, second) * 31) ^ (Math.abs(Math.min(first, second)));
	}

	public boolean equals(Object o) {
		if (o instanceof WayPair) {
			WayPair other = (WayPair) o;
			return (first == other.first && second == other.second) || (first == other.second && second == other.first);
					//&& (startA.equals(other.startA) || startA.equals(other.startB)));
		}
		return false;
	}

}
