package hu.herrbert74.osm.clcprocessor.controllers;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class CLCRelationsHandler extends DefaultHandler {

	HashMap<Integer, CustomWay> clcMainWays = new HashMap<Integer, CustomWay>();
	HashMap<Integer, CustomRelation> clcMainRelations = new HashMap<Integer, CustomRelation>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomRelation cr = new CustomRelation();
	boolean isRelationMember = false;

	public CLCRelationsHandler(HashMap<Integer, CustomWay> clcMainWays) {
		super();
		this.clcMainWays = clcMainWays;
	}

	public HashMap<Integer, CustomRelation> getRelations() {
		return clcMainRelations;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if ("relation".equals(qName)) {
			cr.setRelationId(Integer.parseInt(attrs.getValue("id")));

		}
		if ("member".equals(qName) && "relation".equals(eleStack.peek())) {
			if (clcMainWays.containsKey(Integer.parseInt(attrs.getValue("ref")))) {
				cr.addMember(new CustomRelationMember(attrs.getValue("type"), Integer.parseInt(attrs.getValue("ref")),
						attrs.getValue("role")));
				if (attrs.getValue("role").equals("outer")) {
					CustomWay cw = clcMainWays.get(Integer.parseInt(attrs.getValue("ref")));
					Iterator<Map.Entry<String, String>> it = cw.getTags().entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
						cr.addTag(pairs.getKey(), pairs.getValue());
					}
					cr.addTag("type", "multipolygon");
				}
				isRelationMember = true;
			}

		}
		eleStack.push(qName);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		eleStack.pop();
		if ("relation".equals(qName)) {
			if (isRelationMember) {
				clcMainRelations.put(cr.getRelationId(), cr);
			}
			cr = new CustomRelation();
			isRelationMember = false;
		}

	}
}