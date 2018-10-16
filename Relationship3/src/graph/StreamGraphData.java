package graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.Constants;
import main.Count;
import main.Log;
import main.WholeSystem;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.AStar;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.measure.ClosenessCentrality;
import org.graphstream.algorithm.measure.EigenvectorCentrality;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

import rdf.ItemRDF;
import rdf.ListRDF;
import rdf.NodeRDF;
import rdf.OneRDF;
import rdf.QuerySparql;
import rdf.SetQuerySparql;
import user.Concept;
import user.ConceptsGroup;


public class StreamGraphData {
	
	private Graph streamGraph;
	private AStar astar;
	private int currentLevelRelationshipBetweenOriginalConcepts;
	private boolean isChangedStreamGraph;  // used to avoid recalculate levelRelationship unnecessarily
	private QuantityNodesEdges total;
    private QuantityNodesEdges duplicated;
    private QuantityNodesEdges deleted;
		
	public enum DeletedStatus { yes_SelectedConcept, yes_CommonConcept, no_RecoveredNode, no_OriginalConcept, no_NotFound }; 

	public StreamGraphData() {
		this.streamGraph = new SingleGraph(
				Constants.nameGraph,
				true,                   // non-fatal error throws an exception = verify duplicated nodes
				false,                  // auto-create
				Constants.maxNodes,
				Constants.minEdges
				);
		this.astar = new AStar(this.streamGraph);
		this.isChangedStreamGraph = true;
		this.total      = new QuantityNodesEdges();
		this.duplicated = new QuantityNodesEdges();
		this.deleted    = new QuantityNodesEdges();
	}
	
	public Graph getStreamGraph() {
		return this.streamGraph;
	}
	public int getCurrentLevelRelationshipBetweenOriginalConcepts() {
		return this.currentLevelRelationshipBetweenOriginalConcepts;
	}
	public void setCurrentLevelRelationshipBetweenOriginalConcepts(int level) {
		this.currentLevelRelationshipBetweenOriginalConcepts = level;
	}
	public boolean isChangedStreamGraph() {
		return this.isChangedStreamGraph;
	}
	public boolean setChangedStreamGraph(boolean value) {
		return this.isChangedStreamGraph = value;
	}
	
	// intern control of quantity of nodes and edges

	// total:
	public int getTotalNodes() {
		return this.total.getNumNodes();
	}
	public void incTotalNodes() {
		this.total.incNumNodes();
	}
	public void incTotalNodes(int value) {
		this.total.incNumNodes(value);
	}

	public int getTotalEdges() {
		return this.total.getNumEdges();
	}
	public void incTotalEdges() {
		this.total.incNumEdges();
	}
	public void incTotalEdges(int value) {
		this.total.incNumEdges(value);
	}

	// duplicated
	public int getTotalNodesDuplicate() {
		return this.duplicated.getNumNodes();
	}
	public void incTotalNodesDuplicate() {
		this.duplicated.incNumNodes();
	}
	public void incTotalNodesDuplicate(int value) {
		this.duplicated.incNumNodes(value);
	}

	public int getTotalEdgesDuplicate() {
		return this.duplicated.getNumEdges();
	}
	public void incTotalEdgesDuplicate() {
		this.duplicated.incNumEdges();
	}
	public void incTotalEdgesDuplicate(int value) {
		this.duplicated.incNumEdges(value);
	}

	// deleted
	public int getTotalNodesDeleted() {
		return this.deleted.getNumNodes();
	}
	public void incTotalNodesDeleted() {
		this.deleted.incNumNodes();
	}
	public void incTotalNodesDeleted(int value) {
		this.deleted.incNumNodes(value);
	}

	public int getTotalEdgesDeleted() {
		return this.deleted.getNumEdges();
	}
	public void incTotalEdgesDeleted() {
		this.deleted.incNumEdges();
	}
	public void incTotalEdgesDeleted(int value) {
		this.deleted.incNumEdges(value);
	}
	
	// actual and real quantity of nodes and edges
	public int getRealTotalNodes() {
		return this.streamGraph.getNodeCount();
	}
	public int getRealTotalEdges() {
		return this.streamGraph.getEdgeCount();
	}	
		
