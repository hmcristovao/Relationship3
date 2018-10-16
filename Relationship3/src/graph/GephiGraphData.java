package graph;

import java.io.File;

import main.Log;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

public class GephiGraphData {
	private Workspace workspace;
	private GraphModel graphModel;
	private AttributeModel attributeModel;
	private AttributeTable attributeTable;
	private AttributeColumn betweennessColumn;
	private AttributeColumn closenessColumn;
	private AttributeColumn eigenvectorColumn;
	private AttributeColumn eccentricityColumn;
	private AttributeColumn connectedComponentColumn;
	private Graph gephiGraph;
	
	public GephiGraphData() {
		ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
		projectController.newProject();
		this.workspace       = projectController.getCurrentWorkspace();
		this.graphModel      = Lookup.getDefault().lookup(GraphController.class).getModel();
		this.attributeModel  = Lookup.getDefault().lookup(AttributeController.class).getModel();
		this.attributeTable  = this.attributeModel.getNodeTable();
		this.gephiGraph      = this.graphModel.getGraph();

		// add attributes: "betweenness", "closeness", "eigenvector" and "connectedcomponent"
		// descobri que o tool kit do Gephi já cria automanticamente essas colunas após o cálculo do GraphTable 
		//this.betweennessColumn        =  attributeModel.getNodeTable().addColumn("betweenness",        AttributeType.DOUBLE);
		//this.closenessColumn          =  attributeModel.getNodeTable().addColumn("closeness",          AttributeType.DOUBLE);
		//this.eigenvectorColumn        =  attributeModel.getNodeTable().addColumn("eigenvector",        AttributeType.DOUBLE);
		//this.connectedComponentColumn =  attributeModel.getNodeTable().addColumn("connectedcomponent", AttributeType.INT);
		
		// fill in calculateGephiGraphDistanceMeasures() and calculateGephiGraphEigenvectorMeasure() methods
		this.betweennessColumn        = null;
		this.closenessColumn          = null;
		this.eigenvectorColumn        = null;
		this.eccentricityColumn       = null;
		this.connectedComponentColumn = null;
	}
	
	public GraphModel getGraphModel() {
		return this.graphModel;
	}
	public AttributeModel getAttributeModel() {
		return this.attributeModel;
	}
	public AttributeTable getAttributeTable() {
		return this.attributeTable;
	}
	public Graph getGephiGraph() {
		return this.gephiGraph;
	}
	public void setAttributeTable(AttributeTable attributeTable) {
		this.attributeTable = attributeTable;
	}
	public AttributeColumn getBetweennessColumn() {
		return this.betweennessColumn;
	}

	public AttributeColumn getClosenessColumn() {
		return this.closenessColumn;
	}

	public AttributeColumn getEigenvectorColumn() {
		return this.eigenvectorColumn;
	}
	public AttributeColumn getEccentricityColumn() {
		return this.eccentricityColumn;
	}
	public AttributeColumn getConnectedComponentColumn() {
		return this.connectedComponentColumn;
	}

	public QuantityNodesEdges getRealQuantityNodesEdges() {
		QuantityNodesEdges quantityNodesEdges = new QuantityNodesEdges(this.gephiGraph.getNodeCount(),this.gephiGraph.getEdgeCount());
		return quantityNodesEdges;
	}
	
	// calculate measures of the table: betweenness, closeness and eccentricity
	public void calculateGephiGraphDistanceMeasures() {
		GraphDistance graphDistance = new GraphDistance();
		graphDistance.setDirected(false);
		graphDistance.setNormalized(true);
		graphDistance.execute(this.graphModel, this.attributeModel);
		this.attributeTable 	= this.attributeModel.getNodeTable();
		this.betweennessColumn  = this.attributeTable.getColumn(GraphDistance.BETWEENNESS);
		this.closenessColumn    = this.attributeTable.getColumn(GraphDistance.CLOSENESS);
		this.eccentricityColumn = this.attributeTable.getColumn(GraphDistance.ECCENTRICITY);
	}
	
	// calculate eigenvector measure of the table
	public void calculateGephiGraphEigenvectorMeasure() {
		EigenvectorCentrality ec = new EigenvectorCentrality();
		ec.execute(this.graphModel, this.attributeModel);
		this.attributeTable 	= this.attributeModel.getNodeTable();
		this.eigenvectorColumn  = this.attributeTable.getColumn(EigenvectorCentrality.EIGENVECTOR);		
	}
	
	// classify connected components
	// work with current gephi graph (wherefore is better before to use: buildGephiGraphData_NodesTableHash_NodesTableArray_fromStreamGraph())
	public int classifyConnectedComponent() {
		ConnectedComponents connectedComponents = new ConnectedComponents();
		connectedComponents.execute(this.graphModel, this.attributeModel);
		this.connectedComponentColumn = this.attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);
		int count = connectedComponents.getConnectedComponentsCount();
		return count;
	}
	
	// create a file to especific iteration marked into of the fileGexf string
	// MUST be execute after calculation of Graph Table, to added the measure columns
	public void buildGexfGraphFile(String fileGexf) throws Exception {
		//Export full graph
		ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
	    // ??? exportController.exportFile(new File("io_gexf.gexf")); 
		GraphExporter exporter = (GraphExporter) exportController.getExporter("gexf");     //Get GEXF exporter
		exporter.setExportVisible(false);  // exports all graph
		exporter.setWorkspace(this.workspace);
	    exportController.exportFile(new File(fileGexf), exporter);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\nGephi Graph:\n\n");
		//Iterate over nodes
		for(Node node : this.gephiGraph.getNodes()) {
		    str.append(node.getNodeData().getId());
		    str.append("\n[Degree: ");
		    str.append(this.gephiGraph.getDegree(node));
		    str.append("] ");
		    str.append("[Label: ");
		    str.append(node.getNodeData().getLabel());
		    str.append("]\n");
	    	str.append("Edges:\n");
	    	for(Edge edge : this.gephiGraph.getEdges(node)) {
	    		str.append("      ");
		    	str.append(edge.getEdgeData().getLabel());
	    		str.append("\n");
		    }
	    	str.append("\n");
		}
		return str.toString();
	}
}
