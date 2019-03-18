package bl.postprocessor;

import callgraph.*;

/**
 * Very simple class to represent an edge.
 * @author Christopher Oßner
 *
 */
public class Edge{
	
	private Signature start;
	
	private Signature end;
	
	/**
	 * @param start fully qualified signature of the start (the string used as label in the LGF)
	 * @param end fully qualified signature of the end (the string used as label in the LGF)
	 */
	Edge(String start, String end){
		String startType = detectSignatureType(start);
		String endType = detectSignatureType(end);
		if( ! startType.equals(endType) ){
			throw new RuntimeException("Type detection failed! compared: \n"+start+" -> "+end);
		}
		this.start = new Signature( startType, start);
		this.end = new Signature( endType, end);
	}
	
	Edge(Signature start, Signature end){
		this.start = new Signature(start);
		this.end = new Signature(end);
	}
	
	/**
	 * @return the fully qualified signature of the start vertex
	 */
	public Signature getStart(){return start;}
	
	/**
	 * @return the fully qualified signature of the end vertex
	 */
	public Signature getEnd(){return end;}
	
	/**
	 * @return describing the annotations
	 */
	public String[] getAnnotationNames(){
		String type = start.getType();
		if(type.equals("package"))
			return AdjacenceElement.PACKAGE_ANNOTATIONS;
		else if(type.equals("class"))
			return AdjacenceElement.CLASS_ANNOTATIONS;
		else if(type.equals("method"))
			return AdjacenceElement.METHOD_ANNOTATIONS;
		else
			throw new RuntimeException("Signatures type is not valid.");
	}
	
	public String toString(){
		return start.getSignature() + "->" + end.getSignature();
	}
	
	/**
	 * Detects the type {package, class, method} for the passed argument.
	 * @param sig the signature for which to detect the type
	 * @return the type
	 */
	public static String detectSignatureType(String sig){
		if( sig.matches(".*\\(.*\\).*") )
			return "method";
		else if( sig.matches(".*\\.[A-Z].*") )
			return "class";
		else
			return "package";
	}
	
}