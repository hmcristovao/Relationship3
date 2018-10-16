package map;

import graph.GephiGraphData;
import graph.NodeData;
import graph.SystemGraphData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import user.Concept;
import user.Concept.ConceptStatus;
import user.ConceptsGroup;
import main.Constants;
import main.Count;
import main.Log;
import main.WholeSystem;

public class ConceptMap {
	private List<Proposition> propositions;
	private Map<String,SimpleConcept> concepts;  // will be filled in fillAttributesOfFileCXL()
	private Map<String,String> links;            // will be filled in fillAttributesOfFileCXL()
			
	public ConceptMap() {
		this.propositions = new ArrayList<Proposition>();	
		this.concepts     = new HashMap<String, SimpleConcept>();  // id, SimpleConcept
		this.links        = new HashMap<String, String>();		   // id, label of link
	}
	
	public List<Proposition> getPropositions() {
		return this.propositions;
	}

	public int size() {
		return this.propositions.size();
	}
	
	public boolean insert(NodeData sourceConcept, String rawLink, NodeData targetConcept) {
		Proposition proposition = new Proposition( sourceConcept, rawLink, targetConcept);
		if(this.isExist(proposition))
			return false;
		this.propositions.add(proposition);
		return true;
	}

	public boolean insert(Concept concept) {
		Proposition proposition = new Proposition(concept);
		if(this.isExist(proposition))
			return false;
		this.propositions.add(proposition);
		return true;
	}
	
	public boolean isExist(Proposition newProposition) {
		for(Proposition proposition : this.propositions) {
			if(proposition.equals(newProposition))
				return true;
		}
		return false;
	}
	
