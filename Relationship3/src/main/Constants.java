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
			{"�","&#xe1;"},
			{"�","&#xe9;"},
			{"�","&#xed;"},
			{"�","&#xf3;"},
			{"�","&#xfa;"},
			{"�","&#xc1;"},
			{"�","&#xc9;"},
			{"�","&#xcd;"},
			{"�","&#xd3;"},
			{"�","&#xda;"},
			
			{"�","&#xe2;"},
			{"�","&#xea;"},
			{"�","&#xee;"},
			{"�","&#xf4;"},
			{"�","&#xfb;"},
			{"�","&#xc2;"},
			{"�","&#xca;"},
			{"�","&#xce;"},
			{"�","&#xd4;"},
			{"�","&#xdb;"},

			{"�","&#xe0;"},
			{"�","&#xe8;"},
			{"�","&#xec;"},
			{"�","&#xf2;"},
			{"�","&#xf9;"},
			{"�","&#xc0;"},
			{"�","&#xc8;"},
			{"�","&#xcc;"},
			{"�","&#xd2;"},
			{"�","&#xd9;"},
			
			{"�","&#xe4;"},
			{"�","&#xeb;"},
			{"�","&#xef;"},
			{"�","&#xf6;"},
			{"�","&#xfc;"},
			{"�","&#xc4;"},
			{"�","&#xcb;"},
			{"�","&#xcf;"},
			{"�","&#xd6;"},
			{"�","&#xdc;"},

			{"�","&#xe3;"},
			{"�","&#xf5;"},
			{"�","&#xf1;"},
			{"�","&#xc3;"},
			{"�","&#xd5;"},
			{"�","&#xd1;"},
			
			{"�","&#xe7;"},
			{"�","&#xc7;"},

			{"�","&#xba;"},
			{"�","&#xaa;"}
		};
		
		
		

}
