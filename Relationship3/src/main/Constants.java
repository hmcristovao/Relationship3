package main;
public interface Constants {

		String nameConfigFile = "config.txt";
		
	    String addressBasic = "http://relationship/";
		String addressImage = "http://commons.wikimedia.org/wiki/File:";
		String markQueryReplacement = "#######";
		String originalConceptAddress = "http://dbpedia.org/resource/";
		String nameGraph = "Graph relationship";
		String nameGephiWorkspace = "workspace0";

		// ignore capital letter
		boolean ignoreCaseConcept = false;
		
		boolean edgeLabelStreamGephi = true;
		boolean nodeLabelStreamGephi = false; // original concepts always have label in Stream Gephi
		
		boolean edgeLabelFileGephi   = true;
		boolean nodeLabelFileGephi   = false; // original concepts always have label in Stream Gephi

		boolean directedStreamGraph = false; // must be FALSE because AStar class only work in undirected graph.
		// AStar is used to calculate paths between original concepts
		// but, if false do not accept edge that to go and to come in thd same nodes (case 9)
		
		boolean directedGephiGraph  = true;  // only accept true 
		
		// indicate value of concepts that do not belong to connected component (for example: original concepts)
		int withoutConnectedComponent = -1;
		
		int maxNodes = 50000;
		int minEdges = 50000;
		
		String doubleLine = "\n=============================================================================================\n";
		String singleLine = "\n---------------------------------------------------------------------------------------------\n";
		String starsLine  = "\n*********************************************************************************************\n";
		
		String characteres[][] = {
			{"á","&#xe1;"},
			{"é","&#xe9;"},
			{"í","&#xed;"},
			{"ó","&#xf3;"},
			{"ú","&#xfa;"},
			{"Á","&#xc1;"},
			{"É","&#xc9;"},
			{"Í","&#xcd;"},
			{"Ó","&#xd3;"},
			{"Ú","&#xda;"},
			
			{"â","&#xe2;"},
			{"ê","&#xea;"},
			{"î","&#xee;"},
			{"ô","&#xf4;"},
			{"û","&#xfb;"},
			{"Â","&#xc2;"},
			{"Ê","&#xca;"},
			{"Î","&#xce;"},
			{"Ô","&#xd4;"},
			{"Û","&#xdb;"},

			{"à","&#xe0;"},
			{"è","&#xe8;"},
			{"ì","&#xec;"},
			{"ò","&#xf2;"},
			{"ù","&#xf9;"},
			{"À","&#xc0;"},
			{"È","&#xc8;"},
			{"Ì","&#xcc;"},
			{"Ò","&#xd2;"},
			{"Ù","&#xd9;"},
			
			{"ä","&#xe4;"},
			{"ë","&#xeb;"},
			{"ï","&#xef;"},
			{"ö","&#xf6;"},
			{"ü","&#xfc;"},
			{"Ä","&#xc4;"},
			{"Ë","&#xcb;"},
			{"Ï","&#xcf;"},
			{"Ö","&#xd6;"},
			{"Ü","&#xdc;"},

			{"ã","&#xe3;"},
			{"õ","&#xf5;"},
			{"ñ","&#xf1;"},
			{"Ã","&#xc3;"},
			{"Õ","&#xd5;"},
			{"Ñ","&#xd1;"},
			
			{"ç","&#xe7;"},
			{"Ç","&#xc7;"},

			{"º","&#xba;"},
			{"ª","&#xaa;"}
		};
		
		
		

}