	// get RDFs and convert them to StreamGraph, but this fucntion is call: 
	//    in the firt iteration build StreamGraphData and EdgeTable from RDFs
	//    in the second iteration so foth, just add new data (RDFs) into StreamGraphData and EdgeTable
    // (build stream graph from rdfs in set query Sparql)
	// discard useless concepts (use WholeSystem.uselessConceptsTable to do this operation)
	public QuantityNodesEdges buildStreamGraphData_buildEdgeTable_fromRdfs(SetQuerySparql setQuerySparql, Count countUselessRDFs) {
		QuerySparql querySparql;
		ListRDF listRDF;
		OneRDF oneRDF;
		QuantityNodesEdges quantityNodesEdgesOut = new QuantityNodesEdges();
		QuantityNodesEdges quantityNodesEdges    = new QuantityNodesEdges();
		for(int i=0; i < setQuerySparql.getListQuerySparql().size(); i++) {
			querySparql = setQuerySparql.getListQuerySparql().get(i);
			listRDF = querySparql.getListRDF();
			for(int j=0; j < listRDF.size(); j++) {
				// get RDF elements
				oneRDF = listRDF.getList().get(j);
				// insert into of graph
				quantityNodesEdges.reset();
				
				// it maps RDF data to Graph data:
				this.insertRDF_withinStreamGraph(oneRDF, quantityNodesEdges, countUselessRDFs);
				
				this.incTotalNodes(quantityNodesEdges.getNumNodes());
				this.incTotalEdges(quantityNodesEdges.getNumEdges());
				this.incTotalNodesDuplicate(2 - quantityNodesEdges.getNumNodes());
				this.incTotalEdgesDuplicate(1 - quantityNodesEdges.getNumEdges());
				quantityNodesEdgesOut.incNumNodes(quantityNodesEdges.getNumNodes());
				quantityNodesEdgesOut.incNumEdges(quantityNodesEdges.getNumEdges());
			}
		}
		this.setChangedStreamGraph(true);
		return quantityNodesEdgesOut;
	}
	// only used by buildStreamGraphData_buildEdgeTable
	// work with only one triple RDF each Time
	// discard useless concepts (use WholeSystem.uselessConceptsTable to do this operation)
	// return whether it can or it can not insert RDF as graph element
	private void insertRDF_withinStreamGraph(OneRDF oneRDF, QuantityNodesEdges quantityNodesEdges, Count countUselessRDFs) { 
		// split elements of RDF:
		ItemRDF subjectRDF   = oneRDF.getSubject() ;
		ItemRDF predicateRDF = oneRDF.getPredicate();
		ItemRDF objectRDF    = oneRDF.getObject();
		// verify whether one of nodes belong to RDF is useless
		// in this case, discard RDF
		if(WholeSystem.configTable.getBoolean("isEnableUselessTable")) {
			if(WholeSystem.getUselessConceptsTable().containsKeyAndPlusOne(subjectRDF.getShortBlankName()) ||
					WholeSystem.getUselessConceptsTable().containsKeyAndPlusOne(objectRDF.getShortBlankName())) {
				countUselessRDFs.incCount();
				return;
			}
		}	
		
		Node node = null;
		Edge edge = null;
		// work with the RDF subject
		try {
			// try case 01
			node = this.streamGraph.addNode(subjectRDF.getShortBlankName()); // if it can not, because it exists, go to case 02
			quantityNodesEdges.incNumNodes();
			node.addAttribute("fullname",           subjectRDF.getFullName());
			node.addAttribute("shortunderlinename", subjectRDF.getShortUnderlineName());
			node.addAttribute("shortblankname",     subjectRDF.getShortBlankName());  // repeated with node id, but it is important...
			if(Constants.nodeLabelStreamGephi)
				node.addAttribute("label", subjectRDF.getShortBlankName());
			// if original concept then put label
			if(WholeSystem.getConceptsRegister().isOriginalConcept(subjectRDF.getShortBlankName())) { 
				node.addAttribute("label", subjectRDF.getShortBlankName());
			}		
		}
		// starting case 02
		catch(IdAlreadyInUseException e) {
			// repeated node, do nothing, only get it to continue the process
			node = this.streamGraph.getNode(subjectRDF.getShortBlankName());
		}

		// case 12
		// if predicate is one of the cataloged, transform it in attributes into node
		if(predicateRDF.getFullName().equals(Constants.addressBasic + "homepage"))
			node.addAttribute("homepage", objectRDF.getShortBlankName());
		else if(predicateRDF.getFullName().equals(Constants.addressBasic + "comment"))
			node.addAttribute("comment", objectRDF.getShortBlankName());
		else if(predicateRDF.getFullName().equals(Constants.addressBasic + "abstract"))
			node.addAttribute("abstract", objectRDF.getShortBlankName());
		else if(predicateRDF.getFullName().equals(Constants.addressBasic + "image"))
			node.addAttribute("image", objectRDF.getShortBlankName());
        
		// continue case 02
		// to common predicate (unknown predicate)
		// work with the object RDF
		else {
			try {
				// try case 01
				node = this.streamGraph.addNode(objectRDF.getShortBlankName());
				quantityNodesEdges.incNumNodes();
				node.addAttribute("fullname",           objectRDF.getFullName());
				node.addAttribute("shortunderlinename", objectRDF.getShortUnderlineName());
				node.addAttribute("shortblankname",     objectRDF.getShortBlankName());
				if(Constants.nodeLabelStreamGephi)
					node.addAttribute("label", objectRDF.getShortBlankName());
				// if original concept then put label
				if(WholeSystem.getConceptsRegister().isOriginalConcept(objectRDF.getShortBlankName())) { 
					node.addAttribute("label", objectRDF.getShortBlankName());
				}
			}
			catch(IdAlreadyInUseException e) {
				// case 03
				// repeated node, do nothing, only get it to continue the process
				node = this.streamGraph.getNode(objectRDF.getShortBlankName());
			}
			
			// work with predicate RDF
			try {
				// case 01
				edge = this.streamGraph.addEdge(predicateRDF.getShortBlankName(), subjectRDF.getShortBlankName(), objectRDF.getShortBlankName(),Constants.directedStreamGraph);
				edge.addAttribute("fullname",           predicateRDF.getFullName());
				edge.addAttribute("shortunderlinename", predicateRDF.getShortUnderlineName());
				edge.addAttribute("shortblankname",     predicateRDF.getShortBlankName());
				edge.addAttribute("repeatedtimes",      "0");  // quantity of repeated edges to same pair of nodes (default: 1) 
				// put new edge in hash table
				// if existent predicate (I don't know why!!! because the "catch" should get...) do nothing
				WholeSystem.getEdgesTable().put(predicateRDF.getShortBlankName());
				quantityNodesEdges.incNumEdges();
				if(Constants.edgeLabelStreamGephi)
					edge.addAttribute("label", predicateRDF.getShortBlankName());
			}
			// case 4(scratch): same predicate (different subject and object)
			// case 5(scratch): same subject and predicate (different object)
			// case 6(scratch): same predicate and object (different subject)
			// case 10(scratch): subject and object commuted, moreover same predicate
			catch(IdAlreadyInUseException e) {
				try {
					// insert a count element in the id edge and try again
                    String newNameEdge = WholeSystem.getEdgesTable().getNewName(predicateRDF.getShortBlankName());  // add "#n" in the edge name
					edge = this.streamGraph.addEdge(newNameEdge, subjectRDF.getShortBlankName(), objectRDF.getShortBlankName(),Constants.directedStreamGraph);
					quantityNodesEdges.incNumEdges();
					if(Constants.edgeLabelStreamGephi)
						// add modifier to differentiate each link into the graph
						edge.addAttribute("label", newNameEdge);
				}
				// case 7(scratch): same subject, predicate and object    
				catch(EdgeRejectedException e2) {
					Edge repeatedEdge = this.streamGraph.getEdge(predicateRDF.getShortBlankName()); 
					// store +1 in edge "repeatedTimes" attribute. Do not create other edge
					int currentTimes = Integer.parseInt((String)repeatedEdge.getAttribute("repeatedtimes"));
					currentTimes++;
					repeatedEdge.addAttribute("repeatedtimes", String.valueOf(currentTimes));
					// update the edge table
					WholeSystem.getEdgesTable().incEdgeTimes(predicateRDF.getShortBlankName());
				}
			}
			// case 8(scratch): same subject and object (different predicate) 
			catch(EdgeRejectedException e) {
				// search the existent edge with subject and object
				Node sourceNode = this.streamGraph.getNode(subjectRDF.getShortBlankName()); 
				Node targetNode = this.streamGraph.getNode(objectRDF.getShortBlankName()); 
				Edge existentEdge = sourceNode.getEdgeBetween(targetNode);
				// create a extension to the edge attribute. Do not create other edge
				// seach next free attribute...
				int numberNextFreeAttribute = 0;
				for( ; ; numberNextFreeAttribute++) {
					if(existentEdge.getAttribute("nextedge"+numberNextFreeAttribute) == null) 
						break;
				}
				existentEdge.addAttribute("nextedge"+numberNextFreeAttribute, predicateRDF.getShortBlankName());
			}
		}
		return;
	}
	
