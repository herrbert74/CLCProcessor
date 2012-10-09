package hu.herrbert74.osm.clcprocessor.dao;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomRelation;
import hu.herrbert74.osm.clcprocessor.osmentities.CustomWay;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class XMLFunctions {
	public static void writePolygon(ArrayList<CustomNode> polygon,
			String filename) {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		FileOutputStream stream;
		try {
			stream = new FileOutputStream("c:\\osm\\" + filename);
		} catch (Exception e) {
			System.out.append("nincs file");
			return;
		}
		try {
			writer = factory.createXMLStreamWriter(stream);

			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement("osm");
			writer.writeAttribute("version", "0.6");
			writer.writeAttribute("upload", "true");
			writer.writeAttribute("generator", "JOSM");
			writer.writeCharacters("\r\n");

			for (int i = 0; i < polygon.size(); i++) {
				writer.writeStartElement("node");
				writer.writeAttribute("id",
						"-" + Integer.toString(polygon.get(i).getNodeId()));
				writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
				writer.writeAttribute("visible", "true");
				writer.writeAttribute("lat",
						Double.toString(polygon.get(i).getLat()));
				writer.writeAttribute("lon",
						Double.toString(polygon.get(i).getLon()));
				writer.writeEndElement();
				writer.writeCharacters("\r\n");
			}

			writer.writeStartElement("way");
			writer.writeAttribute("id", "-23456");
			writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
			writer.writeAttribute("visible", "true");
			writer.writeCharacters("\r\n");

			for (int i = 0; i < polygon.size(); i++) {
				writer.writeStartElement("nd");
				writer.writeAttribute("ref",
						"-" + Integer.toString(polygon.get(i).getNodeId()));
				writer.writeEndElement();
				writer.writeCharacters("\r\n");

			}
			writer.writeEndElement();
			writer.writeCharacters("\r\n");

			writer.writeEndElement();
			writer.writeEndDocument();

			writer.flush();
			writer.close();
		} catch (Exception e) {
			return;
		}
	}

	public static void writeOSM(HashMap<Integer, CustomNode> clcMainNodes,
			HashMap<Integer, CustomWay> clcMainWays,
			HashMap<Integer, CustomRelation> clcMainRelations, String filename) {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		FileOutputStream stream;
		try {
			stream = new FileOutputStream("c:\\osm\\" + filename);
		} catch (Exception e) {
			System.out.append("nincs file");
			return;
		}
		try {
			writer = factory.createXMLStreamWriter(stream);

			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement("osm");
			writer.writeAttribute("version", "0.6");
			writer.writeAttribute("upload", "true");
			writer.writeAttribute("generator", "JOSM");
			writer.writeCharacters("\r\n");

			// nodes
			for (CustomNode cn : clcMainNodes.values()) {
				writer.writeStartElement("node");
				writer.writeAttribute("id", Integer.toString(cn.getNodeId()));
				writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
				writer.writeAttribute("visible", "true");
				writer.writeAttribute("lat", Double.toString(cn.getLat()));
				writer.writeAttribute("lon", Double.toString(cn.getLon()));
				writer.writeEndElement();
				writer.writeCharacters("\r\n");
			}

			// ways
			for (CustomWay cw : clcMainWays.values()) {
				writer.writeStartElement("way");
				writer.writeAttribute("id", Integer.toString(cw.getWayId()));
				writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
				writer.writeAttribute("visible", "true");
				writer.writeCharacters("\r\n");

				for (int i = 0; i < cw.getMembers().size(); i++) {
					writer.writeStartElement("nd");
					writer.writeAttribute("ref",
							Integer.toString(cw.getMembers().get(i)));
					writer.writeEndElement();
					writer.writeCharacters("\r\n");

				}
				writer.writeEndElement();
				writer.writeCharacters("\r\n");
			}
			// relations
			for (CustomRelation cr : clcMainRelations.values()) {
				writer.writeStartElement("relation");
				writer.writeAttribute("id",
						Integer.toString(cr.getRelationId()));
				writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
				writer.writeAttribute("visible", "true");
				writer.writeCharacters("\r\n");

				for (int i = 0; i < cr.getMembers().size(); i++) {
					writer.writeStartElement("member");
					writer.writeAttribute("ref",
							Integer.toString(cr.getMembers().get(i).getRef()));
					writer.writeAttribute("type", cr.getMembers().get(i)
							.getType());
					writer.writeAttribute("role", cr.getMembers().get(i)
							.getRole());
					writer.writeEndElement();
					writer.writeCharacters("\r\n");
				}

				Iterator<Map.Entry<String, String>> it = cr.getTags()
						.entrySet().iterator();
				while (it.hasNext()) {
					writer.writeStartElement("tag");

					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
							.next();
					writer.writeAttribute("k", pairs.getKey());
					writer.writeAttribute("v", pairs.getValue());
					writer.writeEndElement();
					writer.writeCharacters("\r\n");
					//it.remove(); // avoids a ConcurrentModificationException
				}

				writer.writeEndElement();
				writer.writeCharacters("\r\n");
			}
			writer.writeEndDocument();

			writer.flush();
			writer.close();
		} catch (Exception e) {

		}
	}
}
