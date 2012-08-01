package hu.herrbert74.osm.clcprocessor.osmentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomWay {
	int id;
	ArrayList<Integer> members;
	HashMap<String, String> tags;
	boolean isFullRound;
	
	public CustomWay(){
		id = 0;
		members = new ArrayList<Integer>();
		tags = new HashMap<String, String>();
		isFullRound = false;
	}
	
	public boolean isFullRound() {
		return isFullRound;
	}

	public void setFullRound(boolean isFullRound) {
		this.isFullRound = isFullRound;
	}

	public int getWayId() {
		return id;
	}
	public void setWayId(int id) {
		this.id = id;
	}
	public ArrayList<Integer> getMembers() {
		return members;
	}
	
	public void addMember(Integer member) {
		members.add(member);
	}
	
	public void setMembers(List<Integer> otherMembers) {
		members =  new ArrayList<Integer> ( otherMembers);
	}
	
	public void addMembers(List<Integer> otherMembers) {
		members.addAll(otherMembers);
	}
	
	public void insertMember(int pos, Integer member) {
		members.add(pos, member);
	}
	
	public void addTag(String K, String V){
		tags.put(K, V);
	}
	
	public HashMap<String, String> getTags() {
		return tags;
	}

	public int containsNode(int nodeId){
		int result = -1;
		for(int i = 0; i < members.size(); i++){
			if(nodeId == members.get(i)){
				result = i;
			}
		}
		return result;
	}

	/*public ArrayList<CustomWay> extractWays(ArrayList<NodePair> compactedStartEndPairs) {
		ArrayList<CustomWay> result = new ArrayList<CustomWay>();
		for(NodePair startEndPair : compactedStartEndPairs){
			CustomWay newWay = new CustomWay();
			if(startEndPair.getFirst() > startEndPair.getSecond()){
				newWay.addMembers(getMembers().subList(startEndPair.getFirst() + 1, getMembers().size()));
				newWay.addMembers(getMembers().subList(0, startEndPair.getSecond()));
			}else{
				newWay.addMembers(getMembers().subList(startEndPair.getFirst() + 1, startEndPair.getSecond()));
			}
			result.add(newWay);
		}
		return result;
	}*/
}
