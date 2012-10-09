package hu.herrbert74.osm.clcprocessor.dao;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OsmWaysHandler extends DefaultHandler {
	Map<Integer, CustomWay> villageWaysMap = new HashMap<Integer, CustomWay>();
	private final Stack<String> eleStack = new Stack<String>();
	private CustomWay vw = new CustomWay();

	public Map<Integer, CustomWay> getWays() {
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
			villageWaysMap.put(vw.getWayId(), vw);
			vw = new CustomWay();
		}

	}
}
