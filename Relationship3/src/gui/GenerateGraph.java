package gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import edu.uci.ics.jung.graph.Graph;

public class GenerateGraph {

	public static void parseTxtIntoGraph(Graph<VertexType, EdgeType> graphFromTxtFile, String fileLocation) {
		
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileLocation);
			bufferedReader = new BufferedReader(fileReader);
			String sCurrentLine;
			String composedTermOrigin = "";
			String composedTermRelation = "";
			String composedTermTarget = "";
			boolean origin = false;
			boolean relation = false;
			boolean target = false;
			while ((sCurrentLine = bufferedReader.readLine()) != null) {
				System.out.println(sCurrentLine);
				Scanner scanTerm = new Scanner(sCurrentLine);
				// assumes the line has a certain structure: Term \t Relation \t
				// Target
				scanTerm.useDelimiter("\t");
				if (scanTerm.hasNext()) {
					composedTermOrigin = scanTerm.next();
					origin = true;
				}
				if (scanTerm.hasNext()) {
					composedTermRelation = scanTerm.next();
					relation = true;
				}
				if (scanTerm.hasNext()) {
					composedTermTarget = scanTerm.next();
					target = true;
				}
				if (relation == true) {
					EdgeType edge = new EdgeType(composedTermRelation);
					graphFromTxtFile.addEdge(edge, new VertexType(composedTermOrigin), new VertexType(composedTermTarget));
				} else if (origin == true) {
					graphFromTxtFile.addVertex(new VertexType(composedTermOrigin));
				}
				origin = false;
				relation = false;
				target = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}
}