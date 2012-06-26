package hu.herrbert74.osm.clcprocessor;

import hu.herrbert74.osm.clcprocessor.controllers.SettlementChooserController;
import hu.herrbert74.osm.clcprocessor.models.SettlementChooserModel;
import hu.herrbert74.osm.clcprocessor.views.SettlementChooserView;
import hu.herrbert74.osm.clcprocessor.villagepolygon.VillagePolygon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

public class CLCProcessor implements CLCProcessorConstants {
	
	static int canvasStyle = SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL;

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
	/*public static void main(String[] args) {
	    final Display display = new Display();
	    final Shell shell = new Shell(display);
	    shell.setLayout(new FillLayout());
	    shell.setBackground(display.getSystemColor((SWT.COLOR_CYAN)));
	    shell.setText("Canvas Test");
	    FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		FormData createCLCData = new FormData((int) (shell.getSize().x * 0.5), 300);
		createCLCData.left = new FormAttachment(5);
		createCLCData.top = new FormAttachment(5);
		final Canvas canvas = new Canvas (shell, canvasStyle);
		canvas.setLayoutData(createCLCData);
	    canvas.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	    canvas.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));

	    final Point timelineSize = new Point(80, 1600);
	    final Point offset = new Point(0,0);
	    final ScrollBar hBar = canvas.getVerticalBar();

	    // Create a paint handler for the canvas
	    canvas.addPaintListener(new PaintListener() {
	      public void paintControl(PaintEvent e) {
	    	  System.out.println("canvas");
	        for (int y = 100; y < timelineSize.y; y += 100)
	        {
	          e.gc.drawLine(0, y + offset.y, 20, y + offset.y);
	          e.gc.drawText(Integer.toString(y), 30, y + offset.y, true);
	        }
	      }
	    });

	 // The below event handlers allow for horizontal scrolling functionality
	    hBar.setIncrement(100);
	    hBar.addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event e) {
	        	System.out.println("hbar");
	            int hSelection = hBar.getSelection();
	            int destX = -hSelection - offset.y;
	            canvas.scroll(0, destX , 0, 0, timelineSize.x, timelineSize.y, false);
	            offset.y = -hSelection;     
	        }
	    });

	    canvas.addListener(SWT.Resize, new Listener() {
	        public void handleEvent(Event e) {
	        	System.out.println("canvaslistener");
	          Rectangle client = canvas.getClientArea();
	          hBar.setMaximum(timelineSize.y);
	          hBar.setThumb(Math.min(client.height, timelineSize.y));
	          int hPage = timelineSize.x - client.height;
	          int hSelection = hBar.getSelection();
	          if (hSelection >= hPage) {
	            if (hPage <= 0)
	              hSelection = 0;
	            offset.y = -hSelection;
	          }
	          shell.redraw();
	        }
	      });

	    shell.open();
	    while(!shell.isDisposed()) {
	        if(!display.readAndDispatch()) {
	            display.sleep();
	        }
	    }
	    display.dispose();

	  }*/

}
