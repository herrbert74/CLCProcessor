package hu.herrbert74.osm.clcprocessor.clcpolygon;

import hu.herrbert74.osm.clcprocessor.utils.LatLongUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CLCPolygonHelper {
	/*ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
	ArrayList<VillageRelation> villageRelations = new ArrayList<VillageRelation>();
	ArrayList<VillageWay> villageWays = new ArrayList<VillageWay>();*/
	ArrayList<CLCPolygon> villagePolygons = new ArrayList<CLCPolygon>();
	Map<Integer, CLCNode> villageNodesMap = new HashMap<Integer, CLCNode>(); 
	Map<Integer, CLCWay> villageWaysMap = new HashMap<Integer, CLCWay>();
	Map<String, CLCRelation> villageRelationsMap = new HashMap<String, CLCRelation>(); 
		
	public void findWayNames(File file) throws ParserConfigurationException,
			SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		/*System.out.println("Reading relations");
		OsmRelationsHandler relationsHandler = new OsmRelationsHandler();
		parser.parse(file, relationsHandler);
		villageRelationsMap = relationsHandler.getRelations();
		
		System.out.println("Reading ways");
		OsmWaysHandler waysHandler = new OsmWaysHandler();
		parser.parse(file, waysHandler);
		villageWaysMap = waysHandler.getWays();*/

		System.out.println("Reading nodes");
		OsmNodesHandler nodesHandler = new OsmNodesHandler();
		parser.parse(file, nodesHandler);
		villageNodesMap = nodesHandler.getNodes();

		villagePolygons = createVillagePoligons();
	}

	private ArrayList<CLCPolygon> createVillagePoligons() {
		ArrayList<CLCPolygon> villagePoligons = new ArrayList<CLCPolygon>();
		int z = 0;
		for (CLCRelation vr : villageRelationsMap.values()) {
			System.out.println("polygon: " + Integer.toString(++z) + "/" + Integer.toString(villageRelationsMap.size()));
			CLCPolygon vp = new CLCPolygon();
			vp.setName(vr.getName());
			for (int member : vr.members) {
				CLCWay vw = villageWaysMap.get(member);
				for (int memberNode : vw.members) {
					vp.addVillageNode(villageNodesMap.get(memberNode));
				}
			}
			villagePoligons.add(vp);
		}
		return villagePoligons;
	}

	private static class OsmNodesHandler extends DefaultHandler {
		Map<Integer, CLCNode> villageNodesMap = new HashMap<Integer, CLCNode>(); 
		private final ArrayList<CLCNode> villageNodes = new ArrayList<CLCNode>();
		private final Stack<String> eleStack = new Stack<String>();
		private CLCNode vn = new CLCNode();
		int lastKey;
		
		public Map<Integer, CLCNode> getNodes() {
			return villageNodesMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("node".equals(qName)) {
				vn.setNodeId(Integer.parseInt(attrs.getValue("id")));
				vn.setLat(Double.parseDouble(attrs.getValue("lat")));
				vn.setLon(Double.parseDouble(attrs.getValue("lon")));
			}
			eleStack.push(qName);

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eleStack.pop();
			if ("node".equals(qName)) {
				villageNodesMap.put((Integer) vn.nodeId, vn);
				if(villageNodesMap.size()>1){
					double lat1 = villageNodesMap.get(lastKey).lat;
					double lat2 = villageNodesMap.get(vn.nodeId).lat;
					double lon1 = villageNodesMap.get(lastKey).lon;
					double lon2 = villageNodesMap.get(vn.nodeId).lon;
					double dist = LatLongUtil.distance(lat1, lon1, lat2, lon2);
					System.out.println("lat1: " + lat1 + " lat2: " + lat2 + " lon1: " + lon1 + " lon2: " + lon2 + " dist: " + dist );
				}
				lastKey = vn.nodeId;
				vn = new CLCNode();
			}
		}
	}

	private static class OsmRelationsHandler extends DefaultHandler {
		Map<String, CLCRelation> villageRelationsMap = new HashMap<String, CLCRelation>();
		private final ArrayList<CLCRelation> villageRelations = new ArrayList<CLCRelation>();
		private final Stack<String> eleStack = new Stack<String>();
		private CLCRelation vr = new CLCRelation();

		public Map<String, CLCRelation> getRelations() {
			return villageRelationsMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("tag".equals(qName) && "relation".equals(eleStack.peek())) {
				String key = attrs.getValue("k");
				if ("name".equals(key)) {
					vr.setName(attrs.getValue("v"));
				}
			}
			if ("member".equals(qName) && "relation".equals(eleStack.peek())) {
				String key = attrs.getValue("type");
				if ("way".equals(key)) {
					vr.addMember(Integer.parseInt(attrs.getValue("ref")));
				}
			}
			eleStack.push(qName);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eleStack.pop();
			if ("relation".equals(qName)) {
				villageRelationsMap.put(vr.name, vr);
				vr = new CLCRelation();
			}
		}
	}

	private static class OsmWaysHandler extends DefaultHandler {
		Map<Integer, CLCWay> villageWaysMap = new HashMap<Integer, CLCWay>(); 
		private final ArrayList<CLCWay> villageWays = new ArrayList<CLCWay>();
		private final Stack<String> eleStack = new Stack<String>();
		private CLCWay vw = new CLCWay();

		public Map<Integer, CLCWay> getWays() {
			return villageWaysMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if ("way".equals(qName)) {
				vw.setWayId(Integer.parseInt(attrs.getValue("id")));

			}
			if ("nd".equals(qName) && "way".equals(eleStack.peek())) {
				vw.addMember(Integer.parseInt(attrs.getValue("ref")));
			}
			eleStack.push(qName);

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eleStack.pop();
			if ("way".equals(qName)) {
				villageWaysMap.put(vw.id, vw);
				vw = new CLCWay();
			}

		}
	}
}
