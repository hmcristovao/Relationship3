package myBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class MyKnowledgeBase {

	private Map<String, Links> nodeHash;
	
	public MyKnowledgeBase() {
		this.nodeHash = new HashMap<String,Links>();
	}
	
	public int size() {
		return this.nodeHash.size(); 
	}
	public void insert(String nodeSubject, String predicate, String nodeObject) {
	
		// remove possible of quotation marks in each sentences
		nodeSubject = MyKnowledgeBase.removeQuotationMarks(nodeSubject);
		predicate   = MyKnowledgeBase.removeQuotationMarks(predicate);
		nodeObject  = MyKnowledgeBase.removeQuotationMarks(nodeObject);		
		
		Link link;
		
		link = new Link(predicate, nodeObject, true);
		this.insertElement(nodeSubject, link);
		
		link = new Link(predicate, nodeSubject, false);
		this.insertElement(nodeObject, link);
		
	}
	
	public void insertElement(String nodeDesc, Link link) {
		// if node1Desc already exists
		if(this.nodeHash.containsKey(nodeDesc)) {
			Links linkList = this.nodeHash.get(nodeDesc);
			linkList.add(link);
		}
		// if node1Desc do not exist yet
		else {
			Links newLinkList = new Links();
			newLinkList.add(link);
			this.nodeHash.put(nodeDesc,  newLinkList);
		}
	}

	
	public Links get(String nodeDesc) {
		return this.nodeHash.get(nodeDesc);
	}

	// this method is important because the Excel put quotation marks when it find comma in sentence (in generation of file separated for TAB)
	public static String removeQuotationMarks(String str) {
		// if exists quotation marks in the begin and in the end of string, then remove them.
		if(str.charAt(0) == '\"' && str.charAt(str.length()-1) == '\"') {
			str = str.substring(1, str.length()-1);
		}
		return str;
	}

	
	public String toString() {
		// at first: sort
		TreeSet<String> sortSet = new TreeSet<String>();
		Iterator<String> i = this.nodeHash.keySet().iterator(); 
		while(i.hasNext()) {
		   String key     = (String)i.next(); 
		   Links linkList = this.nodeHash.get(key);
		   sortSet.add(key + " => " + linkList.toString());
		}	
		// second: list
		StringBuilder out = new StringBuilder();
		for(String str : sortSet) {
			out.append(str);
			out.append("\n");
		}
		return out.toString();
	}
}