	// don't used (instead of, it was used Gephi Tool Kit)
	public void computeBetweennessCentrality() {
		BetweennessCentrality betweenness = new BetweennessCentrality();
		betweenness.init(this.getStreamGraph());
		//betweenness.setUnweighted();
		betweenness.setCentralityAttributeName("betweenness");
		betweenness.compute();
	}
	// don't used (instead of, it was used Gephi Tool Kit)
	public void computeClosenessCentrality() {
		ClosenessCentrality closeness = new ClosenessCentrality();
		closeness.init(this.getStreamGraph());
		closeness.setCentralityAttribute("closeness");
		closeness.compute();
	}
	// don't used (instead of, it was used Gephi Tool Kit)
	public void computeEigenvectorCentrality() {
		EigenvectorCentrality eingenvector = new EigenvectorCentrality();
		eingenvector.init(this.getStreamGraph());
		eingenvector.setCentralityAttribute("eingenvector");
		eingenvector.compute();	
	}

	/*
	// do not working...
	public void addNewConceptsLabel(ConceptsGroup newConcepts) {
		Node node;
		for(Concept concept : newConcepts.getList()) {
			node = this.streamGraph.getNode(concept.getBlankName());
			// some nodes will not be found because they are from "ConceptCategory"
			if(node != null) {
			   node.addAttribute("label", concept.getBlankName());  // the process enters here, but this act do not updade the graph visualization!!!
			   //node.changeAttribute("label", concept.getBlankName());  // it is also not working
			}
		}
	}
    */
	
