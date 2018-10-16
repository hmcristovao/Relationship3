package map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import parser.Token;

public class VocabularyTable {
	Map<String,String> table;
	
	public VocabularyTable() {
		this.table = new HashMap<String,String>();
	}
	public void put(Token rdfToken, Token mapToken) {
		this.put(rdfToken.image.trim(), mapToken.image.trim());
	}
	public void put(String rdfString, String mapLinkString) {
		this.table.put(rdfString, mapLinkString);
	}
	public String get(String rdfString) {
		return this.table.get(rdfString);
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
		   String value = this.table.get(key);
		   sortSet.add(key + " => " + value);
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
