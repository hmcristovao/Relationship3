package graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import parser.Token;

// it mainly has been used in StreamGraph.insertRDF_withinTheStreamGraph

public class UselessConceptsTable {
	Map<String, Integer> table;
	
	public UselessConceptsTable() {
		this.table = new HashMap<String,Integer>();
	}
	public boolean containsKey(String uselessConcept) {
		return this.table.containsKey(uselessConcept);
	}
	public boolean containsKeyAndPlusOne(String uselessConcept) {
		boolean bool = this.table.containsKey(uselessConcept);
		if(bool) {
			int count = this.get(uselessConcept);
		    count++;
		    this.table.put(uselessConcept, new Integer(count));
		    return true;
		}
		return false;
	}
	public void insert(String uselessConcept) {
		this.table.put(uselessConcept, new Integer(0));
	}
	public int get(String uselessConcept) {
		Integer integer = this.table.get(uselessConcept);
		return integer.intValue();
	}
	public int size() {
		return this.table.size();
	}
	public String toString() {
		// at first: sort
		TreeSet<String> sortSet = new TreeSet<String>();
		Iterator<String> i = this.table.keySet().iterator(); 
		while(i.hasNext()) {
		   String key   = (String)i.next(); 
		   int value = this.table.get(key);
		   sortSet.add(key + " (count: " + value + ")");
		}	
		// second: list
		StringBuilder out = new StringBuilder();
		for(String str : sortSet) {
			out.append("   ");
			out.append(str);
			out.append("\n");
		}
		return out.toString();
	}
}
