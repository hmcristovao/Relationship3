// It simply contains a general list of edges names.
// It does not contain information of edges.

package graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import main.Log;
import main.WholeSystem;

public class EdgesTableHash {
	private Map<String,EdgeData> table; 
	
	public EdgesTableHash() {
		this.table = new HashMap<String, EdgeData>();
	}	
	public int size() {
		return this.table.size();
	}
	public EdgeData get(String idEdge) {
		if(this.table.containsKey(idEdge))
			return this.table.get(idEdge);
		return null;
	}
	public String getNewName(String idEdge) {
		if(this.table.containsKey(idEdge)) 
			return this.table.get(idEdge).getNameAndIncrement();
		return null;
	}
	public void put(String idEdge, EdgeData edgeData) {
		this.table.put(idEdge, edgeData);
	}
	public void put(String idEdge) {
		if(!this.containsKey(idEdge)) {
		   EdgeData edgeData = new EdgeData(idEdge);
		   this.put(idEdge, edgeData);
		}
		else
			System.err.println("Repeated edge inserted in graph: "+idEdge);
	}
	public boolean containsKey(String idEdge) {
		return this.table.containsKey(idEdge);
	}
	public int incEdgeTimes(String idEdge) {
		EdgeData edgeData = this.get(idEdge);
		if(edgeData == null)
			return -1;
		return edgeData.incTimes();
	}
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(String idEdge: this.table.keySet()) {
			str.append((this.table.get(idEdge)).toString());
			str.append("\n");
		}
		return  str.toString();
	}
}


