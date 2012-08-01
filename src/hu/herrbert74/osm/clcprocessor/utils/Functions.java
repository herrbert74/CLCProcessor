package hu.herrbert74.osm.clcprocessor.utils;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.util.ArrayList;
import java.util.HashMap;

public class Functions {

	public static boolean isNodeInsidePolygon(ArrayList<CustomNode> p, CustomNode n) {
		boolean c = false;
		int s = p.size();
		double x = n.getLon();
		double y = n.getLat();
		int i, j = 0;
		for (i = 0, j = s - 1; i < s; j = i++) {
			double pxi = p.get(i).getLon();
			double pyi = p.get(i).getLat();
			double pxj = p.get(j).getLon();
			double pyj = p.get(j).getLat();
			if ((((pyi <= y) && (y < pyj)) || ((pyj <= y) && (y < pyi)))
					&& (x < (pxj - pxi) * (y - pyi) / (pyj - pyi)
							+ pxi))
				c = !c;
		}
		return c;
	}
	
	public static boolean wayContainsNode(CustomWay way, int node){
		boolean wayContainsNode = false;
		for(int nodeId:way.getMembers()){
			if(nodeId == node){
				wayContainsNode = true;
			}
		}
		return wayContainsNode;
	}
	
	public static int getParentRelation(CustomWay cw, HashMap<Integer, CustomRelation> crs){
		int crId = -1;
		for(CustomRelation cr: crs.values()){
			for(int i = 0; i < cr.getMembers().size(); i++){
				if(cr.getMembers().get(i).getRef() == cw.getWayId()){
					crId = cr.getRelationId();
				}
			}
		}
		return crId;
	}
}
