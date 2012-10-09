package hu.herrbert74.osm.clcprocessor.dao;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelationMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OsmRelationsHandler extends DefaultHandler {
	Map<String, CustomRelation> villageRelationsMap = new HashMap<String, CustomRelation>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomRelation vr = new CustomRelation();

	public Map<String, CustomRelation> getRelations() {
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
				vr.addMember(new CustomRelationMember("way", Integer.parseInt(attrs.getValue("ref")), "outer"));
			}
		}
		eleStack.push(qName);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		eleStack.pop();
		if ("relation".equals(qName)) {
			villageRelationsMap.put(vr.getName(), vr);
			vr = new CustomRelation();
		}
	}
}