package main;

public class Count {
	int integer;
	public Count(int n) {
		this.integer = n;
	}
	public int getCount() {
		return this.integer;
	}
	public void setCount(int n) {
		this.integer = n;
	}
	public void incCount(int n) {
		this.integer += n;
	}
	public void incCount() {
		this.integer++;
	}
	public String toString() {
		return String.valueOf(this.integer);
	}
}
