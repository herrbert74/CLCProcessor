package hu.herrbert74.osm.clcprocessor.utils;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;

import java.util.ArrayList;

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
}
