package main;

import graph.*;
import rdf.*;
import user.*;
import map.*;
import myBase.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class WholeSystem {
	public static  ConfigTable configTable = new ConfigTable();
	private static UselessConceptsTable uselessConceptsTable = new UselessConceptsTable();
	private static RdfsFilesTable rdfsFilesTable = new RdfsFilesTable();
	private static MyKnowledgeBase myKnowledgeBase = new MyKnowledgeBase();
	private static StreamGraphData streamGraphData = new StreamGraphData();  // It manages the Gephi graph visualization, just in Time.  Only one to store all iterations.
	private static ConceptsGroup conceptsRegister = new ConceptsGroup();
	private static ConceptsGroup originalConcepts = null;  // will be filled in MainProcess.parseTerms()
	private static int quantityOriginalConcepts;  // it will be filled from method MainProcess.parseTerms()
	private static int quantityPathsBetweenOriginalConcetps; // it will be filled from method MainProcess.parseTerms()
	private static EdgesTableHash edgesTable = new EdgesTableHash(); 
	private static LinkedList<SetQuerySparql> listSetQuerySparql = new LinkedList<SetQuerySparql>();
	private static LinkedList<SystemGraphData> listSystemGraphData = new LinkedList<SystemGraphData>();;
	private static int goalConceptsQuantity; // it will be calculated by WholeSystem.initGoalmaxConceptsQualtity() from MainProcess.parseTerms()
	private static int maxConceptsQuantity;  // it will be calculated by WholeSystem.initGoalmaxConceptsQualtity() from MainProcess.parseTerms()
	private static NodesTableArray sortEccentricityAndAverageSelectedConcepts;  // it will be filled at algorithm final fase 
	private static NodesTableArray sortEccentricityAndAverageRemainingConcepts; // it will be filled at algorithm final fase 
	private static NodesTableArray finalHeadNodes; // it will be filled at MainProcess.buildFinalHeadNodesFromOriginalConceptsAndSelectedNodes
	private static VocabularyTable vocabularyTable = new VocabularyTable();
	private static ConceptMap conceptMap = new ConceptMap();
	
	public static void initQuantityOriginalConcepts(int quantity) {
		WholeSystem.quantityOriginalConcepts = quantity;
	}
	public static int getQuantityOriginalConcepts() {
		return WholeSystem.quantityOriginalConcepts;
	}
	public static int getQuantityPathsBetweenOriginalConcetps() {
		return WholeSystem.quantityPathsBetweenOriginalConcetps;
	}
	
	// goalConcepts = log2(1 / 2 * #original_concepts) + factor) + #original_concepts
	public static void initGoalMaxConceptsQuantity() {
		int originalConceptsQuantity = WholeSystem.getQuantityOriginalConcepts();
		WholeSystem.goalConceptsQuantity = (int)(
				                                 (Math.log(1.0/(double)originalConceptsQuantity) / Math.log(2)) + 4 
				                                 + WholeSystem.configTable.getDouble("conceptsQuantityCalculationFactor") + originalConceptsQuantity
				                                );
		WholeSystem.maxConceptsQuantity = WholeSystem.goalConceptsQuantity + WholeSystem.configTable.getInt("conceptsMinMaxRange");
	}
	public static void initQuantityPathsBetweenOriginalConcetps() {
		int maximumPaths=0;
		for(int i=1; i < WholeSystem.quantityOriginalConcepts; i++)
			maximumPaths += i;
		WholeSystem.quantityPathsBetweenOriginalConcetps = maximumPaths;
	}
	
	public static RdfsFilesTable getRdfsFileTable() {
		return WholeSystem.rdfsFilesTable;
	}
	 
	public static MyKnowledgeBase getMyKnowledgeBase() {
		return WholeSystem.myKnowledgeBase;
	}
	
	public static StreamGraphData getStreamGraphData() {
		return WholeSystem.streamGraphData;
	}
	public static ConceptsGroup getConceptsRegister() {
		return WholeSystem.conceptsRegister;
	}
	public static ConceptsGroup getOriginalConcepts() {
		return WholeSystem.originalConcepts;
	}
	public static void setOriginalConcepts(ConceptsGroup conceptsGroup) {
		WholeSystem.originalConcepts = conceptsGroup;
	}
	public static EdgesTableHash getEdgesTable() {
		return WholeSystem.edgesTable;
	}

	public static LinkedList<SetQuerySparql> getListSetQuerySparql() {
		return WholeSystem.listSetQuerySparql;
	}
	public static LinkedList<SystemGraphData> getListSystemGraphData() {
		return WholeSystem.listSystemGraphData;
	}
	public static NodesTableArray getFinalHeadNodes() {
		return WholeSystem.finalHeadNodes;
	}
	public static void setFinalHeadNodes(NodesTableArray table) {
		WholeSystem.finalHeadNodes = table;
	}
	public static ConceptMap getConceptMap() {
		return WholeSystem.conceptMap;
	}
	public static VocabularyTable getVocabularyTable() {
		return WholeSystem.vocabularyTable;
	}
	public static UselessConceptsTable getUselessConceptsTable() {
		return WholeSystem.uselessConceptsTable;
	}
	public static int getGoalConceptsQuantity() {
		return WholeSystem.goalConceptsQuantity; 
	}
	public static int getMaxConceptsQuantity() {
		return WholeSystem.maxConceptsQuantity; 
	}
	public static NodesTableArray getSortEccentricityAndAverageSelectedConcepts() {
		return WholeSystem.sortEccentricityAndAverageSelectedConcepts;
	}
	public static void setSortEccentricityAndAverageSelectedConcepts(NodesTableArray nodesTableArray) {
		WholeSystem.sortEccentricityAndAverageSelectedConcepts = nodesTableArray;
	}
	public static NodesTableArray getSortEccentricityAndAverageRemainingConcepts() {
		return WholeSystem.sortEccentricityAndAverageRemainingConcepts;
	}
	public static void setSortEccentricityAndAverageRemainingConcepts(NodesTableArray nodesTableArray) {
		WholeSystem.sortEccentricityAndAverageRemainingConcepts = nodesTableArray;
	}
	public static void insertListSetQuerySparql(SetQuerySparql setQuerySparql) {
		WholeSystem.listSetQuerySparql.add(setQuerySparql);
	}
	public static void insertListSystemGraphData(SystemGraphData systemGraphData) {
		WholeSystem.listSystemGraphData.add(systemGraphData);
	}
	
	public static void copyFile(String sourceFile, String targetFile) throws Exception {
		InputStream in = new FileInputStream(new File(sourceFile));
		OutputStream out = new FileOutputStream(new File(targetFile));           
		byte[] buffer = new byte[1024];
		int lenght;
		while ((lenght= in.read(buffer)) > 0) {
			out.write(buffer, 0, lenght);
		}
		in.close();
		out.close();
    }
	 
}
