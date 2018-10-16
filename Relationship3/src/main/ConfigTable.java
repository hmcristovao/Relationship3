package main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import parser.Token;

public class ConfigTable {
	Map<String,String> table;
	
	public ConfigTable() {
		this.table = new HashMap<String,String>();
	}
	public void insert(String var, String value) {
		this.table.put(var, value);
	}
	public Object get(String var) {
		return this.table.get(var);
	}
	public String getString(String var) {
		return this.table.get(var);
	}
	public int getInt(String var) {
		return Integer.parseInt(this.table.get(var));
	}
	public double getDouble(String var) {
		return Double.parseDouble(this.table.get(var));
	}
	public boolean getBoolean(String var) {
		return Boolean.parseBoolean(this.table.get(var));
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
		   sortSet.add(key + " = " + value);
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
