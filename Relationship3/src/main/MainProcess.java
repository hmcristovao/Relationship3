// v7.4 - changed ampersand character. Working!
// (was verifyed that mapping case 9 do not work because stream graph must be undirected to AStar class work correctly.
// Case 09 = source and target node with links to go and to come)

package main;

import graph.NodeData;
import graph.QuantityNodesEdges;
import graph.StreamGraphData.DeletedStatus;
import graph.SystemGraphData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import map.Proposition;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.gephi.JSONSender;

import parser.*;
import rdf.SetQuerySparql;
import user.*;

// this class manages the main process and it does system logs
public class MainProcess {	
	private static ParserBasic  firstParser;
	private static ParserMyKnowledgeBase secondParser;
	private static int iteration, baseConnectedComponentCount, nodeDataPos;
	private static SetQuerySparql  currentSetQuerySparql;
	private static SystemGraphData currentSystemGraphData, oldSystemGraphData;
	private static NodeData nodeDataWithLeastEccentricityAndAverage;  // used for selectedConcepts(off) and remainigConcepts
	private static Node currentNode;
	private static List<Edge> currentEdgeSet;
	private static Concept currentConcept;
	
	private enum Time {t1_whileIteration, t2_afterIterationAndKcore, t3_afterHeadNodesPaths, t4_afterSteadyUniqueConnected, t5_finalGraph };
	
    public static void main(String args[])  throws Exception {

    	try {
			// ***** Initial Stage *****
			iteration = 0;
			start();
			parseTerms();
			parseUselessConcepts();
			parseVocabulary();
			readRdfsFileNameToRdfsFileTable();
			readMyKnowledgeBaseFile();

			// ***** Iterations Stage *****
			do {
				indicateIterationNumber();
				updateCurrentSetQuerySparqlVar();
				assemblyQueries();
				collectRDFsAllQueries();
				removeConceptsWithZeroRdfs();  // exit whether original concept has zero RDFs
				createCurrentSystemGraphData();
				connectStreamVisualization();
				buildStreamGraphData_buildEdgeTable_fromRdfs();
				showQuantitiesStreamGraph();
				if(iteration >= 1) 
					copyAllObjectsLastIteration();
				if(isApplyNDegreeFilterTrigger())
 				    applyNDegreeFilterTrigger(WholeSystem.configTable.getInt("nDegreeFilter"), false);
				buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();
				clearStreamGraphSink();
				classifyConnectedComponent();
				calculateRelationshipLevelBetweenOriginalConcepts();  // only whether connected component > 1
				buildGexfGraphFile(Time.t1_whileIteration);
				// conditionals set to continue the iteration
				if(breakIteration())
					break;
				calculateDistanceMeasuresWholeNetwork();
				storeDistanceMeasuresWholeNetworkToMainNodeTable();
				calculateEigenvectorMeasureWholeNetwork();
				storeEigenvectorMeasuresWholeNetworkToMainNodeTable();
				sortMeasuresWholeNetwork();
				buildSubGraphsRanks();
				buildSubGraphsTablesInConnectedComponents();
				sortConnectedComponentsRanks();
				selectLargestNodesByBetweennessCloseness();
				selectLargestNodesByEigenvector();
				reportSelectedNodesToNewIteration();
				if(WholeSystem.configTable.getBoolean("additionNewConceptWithoutCategory"))
					duplicateConceptsWithoutCategory(iteration);				
				prepareDataToNewIteration();
				iteration++;
			} while(true);
			
			// ***** Intermediate Stage 1 *****
			// (only entry in this stage if connected component = 1, or if maximum iteration achived)
			int lastIterationWithinOfLoopWithDistanceMeasuresCalculation = iteration-1;
			indicateAlgorithmIntermediateStage1(); // apply k-core
			if(isApplyKCoreFilterTrigger()) {
				applyKCoreFilterTrigger(WholeSystem.configTable.getInt("kCoreFilter"), false); 
				calculateRelationshipLevelBetweenOriginalConcepts_allCases();  // regardless of the value of connected component
				iteration++;
				createCurrentSystemGraphData();
				buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();
				classifyConnectedComponent();
				buildGexfGraphFile(Time.t2_afterIterationAndKcore);
			}
			
			// ***** Intermediate Stage 2 *****
			indicateAlgorithmIntermediateStage2(); // build finalHeadNodes to calculate paths to final stage
			buildFinalHeadNodesFromOriginalConceptsAndSelectedConcepts(lastIterationWithinOfLoopWithDistanceMeasuresCalculation);
			filterStreamGraphWithNodesAndEdgesBelongToShortestPathsOfFinalHeadNodes();
			iteration++;
			createCurrentSystemGraphData();
			buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();
			classifyConnectedComponent();
 			buildGexfGraphFile(Time.t3_afterHeadNodesPaths);
 			
			// ***** Intermediate Stage 3 *****
			indicateAlgorithmIntermediateStage3(); // remove nodes steadying connected component == 1
			iteration++;
			createCurrentSystemGraphData();
			buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();
			calculateDistanceMeasuresWholeNetwork();
			storeDistanceMeasuresWholeNetworkToMainNodeTable();
			calculateEigenvectorMeasureWholeNetwork();
			storeEigenvectorMeasuresWholeNetworkToMainNodeTable();
			classifyConnectedComponent();
 			createSortEccentricityAndAverageOnlyRemainingConcepts();
			baseConnectedComponentCount = currentSystemGraphData.getConnectedComponentsCount();
			nodeDataPos = 0;
			// loop while:
			// 1. quantity of selected concepts + original concepts > goal of total concepts, AND
			// 2. there are not concepts to try the remotion (because the quantity of componentes connected is improving)
			while(WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount() + WholeSystem.getQuantityOriginalConcepts() > WholeSystem.getGoalConceptsQuantity()
				  && nodeDataPos < WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount()) {
			
				getNodeDataWithLeastEccentricityAndAverageFromRemainingConcepts();
				
				// if node is link with original concepts then get the next
				// it is necessary that the node has only one edge to be not able to exclude
				// (try to fix a bug in Gephi Tool Kit - calculate with wrong the value of connected component)
				if(WholeSystem.configTable.getBoolean("isFixBugInGephiToolKit")) {
					if(isCurrentNodeHasLinkWithAnEspecificOriginalConcept()) { 
						nodeDataPos++;
						continue;
					}
				}
				
				if(WholeSystem.configTable.getBoolean("isKeepNeighborsOfOriginalConcepts")) {
					if(isCurrentNodeHasLinkWithOriginalConcepts()) {
						nodeDataPos++;
						continue;
					}
				}
				storeCurrentInformationsAboutEnvironmentAndNodeWillBeDeleted();
				// delete the node in the stream graph
				currentConcept = WholeSystem.getConceptsRegister().getConcept(currentNode.getId());
				DeletedStatus deletedStatus = WholeSystem.getStreamGraphData().deleteNode(currentNode, false);
				// create new environment (with a new gephi graph) and 
				// calculate the new connected component
				iteration++;
				createCurrentSystemGraphData();
				buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();				
				classifyConnectedComponent(); 
				// if connected component quantity improved then recover the deleted node, edges (to stream graph)
				if(currentSystemGraphData.getConnectedComponentsCount() > baseConnectedComponentCount) {
					recoverEnvironmentAndNodeAndEdges(deletedStatus);
					nodeDataPos++;
					iteration--;
				}
				// if ok then start at the first node again (the least average node)
				else {
					calculateDistanceMeasuresWholeNetwork();
					storeDistanceMeasuresWholeNetworkToMainNodeTable();
					calculateEigenvectorMeasureWholeNetwork();
					storeEigenvectorMeasuresWholeNetworkToMainNodeTable();
					nodeDataPos = 0;
					createSortEccentricityAndAverageOnlyRemainingConcepts(); 
				}
			} 
			// verify whether the goal was achieved: quantity of selected concepts + original concepts == goal of total concepts
			if(WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount() 
			   + WholeSystem.getQuantityOriginalConcepts() <= WholeSystem.getGoalConceptsQuantity())
				Log.consoleln("- Goal achieved!!!"); 
			else {
				if(WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount() + WholeSystem.getQuantityOriginalConcepts() 
						<= WholeSystem.getMaxConceptsQuantity())
					Log.consoleln("   (However, it is less than the maximum "+WholeSystem.getMaxConceptsQuantity()+" nodes)");
				else
					Log.consoleln("- Goal did not achieve.");
			}

			// reportAfterSelectionMainConcepts_selectedConcepts();
			reportAfterSelectionMainConcepts_remainingConcepts();

			
			// ***** Final Stage *****
            indicateAlgorithmFinalStage(); // building concept map
            // create the last GEXF file that represent the graph
            buildGexfGraphFile(Time.t4_afterSteadyUniqueConnected);
            showUselessConceptsStatistic();
           
            buildRawConceptMapFromStreamGraph();
			upgradeConceptMap_heuristic_01_removeLinkNumber();
			upgradeConceptMap_heuristic_02_vocabularyTable();
			upgradeConceptMap_heuristic_03_categoryInTargetConcept();
			upgradeConceptMap_heuristic_04_categoryInSourceConcept();
            upgradeConceptMap_heuristic_05_createOriginalConceptsWithZeroDegree();
            upgradeConceptMap_heuristic_06_joinEqualsConceptsWithoutAndWithCategory();
            upgradeConceptMap_heuristic_07_removeSelfReference();
            
			buildGexfGraphFileFromConceptMap();
			buildTxtFileFromConceptMap();
						
			upgradeConceptMap_heuristic_08_changeAmpersandCharacterInCxlFile();
			upgradeConceptMap_heuristic_09_putNewLineInCategory();
			upgradeConceptMap_heuristic_10_putNewLineInLongSentence();
			upgradeConceptMap_heuristic_11_setAccentedCharacterInCxlFile();
			buildCxlFileFromConceptMap();
			
			end();
		}
		
		// ***** Error treatment *****
		catch(FileNotFoundException e) {
			System.err.println("Error: file not found.");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("Error: problem with the persistent file: " + e.getMessage());
			e.printStackTrace();
		}
		catch(TokenMgrError e) {
			System.err.println("Lexical error: " + e.getMessage());
			e.printStackTrace();
		}
		catch(SemanticException e) {
			System.err.println("Semantic error: " + e.getMessage());
			e.printStackTrace();
		}
		catch(ParseException e) {
			System.err.println("Sintax error: " + e.getMessage());
			e.printStackTrace();
		}
		// get the another errs
		catch(Exception e) {
			System.err.println("Other error: " + e.getMessage());
			e.printStackTrace();
		}
		// if error then close all log files
		Log.close();
	}
	
	
	
	
	// ***** Static methods to macro support *****
	
