package hu.herrbert74.osm.clcprocessor.osmentities;

public class CustomNode implements Comparable<CustomNode>{
	int nodeId;
	public CustomNode(double lon, double lat){
		this.lon = lon;
		this.lat = lat;
	}
	
	public CustomNode(){
	}
	
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	double lat;
	double lon;
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}

	@Override
	public int compareTo(CustomNode o) {
		Double lat2 = lat;
		int i = lat2.compareTo(o.lat);
	    if (i != 0) return i;

	    Double lon2 = lon;
	    i = lon2.compareTo(o.lon);
	    
	    return i;

	}
	
}
