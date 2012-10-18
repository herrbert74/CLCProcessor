package hu.herrbert74.osm.clcprocessor;

public interface CLCProcessorConstants {
	public final String OSM_VILLAGEBORDERS = "c:\\osm\\osmdata\\village_boundaries\\villages.osm";
	public final String OSM_CLCDATA = "c:\\osm\\osmdata\\clc\\clc06_hu_resave.osm";
	public final String FOLDER_VILLAGEBORDERS = "c:\\osm\\osmdata\\village_boundaries";
	/*public final String[] MAIN_VILLAGES = {};
	public final String[] NEIGHBOUR_VILLAGES = {};
	public final String[] EXCLUDED_VILLAGES = {};*/
	public final String[] MAIN_VILLAGES = {"Bársonyos", "Kerékteleki", "Ászár", "Kisbér", "Bakonyszombathely", "Bakonybánk", "Réde"};
	public final String[] NEIGHBOUR_VILLAGES = {"Bakonyszentkirály", "Csatka", "Súr", "Ácsteszér", "Aka", "Vérteskethely", "Ete"};
	public final String[] EXCLUDED_VILLAGES = {"Sikátor", "Veszprémvarsány", "Mezőörs", "Táp", "Tápszentmiklós", "Tárkány"};

}
