package hu.herrbert74.osm.clcprocessor;

import hu.herrbert74.osm.clcprocessor.clcpolygon.CLCPolygonHelper;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygonHelper;

import java.io.File;

public class CLCProcessor implements CLCProcessorConstants {
	
	VillagePolygonHelper villagePolygonHelper;
	CLCPolygonHelper clcPolygonHelper;
	
	private CLCProcessor(){
		villagePolygonHelper = new VillagePolygonHelper();
		clcPolygonHelper = new CLCPolygonHelper();
	}
	
	public static void main(String[] args) throws Exception {
		File osmXmlFile = new File(OSM_VILLAGEBORDERS);
		CLCProcessor clcProcessor = new CLCProcessor();
		clcProcessor.villagePolygonHelper.findWayNames(osmXmlFile);
		osmXmlFile = new File(OSM_CLCDATA);
		//clcProcessor.clcPolygonHelper.findWayNames(osmXmlFile);
	}
}
