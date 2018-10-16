package main;
import parser.*;

// herda de ParseExpection para aproveitar o throws colocado automaticamente em todos os métodos gerados pelo Javacc
@SuppressWarnings("serial")
public class SemanticException extends ParseException {
	public SemanticException(String msg) {
		super(msg);
	}  
}