	// remove part of link with #DDD...D
	public int upgradeConceptMap_heuristic_01_removeLinkNumber() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null)
			{
				// verify whether there is a substring "#DDDD" (D=digit)
				int pos = proposition.getLink().lastIndexOf('#');
				if(pos != -1) {
					if(proposition.getLink().charAt(pos+1) >= '0' && proposition.getLink().charAt(pos+1) <= '9' &&
							proposition.getLink().charAt(pos+2) >= '0' && proposition.getLink().charAt(pos+2) <= '9' &&
							proposition.getLink().charAt(pos+3) >= '0' && proposition.getLink().charAt(pos+3) <= '9') {
						proposition.setLink(proposition.getLink().substring(0, pos));
						n++;
					}
				}
			}
		}
		return n;
	}
	// create the final map concept
	// it only changes the links from vocabularyTable
	public int upgradeConceptMap_heuristic_02_vocabularyTable() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			String newLink = WholeSystem.getVocabularyTable().get(proposition.getLink());
			if(newLink != null) {
				proposition.setLink(newLink);
				n++;
			}
		}
		return n;
	}
	// the prefix "ConceptCategory:" in target concept is changed to:
	// link: "belongs to"
	// target concept: "... category"
	public int upgradeConceptMap_heuristic_03_categoryInTargetConcept() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null)
			{
				if(Concept.verifyIfCategory(proposition.getTargetConcept().getLabel())) {
					proposition.setLink("belongs to");
					String newTargetConcept = Concept.extractCategory(proposition.getTargetConcept().getLabel()) + " category";
					proposition.setTargetConcept(newTargetConcept);
					n++;
				}
			}
		}
		return n;
	}
	// the prefix "Category:" in source concept is changed to:
	// source concept: "... category"
	public int upgradeConceptMap_heuristic_04_categoryInSourceConcept() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null)
			{
				if(Concept.verifyIfCategory(proposition.getSourceConcept().getLabel())) {
					String newSourceConcept = Concept.extractCategory(proposition.getSourceConcept().getLabel()) + " category";
					proposition.setSourcetConcept(newSourceConcept);
					n++;
				}
			}
		}
		return n;
	}
	
	public ConceptsGroup upgradeConceptMap_heuristic_05_createOriginalConceptsWithZeroDegree(SystemGraphData currentSystemGraphData) {
		// search in Gephi Graph, alone original concepts 
		ConceptsGroup originalConcepts = new ConceptsGroup();
		for( Node node : currentSystemGraphData.getGephiGraphData().getGephiGraph().getNodes() ) {
			if(currentSystemGraphData.getGephiGraphData().getGephiGraph().getDegree(node) == 0) {
				if(WholeSystem.getConceptsRegister().isOriginalConcept(node.getNodeData().getLabel())) {
					originalConcepts.add(WholeSystem.getConceptsRegister().getConcept(node.getNodeData().getLabel()));
				}
			}
		}
		// insert concepts selected, without link in concept map
		for(Concept concept : originalConcepts.getList()) {
			WholeSystem.getConceptMap().insert(concept);
		}
		return originalConcepts;
	}

	// return quantity of concepts changed
	public int changeConceptStringToStringAllMap(String oldConcept, String newConcept) {
		int n = 0;
		// search for another concepts in map to also change
		for( Proposition prop : this.getPropositions()) {
			if(prop.getLink() != null) {
				// if is source concept
				if(oldConcept.equals(prop.getSourceConcept().getLabel())) {
					prop.setSourcetConcept(newConcept);
					n++;
				}
				// or if is target concept
				if(oldConcept.equals(prop.getTargetConcept().getLabel())) {
					prop.setTargetConcept(newConcept);
					n++;
				}
			}
		}
		return n;
	}
	
	// return quantity of concepts changed
	public int changeConceptStringToObjectAllMap(String oldConcept, SimpleConcept newConcept) {
		int n = 0;
		// search for another concepts in map to also change
		for( Proposition prop : this.getPropositions()) {
			if(prop.getLink() != null) {
				// if is source concept
				if(oldConcept.equals(prop.getSourceConcept().getLabel())) {
					prop.setSourceConcept(newConcept);
					n++;
				}
				// or if is target concept
				if(oldConcept.equals(prop.getTargetConcept().getLabel())) {
					prop.setTargetConcept(newConcept);
					n++;
				}
			}
		}
		return n;
	}
	
	public int upgradeConceptMap_heuristic_06_joinEqualsConceptsWithoutAndWithCategory() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() == null) {
				continue;
			}
			
			// verify if "category" is in source concept and if the description is the same
			// in this case, remove "category" of source concept of all concepts in map
			String        simpleConceptWithouCategoryString = null;
			int sourceConceptLength = proposition.getSourceConcept().getLabel().length();
			if(sourceConceptLength > 8) {
				if(proposition.getSourceConcept().getLabel().substring(sourceConceptLength-8).equals("category")) {
					// get only description (without category)
					simpleConceptWithouCategoryString = proposition.getSourceConcept().getLabel().substring(0,sourceConceptLength-8).trim();
					// if two concept are sufix equals, set the concept to the same description
					if(proposition.getTargetConcept().getLabel().equals(simpleConceptWithouCategoryString)) {
						n += this.changeConceptStringToObjectAllMap(proposition.getSourceConcept().getLabel(), proposition.getTargetConcept());
					}
				}
			}

			// verify if "category" is in target concept and if the description is the same
			// in this case, remove "category" of source concept of all concepts in map
			int targetConceptLength = proposition.getTargetConcept().getLabel().length();
			if(targetConceptLength > 8) {
				if(proposition.getTargetConcept().getLabel().substring(targetConceptLength-8).equals("category")) {
					// get only description (without category)
					simpleConceptWithouCategoryString = proposition.getTargetConcept().getLabel().substring(0,targetConceptLength-8).trim();
					// if two concept are sufix equals, set the concept to the same description
					if(proposition.getSourceConcept().getLabel().equals(simpleConceptWithouCategoryString)) {
						n += this.changeConceptStringToObjectAllMap(proposition.getTargetConcept().getLabel(), proposition.getSourceConcept());
					}
				}
			}
		} 
		return n;
	}
	
	public int upgradeConceptMap_heuristic_07_removeSelfReference() {
		int n = 0;
		List<Proposition> excludedPropositions = new ArrayList<Proposition>();
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null)
			{
				if(proposition.getSourceConcept().getLabel().equals(proposition.getTargetConcept().getLabel())) {
					excludedPropositions.add(proposition);
					n++;
				}
			}
		}
		for(Proposition proposition : excludedPropositions) {
			this.propositions.remove(proposition);
		}
		return n;
	}

	// must be run before heuristics 09, 10 and 11 because they insert several ampersand characteres
	public int upgradeConceptMap_heuristic_08_changeAmpersandCharacterInCxlFile() {
		int total = 0;
		StringBuilder str;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null) {
				str = new StringBuilder(proposition.getSourceConcept().getLabel());
				total += ConceptMap.setAmpersandCharacter(str);
				proposition.getSourceConcept().setLabel(str.toString());

				str = new StringBuilder(proposition.getTargetConcept().getLabel());
				total += ConceptMap.setAmpersandCharacter(str);
				proposition.getTargetConcept().setLabel(str.toString());
				
				str = new StringBuilder(proposition.getLink());
				total += ConceptMap.setAmpersandCharacter(str);
				proposition.setLink(str.toString());
			}
		}
		return total;		
	}
	private static int setAmpersandCharacter(StringBuilder str) {
		int n = 0;
		// only change if do not contains '&' (="&amp;")
		if(str.indexOf("&amp;") == -1) {
			for(int i=0; i<str.length(); i++) {
				if(str.charAt(i) == '&') {
					str.replace(i, i+1, "&amp;");
					n++;
				}
			}
		}
		return n;
	}

	// insert new line before category word
	public int 	upgradeConceptMap_heuristic_09_putNewLineInCategory() {
		int n = 0;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null)
			{
				int sourceConceptLength = proposition.getSourceConcept().getLabel().length();
				if(sourceConceptLength > 8) {
					if(proposition.getSourceConcept().getLabel().substring(sourceConceptLength-8).equals("category")) {
						String newSourceConcept = proposition.getSourceConcept().getLabel().replace("category","&#xa;category");
						proposition.setSourcetConcept(newSourceConcept);
						n++;
					}
				}
				int targetConceptLength = proposition.getTargetConcept().getLabel().length();
				if(targetConceptLength > 8) {
					if(proposition.getTargetConcept().getLabel().substring(targetConceptLength-8).equals("category")) {
						String newTargetConcept = proposition.getTargetConcept().getLabel().replace("category","&#xa;category");
						proposition.setTargetConcept(newTargetConcept);
						n++;
					}
				}
			}
		}
		return n;
	}
	
	// break long sentences inserting new lines 
	public int 	upgradeConceptMap_heuristic_10_putNewLineInLongSentence() {
		int total = 0;
		Count count = new Count(0);
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null) {
				count.setCount(0);
				proposition.getSourceConcept().setLabel(
						ConceptMap.putNewLineInLongSentence(proposition.getSourceConcept().getLabel(), 
						                                    WholeSystem.configTable.getInt("maxLineLengthConcept"),
						                                    count)
				);
				total += count.getCount();
				count.setCount(0);
				proposition.getTargetConcept().setLabel(
						ConceptMap.putNewLineInLongSentence(proposition.getTargetConcept().getLabel(), 
						                                    WholeSystem.configTable.getInt("maxLineLengthConcept"),
						                                    count)
				);
				total += count.getCount();
				count.setCount(0);
				proposition.setLink(
						ConceptMap.putNewLineInLongSentence(proposition.getLink(), 
						                                    WholeSystem.configTable.getInt("maxLineLengthLinkPhrase"),
						                                    count)
				);		
				total += count.getCount();
				count.setCount(0);
			}
		}
		return total;
	}
	private static String putNewLineInLongSentence(String str, int max, Count n) {
		// if contains new line, it does nothing
		if(str.length() > max && !str.contains("&#xa;")) {
			int countNewLine = str.length() / max;
			StringBuilder s = new StringBuilder(str);
			int i,k;
			for(i=max, k=max; i<str.length()-max/2 && n.getCount() < countNewLine; i++, k++) {
				if(k>=max && s.charAt(i)==' ') {
					s.insert(i+1,"&#xa;");
					k=0;
					n.incCount();
				}
			}
			str = s.toString();
		}	
		return str;
	}
	
	public int upgradeConceptMap_heuristic_11_setAccentedCharacterInCxlFile() {
		int total = 0;
		StringBuilder str;
		for( Proposition proposition : this.getPropositions()) {
			// at first, verify whether it is not alone concept 
			if(proposition.getLink() != null) {
				str = new StringBuilder(proposition.getSourceConcept().getLabel());
				total += ConceptMap.setAccentedCharacter(str);
				proposition.getSourceConcept().setLabel(str.toString());

				str = new StringBuilder(proposition.getTargetConcept().getLabel());
				total += ConceptMap.setAccentedCharacter(str);
				proposition.getTargetConcept().setLabel(str.toString());
				
				str = new StringBuilder(proposition.getLink());
				total += ConceptMap.setAccentedCharacter(str);
				proposition.setLink(str.toString());
			}
		}
		return total;		
	}
	private static int setAccentedCharacter(StringBuilder str) {
		int n = 0;
		for(int i=0; i<str.length(); i++) {
			for(int k=0; k<Constants.characteres.length; k++)
				if(str.charAt(i) == Constants.characteres[k][0].charAt(0)) {
					str.replace(i, i+1, Constants.characteres[k][1]);
					n++;
				}
		}	
		return n;
	}

	// create a gephiGraph from concept map and generate a gexf file
	public void buildGexfGraphFileFromConceptMap(String fileGexf) throws Exception {
		// at first, create a new gephi graph
		GephiGraphData gephiGraphData = new GephiGraphData();
		// second: fill this gephi graph with concept map data
		AttributeColumn labelAttributeColumn = gephiGraphData.getAttributeModel().getNodeTable().getColumn("Label");
		int edgeIdNumber = 0;
		for(Proposition proposition : this.propositions) {
 			// create 1º node gephiNode
			Node nodeSource = gephiGraphData.getGraphModel().factory().newNode(proposition.getSourceConcept().getLabel());
			nodeSource.getNodeData().getAttributes().setValue(labelAttributeColumn.getIndex(), proposition.getSourceConcept().getLabel());	
			gephiGraphData.getGephiGraph().addNode(nodeSource);
			
			// whether it not special case (alone concept)
			if(proposition.getTargetConcept() != null) {	
				// create 2º node gephiNode
				Node nodeTarget = gephiGraphData.getGraphModel().factory().newNode(proposition.getTargetConcept().getLabel());
				nodeTarget.getNodeData().getAttributes().setValue(labelAttributeColumn.getIndex(), proposition.getTargetConcept().getLabel());
				gephiGraphData.getGephiGraph().addNode(nodeTarget);

				// create the edge (put different id to each edge because it is not possible same id in the record of getxf) 
				Edge edge = gephiGraphData.getGephiGraph().getGraphModel().factory().newEdge("#"+edgeIdNumber, nodeSource, nodeTarget, 1, true);
				edge.getEdgeData().getAttributes().setValue(labelAttributeColumn.getIndex(), proposition.getLink());				
				gephiGraphData.getGephiGraph().addEdge(edge);
				edgeIdNumber++;
			}
		}
 		// third: calculate measures
 		gephiGraphData.calculateGephiGraphDistanceMeasures();
 		gephiGraphData.calculateGephiGraphEigenvectorMeasure();
 		gephiGraphData.classifyConnectedComponent();
 		
		// fourth: create a file from gephi graph
		gephiGraphData.buildGexfGraphFile(fileGexf);
	}
		
	// create a TXT file from concept map
	// use tab ('\t') to separate concepts and links
	public void buildTxtFileFromConceptMap(String fileTxt) throws Exception {
		StringBuilder str = new StringBuilder();
		for(Proposition proposition : this.propositions) {
 			str.append(proposition.getSourceConcept().getLabel());
 			str.append('\t');
 			// verify whether special case (alone concept)
 			if(proposition.getLink() != null) {
 				str.append(proposition.getLink());
 				str.append('\t');
 				str.append(proposition.getTargetConcept().getLabel());
 			}
 			str.append('\r');
			str.append('\n');
		}
		BufferedWriter outFile = new BufferedWriter(new FileWriter(fileTxt)); 
		outFile.write(str.toString());
		outFile.close();
	}
		
	// fill diferents ids of the links, according to the grouping linked with the same concepts
	// return quantity of groups (quantity of differents links)
	private int fillLinksIdInPropositionTable() {
		
		// ==================================================================================
	    //  First stage of algorithm
		// ==================================================================================
		
		// structure to store the the groups with their propositions
		Map<String,List<Proposition>> linkTable = new HashMap<String,List<Proposition>>();

		// create all possible groups: concept+link and link+concept
		for(Proposition prop : this.propositions) {
			linkTable.put(prop.getSourceConcept().getLabel()+prop.getLink(), new ArrayList<Proposition>());
			linkTable.put(prop.getLink()+prop.getTargetConcept().getLabel(), new ArrayList<Proposition>());
		}
		// fill each group with their proposition
		List<Proposition> group;
		for(Proposition prop : this.propositions) {
			group = linkTable.get(prop.getSourceConcept().getLabel()+prop.getLink());
			group.add(prop);
			group = linkTable.get(prop.getLink()+prop.getTargetConcept().getLabel());
			group.add(prop);
		}
				// copy groups to an ArrayList
		List<ArrayList<Proposition>> groupsList = new ArrayList<ArrayList<Proposition>>();
		for(String key : linkTable.keySet()) {
			groupsList.add((ArrayList<Proposition>)linkTable.get(key));
		}
		// verify the equals to each group
		int numLinkingPhrase = 1;
		for(int i=0; i < groupsList.size()-1; i++) {
			List<ArrayList<Proposition>> equalsAuxList = new ArrayList<ArrayList<Proposition>>();
			equalsAuxList.add(groupsList.get(i));
			for(int j=i+1; j < groupsList.size(); j++) {
				// verify whether group 'i' is equal group 'j'
				if(ConceptMap.isEqualsGroup(groupsList.get(i),groupsList.get(j))) {
					equalsAuxList.add(groupsList.get(j));
				}
			}
			// determine new id link to the equals groups (only when there are more than one)
			if(equalsAuxList.size() > 1) {
				for(int k=0; k < equalsAuxList.size(); k++) {
					for(int l=0; l < equalsAuxList.get(k).size(); l++) {
						equalsAuxList.get(k).get(l).setIdLinkingPhrase("l"+numLinkingPhrase);
					}
				}
				this.links.put("l"+numLinkingPhrase, equalsAuxList.get(0).get(0).getLink());
				numLinkingPhrase++;
			}
		}
		
		// verify if still there are groups without id link in all elements
		// if yes then put l# in id link
        // (could be better - links more grouped - whether the verify begin for larger groups)
		for(int i=0; i < groupsList.size(); i++) {
			// verify if all propositions do not have id link 
			boolean isExistIdLink = false;
			for(int j=0; j < groupsList.get(i).size(); j++) {
				if(groupsList.get(i).get(j).getIdLinkingPhrase() != null) {
					isExistIdLink = true;
				}
			}
			if(!isExistIdLink) {
				for(int j=0; j < groupsList.get(i).size(); j++) {
					groupsList.get(i).get(j).setIdLinkingPhrase("l"+numLinkingPhrase);
				}
				this.links.put("l"+numLinkingPhrase, groupsList.get(i).get(0).getLink());				
				numLinkingPhrase++;
			}
		}

		// ==================================================================================
	    //  Last stage of algorithm (repechage of the propositions without id link)
		// ==================================================================================

		// verify if still there are propositions without id link
		// if yes then create a new collection and begin again this algorithm...
		List<Proposition> propositionsLastStage = new ArrayList<Proposition>();	
		for(int i=0; i < groupsList.size(); i++) {
			for(int j=0; j < groupsList.get(i).size(); j++) {
				if(groupsList.get(i).get(j).getIdLinkingPhrase() == null) {
					propositionsLastStage.add(groupsList.get(i).get(j));
				}
			}
		}
		// if there is any propostion still without id link, starts again the algorithm...
		if(propositionsLastStage.size() > 0) {
			
			// structure to store the the groups with their propositions
			Map<String,List<Proposition>> linkTable2 = new HashMap<String,List<Proposition>>();

			// create all possible groups: concept+link and link+concept
			for(Proposition prop : propositionsLastStage) {
				linkTable2.put(prop.getSourceConcept().getLabel()+prop.getLink(), new ArrayList<Proposition>());
				linkTable2.put(prop.getLink()+prop.getTargetConcept().getLabel(), new ArrayList<Proposition>());
			}
			// fill each group with their proposition
			List<Proposition> group2;
			for(Proposition prop : propositionsLastStage) {
				group2 = linkTable2.get(prop.getSourceConcept().getLabel()+prop.getLink());
				group2.add(prop);
				group2 = linkTable2.get(prop.getLink()+prop.getTargetConcept().getLabel());
				group2.add(prop);
			}
			// copy groups to an ArrayList
			List<ArrayList<Proposition>> groupsList2 = new ArrayList<ArrayList<Proposition>>();
			for(String key : linkTable2.keySet()) {
				groupsList2.add((ArrayList<Proposition>)linkTable2.get(key));
			}
			// verify the equals to each group
			for(int i=0; i < groupsList2.size()-1; i++) {
				List<ArrayList<Proposition>> equalsAuxList2 = new ArrayList<ArrayList<Proposition>>();
				equalsAuxList2.add(groupsList2.get(i));
				for(int j=i+1; j < groupsList2.size(); j++) {
					// verify whether group 'i' is equal group 'j'
					if(ConceptMap.isEqualsGroup(groupsList2.get(i),groupsList2.get(j))) {
						equalsAuxList2.add(groupsList2.get(j));
					}
				}
				// determine new id link to the equals groups (only when there are more than one)
				if(equalsAuxList2.size() > 1) {
					for(int k=0; k < equalsAuxList2.size(); k++) {
						for(int l=0; l < equalsAuxList2.get(k).size(); l++) {
							equalsAuxList2.get(k).get(l).setIdLinkingPhrase("l"+numLinkingPhrase);
						}
					}
					this.links.put("l"+numLinkingPhrase, equalsAuxList2.get(0).get(0).getLink());
					numLinkingPhrase++;
				}
			}

			// verify if still there are groups without id link in all elements
			// if yes then put l# in id link
			// (could be better - links more grouped - whether the verify begin for larger groups)
			for(int i=0; i < groupsList2.size(); i++) {
				// verify if all propositions do not have id link 
				boolean isExistIdLink = false;
				for(int j=0; j < groupsList2.get(i).size(); j++) {
					if(groupsList2.get(i).get(j).getIdLinkingPhrase() != null) {
						isExistIdLink = true;
					}
				}
				if(!isExistIdLink) {
					for(int j=0; j < groupsList2.get(i).size(); j++) {
						groupsList2.get(i).get(j).setIdLinkingPhrase("l"+numLinkingPhrase);
					}
					this.links.put("l"+numLinkingPhrase, groupsList2.get(i).get(0).getLink());				
					numLinkingPhrase++;
				}
			}

			// verify if still there are propositions without id link
			// if yes then put a new l# in its link
			for(int i=0; i < groupsList2.size(); i++) {
				for(int j=0; j < groupsList2.get(i).size(); j++) {
					if(groupsList2.get(i).get(j).getIdLinkingPhrase() == null) {
						groupsList2.get(i).get(j).setIdLinkingPhrase("l"+numLinkingPhrase);
						this.links.put("l"+numLinkingPhrase, groupsList2.get(i).get(j).getLink());				
						numLinkingPhrase++;
					}
				}
			}
		}
		
		return numLinkingPhrase;
	}
	
	// verify whether two groups are equals
	// all prefix (sourceConcept+link) or same sufix (link+targetConcept) of one are equals of other.
	public static boolean isEqualsGroup(List<Proposition> group1, List<Proposition> group2) {

		// if second group is already used (has idLink), go out.
		if(group2.get(0).getIdLinkingPhrase() != null)
			return false;
		
		// if size groups are differents then all elements must be equals
		if(group1.size() != group2.size()) {

			boolean isEquals = true;
			// at first verify whether all prefix are equals
			for(int i=0; i < group1.size(); i++) {
				String prefix_i = group1.get(i).getSourceConcept().getLabel() + group1.get(i).getLink();
				for(int j=0; j < group2.size(); j++) {
					String prefix_j = group2.get(j).getSourceConcept().getLabel() + group2.get(j).getLink();
					if(!prefix_i.equals(prefix_j)) {
						isEquals = false;
						break;
					}
				}
			}
			if(isEquals)
				return true;
			else
				isEquals = true;
			// second: verify whether all sufix are equals
			for(int i=0; i < group1.size(); i++) {
				String sufix_i = group1.get(i).getLink() + group1.get(i).getTargetConcept().getLabel();
				for(int j=0; j < group2.size(); j++) {
					String sufix_j = group2.get(j).getLink() + group2.get(j).getTargetConcept().getLabel();
					if(!sufix_i.equals(sufix_j)) {
						isEquals = false;
						break;
					}
				}
			}
			return isEquals;
		}
		
		// if size groups are equals then the equality is more loose
		else
		{
			int size = group1.size();  // anyone two groups
			// at first verify whether all prefix are equals
			// ...firt group with second group
			int equalsCount = 0;
			for(int i=0; i < group1.size(); i++) {
				String prefix_i = group1.get(i).getSourceConcept().getLabel() + group1.get(i).getLink();
				for(int j=0; j < group2.size(); j++) {
					String prefix_j = group2.get(j).getSourceConcept().getLabel() + group2.get(j).getLink();
					if(prefix_i.equals(prefix_j)) {
						equalsCount++;
						break;
					}
				}
			}
			if(equalsCount == size) {
				// ...second group with first group
				equalsCount = 0;
				for(int i=0; i < group2.size(); i++) {
					String prefix_i = group2.get(i).getSourceConcept().getLabel() + group2.get(i).getLink();
					for(int j=0; j < group1.size(); j++) {
						String prefix_j = group1.get(j).getSourceConcept().getLabel() + group1.get(j).getLink();
						if(prefix_i.equals(prefix_j)) {
							equalsCount++;
							break;
						}
					}
				}
				if(equalsCount == size)
					return true;
			}

			// second: verify whether all sufix are equals
			// ...firt group with second group
			equalsCount = 0;
			for(int i=0; i < group1.size(); i++) {
				String prefix_i = group1.get(i).getLink() + group1.get(i).getTargetConcept().getLabel();
				for(int j=0; j < group2.size(); j++) {
					String prefix_j = group2.get(j).getLink() + group2.get(j).getTargetConcept().getLabel();
					if(prefix_i.equals(prefix_j)) {
						equalsCount++;
						break;
					}
				}
			}
			if(equalsCount == size) {
				// ...second group with first group
				equalsCount = 0;
				for(int i=0; i < group2.size(); i++) {
					String prefix_i = group2.get(i).getLink() + group2.get(i).getTargetConcept().getLabel();
					for(int j=0; j < group1.size(); j++) {
						String prefix_j = group1.get(j).getLink() + group1.get(j).getTargetConcept().getLabel();
						if(prefix_i.equals(prefix_j)) {
							equalsCount++;
							break;
						}
					}
				}
				if(equalsCount == size)
					return true;
			}
			return false;
		}
	}
	
	
	// create attributes of CXL file from Propositions in ConceptMap (after processing of all heuristics)
	private int fillAttributesInPropositionTable() {
				
		this.fillLinksIdInPropositionTable();
		
		int numConcept       = 1;  
		int numJoin          = 1;       

		String idFoundConcept;  
		String idFoundJoin;

		for(int i=0; i < this.propositions.size(); i++) {
			Proposition prop_i = this.propositions.get(i);
			
			// special case: concept alone
			if(prop_i.getTargetConcept() == null) {
				idFoundConcept = "c"+numConcept;
				numConcept++;
				prop_i.setIdSourceConcept(idFoundConcept);
				this.concepts.put(idFoundConcept, prop_i.getSourceConcept());
				continue;
			}
			
			// figure out the source concept
			idFoundConcept = null;
			for(int j=0; j < i; j++) {
				Proposition prop_j = this.propositions.get(j);
				// search in the source concepts
				if(prop_i.getSourceConcept().getLabel().equals(prop_j.getSourceConcept().getLabel())) {
					idFoundConcept = prop_j.getIdSourceConcept();
					break;
				}
				// search in the target concepts
				if(prop_i.getSourceConcept().getLabel().equals(prop_j.getTargetConcept().getLabel())) {
					idFoundConcept = prop_j.getIdTargetConcept();
					break;
				}
			}
			// if did not find:
			if(idFoundConcept == null) {
				idFoundConcept = "c"+numConcept;
				numConcept++;
			}
			prop_i.setIdSourceConcept(idFoundConcept);
			this.concepts.put(idFoundConcept, prop_i.getSourceConcept());
		
			
			
			// figure out the target concept
			idFoundConcept = null;
			for(int j=0; j < i; j++) {
				Proposition prop_j = this.propositions.get(j);
				// search in the source concepts
				if(prop_i.getTargetConcept().getLabel().equals(prop_j.getSourceConcept().getLabel())) {
					idFoundConcept = prop_j.getIdSourceConcept();
					break;
				}
				// search in the target concepts
				if(prop_i.getTargetConcept().getLabel().equals(prop_j.getTargetConcept().getLabel())) {
					idFoundConcept = prop_j.getIdTargetConcept();
					break;
				}
			}
			// if did not find:
			if(idFoundConcept == null) {
				idFoundConcept = "c"+numConcept;
				numConcept++;
			}
			prop_i.setIdTargetConcept(idFoundConcept);
			this.concepts.put(idFoundConcept, prop_i.getTargetConcept());		
		
			
			
			// figure out the join
			idFoundJoin = null;
			for(int j=0; j < i; j++) {
				Proposition prop_j = this.propositions.get(j);
				// search for source concept and link equals
				if(prop_i.getSourceConcept().getLabel().equals(prop_j.getSourceConcept().getLabel()) &&
				   prop_i.getIdLinkingPhrase().equals(prop_j.getIdLinkingPhrase())) {
					idFoundJoin = prop_j.getIdSourceJoin();
					break;
				}
			}
			// if found:
			if(idFoundJoin != null) {
				prop_i.setIdSourceJoin(idFoundJoin);
				prop_i.setIdTargetJoin(numJoin);
				numJoin++;
			}
			// if did not find, then will figure out the join and link (target):
			else {
				idFoundJoin = "j"+numJoin;
				prop_i.setIdSourceJoin(idFoundJoin);
				numJoin++;
				
				idFoundJoin = null;
				for(int j=0; j < i; j++) {
					Proposition prop_j = this.propositions.get(j);
					// search for target concept and link equals
					if(prop_i.getTargetConcept().getLabel().equals(prop_j.getTargetConcept().getLabel()) &&
							prop_i.getIdLinkingPhrase().equals(prop_j.getIdLinkingPhrase())) {
						idFoundJoin = prop_j.getIdTargetJoin();
						break;
					}
				}
				// if did not find:
				if(idFoundJoin == null) {
					idFoundJoin = "j"+numJoin;
					numJoin++;
				}
				prop_i.setIdTargetJoin(idFoundJoin);
			}
		}
		return numJoin-1;
	}

	// create a CLX file from concept map
	// return content of CLX file
	public String buildCxlFileFromConceptMap(String fileClx) throws Exception {
		// at firt, fill attributes of Proposition class to create CLX file
		int countJ = this.fillAttributesInPropositionTable();

		// buffer to store the content that will be stored in CLX file
		StringBuilder str = new StringBuilder();
	
		str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		str.append("<cmap xmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n");
		str.append("xmlns=\"http://cmap.ihmc.us/xml/cmap/\"\r\n");
		str.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n");
		str.append("xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\">\r\n");
		
		str.append("<map>\r\n");
		
		// section: 
		str.append("<concept-list>\r\n");
		for(int i=1; i <= this.concepts.size(); i++) {
			SimpleConcept simpleConcept = this.concepts.get("c"+i); // get id's: c1, c2, c3, ...
			str.append("\t<concept id=\"");
			str.append(simpleConcept.getIdConcept());
			str.append("\" label=\"");
			str.append(simpleConcept.getLabel());
			str.append("\"");
			if(simpleConcept.getNodeData().getAbstractAttribute() != null) {
				str.append(" short-comment=\"");
				str.append(simpleConcept.getNodeData().getAbstractAttribute()
						   .replaceAll("\\\"", "'").replace("\\","").replace("&", "and"));
				str.append(" (font: DBPEDIA)");
				str.append("\" long-comment=\"\"");
			}
			else if(simpleConcept.getNodeData().getCommentAttribute() != null) {
				str.append(" short-comment=\"");
				str.append(simpleConcept.getNodeData().getCommentAttribute()
						   .replaceAll("\\\"", "'").replace("\\","").replace("&", "and"));
				str.append(" (font: DBPEDIA)");
				str.append("\" long-comment=\"\"");
			}
			str.append("/>\r\n");
		}
		str.append("</concept-list>\r\n");
		
		// section: <linking-phrase-list>
		str.append("<linking-phrase-list>\r\n");
		for(int i=1; i <= this.links.size(); i++) {
			String idLink = "l"+i;   // get id's: l1, l2, l3, ...
			String labelLink = this.links.get(idLink); 
			str.append("\t<linking-phrase id=\"");
			str.append(idLink);
			str.append("\" label=\"");
			str.append(labelLink);
			str.append("\"/>\r\n");
		}
		str.append("</linking-phrase-list>\r\n");
		
		// section: <connection-list>
		str.append("<connection-list>\r\n");
		for(Proposition proposition : this.propositions) {
			// special case: concept alone
			if(proposition.getTargetConcept() == null) {
				continue;
			}
			// source join
			str.append("\t<connection id=\"");
			str.append(proposition.getIdSourceJoin());
			str.append("\" from-id=\"");
			str.append(proposition.getIdSourceConcept());
			str.append("\" to-id=\"");
			str.append(proposition.getIdLinkingPhrase());
			str.append("\"/>\r\n");
			// target join
			str.append("\t<connection id=\"");
			str.append(proposition.getIdTargetJoin());
			str.append("\" from-id=\"");
			str.append(proposition.getIdLinkingPhrase());
			str.append("\" to-id=\"");
			str.append(proposition.getIdTargetConcept());
			str.append("\"/>\r\n");
		}
		str.append("</connection-list>\r\n");

		// section: <concept-appearance-list>
		str.append("<concept-appearance-list>\r\n");
		for(int i=1; i <= this.concepts.size(); i++) {
			SimpleConcept simpleConcept = this.concepts.get("c"+i); // get id's: c1, c2, c3, ...
			str.append("\t<concept-appearance id=\"");
			str.append(simpleConcept.getIdConcept());
			str.append("\" ");
			if(simpleConcept.getNodeData().getStatus() == ConceptStatus.originalConcept) {
				str.append("background-color=\"");
				str.append(WholeSystem.configTable.getString("backGroundcolorOriginalConcept").replace(".", ","));
				str.append("\" ");
			}
			if(simpleConcept.getNodeData().getAbstractAttribute() != null || simpleConcept.getNodeData().getCommentAttribute() != null) {
				str.append("border-thickness=\"");
				str.append(WholeSystem.configTable.getString("borderThicknessConceptWithHint"));
				str.append("\" ");
			}
			str.append("/>\r\n");
		}
		str.append("</concept-appearance-list>\r\n");

		
		// section: <connection-appearance-list>
		str.append("<connection-appearance-list>\r\n");
		for(int i=1; i <= countJ; i++) {
			str.append("\t<connection-appearance id=\"");
			str.append("j"+i);
			str.append("\" from-pos=\"center\" to-pos=\"center\" type=\"straight\" arrowhead=\"yes\"/>\r\n");
		}
		str.append("</connection-appearance-list>\r\n");
		
		str.append("</map>\r\n");
		str.append("</cmap>\r\n");		
	
		// save in file
		String cxlFileContent = str.toString();
		BufferedWriter outFile = new BufferedWriter(new FileWriter(fileClx)); 
		outFile.write(cxlFileContent);
		outFile.close();
		
		return cxlFileContent;
	}


	public String toStringComplete() {
		StringBuilder out = new StringBuilder();
		out.append("Total propositions: ");
		out.append(this.propositions.size());
		out.append("\n");
		int i =1;
		for(Proposition p : this.propositions) {
			out.append("\nProposition ");
			out.append(i);
			out.append(":\n");
			out.append(p.toStringComplete());
			out.append("\n");
			i++;
		}
		return out.toString();
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder();
		for(Proposition p : this.propositions) {
			out.append("   ");
			out.append(p.toString());
			out.append("\n");
		}
		return out.toString();
	}
}

