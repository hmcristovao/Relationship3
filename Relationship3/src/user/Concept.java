package user;

import parser.Token;
import main.*;

public class Concept {
	private String        fullName;  // with address and underline
	private String        blankName;
	private String        underlineName; // with underline
	private ConceptStatus conceptStatus;
	private int           iteration;
	private ConceptCategory  	  conceptCategory;
	private int           quantityRdfs;
	private int           registerConnectedComponent[];  // indexed by iteration number

	public enum ConceptStatus {commonConcept, originalConcept, selected, selectedBetweennessClosenessConcept, selectedEigenvectorConcept, noStatus};
	public enum ConceptCategory {no, yes, was};
	
	public Concept(String fullName, String blankName, ConceptStatus conceptStatus, int iteration, ConceptCategory conceptCategory, int quantityRdfs, int connectedComponent) {
		this.fullName                   = fullName;
		this.blankName                  = blankName.trim();
		this.underlineName              = Concept.blankToUnderline(this.blankName);
		this.conceptStatus              = conceptStatus;
		this.iteration                  = iteration;
		this.conceptCategory            = (Concept.verifyIfCategory(this.blankName)==true) ? ConceptCategory.yes : ConceptCategory.no;
		this.quantityRdfs               = quantityRdfs;
		this.registerConnectedComponent = new int[WholeSystem.configTable.getInt("maxIteration")];
		for(int i=0; i<WholeSystem.configTable.getInt("maxIteration"); i++)
			this.registerConnectedComponent[i] = Constants.withoutConnectedComponent;
		this.registerConnectedComponent[iteration] = connectedComponent;
	}
	public Concept(Token token) {
		// it is before the first iteration
		
		this(Constants.originalConceptAddress+Concept.blankToUnderline(token.image), token.image, ConceptStatus.originalConcept, 0, ConceptCategory.no, 0, Constants.withoutConnectedComponent);  
	}
		
	public String getFullName() {
		return this.fullName;
	}
	public String getBlankName() {
		return this.blankName;
	}
	public ConceptStatus getStatus() {
		return this.conceptStatus;
	}
	public int getIteration() {
		return this.iteration;
	}
	public boolean isOriginal() {
		return this.conceptStatus == ConceptStatus.originalConcept;
	}
	public String getUnderlineConcept() {
		return this.underlineName;
	}
	public ConceptCategory getCategory() {
		return this.conceptCategory;
	}
	public static String categoryToString(ConceptCategory conceptCategory) {
		if(conceptCategory == ConceptCategory.no)
			return "no";
		else if(conceptCategory == ConceptCategory.yes)
			return "yes";
		else if(conceptCategory == ConceptCategory.was)
			return "was";
		else
			return "error";
	}
	public String strCategory() {
		return Concept.categoryToString(this.conceptCategory);
	}
	public int getQuantityRdfs() {
		return this.quantityRdfs;
	}
	public void setQuantityRdfs(int n) {
		this.quantityRdfs = n;
	}
	
	public int[] getRegisterNodeData() {
		return this.registerConnectedComponent;
	}
	public int getNodeData(int i) {
		return this.registerConnectedComponent[i];
	}
	public int getConnectedComponent(int iteration) {
		return this.registerConnectedComponent[iteration];
	}

	
	public static String blankToUnderline(String str) {
		return str.trim().replace(" ","_");
	}
	
	public static String underlineToBlank(String str) {
		return str.replace("_"," ");
	}
	
	// verify if exist "Category:" as preceded of the concept
	public static boolean verifyIfCategory(String str) {
		if(str.length() > 9)
			if(str.substring(0, 9).compareTo("Category:") == 0)
				return true;
		return false;
	}
	public static String extractCategory(String shortName) {
		return shortName.substring(9);
	}
	public static String extractCategoryFullName(String fullName) {
		int posCategory    = fullName.indexOf("Category:");
		if(posCategory != -1) {
			String onlyAddress = fullName.substring(0, posCategory);
		    String onlyName    = fullName.substring(posCategory+9); 
		    return onlyAddress + onlyName;
		}
		return fullName;
	}

	public static String statusToString(ConceptStatus conceptStatus) {
		if(conceptStatus == ConceptStatus.commonConcept)
			return "common";
		else if(conceptStatus == ConceptStatus.originalConcept)
			return "original";
		else if(conceptStatus == ConceptStatus.selectedBetweennessClosenessConcept)
			return "betweenness+closeness";
		else if(conceptStatus == ConceptStatus.selectedEigenvectorConcept)
			return "eigenvector";
		else if(conceptStatus == ConceptStatus.selected)
			return "selected";
		else 
			return "";
	}
	public static ConceptStatus stringToStatus(String str) {
		if(str.equals("common"))
			return ConceptStatus.commonConcept;
		else if(str.equals("original"))
			return ConceptStatus.originalConcept;
		else if(str.equals("betweenness+closeness"))
			return ConceptStatus.selectedBetweennessClosenessConcept;
		else if(str.equals("eigenvector"))
			return ConceptStatus.commonConcept;
		else if(str.equals("selected"))
			return ConceptStatus.selected;
		else 
			return ConceptStatus.noStatus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Concept))
			return false;
		Concept other = (Concept) obj;
		if (this.fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!this.fullName.equals(other.fullName))
			return false;
		return true;
	}

	public String toStringShort() {
		return this.blankName +
	           " - "  + Concept.statusToString(this.conceptStatus) +
	           " (it: " + this.iteration + ")";
	}
	public String toStringLong() {
		String out = "[fullName: "  + this.fullName +    "]" +
					 "[blankName: " + this.blankName +    "]" +
					 "[underline: " + this.underlineName +"]" + 
				     "[category: "  + this.strCategory() + "]" +
				     "[rdf quantity: "+ this.quantityRdfs + "]" +
					 "[conceptStatus: "    + Concept.statusToString(this.conceptStatus) + "]" +
				     "[iteration: " + this.iteration + "]" +
					 "[connected components: ";
		for(int i=0; i<this.registerConnectedComponent.length; i++) 
			out += this.registerConnectedComponent[i] + " ";
		out += "]\n";
		return out;
	}
	@Override
	public String toString() {
		String out = this.underlineName + 
				     " / "  + this.blankName +
		             " - "  + Concept.statusToString(this.conceptStatus) +
		             " (i: " + this.iteration + ")";
		if(this.conceptCategory == ConceptCategory.yes || this.conceptCategory == ConceptCategory.was)
			out += " - (category: " + this.strCategory() + ")";
		return out;
	}
}