	// Apply K-core on the Graph
	// return quantity of concepts deleted
	public int applyKCoreFilterTrigger(int k, boolean isForcedRelationship, Count quantityDeletedSelectedConcepts, Count quantityRecoveredNodes) {
		int total = 0, subtotal;
		do {
			subtotal = this.applyNdegreeFilterTrigger(k, isForcedRelationship, quantityDeletedSelectedConcepts, quantityRecoveredNodes);
			total += subtotal;
		}while(subtotal != 0);
		this.setChangedStreamGraph(true);
		return total;
	}
	
	// Apply n-degree filter on the Graph
	// return quantity of concepts deleted
	public int applyNdegreeFilterTrigger(int n, boolean isForcedRelationship, Count quantityDeletedSelectedConcepts, Count quantityRecoveredNodes) {
		LinkedList<Node> auxList = new LinkedList<Node>();
		// at first select the candidates nodes and put them in an auxiliary list
		for( Node node : this.streamGraph.getEachNode() ) {
			// if node has degree less than nDegreeFilter...
			if(node.getDegree() < n) {
				// ...and it is not original node, store this node
				if(!WholeSystem.getConceptsRegister().isOriginalConcept((String)node.getAttribute("shortblankname"))) {
					auxList.add(node);
				}
			}
		}
		// second: delete the nodes and their respectives edges
		int total = 0;
		DeletedStatus deletedStatus;
		for( Node node : auxList ) {
			deletedStatus = this.deleteNode(node, isForcedRelationship);
			total++;
			// figure out quantities...
			if(deletedStatus == DeletedStatus.yes_SelectedConcept)
				quantityDeletedSelectedConcepts.incCount();
			else if(deletedStatus == DeletedStatus.no_RecoveredNode)
				quantityRecoveredNodes.incCount();
		} 
		this.setChangedStreamGraph(true);
		return total;
	}
	