	private static void start() throws Exception {
		parseConfiguration();
		buildDirectoryStrutureToOutputFiles();
		Log.initFiles();
		Log.consoleln("- Starting process.");
		showBuildDirectoryStructureToOutputFilesInformation();
		showParseConfigurationInformation();
	}
	
	private static void parseConfiguration() throws Exception {
		System.out.println("- Starting parse of configuration and log files.");
		MainProcess.firstParser = new ParserBasic(new FileInputStream(Constants.nameConfigFile));
		MainProcess.firstParser.parseConfigurations(WholeSystem.configTable);
	}
	private static void buildDirectoryStrutureToOutputFiles() throws Exception {
		String newDirectoryStr = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName");
		File newDirectoryFile = new File(newDirectoryStr);  
		newDirectoryFile.mkdir();
		// copy files of current configuration
		WholeSystem.copyFile(WholeSystem.configTable.getString("nameUserTermsFile"), 
				             newDirectoryStr+"\\"+WholeSystem.configTable.getString("nameUserTermsFile").replace(".txt", "_"+WholeSystem.configTable.getString("testName")+".txt"));
		WholeSystem.copyFile("config.txt", 
	                         newDirectoryStr+"\\config_"+WholeSystem.configTable.getString("testName")+".txt");
		WholeSystem.copyFile(WholeSystem.configTable.getString("nameUselessConceptsFile"), 
	                         newDirectoryStr+"\\uselessconcepts_"+WholeSystem.configTable.getString("testName")+".txt");
		WholeSystem.copyFile(WholeSystem.configTable.getString("nameQueryDefaultFile"), 
                             newDirectoryStr+"\\query_"+WholeSystem.configTable.getString("testName")+".txt");
	}
	private static void showBuildDirectoryStructureToOutputFilesInformation() throws Exception {
		String sameReport = "directory structure to output files in "+WholeSystem.configTable.getString("baseDirectory")+
	                        "\\" + WholeSystem.configTable.getString("testName");
		Log.consoleln("- Building "+sameReport);
		Log.outFileCompleteReport("Built "+sameReport);
		Log.outFileShortReport("Built "+sameReport);
	}
	private static void showParseConfigurationInformation() throws Exception {
		String sameReport = "Quantity of configuration parsed: " + WholeSystem.configTable.size() + 
				            " (file: "+Constants.nameConfigFile+")"; 
		Log.consoleln("- Parsing configuration file "+Constants.nameConfigFile+" - "+WholeSystem.configTable.size()+" itens.");
		Log.outFileCompleteReport(sameReport + "\n\n"+ WholeSystem.configTable.toString());
		Log.outFileShortReport(sameReport + "\n\n"+ WholeSystem.configTable.toString());
	}
	private static void parseTerms() throws Exception {
		Log.console("- Parsing user terms");
		MainProcess.firstParser = new ParserBasic(new FileInputStream(WholeSystem.configTable.getString("nameUserTermsFile")));
		WholeSystem.insertListSetQuerySparql(new SetQuerySparql());
		MainProcess.firstParser.parseUserTerms(WholeSystem.getListSetQuerySparql().getFirst());
		WholeSystem.initQuantityOriginalConcepts(WholeSystem.getConceptsRegister().size());
		WholeSystem.initGoalMaxConceptsQuantity();
		WholeSystem.setOriginalConcepts(WholeSystem.getConceptsRegister().getOriginalConcepts());
		WholeSystem.initQuantityPathsBetweenOriginalConcetps();
		Log.consoleln(" - " + WholeSystem.getQuantityOriginalConcepts() + " terms parsed.");
		String sameReport = "Quantity of terms parsed: " + WholeSystem.getQuantityOriginalConcepts() + 
				            " (file: "+WholeSystem.configTable.getString("nameUserTermsFile")+")\n"; 
		Log.outFileCompleteReport(sameReport + WholeSystem.getOriginalConcepts().toStringLong());
		Log.outFileShortReport(sameReport + WholeSystem.getOriginalConcepts().toString());
	}
	private static void parseUselessConcepts() throws Exception {
		if(WholeSystem.configTable.getBoolean("isEnableUselessTable")) {
			Log.console("- Parsing useless concepts");
			MainProcess.firstParser = new ParserBasic(new FileInputStream(WholeSystem.configTable.getString("nameUselessConceptsFile")));
			MainProcess.firstParser.parseUselessConcepts();
			Log.consoleln(" - " + WholeSystem.getUselessConceptsTable().size() + " concepts parsed.");
			String sameReport = "Quantity of useless concepts parsed: " + WholeSystem.getUselessConceptsTable().size() +   
					" (file: "+WholeSystem.configTable.getString("nameUselessConceptsFile")+")\n" +
					"\nUseless concepts parsed:\n" + WholeSystem.getUselessConceptsTable().toString();
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);
		}
	}
	private static void parseVocabulary() throws Exception {
		Log.console("- Parsing vocabulary");
		MainProcess.firstParser = new ParserBasic(new FileInputStream(WholeSystem.configTable.getString("nameVocabularyFile")));
		MainProcess.firstParser.parseSystemVocabulary(WholeSystem.getVocabularyTable());
		Log.consoleln(" - " + WholeSystem.getVocabularyTable().size() + " sentences parsed.");
		String sameReport = "Quantity of vocabulary sentences parsed: " + WholeSystem.getVocabularyTable().size() +   
                " (file: "+WholeSystem.configTable.getString("nameVocabularyFile")+")\n" +
				"\nVocabulary table parsed:\n" + WholeSystem.getVocabularyTable().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void readRdfsFileNameToRdfsFileTable() throws Exception {
		Log.console("- Reading RDFs files names to put in table");
		WholeSystem.getRdfsFileTable().init(WholeSystem.configTable.getString("dirRdfsPersistenceFiles"));  
 		Log.consoleln(" - " + WholeSystem.getRdfsFileTable().size() + " RDFs files identified.");
		String sameReport = "RDFs files identified: "+WholeSystem.getRdfsFileTable().size()+".";
		Log.outFileCompleteReport(sameReport+"\n\n"+WholeSystem.getRdfsFileTable());
		Log.outFileShortReport(sameReport);			
	}
	
	private static void readMyKnowledgeBaseFile() throws Exception {
		Log.console("- Reading my knowledge base file");
		MainProcess.secondParser = new ParserMyKnowledgeBase(new FileInputStream(WholeSystem.configTable.getString("nameMyKnowledgeBaseFile")));
		MainProcess.secondParser.parseMyKnowledgeBase();
		Log.consoleln(" - " + WholeSystem.getMyKnowledgeBase().size() + " nodes parsed.");
		String sameReport = "Quantity of nodes of the my knowledge base parsed: " + WholeSystem.getMyKnowledgeBase().size() +   
				" (file: "+WholeSystem.configTable.getString("nameMyKnowledgeBaseFile")+")\n";
		Log.outFileCompleteReport(sameReport + "\nNodes and links parsed:\n\n" + WholeSystem.getMyKnowledgeBase().toString());
		Log.outFileShortReport(sameReport);
	}

	private static void indicateIterationNumber() throws Exception {
		Log.consoleln("\n*** Iteration "+iteration+" ***");
		String sameReport = Constants.starsLine+"Iteration "+iteration+Constants.starsLine;
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void indicateAlgorithmIntermediateStage1() throws Exception {
		Log.consoleln("\n*** Intermediate stage 1 (apply k-core) ***");
		String sameReport = Constants.starsLine+"Intermediate stage 1 (apply k-core)"+Constants.starsLine;
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void indicateAlgorithmIntermediateStage2() throws Exception {
		String sameReport = "Current concepts quantity: "+
		        WholeSystem.getStreamGraphData().getRealTotalNodes()+" stream graph  ("+
		        WholeSystem.getQuantityOriginalConcepts() + " original concepts).  "+
                "Goal of concepts quantity: "+
		        WholeSystem.getGoalConceptsQuantity()+ " good, "+
                WholeSystem.getMaxConceptsQuantity()+" maximum.";
		Log.consoleln("\n*** Intermediate stage 2 (build head nodes list to calculate paths in the final stage) ***\n- " + sameReport);
		String sameReport2 = Constants.starsLine+"Intermediate stage 2 (build head nodes list to calculate paths in the final stage)"+Constants.starsLine;
		Log.outFileCompleteReport(sameReport2+sameReport);
		Log.outFileShortReport(sameReport2+sameReport);
	}
	private static void indicateAlgorithmIntermediateStage3() throws Exception {
		String sameReport = "Current concepts quantity: "+
		        WholeSystem.getStreamGraphData().getRealTotalNodes()+" stream graph  ("+
		        WholeSystem.getQuantityOriginalConcepts() + " original concepts).  "+
                "Goal of concepts quantity: "+
		        WholeSystem.getGoalConceptsQuantity()+ " good, "+
                WholeSystem.getMaxConceptsQuantity()+" maximum.";
		Log.consoleln("\n*** Intermediate stage 3 (remove nodes steadying unique connected component ***\n- "+ sameReport);
		String sameReport2 = Constants.starsLine+"Intermediate stage 3 (remove nodes steadying unique connected component)"+Constants.starsLine;
		Log.outFileCompleteReport(sameReport2+sameReport);
		Log.outFileShortReport(sameReport2+sameReport);
	}
	
	private static void indicateAlgorithmFinalStage() throws Exception {
		Log.consoleln("\n*** Final stage (building concept map) ***");
		String sameReport = Constants.starsLine+"Final stage (building concept map)"+Constants.starsLine;
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void updateCurrentSetQuerySparqlVar() throws Exception {
		currentSetQuerySparql = WholeSystem.getListSetQuerySparql().get(iteration);
	}
	private static void assemblyQueries() throws Exception {
		Log.console("- Assembling queries");
		int num = currentSetQuerySparql.assemblyQueries();
		Log.consoleln(" - "+num+" new queries assembled.");
		String sameReport = "Queries assembled: " + num + "\n";
        Log.outFileCompleteReport(sameReport + currentSetQuerySparql.toString());
		Log.outFileShortReport(sameReport + currentSetQuerySparql.toStringShort());
	}
	private static void collectRDFsAllQueries() throws Exception {
		Log.console("- Collecting RDFs");
		Count numRdfsInInternet = new Count(0);
		Count numRdfsInFile     = new Count(0);
		int num =  currentSetQuerySparql.collectRDFsAllQueries(numRdfsInInternet, numRdfsInFile);
		Log.consoleln(" - "+num+" new RDFs triples collected ("+numRdfsInInternet+" in internet, "+numRdfsInFile+" in file).");
		// extract collected quantity of RDFs to each concept
		StringBuilder conceptsOut = new StringBuilder();
		DecimalFormat formater =  new DecimalFormat("00000");
		for(int i=0; i < currentSetQuerySparql.getTotalConcepts(); i++) {
			conceptsOut.append("\n");
			conceptsOut.append(formater.format(currentSetQuerySparql.getListQuerySparql().get(i).getListRDF().size()));
			conceptsOut.append(" RDFs to concept \"");
			conceptsOut.append(currentSetQuerySparql.getListQuerySparql().get(i).getConcept().getBlankName());
			conceptsOut.append("\"");
		}
		String sameReport = "Total collected RDFs: " + num + " ("+numRdfsInInternet+" in internet, "+numRdfsInFile+" in file)\n" + conceptsOut.toString();
        Log.outFileCompleteReport(sameReport + "\n\n" + currentSetQuerySparql.toString());
		Log.outFileShortReport(sameReport);
	}
	private static void removeConceptsWithZeroRdfs() throws Exception {
		Log.console("- Looking for concepts with zero RDFs");
		ConceptsGroup excludedConcepts = currentSetQuerySparql.removeConceptsWithZeroRdfs();
		if(excludedConcepts.size() == 0)
			Log.consoleln(" - neither concept found.");
		else
			Log.consoleln(" - " + excludedConcepts.size() + " concepts found and excluded.");
		String sameReport = "Concepts with zero RDFs: ";
		if(excludedConcepts.size() == 0) 
			sameReport += "neither";
		else
			sameReport += "\n" + excludedConcepts.toString();
	    Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
		// verify whether there is any original concept with zero RDFs
		if(excludedConcepts.getQuantityOriginalConcept() > 0) {
			String sameReport2 = "*** Algorithm stoped - there is original concept with zero RDFs\n";
			sameReport2 += excludedConcepts.getOriginalConcepts().toString();
			Log.consoleln(sameReport2);
			Log.outFileCompleteReport(sameReport2);
			Log.outFileShortReport(sameReport2);
			end();
			System.exit(1);
		}
	}	
	private static void createCurrentSystemGraphData() throws Exception {
		WholeSystem.insertListSystemGraphData(new SystemGraphData());
		currentSystemGraphData = WholeSystem.getListSystemGraphData().get(iteration);
	}
	private static void connectStreamVisualization() throws Exception {
		if(WholeSystem.configTable.getBoolean("graphStreamVisualization")) {
			Log.consoleln("- Connecting Stream Visualization.");
			WholeSystem.getStreamGraphData().getStreamGraph().display(true);
		}
		if(WholeSystem.configTable.getBoolean("gephiVisualization")) {
			Log.consoleln("- Connecting with Gephi.");
			JSONSender sender = new JSONSender("localhost", 8080, Constants.nameGephiWorkspace);
			WholeSystem.getStreamGraphData().getStreamGraph().addSink(sender);
		}
	}
	// get RDFs and convert them to StreamGraph, but this fucntion is call: 
	//    in the firt iteration build StreamGraphData and EdgeTable
	//    in the second iteration so foth, just add new data into StreamGraphData and EdgeTable
	// discard useless concepts (use WholeSystem.uselessConceptsTable to do this operation)
	private static void buildStreamGraphData_buildEdgeTable_fromRdfs() throws Exception {
		Log.console("- Building Stream Graph Data");
		Count countUselessRDFs = new Count(0);
		QuantityNodesEdges quantityNodesEdges = WholeSystem.getStreamGraphData().buildStreamGraphData_buildEdgeTable_fromRdfs(currentSetQuerySparql, countUselessRDFs);
		Log.consoleln(" - "+quantityNodesEdges.getNumNodes()+" new nodes, "+quantityNodesEdges.getNumEdges()+" new edges in Stream Graph - " +
				countUselessRDFs + " useless RDFs.");
		Log.consoleln("- Creating edge hash table - "+WholeSystem.getEdgesTable().size()+" edges.");
		String sameReport = "Stream Graph Data created (graph used in the preview): \n" + 
		        quantityNodesEdges.getNumNodes() + " new nodes, " + 
				quantityNodesEdges.getNumEdges() + " new edges in the visualization graph.\n" +
		        WholeSystem.getStreamGraphData().getRealTotalNodes() + " total nodes, " +
		        WholeSystem.getStreamGraphData().getRealTotalEdges() + " total edges\n" +
		        countUselessRDFs + " useless RDFs.";
		String sameReport2 = "\n\nEdge hash table created:" + 
				"\n("+WholeSystem.getEdgesTable().size()+" edges).";
        Log.outFileCompleteReport(sameReport + WholeSystem.getStreamGraphData().toString() + sameReport2 + "\n"+WholeSystem.getEdgesTable().toString());
		Log.outFileShortReport(sameReport + WholeSystem.getStreamGraphData().toStringShort() + sameReport2);
	}
	private static void showQuantitiesStreamGraph() throws Exception {
		Log.consoleln("- Quantities Stream Graph built: "+WholeSystem.getStreamGraphData().getRealTotalNodes()+
				" nodes, "+WholeSystem.getStreamGraphData().getRealTotalEdges()+" edges.");
	}
	// apply Ndegree filter if:
	//       achieved # iteration
	//  and  achieved # nodes
	//  and  ( connected component == 1  OR isUniqueConnectedComponetToApplyNdegreeFilter = false) 
	private static boolean isApplyNDegreeFilterTrigger() throws Exception {
		Log.console("- Verifying whether apply N-degree filter: ");
		String sameReport = "iteration ("+iteration+": >= "+WholeSystem.configTable.getInt("iterationTriggerApplyNDegreeFilterAlgorithm")+"), " 
		                   +"nodes count ("+WholeSystem.getStreamGraphData().getRealTotalNodes()+": > "+WholeSystem.configTable.getInt("quantityNodesToApplyNdegreeFilter")+")";
		if(WholeSystem.configTable.getBoolean("isUniqueConnectedComponentToApplyNdegreeFilter")) {
			sameReport +=  ", connected component ("
		                   + (iteration==0 ? "?" : WholeSystem.getListSystemGraphData().get(iteration-1).getConnectedComponentsCount())
		                   +": = 1)"; 
		}
		if(iteration >= WholeSystem.configTable.getInt("iterationTriggerApplyNDegreeFilterAlgorithm") 
		   && WholeSystem.getStreamGraphData().getRealTotalNodes() > WholeSystem.configTable.getInt("quantityNodesToApplyNdegreeFilter")
		   && 
		      ( WholeSystem.getListSystemGraphData().get(iteration-1).getConnectedComponentsCount() == 1
		       || !WholeSystem.configTable.getBoolean("isUniqueConnectedComponentToApplyNdegreeFilter")
		      ) ) { 
			Log.consoleln(sameReport+" - OK!");
	        Log.outFileCompleteReport("Apply N-degree filter "+sameReport);
			Log.outFileShortReport("Apply N-degree filter "+sameReport);		
			return true;
		}
		else {
			Log.consoleln(sameReport+" - not ok.");
	        Log.outFileCompleteReport("Do not apply N-degree filter "+sameReport);
			Log.outFileShortReport("Do not apply N-degree filter "+sameReport);		
			return false;
		}
	}
	private static boolean isApplyKCoreFilterTrigger() throws Exception {
		Log.console("- Verifying whether apply K-core filter: ");
		String sameReport = "quantity of nodes ("+WholeSystem.getStreamGraphData().getRealTotalNodes()+": > "+WholeSystem.configTable.getInt("quantityNodesToApplyKcoreFilter")+")"; 
		if(WholeSystem.getStreamGraphData().getRealTotalNodes() > WholeSystem.configTable.getInt("quantityNodesToApplyKcoreFilter")) { 
			Log.consoleln(sameReport+" - OK!");
	        Log.outFileCompleteReport("Apply K-core filter "+sameReport);
			Log.outFileShortReport("Apply K-core filter "+sameReport);		
			return true;
		}
		else {
			Log.consoleln(sameReport+" - not ok.");
	        Log.outFileCompleteReport("Do not apply K-core filter "+sameReport);
			Log.outFileShortReport("Do not apply K-core filter "+sameReport);		
			return false;
		}
	}
	private static boolean isCurrentNodeHasLinkWithOriginalConcepts() throws Exception {
		Node node = nodeDataWithLeastEccentricityAndAverage.getStreamNode();
		Log.console("- Verifying whether "+node.getId()+" has link with original concepts: ");
		String sameReport;
		if(WholeSystem.getStreamGraphData().isNodeHasLinkWithOriginalConcepts(node)) { 
			Log.consoleln(" - yes, it has. Then it will not be removed.");
			sameReport = "Node "+node.getId()+" has link with original concepts. It was not removed.";
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);		
			return true;
		}
		else {
			Log.consoleln(" - no, it hasn't. Then it will be able to remotion.");
			sameReport = "Node "+node.getId()+" has not link with original concepts. It will be able to remotion.";
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);		
			return false;
		}
	}
	private static boolean isCurrentNodeHasLinkWithAnEspecificOriginalConcept() throws Exception {
		String concept0 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug0");
		String concept1 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug1");
		String concept2 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug2");
		String concept3 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug3");
		String concept4 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug4");
		String concept5 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug5");
		String concept6 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug6");
		String concept7 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug7");
		String concept8 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug8");
		String concept9 = WholeSystem.configTable.getString("originalConceptWithGephiToolKitBug9");
		Node node = nodeDataWithLeastEccentricityAndAverage.getStreamNode();
		Log.console("- Verifying whether "+node.getId()+" has link with "+concept0+", "+concept1+", "+concept2+", "+concept3+", "+concept4+", "+concept5+", "+concept6+", "+concept7+", "+concept8+", "+concept9+": ");
		String sameReport;
		if(WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept0) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept1) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept2) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept3) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept4) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept5) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept6) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept7) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept8) ||
		   WholeSystem.getStreamGraphData().isNodeHasLinkWithAnEspecificOriginalConcept(node, concept9)) { 
			Log.consoleln(" - yes, it has. Then it will not be removed.");
			sameReport = "Node "+node.getId()+" has link with "+concept0+" or "+concept1+" or "+concept2+" or "+concept3+" or "+concept4+" or "+concept5+" or "+concept6+" or "+concept7+" or "+concept8+" or "+concept9+". It was not removed.";
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);		
			return true;
		}
		else {
			Log.consoleln(" - no, it hasn't. Then it will be able to remotion.");
			sameReport = "Node "+node.getId()+" has not link with "+concept0+", "+concept1+", "+concept2+", "+concept3+", "+concept4+", "+concept5+", "+concept6+", "+concept7+", "+concept8+", "+concept9+". It will be able to remotion.";
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);		
			return false;
		}
	}
	// if it is second iteration so forth, copy all objects (ListQuerySparql) of the last iteration
	private static void copyAllObjectsLastIteration() throws Exception {
		Log.console("- Second iteration or more: copying old elements of the last iteration");
		int n = currentSetQuerySparql.insertListQuerySparql(WholeSystem.getListSetQuerySparql().get(iteration-1).getListQuerySparql());
		Log.consoleln(" - "+n+" elements copied.");
		String sameReport = "Second iteration or more: "+n+" old elements copied from last iteration.\n";
        Log.outFileCompleteReport(sameReport + WholeSystem.getListSetQuerySparql().get(iteration-1).toString());
		Log.outFileShortReport(sameReport + WholeSystem.getListSetQuerySparql().get(iteration-1).toStringShort());
	}
	// isForcedReationship determines that the node will only remove if the level of relationship between original concepts do not change
	private static void applyNDegreeFilterTrigger(int n, boolean isForcedRelationship) throws Exception {
		Log.console("- Starting "+n+"-degree filter algorithm "+
				(isForcedRelationship ? "(" : "(non ") + "forced relationship between original concepts)");
		Count quantityDeletedSelectedConcepts = new Count(0);
		Count quantityRecoveredNodes = new Count(0);
		int numOldNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numOldEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		// call algorithm:
		WholeSystem.getStreamGraphData().applyNdegreeFilterTrigger(n, isForcedRelationship, quantityDeletedSelectedConcepts, quantityRecoveredNodes);
		int numCurrentNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numCurrentEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		Log.console(" - "+ quantityRecoveredNodes + " recovered nodes");
		Log.console(" - "+ (numOldNodes - numCurrentNodes) +" deleted nodes");
		Log.console(" ("+ quantityDeletedSelectedConcepts +" selected concepts)");
		Log.consoleln(" and "+ (numOldEdges - numCurrentEdges) +" deleted edges.");
		Log.consoleln("- Remained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.");
		String sameReport = "Runned "+n+"-degree filter algorithm "+
				(isForcedRelationship ? "(" : "(non ") + "forced relationship between original concepts)" +
				quantityRecoveredNodes + " recovered nodes - " +
				(numOldNodes - numCurrentNodes) +" deleted nodes" +
				"("+ quantityDeletedSelectedConcepts +" selected concepts)" + 
				" and "+ (numOldEdges - numCurrentEdges) +" deleted edges" +
				"\nOld Stream Graph: "+numOldNodes+" nodes, "+numOldEdges+" edges." +
				"\nRemained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.";
        Log.outFileCompleteReport(sameReport + "\n\n" + WholeSystem.getStreamGraphData().toString() );
		Log.outFileShortReport(sameReport);
	}			
	private static void applyKCoreFilterTrigger(int k, boolean isForcedRelationship) throws Exception {
		Log.console("- Starting "+ k +"-core filter algorithm ");
		Count quantityDeletedSelectedConcepts = new Count(0);	
		Count quantityRecoveredNodes = new Count(0);
		int numOldNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numOldEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		// call algorithm:
		WholeSystem.getStreamGraphData().applyKCoreFilterTrigger(k, isForcedRelationship, quantityDeletedSelectedConcepts, quantityRecoveredNodes);
		int numCurrentNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numCurrentEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		Log.console(" - "+ quantityRecoveredNodes + " recovered nodes");
		Log.console(" - "+ (numOldNodes - numCurrentNodes) +" deleted nodes");
		Log.console(" ("+ quantityDeletedSelectedConcepts +" selected concepts)");
		Log.consoleln(" and "+ (numOldEdges - numCurrentEdges) +" deleted edges");
		Log.consoleln("- Remained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.");
		String sameReport = "Runned " + k + "-core filter algorithm\n" +
				(isForcedRelationship ? "(" : "(non ") + "forced relationship between original concepts)" +
				quantityRecoveredNodes + " recovered nodes - " +
				(numOldNodes - numCurrentNodes) +" deleted nodes" +
				"("+ quantityDeletedSelectedConcepts +" selected concepts)" + 
				" and "+ (numOldEdges - numCurrentEdges) +" deleted edges" +
				"\nOld Stream Graph: "+numOldNodes+" nodes, "+numOldEdges+" edges." +
				"\nRemained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.";
        Log.outFileCompleteReport(sameReport + "\n\n" + WholeSystem.getStreamGraphData().toString() );
		Log.outFileShortReport(sameReport);
	}
			
	private static void buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph() throws Exception {
		Log.console("- Building Gephi Graph Data, Nodes Table Hash and Nodes Table Array from Stream Graph");
		// call function:
		QuantityNodesEdges quantityNodesEdges = currentSystemGraphData.buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph();
		Log.consoleln(" - "+quantityNodesEdges.getNumNodes()+" nodes, "+quantityNodesEdges.getNumEdges()+" edges in the graph structure.");
		String sameReport = "Built Gephi Graph Data, Nodes Table Hash and Nodes Table Array from Stream Graph\n"+
				quantityNodesEdges.getNumNodes()+" nodes, "+quantityNodesEdges.getNumEdges()+" edges in the graph structure." +
				"\nReal quantities: "+currentSystemGraphData.getGephiGraphData().getRealQuantityNodesEdges().toString();
		Log.outFileCompleteReport(sameReport + "\n" + currentSystemGraphData.getGephiGraphData().toString());
		Log.outFileShortReport(sameReport);
	}
	private static void clearStreamGraphSink() throws Exception {
		if(WholeSystem.configTable.getBoolean("gephiVisualization"))  
			WholeSystem.getStreamGraphData().getStreamGraph().clearSinks();
	}
	private static void calculateDistanceMeasuresWholeNetwork() throws Exception {
		Log.console("- Calculating distance measures of the whole network");
		currentSystemGraphData.getGephiGraphData().calculateGephiGraphDistanceMeasures();
		Log.consoleln(" - "+currentSystemGraphData.getGephiGraphData().getRealQuantityNodesEdges().toString() + ".");
		String sameReport = "Distance measures of the whole network calculated." + 
				"\n(betweenness and closeness to "+currentSystemGraphData.getGephiGraphData().getRealQuantityNodesEdges().toString()+")";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void calculateEigenvectorMeasureWholeNetwork() throws Exception {
		Log.console("- Calculating eigenvector measure of the whole network");
		currentSystemGraphData.getGephiGraphData().calculateGephiGraphEigenvectorMeasure();
		Log.consoleln(" - "+currentSystemGraphData.getGephiGraphData().getRealQuantityNodesEdges().toString() + ".");
		String sameReport = "Eigenvector measure of the whole network calculated." + 
				"\n(eigenvector to "+currentSystemGraphData.getGephiGraphData().getRealQuantityNodesEdges().toString()+")";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void storeDistanceMeasuresWholeNetworkToMainNodeTable() throws Exception {
		Log.consoleln("- Storing distance measures of the whole network to main node table.");
		currentSystemGraphData.storeDistanceMeasuresWholeNetwork();
		String sameReport = "Stored distance measures of the whole network to main node table.";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	} 
	private static void storeEigenvectorMeasuresWholeNetworkToMainNodeTable() throws Exception {
		Log.consoleln("- Storing eigenvector measures of the whole network to main node table.");
		currentSystemGraphData.storeEigenvectorMeasuresWholeNetwork();
		String sameReport = "Stored eigenvector measures of the whole network to main node table.";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	} 
	private static void sortMeasuresWholeNetwork() throws Exception {
		Log.consoleln("- Sorting measures of the whole network.");
		currentSystemGraphData.sortBetweennessWholeNetwork();
		currentSystemGraphData.sortClosenessWholeNetwork();
		currentSystemGraphData.sortEccentricityWholeNetwork();
		currentSystemGraphData.sortEigenvectorWholeNetwork();
		String sameReport = "Sorted measures of the whole network.";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void calculateRelationshipLevelBetweenOriginalConcepts() throws Exception {
		Log.console("- Calculating level of relationship between original concepts - ");
		int num = 0;
		if(currentSystemGraphData.getConnectedComponentsCount() > 1)
			num = WholeSystem.getStreamGraphData().calculateRelationshipLevelBetweenOriginalConcepts();
		WholeSystem.getStreamGraphData().setCurrentLevelRelationshipBetweenOriginalConcepts(num);
		int maximumLevel = WholeSystem.getQuantityPathsBetweenOriginalConcetps();
		String sameReport = "level: " + num + "/" + maximumLevel + " ("+ (int)(((double)(maximumLevel-num)/maximumLevel)*100)+"% complete).";
		Log.consoleln(sameReport);
		String sameReport2 = "Calculated level of relationship between original concepts\n";
        Log.outFileCompleteReport(sameReport+sameReport2);
		Log.outFileShortReport(sameReport+sameReport2);
	}	

	// regardless of the value of connected component
	private static void calculateRelationshipLevelBetweenOriginalConcepts_allCases() throws Exception {
		Log.console("- Calculating level of relationship between original concepts - ");
		int num = WholeSystem.getStreamGraphData().calculateRelationshipLevelBetweenOriginalConcepts();
		int maximumLevel = WholeSystem.getQuantityPathsBetweenOriginalConcetps();
		String sameReport = "level: " + num + "/" + maximumLevel + " ("+ (int)(((double)(maximumLevel-num)/maximumLevel)*100)+"% complete).";
		Log.consoleln(sameReport);
		String sameReport2 = "Calculated level of relationship between original concepts\n";
        Log.outFileCompleteReport(sameReport+sameReport2);
		Log.outFileShortReport(sameReport+sameReport2);
	}	
	
	// work with current gephi graph (wherefore is better before to use: buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph())
	private static void classifyConnectedComponent() throws Exception {
		Log.console("- Classifying connected component");
		int num = currentSystemGraphData.getGephiGraphData().classifyConnectedComponent();
		currentSystemGraphData.setConnectedComponentsCount(num);
		Log.consoleln(" - quantity of connected components: " + num + ".");
		String sameReport = "Connected component classified\n" + 
				num + " connected components.";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void buildSubGraphsRanks() throws Exception {
		Log.consoleln("- Building sub-graphs ranks.");
		currentSystemGraphData.buildSubGraphRanks();
		String sameReport = "Sub-graphs ranks built.";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void buildGexfGraphFile(Time time) throws Exception {
		String nameFileGexf = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName")+"\\"+
				              WholeSystem.configTable.getString("nameGexfGraphFile");
		if(time == Time.t1_whileIteration) 
			nameFileGexf = nameFileGexf.replace(".gexf", "_t1_iteration" + (iteration<=9?"0"+iteration:iteration) + ".gexf");
		else if(time == Time.t2_afterIterationAndKcore)
	   		nameFileGexf = nameFileGexf.replace(".gexf", "_t2_after_iterations_and_kcore.gexf");
		else if(time == Time.t3_afterHeadNodesPaths)
	   		nameFileGexf = nameFileGexf.replace(".gexf", "_t3_after_head_nodes_path.gexf");
		else if(time == Time.t4_afterSteadyUniqueConnected)
			nameFileGexf = nameFileGexf.replace(".gexf", "_t4_after_steady_unique_connected.gexf");	
		Log.console("- Building GEXF Graph File");
		currentSystemGraphData.getGephiGraphData().buildGexfGraphFile(nameFileGexf);
		Log.consoleln(" (generated file: " + nameFileGexf + ").");
		String sameReport = "GEXF graph file generated: " + nameFileGexf;
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void buildSubGraphsTablesInConnectedComponents() throws Exception {
		Log.consoleln("- Building sub-graphs tables belong to connected components.");
		currentSystemGraphData.buildSubGraphsTablesInConnectedComponents();
		String sameReport = "Sub-graphs tables belong to connected components built.";
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void sortConnectedComponentsRanks() throws Exception {
		Log.consoleln("- Sorting connected components ranks.");
		currentSystemGraphData.sortConnectecComponentRanks();
		String sameReport = "Connected components ranks sorted.";
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void selectLargestNodesByBetweennessCloseness() throws Exception {
		Log.console("- Selecting largest nodes by betweenness+closeness");
		int num = currentSystemGraphData.selectLargestNodesBetweennessCloseness(iteration);
		Log.consoleln(" - "+num+" new selected concepts.");
		String sameReport = "Largest nodes by betweenness+closeness: " + num + " nodes.";
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void selectLargestNodesByEigenvector() throws Exception {
		Log.console("- Selecting largest nodes by eigenvector");
		int num = currentSystemGraphData.selectLargestNodesEigenvector(iteration);
		Log.consoleln(" - "+num+" new selected concepts.");
		String sameReport = "Largest nodes by eigenvector: " + num + " nodes."; 
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void reportSelectedNodesToNewIteration() throws Exception {
		Log.consoleln("- Reporting selected nodes to new iteration.");
		Log.outFileCompleteReport("Current System Graph Data:\n" + currentSystemGraphData.toString());
		if(WholeSystem.configTable.getInt("quantityNodesShortReport") > 0)
			Log.outFileShortReport("Current System Graph Data:\n" + currentSystemGraphData.toStringShort(WholeSystem.configTable.getInt("quantityNodesShortReport")));
		String sameReport = "Final report of iteration "+iteration+"\n"+currentSystemGraphData.reportSelectedNodes(iteration);			
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void duplicateConceptsWithoutCategory(int iteration) throws Exception {
		Log.console("- Duplicating concepts with \"Category:\" subword");
		ConceptsGroup newConcepts = WholeSystem.getConceptsRegister().duplicateConceptsWithoutCategory(iteration);
		Log.consoleln(" - "+newConcepts.size()+" new concepts inserted.");
		String sameReport = "Duplicated "+newConcepts.size()+" concepts with \"Category:\" subword:\n"
				            + newConcepts.toString(); 
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}	
	private static boolean breakIteration() throws Exception {
		Log.console("- Verifying exit conditions: ");
		String sameReport = "maximum iteration ("+iteration+": = "+WholeSystem.configTable.getInt("maxIteration")+") \n  or " 
		                   +"[ minimum iteration ("+iteration+": >= "+WholeSystem.configTable.getInt("minIterationToVerifyUniqueConnectedComponent")+") and "
		                   +"connected component ("+currentSystemGraphData.getConnectedComponentsCount()+": = 1) ]\n  or "
		                   +"[ minimum iteration ("+iteration+": >= "+WholeSystem.configTable.getInt("minIterationToVerifyRelationshipBetweenOriginalConcepts")+") and "
		                   +"relationship level between original concepts ("+WholeSystem.getStreamGraphData().getCurrentLevelRelationshipBetweenOriginalConcepts()+": = 0) ]";
		Log.consoleln(sameReport);
		Log.outFileCompleteReport("Verification of exit conditions of loop\n"+sameReport);
		Log.outFileShortReport("Verification of exit conditions of loop\n"+sameReport);		
		// verify the iteration limit				
		if(iteration == WholeSystem.configTable.getInt("maxIteration")) {
			Log.consoleln("- Ending loop, maximum iteration quantity reached.");
			String sameReport2 = "Loop ended, maximum iteration quantity reached.";
			Log.outFileCompleteReport(sameReport2);
			Log.outFileShortReport(sameReport2);
			return true;
		}
		// at least n iterations and connected component == 1
		if(iteration >= WholeSystem.configTable.getInt("minIterationToVerifyUniqueConnectedComponent") 
				&& currentSystemGraphData.getConnectedComponentsCount() == 1) {
			Log.consoleln("- Ending loop, connected component = 1 reached.");
			String sameReport2 = "Loop ended, connected component = 1 reached.";
			Log.outFileCompleteReport(sameReport2);
			Log.outFileShortReport(sameReport2);
			return true;
		}
		// at least n iterations and relationship level between original concepts == 0
		if(iteration >= WholeSystem.configTable.getInt("minIterationToVerifyRelationshipBetweenOriginalConcepts") 
				&& WholeSystem.getStreamGraphData().getCurrentLevelRelationshipBetweenOriginalConcepts() == 0) {
			Log.consoleln("- Ending loop, relationship level between original concepts = 0 reached.");
			String sameReport2 = "Loop ended, relationship level between original concepts = 0 reached.";
			Log.outFileCompleteReport(sameReport2);
			Log.outFileShortReport(sameReport2);
			return true;
		}
		return false;
	}
	private static void prepareDataToNewIteration() throws Exception {
		// preparation to a new iteration
		Log.console("- Preparing data to new iteration");
		// extract new selected concepts
		ConceptsGroup newGroupConcept = WholeSystem.getConceptsRegister().getSelectedConcepts(iteration);
		// put the new concepts into the new instance of SetQuerySparql and add it in WholeSystem 
		SetQuerySparql newSetQuerySparql = new SetQuerySparql();
		newSetQuerySparql.insertListConcept(newGroupConcept);
		WholeSystem.insertListSetQuerySparql(newSetQuerySparql);
		Log.consoleln(" - "+newGroupConcept.size()+" concepts inserted in the set of query Sparql.");
		String sameReport = "Data to new iteration prepared.\n" +
				newGroupConcept.size()+" concepts inserted in the set of query Sparql.\n";
        Log.outFileCompleteReport(sameReport + newGroupConcept.toStringLong());
		Log.outFileShortReport(sameReport + newGroupConcept.toString());
	}
	private static void deleteCommonNodes_remainOriginalAndSelectedConcepts() throws Exception {
		Log.console("- Deleting common nodes, remain only original and selected concepts");
		int numOldNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numOldEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		// call algorithm:
		WholeSystem.getStreamGraphData().deleteCommonNodes_remainOriginalAndSelectedConcepts();
		int numCurrentNodes = WholeSystem.getStreamGraphData().getRealTotalNodes();
		int numCurrentEdges = WholeSystem.getStreamGraphData().getRealTotalEdges();
		Log.console(" - "+ (numOldNodes - numCurrentNodes) +" deleted nodes ");
		Log.consoleln(" and "+ (numOldEdges - numCurrentEdges) +" deleted edges.");
		Log.consoleln("- Remained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.");
		String sameReport = "Deleted all common nodes (remain only original and selected concepts) \n" +
				(numOldNodes - numCurrentNodes) +" deleted nodes" +
				" and "+ (numOldEdges - numCurrentEdges) +" deleted edges" +
				"\nOld Stream Graph: "+numOldNodes+" nodes, "+numOldEdges+" edges." +
				"\nRemained Stream Graph: "+numCurrentNodes+" nodes, "+numCurrentEdges+" edges.";
		Log.outFileCompleteReport(sameReport + "\n\n" + WholeSystem.getStreamGraphData().toString() );
		Log.outFileShortReport(sameReport);
	}
	
	private static void buildFinalHeadNodesFromOriginalConceptsAndSelectedConcepts(int lastIterationWithinOfLoopWithDistanceMeasuresCalculation) throws Exception {
		Log.console("- Building head nodes from original concepts and selected concepts");
		int n = currentSystemGraphData.buildFinalHeadNodesFromOriginalConceptsAndSelectedConcepts(lastIterationWithinOfLoopWithDistanceMeasuresCalculation);
		Log.consoleln(" - "+WholeSystem.getFinalHeadNodes().getCount() + " nodes ("+
						n+" discarded because betweenness equals zero calculated in iteration "+lastIterationWithinOfLoopWithDistanceMeasuresCalculation+").");
		String sameReport = "Built " + WholeSystem.getFinalHeadNodes().getCount() + 
				        "head nodes from original concepts and selected concepts (" +
				        n+" discarded because betweenness equals zero calculated in iteration "+lastIterationWithinOfLoopWithDistanceMeasuresCalculation+").";
		Log.outFileCompleteReport(sameReport + "\n\n" + WholeSystem.getFinalHeadNodes().toString());
		Log.outFileShortReport(sameReport);
	}

	private static void filterStreamGraphWithNodesAndEdgesBelongToShortestPathsOfFinalHeadNodes() throws Exception {
		Log.console("- Filtering stream graph: only nodes and edges belong to shortest path between head nodes");
		int n = WholeSystem.getStreamGraphData().filterStreamGraphWithNodesAndEdgesBelongToShortestPathsOfFinalHeadNodes();			
		Log.consoleln(" - " + n + " paths found.");
		Log.consoleln("- Remained Stream Graph: "+WholeSystem.getStreamGraphData().getRealTotalNodes()+" nodes, "+
				WholeSystem.getStreamGraphData().getRealTotalEdges()+" edges.");
		String sameReport = "Network filtered: remained nodes and edges belong to " + n + " paths found.";
		Log.outFileCompleteReport(sameReport + "\n\n" + WholeSystem.getStreamGraphData().toStringGraph());
		Log.outFileShortReport(sameReport);
	}
	
	// get group of selected concepts and copy them to a new NodesTableArray
	// sort this table e store it in WholeSystem.sortEccentricityAndAverageSelectedConcepts
	// (do not enter: original concepts, concepts that already were category or concepts with zero rdfs)
	private static void createSortEccentricityAndAverageOnlySelectedConcepts() throws Exception {
		Log.console("- Creating eccentricity and average sort table of selected concepts");
		currentSystemGraphData.createSortEccentricityAndAverageOnlySelectedConcepts();
		Log.consoleln(" - "+WholeSystem.getSortEccentricityAndAverageSelectedConcepts().getCount()+" nodes stored and sorted.");
		String sameReport = "Table of selected nodes created and eccentricity and average (betweenness, closeness, eigenvector) sorted.\n";
		Log.outFileCompleteReport(sameReport+WholeSystem.getSortEccentricityAndAverageSelectedConcepts().toString());
		Log.outFileShortReport(sameReport);	
		// zero remain concepts: stop right now
		if(WholeSystem.getSortEccentricityAndAverageSelectedConcepts().getCount() == 0) {
			Log.consoleln("- Stoping. It's not possible to continue with zero selected concepts.");
			sameReport = "Algorithm stoped. It's not possible to continue with zero selected concepts.";
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);	
			end();
			System.exit(0);
		}
	}

	// get group of remaining concepts in Stream Graph (after iterations) and copy them to a new NodesTableArray
	// sort this table e store it in WholeSystem.sortEccentricityAndAverageRemainingConcepts
	// (do not enter: original concepts, concepts that already were category or concepts with zero rdfs)
	private static void createSortEccentricityAndAverageOnlyRemainingConcepts() throws Exception {
		Log.console("- Creating eccentricity and average sort table of remaining concepts");
		currentSystemGraphData.createSortEccentricityAndAverageOnlyRemainingConcepts();
		Log.consoleln(" - "+WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount()+" nodes stored and sorted.");
		String sameReport = "Table of remaining nodes created and eccentricity and average (betweenness, closeness, eigenvector) sorted.\n";
		Log.outFileCompleteReport(sameReport+WholeSystem.getSortEccentricityAndAverageRemainingConcepts().toString());
		Log.outFileShortReport(sameReport);	
	}
	
	
	private static void getNodeDataWithLeastEccentricityAndAverageFromSelectedConcepts() throws Exception {
		nodeDataWithLeastEccentricityAndAverage = WholeSystem.getSortEccentricityAndAverageSelectedConcepts().getNodeData(nodeDataPos);
		String sameReport = "Node data with least eccentricity and average: "
	               +nodeDataWithLeastEccentricityAndAverage.getShortName()
	               +" (position in group: "+nodeDataPos+"/"+WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount()+")";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
	}
	private static void getNodeDataWithLeastEccentricityAndAverageFromRemainingConcepts() throws Exception {
		nodeDataWithLeastEccentricityAndAverage = WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getNodeData(nodeDataPos);
		String sameReport = "Node data with least eccentricity and average: "
	               +nodeDataWithLeastEccentricityAndAverage.getShortName()
	               +" (position in group: "+nodeDataPos+"/"+WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount()+")";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
	}
	private static void storeCurrentInformationsAboutEnvironmentAndNodeWillBeDeleted() {
		// store the current informations of the environment and node that will be deleted (because it can be recovered)
		oldSystemGraphData = currentSystemGraphData;
		currentNode = nodeDataWithLeastEccentricityAndAverage.getStreamNode();
		currentEdgeSet = new ArrayList<Edge>();
		for(Edge currentEdge : currentNode.getEdgeSet()) {
			currentEdgeSet.add(currentEdge);
		}
	}
	
	private static void recoverEnvironmentAndNodeAndEdges(DeletedStatus deletedStatus) throws Exception {
		String sameReport = "Node did not exclude: "+nodeDataWithLeastEccentricityAndAverage.getShortName()
				      +" (connected component improves "+baseConnectedComponentCount+" to "+currentSystemGraphData.getConnectedComponentsCount()+")";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
		WholeSystem.getStreamGraphData().insert(currentNode,currentEdgeSet);
		// recover the last environment
		WholeSystem.getListSystemGraphData().remove(iteration);
		// whether excludedConcept is selected concept, then insert it again into WholeSystem.conceptsRegister
		if(deletedStatus == DeletedStatus.yes_SelectedConcept)
			WholeSystem.getConceptsRegister().add(currentConcept);	
		currentSystemGraphData = oldSystemGraphData;
	}
	private static void recoverEnvironment() throws Exception {
		String sameReport = "Node did not exclude: "+nodeDataWithLeastEccentricityAndAverage.getShortName()
				      +" (level of relationship between original concepts improves)";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
		// recover the last environment
		WholeSystem.getListSystemGraphData().remove(iteration);
		// whether excludedConcept is selected concept, then insert it again into WholeSystem.conceptsRegister
		currentSystemGraphData = oldSystemGraphData;
	}	
	private static void reportAfterSelectionMainConcepts_selectedConcepts() throws Exception {
		String sameReport = "Total concepts:\n"  
				+ "  "+WholeSystem.getSortEccentricityAndAverageSelectedConcepts().getCount() + " selected concepts + " 
				+ WholeSystem.getQuantityOriginalConcepts() + " original concepts = " 
				+ (WholeSystem.getSortEccentricityAndAverageSelectedConcepts().getCount() + WholeSystem.getQuantityOriginalConcepts()) + " total concepts"
				+ "  (goal "+WholeSystem.getGoalConceptsQuantity()
				+ " to " + WholeSystem.getMaxConceptsQuantity() + ")\n"
				+ "  Connected component count: " + currentSystemGraphData.getConnectedComponentsCount()
				+ " (base: " + baseConnectedComponentCount + ")\n" 
				+ "  Stream Graph: "+WholeSystem.getStreamGraphData().getRealTotalNodes() + " nodes, " 
				+ WholeSystem.getStreamGraphData().getRealTotalEdges() + " edges";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
	}
	private static void reportAfterSelectionMainConcepts_remainingConcepts() throws Exception {
		String sameReport = "Total concepts:\n"  
				+ "  "+WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount() + " remaining concepts + " 
				+ WholeSystem.getQuantityOriginalConcepts() + " original concepts = " 
				+ (WholeSystem.getSortEccentricityAndAverageRemainingConcepts().getCount() + WholeSystem.getQuantityOriginalConcepts()) + " total concepts"
				+ "  (goal "+WholeSystem.getGoalConceptsQuantity()
				+ " to " + WholeSystem.getMaxConceptsQuantity() + ")\n"
				+ "  Connected component count: " + currentSystemGraphData.getConnectedComponentsCount()
				+ " (base: " + baseConnectedComponentCount + ")\n" 
				+ "  Stream Graph: "+WholeSystem.getStreamGraphData().getRealTotalNodes() + " nodes, " 
				+ WholeSystem.getStreamGraphData().getRealTotalEdges() + " edges";
		Log.consoleln("- "+sameReport);
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);	
	}

	private static void showUselessConceptsStatistic() throws Exception {
		if(WholeSystem.configTable.getBoolean("isEnableUselessTable")) {
			Log.consoleln("- Recording in log statistic of useless concepts.");
			String sameReport = "Statistic of useless concepts:\n" + WholeSystem.getUselessConceptsTable().toString();
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);
		}
	}
	
	private static void buildRawConceptMapFromStreamGraph()  throws Exception {
		Log.console("- Building raw propositions of the concept map");
		int n =currentSystemGraphData.buildRawConceptMapFromStreamGraph();
		Log.consoleln(" - "+WholeSystem.getConceptMap().size()+" proposition created (" + n + " repeated propositions - eliminated).");
		String sameReport = "Built "+WholeSystem.getConceptMap().size()+" raw propositions of the concept map (" + 
		                     n + " repeated propositions - eliminated):\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void buildGexfGraphFileFromConceptMap() throws Exception {
		String nameFileGexf = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName")+"\\"+
	                          WholeSystem.configTable.getString("nameGexfGraphFile").replace(".gexf", "_t5_concept_map.gexf");
		Log.console("- Building GEXF Graph File from final concept map");
		WholeSystem.getConceptMap().buildGexfGraphFileFromConceptMap(nameFileGexf);
		Log.consoleln(" (generated file: " + nameFileGexf + ").");
		String sameReport = "GEXF graph file generated: " + nameFileGexf;
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void buildTxtFileFromConceptMap() throws Exception {
		Log.console("- Building TXT final Concept Map");
		String fileName = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName")+"\\"+
				          WholeSystem.configTable.getString("nameTxtConceptMapFile");
		WholeSystem.getConceptMap().buildTxtFileFromConceptMap(fileName);
		Log.consoleln(" (generated file: " + fileName + ").");
		String sameReport = "TXT concept map generated: " + fileName;
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
	}
	private static void buildCxlFileFromConceptMap() throws Exception {
		// if there are some alone concept, do not create CXL file
		List<String> aloneConcepts = new ArrayList<String>();
		for(Proposition prop : WholeSystem.getConceptMap().getPropositions()) {
			if(prop.getTargetConcept() == null)  {
				aloneConcepts.add(prop.getSourceConcept().getLabel());
			}
		}
		if(aloneConcepts.size() > 0) {
			String sameReport = "- File CXL did not create because there are alone concepts: "+aloneConcepts.toString();
			Log.consoleln(sameReport);
			Log.outFileCompleteReport(sameReport);
			Log.outFileShortReport(sameReport);
		}
		else {
			Log.console("- Building CXL final Concept Map");
			String fileName = WholeSystem.configTable.getString("baseDirectory")+"\\"+WholeSystem.configTable.getString("testName")+"\\"+
			          WholeSystem.configTable.getString("nameCxlConceptMapFile");
			String clxFileContent = WholeSystem.getConceptMap().buildCxlFileFromConceptMap(fileName);
			Log.consoleln(" (generated file: " + fileName + ").");
			String sameReport = "CXL concept map generated: " + fileName;
			Log.outFileCompleteReport(WholeSystem.getConceptMap().toStringComplete());
			Log.outFileCompleteReport(sameReport);
			Log.outFileCompleteReport(clxFileContent);
			Log.outFileShortReport(sameReport);
		}		
	}
	private static void upgradeConceptMap_heuristic_01_removeLinkNumber()  throws Exception {
		Log.console("- Upgrading the concept map with first heuristic (remove link id number)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_01_removeLinkNumber();
		Log.consoleln(" - " + n + " propositions changed.");
		String sameReport = "Heuristic 01: upgraded "+n+" concept map propositions with remove of link number:\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_02_vocabularyTable()  throws Exception {
		Log.console("- Upgrading the concept map with second heuristic (change links with vocabulary table)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_02_vocabularyTable();
		Log.consoleln(" - " + n + " links name changed.");
		String sameReport = "Heuristic 02: upgraded "+n+" concept map propositions with use of link vocabulary table:\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_03_categoryInTargetConcept()  throws Exception {
		Log.console("- Upgrading the concept map with third heuristic (change category in target concepts and links)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_03_categoryInTargetConcept();
		Log.consoleln(" - " + n + " propositions changed.");
		String sameReport = "Heuristic 03: upgraded "+n+" concept map propositions with change of category in target concept:\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_04_categoryInSourceConcept()  throws Exception {
		Log.console("- Upgrading the concept map with fourth heuristic (change category in source concept)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_04_categoryInSourceConcept();
		Log.consoleln(" - " + n + " propositions changed.");
		String sameReport = "Heuristic 04: upgraded "+n+" concept map propositions with change of category in source concept:\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_05_createOriginalConceptsWithZeroDegree()  throws Exception {
		Log.console("- Upgrading the concept map with fifth heuristic (create original concepts with zero degrees)");
		ConceptsGroup originalConcepts = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_05_createOriginalConceptsWithZeroDegree(currentSystemGraphData);
		Log.consoleln(" - " + originalConcepts.size() + " propositions changed.");
		String sameReport = "Heuristic 05: upgraded "+originalConcepts.size()+" alone original concepts created:\n" + 
							originalConcepts + "\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_06_joinEqualsConceptsWithoutAndWithCategory() throws Exception {
		Log.console("- Upgrading the concept map with sixth heuristic (join equals concepts without and with category)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_06_joinEqualsConceptsWithoutAndWithCategory();
		Log.consoleln(" - " + n + " concepts changed.");
		String sameReport = "Heuristic 06: upgraded "+n+" concepts equaled (same description without and with category)\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}	
	private static void upgradeConceptMap_heuristic_07_removeSelfReference()  throws Exception {
		Log.console("- Upgrading the concept map with seventh heuristic (remove self references)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_07_removeSelfReference();
		Log.consoleln(" - " + n + " propositions changed.");
		String sameReport = "Heuristic 07: upgraded "+n+" concept map propositions with remotions of the self reference:\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	// must be run before heuristics 09, 10 and 11 because they insert several ampersand characteres
	private static void upgradeConceptMap_heuristic_08_changeAmpersandCharacterInCxlFile() throws Exception {
		Log.console("- Upgrading the concept map with eighth heuristic (change ampersand characters in CXL file)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_08_changeAmpersandCharacterInCxlFile();
		Log.consoleln(" - " + n + " characters changed.");
		String sameReport = "Heuristic 10: upgraded "+n+" characters\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}

	private static void upgradeConceptMap_heuristic_09_putNewLineInCategory() throws Exception {
		Log.console("- Upgrading the concept map with nineth heuristic (put new line in category word)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_09_putNewLineInCategory();
		Log.consoleln(" - " + n + " concepts changed.");
		String sameReport = "Heuristic 09: upgraded "+n+" concepts with insertion of new line in category word\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_10_putNewLineInLongSentence() throws Exception {
		Log.console("- Upgrading the concept map with tenth heuristic (put new line in long sentence)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_10_putNewLineInLongSentence();
		Log.consoleln(" - " + n + " new lines inserted.");
		String sameReport = "Heuristic 10: upgraded "+n+" new lines inserted in all long sentences\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	private static void upgradeConceptMap_heuristic_11_setAccentedCharacterInCxlFile() throws Exception {
		Log.console("- Upgrading the concept map with eleventh heuristic (set accented characters in CXL file)");
		int n = WholeSystem.getConceptMap().upgradeConceptMap_heuristic_11_setAccentedCharacterInCxlFile();
		Log.consoleln(" - " + n + " characters changed.");
		String sameReport = "Heuristic 11: upgraded "+n+" characters\n\n" + WholeSystem.getConceptMap().toString();
		Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);		
	}
	

	private static void end() throws Exception {
		Log.consoleln("- Closing.");
		if(WholeSystem.configTable.getBoolean("graphStreamVisualization")) 
			WholeSystem.getStreamGraphData().getStreamGraph().clear();
		String sameReport = "Closed.\nOk!";
        Log.outFileCompleteReport(sameReport);
		Log.outFileShortReport(sameReport);
		Log.consoleln("- The end.");
		Log.close();
	}
}

