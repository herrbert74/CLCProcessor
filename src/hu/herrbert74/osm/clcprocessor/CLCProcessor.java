package hu.herrbert74.osm.clcprocessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CLCProcessor {
	ArrayList<VillageRelation> villageRelations = new ArrayList<VillageRelation>();
	ArrayList<VillageWay> villageWays = new ArrayList<VillageWay>();
	ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
	ArrayList<VillagePoligon> villagePoligons = new ArrayList<VillagePoligon>();
	
	public void findWayNames(File file)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();

		OsmRelationsHandler relationsHandler = new OsmRelationsHandler();
		parser.parse(file, relationsHandler);
		villageRelations=relationsHandler.getRelations();
		
		OsmWaysHandler waysHandler = new OsmWaysHandler();
		parser.parse(file, waysHandler);
		villageWays = waysHandler.getWays();
		
		OsmNodesHandler nodesHandler = new OsmNodesHandler();
		parser.parse(file, nodesHandler);
		villageNodes = nodesHandler.getNodes();
		
		villagePoligons = createVillagePoligons();
		int z = 0;
		int u = z;
		//return handler.getNames();
	}

	private ArrayList<VillagePoligon> createVillagePoligons() {
		int z=0;
		ArrayList<VillagePoligon> villagePoligons = new ArrayList<VillagePoligon>();
		for(VillageRelation vr: villageRelations) {
			VillagePoligon vp = new VillagePoligon();
			vp.setName(vr.getName());
			for(int member: vr.members) {
				for(VillageWay vw: villageWays) {
					if(vw.getWayId() == member){
						z++;
						for(VillageNode vn: villageNodes) {
							if(vw.members.contains((Integer)vn.nodeId)){
								vp.addVillageNode(vn);
								
							}
						}		
					}
				}	
			}
			villagePoligons.add(vp);
		}
		return villagePoligons;
	}

	private static class OsmRelationsHandler extends DefaultHandler {
		private final ArrayList<VillageRelation> villageRelations = new ArrayList<VillageRelation>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageRelation vr = new VillageRelation();
		
		public ArrayList<VillageRelation> getRelations() {
			return villageRelations;
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
				villageRelations.add(vr);
				vr = new VillageRelation();
			}
		}
	}
	
	private static class OsmWaysHandler extends DefaultHandler {
		private final ArrayList<VillageWay> villageWays = new ArrayList<VillageWay>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageWay vw = new VillageWay();

		public ArrayList<VillageWay> getWays() {
			return villageWays;
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
				villageWays.add(vw);
				vw = new VillageWay();
			}

		}
	}
	
	private static class OsmNodesHandler extends DefaultHandler {
		private final ArrayList<VillageNode> villageNodes = new ArrayList<VillageNode>();
		private final Stack<String> eleStack = new Stack<String>();
		private VillageNode vn = new VillageNode();

		public ArrayList<VillageNode> getNodes() {
			return villageNodes;
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
				villageNodes.add(vn);
				vn = new VillageNode();
			}

		}
	}

	private void printWayNames(SortedSet<String> nameSet, PrintStream out) {
		for (String name : nameSet) {
			out.println(name);
		}
	}

	public static void main(String[] args) throws Exception {
		File osmXmlFile = new File("c:\\osm\\osmdata\\village_boundaries\\villages.osm");
		CLCProcessor clcProcessor = new CLCProcessor();
		clcProcessor.findWayNames(osmXmlFile);
		//clcProcessor.printWayNames(nameSet, System.out);
	}
}
