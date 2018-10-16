package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

public class VertexType implements Serializable {

	private final String vertexTitle;
	private Double x = 0.0;
	private Double y = 0.0;
	private Color color = Color.red;

	VertexType(String vertexTitle) {
		this.vertexTitle = vertexTitle;
	}

	@Override
	public String toString() {
		return vertexTitle;
	}

	@Override
	public boolean equals(Object vertex) {
		return ((VertexType) vertex).vertexTitle.equals(this.vertexTitle);
	}

	@Override
	public int hashCode() {
		return vertexTitle.hashCode();
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}

class VertexLabelTransformer implements Transformer<VertexType, String> {
	@Override
	public String transform(VertexType vertex) {
		return vertex.toString();
	}
}

class VertexColorTransformer implements Transformer<VertexType, Paint> {
	@Override
	public Paint transform(VertexType vertex) {
		return vertex.getColor();
	}
}

class PaintPickedVertexTransformer implements Transformer<VertexType, Paint> {
	private PickedInfo<VertexType> pi;
	private Color color;
	private boolean painting = false;

	public PaintPickedVertexTransformer(PickedInfo<VertexType> pi, Color color) {
		this.pi = pi;
		this.color = color;
	}

	public void setPainting(boolean painting) {
		this.painting = painting;
	}

	@Override
	public Paint transform(VertexType vertex) {
		if (painting) {
			if (pi.isPicked(vertex)) {
				vertex.setColor(color);
				return this.color;
			} else {
				return vertex.getColor();
			}
		}
		return vertex.getColor();
	}
}

class VertexStrokeHighlightTransformer implements Transformer<VertexType, Stroke> {
	protected Stroke heavy = new BasicStroke(5);
	protected Stroke medium = new BasicStroke(3);
	protected Stroke light = new BasicStroke(1);
	protected PickedInfo<VertexType> pi;
	protected Graph<VertexType, EdgeType> graph;
	protected boolean highlight = false;

	public VertexStrokeHighlightTransformer(Graph<VertexType, EdgeType> graph, PickedInfo<VertexType> pi) {
		this.graph = graph;
		this.pi = pi;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	@Override
	public Stroke transform(VertexType v) {
		if (highlight) {
			if (pi.isPicked(v)) {
				return heavy;
			} else {
				for (VertexType w : graph.getNeighbors(v)) {
					if (pi.isPicked(w)) {
						return medium;
					}
				}
				return light;
			}
		} else {
			return light;
		}
	}
}