	// remove node of the concepts register, if it is the case
	// NEVER it delete an original concept
	// delete a node and all edges linked it
	// return status of remotion: yes_SelectedConcept, yes_CommonConcept, no_RecoveredNode, no_OriginalConcept, no_NotFound
	public DeletedStatus deleteNode(Node node, boolean isForcedRelationship) {
		Concept equivalentConcept = null;
		// verify whether exist equivalent concept
		if(WholeSystem.getConceptsRegister().isConcept((String)node.getAttribute("shortblankname"))) {
			equivalentConcept = WholeSystem.getConceptsRegister().getConcept((String)node.getAttribute("shortblankname"));
			// if original concept, do not delete
		    if(equivalentConcept.isOriginal()) { 
		        return DeletedStatus.no_OriginalConcept;
		    }
		}
		
		// if isForcedRelationship then store environment to a possible posterior recovery
		Node savedNode =  null;
		List<Edge> savedEdges = null;
		int savedLevelRelationship=0;
		if(isForcedRelationship) {
			savedNode = node;
			savedEdges = new ArrayList<Edge>();
			savedLevelRelationship = this.calculateRelationshipLevelBetweenOriginalConcepts();
			for(Edge edge : node.getEdgeSet()) 
				savedEdges.add(edge);
		}

		// remove all edges linked with this node
		for( Edge edge : node.getEachEdge()) {
			this.streamGraph.removeEdge(edge);
		}
		
		// remove the node of the Stream Graph
		if(this.streamGraph.removeNode(node) == null)
			return DeletedStatus.no_NotFound;

		// indicate that the network was changed
		this.setChangedStreamGraph(true);
		
		// if isForcedRelationship then verify whether levelRelationship changed
		int newLevelRelationship;
		if(isForcedRelationship) {
			newLevelRelationship = this.calculateRelationshipLevelBetweenOriginalConcepts();
			// if level improved then came back and recover the saved environment (node and edges)
			if(newLevelRelationship > savedLevelRelationship) {
				WholeSystem.getStreamGraphData().insert(savedNode, savedEdges);
				this.currentLevelRelationshipBetweenOriginalConcepts = savedLevelRelationship;
				return DeletedStatus.no_RecoveredNode;
			}
		}

		// if it's ok, then terminates the operation of remotion
		this.incTotalNodesDeleted();
		this.incTotalEdgesDeleted(node.getEdgeSet().size());
		this.incTotalNodes(-1);
		this.incTotalEdges(-1*node.getEdgeSet().size());

		// remove the concept of conceptsRegister (if applicable)
		if(equivalentConcept != null) {
			WholeSystem.getConceptsRegister().removeConcept(equivalentConcept.getBlankName());
			return DeletedStatus.yes_SelectedConcept;
		}
		return DeletedStatus.yes_CommonConcept;
	}
	
	// recover a node deleted and all edges linked it
	public void insert(Node node, List<Edge> edges) {
		this.incTotalNodesDeleted(-1);
		this.incTotalEdgesDeleted(-1*edges.size());
		this.incTotalNodes();
		this.incTotalEdges(edges.size());
		
		// insert node
		Node newNode = this.streamGraph.addNode(node.getId());
		newNode.addAttribute("shortblankname", (Object)node.getAttribute("shortblankname"));
		newNode.addAttribute("fullname", (Object)node.getAttribute("fullname"));
		if(node.getAttribute("homepage") != null) 
			newNode.addAttribute("homepage", (Object)node.getAttribute("homepage"));
		if(node.getAttribute("abstract") != null) 
			newNode.addAttribute("abstract", (Object)node.getAttribute("abstract"));
		if(node.getAttribute("comment") != null) 
			newNode.addAttribute("comment", (Object)node.getAttribute("comment"));
		if(node.getAttribute("image") != null) 
			newNode.addAttribute("image", (Object)node.getAttribute("image"));
		
		// insert all edges
		for( Edge edge : edges ) {
			Node nodeSource = edge.getSourceNode();
			Node nodeTarget = edge.getTargetNode();
			this.streamGraph.addEdge(edge.getId(), nodeSource, nodeTarget, Constants.directedStreamGraph);
		}
		this.setChangedStreamGraph(true);
	}

