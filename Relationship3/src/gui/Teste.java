package gui;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class Teste {
	public static void main(String[] args) {
		/*
		 * int layoutNumber = 0; JFrame graphFrame = new JFrame("Graph Frame Test");
		 * graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); Graph<VertexType,
		 * EdgeType> graphTest = new DirectedSparseMultigraph<>(); graphTest =
		 * GraphPersistence.loadGraphInfo("E:\\Relationship\\teste\\graph_info.xml");
		 * StaticLayout<VertexType, EdgeType> sLayout = new StaticLayout<VertexType,
		 * EdgeType>(graphTest, new Transformer<VertexType, Point2D>() { public Point2D
		 * transform(VertexType vertex) { Point2D p = new Point2D.Double(vertex.getX(),
		 * vertex.getY()); return p; } }); VisualizationViewer<VertexType, EdgeType>
		 * currentVV = new VisualizationViewer<VertexType, EdgeType>(sLayout, new
		 * Dimension(1138, 718)); GraphZoomScrollPane scrollPanel = new
		 * GraphZoomScrollPane(currentVV);
		 * currentVV.getRenderContext().setVertexLabelTransformer(new
		 * ToStringLabeller()); currentVV.getRenderContext().setEdgeLabelTransformer(new
		 * EdgeLabelTransformer());
		 * currentVV.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		 * graphFrame.getContentPane().add(scrollPanel); graphFrame.pack();
		 * graphFrame.setVisible(true);
		 */

		JFrame graphFrame = new JFrame("Graph Frame Test");
		graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Graph<VertexType, EdgeType> graph = new DirectedSparseMultigraph<>();
		GenerateGraph.parseTxtIntoGraph(graph, "E:\\Relationship\\teste\\conceptmap_teste.txt");
		AbstractLayout<VertexType, EdgeType> cl = new KKLayout<VertexType, EdgeType>(graph);
		VisualizationViewer<VertexType, EdgeType> currentVV = new VisualizationViewer<VertexType, EdgeType>(
				cl, new Dimension(1000, 1000));
		//currentVV.setGraphLayout(new KKLayout<VertexType, EdgeType>(graph));
		GraphZoomScrollPane scrollPanel = new GraphZoomScrollPane(currentVV);
		graphFrame.getContentPane().add(scrollPanel);
		graphFrame.pack();
		graphFrame.setVisible(true);
		//GraphPersistence.saveGraphInfo("E:\\Relationship\\teste\\graph_info.xml", graph, cl);
		
		JFrame graphFrame2 = new JFrame("Graph Frame Test");
		graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Graph<VertexType, EdgeType> graph2 = new DirectedSparseMultigraph<>();
		graph = GraphPersistence.loadGraphInfo("E:\\Relationship\\teste\\graph_info.xml");
		StaticLayout<VertexType, EdgeType> sLayout = new StaticLayout<VertexType, EdgeType>(graph2,
				new Transformer<VertexType, Point2D>() {
					public Point2D transform(VertexType vertex) {
						Point2D p = new Point2D.Double(vertex.getX(), vertex.getY());
						return p;
					}
				});
		VisualizationViewer<VertexType, EdgeType> currentVV2 = new VisualizationViewer<VertexType, EdgeType>(
				sLayout, new Dimension(1000, 1000));
		GraphZoomScrollPane scrollPanel2 = new GraphZoomScrollPane(currentVV2);
		graphFrame2.getContentPane().add(scrollPanel2);
		graphFrame2.pack();
		graphFrame2.setVisible(true);
		
/*
		JFrame graphFrame2 = new JFrame("Graph Frame Test2");
		graphFrame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentVV.setGraphLayout(new KKLayout<VertexType, EdgeType>(graph));
		GraphZoomScrollPane scrollPanel2 = new GraphZoomScrollPane(currentVV);
		graphFrame2.getContentPane().add(scrollPanel2);
		graphFrame2.pack();
		graphFrame2.setVisible(true);*/

		/*
		 * //***************************************************************************
		 * ***************
		 * GraphPersistence.saveGraphInfo("E:\\Relationship\\teste\\graph_info.xml",
		 * graphTest); GraphPersistence.saveGraphPositionInTXT(currentVV,
		 * "E:\\Relationship\\teste\\graph_persistence.txt"); Graph<VertexType,
		 * EdgeType> loadedGraphTest = new DirectedSparseMultigraph<>(); loadedGraphTest
		 * = GraphPersistence.loadGraphInfo("E:\\Relationship\\teste\\graph_info.xml");
		 * edu.uci.ics.jung.visualization.VisualizationViewer<VertexType, EdgeType>
		 * loadedVV = GraphPersistence.loadGraphPositionFromTXT(
		 * "E:\\Relationship\\teste\\graph_persistence.txt", loadedGraphTest);
		 * GraphZoomScrollPane loadedScrollPanel = new GraphZoomScrollPane(loadedVV);
		 * loadedVV.getRenderContext().setVertexLabelTransformer(new
		 * ToStringLabeller()); loadedVV.getRenderContext().setEdgeLabelTransformer(new
		 * EdgeLabelTransformer());
		 * loadedVV.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		 * //loadedVV.setGraphMouse(DisplayGraph.changeMouseMode(1)); JFrame
		 * loadedGraphFrame = new JFrame("Restore Test");
		 * loadedGraphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 * loadedGraphFrame.getContentPane().add(loadedScrollPanel);
		 * loadedGraphFrame.pack(); loadedGraphFrame.setVisible(true);
		 */
	}
}
