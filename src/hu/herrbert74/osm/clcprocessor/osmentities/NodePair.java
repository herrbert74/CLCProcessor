package hu.herrbert74.osm.clcprocessor.osmentities;

public class NodePair implements Comparable<NodePair>{

	int first;
	int second;
	
	public NodePair(int a, int b) {
		first = a;
		second = b;
	}

	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}

	@Override
	public int compareTo(NodePair o) {
		return first - o.getFirst();
	}

	public void setFirst(int first){
		this.first = first;
	}
	
	public void setSecond(int second){
		this.second = second;
	}
}