	// at moment, this method is not being used in main algorithm
	public void deleteCommonNodes_remainOriginalAndSelectedConcepts() {
		ArrayList<Node> auxLista = new ArrayList<Node>();
		for(Node node : this.streamGraph.getNodeSet()) {
			// if it is not original or selected concepts, separate it to delete it and all its edges
			if(!WholeSystem.getConceptsRegister().isConcept(node.getId())) 
				auxLista.add(node);  // it's ok because it is not possible remove directly of streamGraph (there is a bug in this operation)
		}
		for(Node node: auxLista)
			this.deleteNode(node, false);
		
		this.setChangedStreamGraph(true);
	}
	
    // level=0 indicate that it has all paths
	// level=n indicate that do not have n paths
	public int calculateRelationshipLevelBetweenOriginalConcepts() {
		int level=0;
		// calculate only whether stream graph was changed
		if(this.isChangedStreamGraph) {
			ConceptsGroup originalConcepts = WholeSystem.getOriginalConcepts();
			this.astar.init(this.streamGraph);
			int size = WholeSystem.getOriginalConcepts().size();		
			for(int i=0; i < size-1; i++) {
				for(int j=i+1; j < size; j++) {
					this.astar.compute(originalConcepts.getConcept(i).getBlankName(),originalConcepts.getConcept(j).getBlankName());
					if(this.astar.noPathFound()) {
						level++;
					}
				}
			}
			this.setChangedStreamGraph(false);
			this.currentLevelRelationshipBetweenOriginalConcepts = level;
		}
		// else get the store value
		else {
			level = this.currentLevelRelationshipBetweenOriginalConcepts;
		}
		return level;
	}
	
	public int filterStreamGraphWithNodesAndEdgesBelongToShortestPathsOfFinalHeadNodes() throws Exception {
		// seleciona todos os nodes e edges dos paths, que irão permanecer no gráfico
		Map<String, Node> finalNodes = new HashMap<String, Node>();
		Map<String, Edge> finalEdges = new HashMap<String, Edge>();
		int countPaths = 0;
		for(int i=0; i < WholeSystem.getFinalHeadNodes().getCount()-1; i++) {
			for(int j=i+1; j < WholeSystem.getFinalHeadNodes().getCount(); j++) {
				countPaths++;
				this.astar.compute(WholeSystem.getFinalHeadNodes().getNodeData(i).getShortName(), 
						           WholeSystem.getFinalHeadNodes().getNodeData(j).getShortName());
				if(!this.astar.noPathFound()) {
					Path path = astar.getShortestPath();
					for(Node node : path.getEachNode())
						finalNodes.put(node.getId(), node);
					for(Edge edge : path.getEachEdge())
						finalEdges.put(edge.getId(), edge);
				}
			}
		}
		// separate nodes and edges that do NOT were selecte, to posterior remotion
		List<Node> nodesToRemove = new ArrayList<Node>();
//		List<Edge> edgesToRemove = new ArrayList<Edge>();
		for(Node node : this.streamGraph.getEachNode()) {
			if(!finalNodes.containsKey(node.getId()))
				nodesToRemove.add(node);
		}
//		for(Edge edge : this.streamGraph.getEachEdge()) {
//			if(!finalEdges.containsKey(edge.getId()))
//				edgesToRemove.add(edge);
//		}
//		
		// remove in the stream graph all nodes and edges that do NOT were selected
		for(Node node : nodesToRemove) {
			this.streamGraph.removeNode(node);
		}
//		for(Edge edge : edgesToRemove) {
//			this.streamGraph.removeEdge(edge);
//		}
		return countPaths;
	}
	
