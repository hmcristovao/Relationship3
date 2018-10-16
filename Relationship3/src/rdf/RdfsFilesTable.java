package rdf;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import main.Constants;
import main.WholeSystem;
import myBase.Links;
import parser.Token;

public class RdfsFilesTable  implements Serializable   {
	Map<String, String> table; // concept, fileName (change : to ;)
	
	public RdfsFilesTable() {
		this.table = new HashMap<String,String>();
	}
	public void init(String directory) {
		File directoryFile = new File(directory);  
        for(File file : directoryFile.listFiles()) { 
           String concept = RdfsFilesTable.formatToConcept(file.getName());
           this.put(concept, file.toString());  
        }
	}
	public boolean containsKey(String fileName) {
		return this.table.containsKey(fileName);
	}
	public void put(String concept, String fileName) {
		this.table.put(concept, fileName);
	}
	public String get(String concept) {
		return this.table.get(concept);
	}
	public int size() {
		return this.table.size();
	}
	
	public static String formatToConcept(String fileName) {
		String str = fileName.replace(';', ':').replace('_', ' ');
		StringBuilder newStr = new StringBuilder();
		for(int i=0; i < str.length() - 4; i++) {
			if(str.charAt(i) == '^') {
			   i++;
			   newStr.append(Character.toUpperCase((str.charAt(i))));
			}
			else
			{
				newStr.append(str.charAt(i));
			}
		}
		return newStr.toString();
	}
	public static String formatToFileName(String concept) {
		String str = concept.replace(':', ';').replace(' ', '_');
		StringBuilder newStr = new StringBuilder();
		for(int i=0; i < str.length(); i++) {
			if(str.charAt(i) >= 'A' && str.charAt(i) <= 'Z' ) {
			   newStr.append('^');
			   newStr.append(Character.toLowerCase((str.charAt(i))));
			}
			else
			{
				newStr.append(str.charAt(i));
			}
		}
		newStr.append(".dat");
		return newStr.toString();
	}
	
	public String toString() {
		// at first: sort
		TreeSet<String> sortSet = new TreeSet<String>();
		Iterator<String> i = this.table.keySet().iterator(); 
		while(i.hasNext()) {
			String key     = (String)i.next(); 
			sortSet.add(key);
		}	
		// second: list
		StringBuilder out = new StringBuilder();
		for(String str : sortSet) {
			out.append(str);
			out.append("\n");
		}
		return out.toString();
	}

	public String toStringAux() {
		StringBuilder out = new StringBuilder();
		Iterator<String> i = this.table.keySet().iterator(); 
		while(i.hasNext()) {
		   String concept = (String)i.next(); 
		   String fileName = this.table.get(concept);
		   out.append(fileName);
		   out.append(", ");
		}
		return out.toString();
	}
}
