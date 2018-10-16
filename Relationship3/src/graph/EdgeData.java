// It is one raw of the table of edges (EdgesTableArray)
package graph;

public class EdgeData {
	private String idEdge;  // is the same short name with space blank   
	int repeatedTimes;   // used in the case: same subject, predicate and object
	int number;  // used in the cases: (1)same subject and predicate, (2)same predicate and object
	
	public EdgeData(String idEdge) {
		this.idEdge = idEdge;
		this.repeatedTimes  = 0;
		this.number = 0;
	}
	public String getIdEdge() {
		return this.idEdge;
	}
	public int getTimes() {
		return this.repeatedTimes;
	}
	public int getNumber() {
		return this.number;
	}
	public int incTimes() {
		this.repeatedTimes++;
		return this.repeatedTimes;
	}
	private void incNumber() {
		this.number++;
	}
	public String getNumberAndIncrement() {
		// format to 3 digits, or more if number > 999
		String formatedNumber = this.number<10 ? "00" : (this.number<100 ? "0" : "");
		formatedNumber += String.valueOf(this.number);
		this.incNumber();
		return formatedNumber;
	}
	public String getNameAndIncrement() {
		return this.idEdge+"#"+this.getNumberAndIncrement();
	}
	
	@Override
	public String toString() {
		return  "[edge id: " + this.idEdge +
				"][times: " + this.repeatedTimes +
				"][current number: " + this.number + "]";
	}
}