	public boolean isNodeHasLinkWithOriginalConcepts(Node currentNode) {
		for(Edge edge : currentNode.getEachEdge()) {
			for(Concept concept : WholeSystem.getOriginalConcepts().getList()) {
				if(edge.getSourceNode().getId().equals(concept.getBlankName()))
					return true;
				if(edge.getTargetNode().getId().equals(concept.getBlankName()))
					return true;
			}
		}
		return false;
	}
	
	// It is necessary that the node has only one edge to be not able to exclude
	public boolean isNodeHasLinkWithAnEspecificOriginalConcept(Node currentNode, String originalConcept) {
		for(Edge edge : currentNode.getEachEdge()) {
			if(edge.getSourceNode().getId().equals(originalConcept))
				// verify if there are another edges
				if(edge.getSourceNode().getDegree() <= 1)
					return true;
			
			if(edge.getTargetNode().getId().equals(originalConcept))
				// verify if there are another edges
				if(edge.getTargetNode().getDegree() <= 1)
					return true;
		}
		return false;
	}

	public static String nodeToString(Node node) {
		StringBuilder str = new StringBuilder();
		str.append("\nID: ");
		str.append(node.toString());
		str.append(" - Full name: ");
		str.append((String)node.getAttribute("fullname"));
		str.append("\n[Degree: ");
		str.append(node.getDegree());
		str.append("] [In degree: ");
		str.append(node.getInDegree());
		str.append("] [OutDegree: ");
		str.append(node.getOutDegree());
		str.append("]");
		if(node.getAttribute("label") != null) {
			str.append("\nLabel: ");
			str.append((String)node.getAttribute("label"));
		}
		if(node.getAttribute("homepage") != null) {
			str.append("\nHomepage: ");
			str.append((String)node.getAttribute("homepage"));
		}
		if(node.getAttribute("abstract") != null) {
			str.append("\nAbstract: ");
			str.append((String)node.getAttribute("abstract"));
		}
		if(node.getAttribute("comment") != null) {
			str.append("\nComment: ");
			str.append((String)node.getAttribute("comment"));
		}
		if(node.getAttribute("image") != null) {
			str.append("\nImage: ");
			str.append((String)node.getAttribute("image"));
		}
		str.append("\nEdges:\n");
		for( Edge edge : node.getEachEdge()) {
			str.append("      ");
			str.append(edge.toString());
			str.append(" (times: ");
			str.append((String)edge.getAttribute("repeatedTimes"));
			str.append(")\n");
			for(int numberExtraEdge = 0; ; numberExtraEdge++) {
				if(edge.getAttribute("nextedge"+numberExtraEdge) == null) 
					break;
				else {
					str.append("         extra edge ");
					str.append(numberExtraEdge);
					str.append(": ");
					str.append((String)edge.getAttribute("nextedge"+numberExtraEdge));
					str.append("\n");
				}
			}
		}
		return str.toString();
	}

	
	public String toStringGraph() {
		StringBuilder str = new StringBuilder();
		Graph graph = this.getStreamGraph();
		str.append("\n\nGraph stream:\n");
		for( Node node : graph.getEachNode() ) {
			str.append(StreamGraphData.nodeToString(node));
		}
		return str.toString();
	}
	
	public String toStringShort() {
		return  "\n\nGraph stream (resume):\n" +
				"\nTotal nodes (counted):  " + this.getTotalNodes() +
				"\nTotal nodes (real):     " + this.getRealTotalNodes() +
				"\nTotal edges (counted):  " + this.getTotalEdges() + 
				"\nTotal duplicated nodes: " + this.getTotalNodesDuplicate() +
				"\nTotal duplicated edges: " + this.getTotalEdgesDuplicate() +
				"\nTotal deleted nodes: "    + this.getTotalNodesDeleted() +
				"\nTotal deleted edges: "    + this.getTotalEdgesDeleted();
	}
	
	public String toString() {
		return  this.toStringGraph() +
				Constants.singleLine +
				this.toStringShort();
	}
}
