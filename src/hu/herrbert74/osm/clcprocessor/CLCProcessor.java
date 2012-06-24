package hu.herrbert74.osm.clcprocessor;

import hu.herrbert74.osm.clcprocessor.controllers.SettlementChooserController;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CLCProcessor implements CLCProcessorConstants {
	
	
	ArrayList<VillagePolygon> villagePolygons = new ArrayList<VillagePolygon>();
	
	public CLCProcessor() {
		SettlementChooserModel scModel 	= new SettlementChooserModel();
		SettlementChooserView scView 	= new SettlementChooserView();

		scModel.addObserver(scView);
		SettlementChooserController scController = new SettlementChooserController();
		scController.addModel(scModel);
		scController.addView(scView);
		scController.initModel();

		scView.addController(scController);
		
		scView.shell.open();
		
		try {
			scModel.read(new File(OSM_VILLAGEBORDERS));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		//and Model, 
		//this was only needed when the view inits the model
		//myView.addModel(myModel);

		
		
		while (!scView.shell.isDisposed()) {
			if (!scView.display.readAndDispatch()) {
				scView.display.sleep();
			}
		}
	}
	
	public static void main(String[] args){
		CLCProcessor clcProcessor = new CLCProcessor();
	}
}
