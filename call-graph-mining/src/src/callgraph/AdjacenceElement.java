package callgraph;
import java.io.*;

/**
 * Instances of this class hold a {@link Signature} and corresponding
 * weights. It serves as class for the representation of the elements,
 * that succeed the key of a {@link AdjacenceList} in a call-graph. 
 * Basically it is a Signature and the corresponding weights.
 * @author Christopher Oßner
 * TODO a better solutions than using a single class for all hierarchy levels
 * would be inheriting from an interface
 */
public class AdjacenceElement implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Names of the annotations on package level
	 */
	public static final String[] PACKAGE_ANNOTATIONS = {"call-frequency",
		"called-classes", "called-methods", "calling-classes", "calling-methods"};

	/**
	 * Names of the annotations on class level
	 */
	public static final String[] CLASS_ANNOTATIONS = {"call-frequency",
	"called-methods", "calling-methods"};

	/**
	 * Names of the annotations on method level
	 */
	public static final String[] METHOD_ANNOTATIONS = {"call-frequency"};

	/**
	 * The signature we want to gather annotations for - end of
	 * an edge...
	 */
	private Signature sig;
	
	/** 
	 * Represents the call frequency in a CallGraph.
	 */
	private int weight;
	
	/**
	 * If it is a package signature, here the number of succeeding classes can be stored.
	 */
	private int noSuccClasses = 0;
	
	/**
	 * If it is a package or class signature, here the number of succeeding methods can be stored.
	 */
	private int noSuccMethods = 0;
	
	/**
	 * Number of preceding classes can be stored here
	 */
	private int noPreClasses = 0;
	
	/**
	 * Number of preceding methods can be stored here
	 */
	private int noPreMethods = 0;
	
	/**
	 * Initializes the element with a weight of 1.
	 * @param sig the signature to be added
	 */
	public AdjacenceElement(Signature sig){
		this.sig = sig;
		this.weight = 1;
	}
	
	/**
	 * Increases the weights for the held signature. 
	 */
	public void addCall(){
		weight++;
	}
	
	/**
	 * Set the call frequency to the specified value.
	 */
	public void setWeight(int x){
		weight = x;
	}
	
	/**
	 * Set number of succeeding classes to specified value
	 */
	public void setNoSuccClasses(int x){
		noSuccClasses = x;
	}
	
	/**
	 * Set number of succeeding classes to specified value
	 */
	public void setNoSuccMethods(int x){
		noSuccMethods = x;
	}
	
	/**
	 * Set number of preceding classes.
	 */
	public void setNoPreClasses(int x){
		noPreClasses = x;
	}
	
	/**
	 * Set number of preceding classes.
	 */
	public void setNoPreMethods(int x){
		noPreMethods = x;
	}
	
	/**
	 * @return the held signature
	 */
	public Signature getSignature(){
		return sig;
	}
	
	/**
	 * @return the call frequency corresponding to the signature
	 */
	public int getWeight(){
		return weight;
	}
	
	/**
	 * @return the annotation that tells how many different classes are called (within a package)
	 */
	public int getNoSuccClasses(){
		if(sig.getType() != "package")
			throw new UnsupportedOperationException("Only packages can have a called classes count");
		return noSuccClasses;
	}
	
	/**
	 * @return the annotation that tells how many different methods are called (within a package or class)
	 */
	public int getNoSuccMethods(){
		if(sig.getType() == "method")
			throw new UnsupportedOperationException("Methods can not have a called methods count");
		return noSuccMethods;
	}
	
	/**
	 * @return the annotation that tells from how many different classes (within a preceding package)
	 * this package is called 
	 */
	public int getNoPreClasses(){
		if(sig.getType() != "package")
			throw new UnsupportedOperationException("Only packages can have a calling classes count");
		return noPreClasses;
	}
	
	/**
	 * @return the annotation that tells from how many different classes (within a preceding package or class)
	 * this package is called 
	 */
	public int getNoPreMethods(){
		if(sig.getType() == "method")
			throw new UnsupportedOperationException("Methods can not have a calling methods count");
		return noPreMethods;
	}
	
	/**
	 * @return all available annotations. The meaning of the annotations is available through
	 * {@link #METHOD_ANNOTATIONS}, {@link #CLASS_ANNOTATIONS} and {@link #PACKAGE_ANNOTATIONS}.
	 */
	public int[] getAnnotations(){
		int[] out = null;
		String type = sig.getType();
		int outSc = 0, outSm = 0, outPc = 0, outPm = 0;
		if(type.equals("package") || type.equals("class") ){
			outSm = noSuccMethods;
			outPm = noPreMethods;
		}
		if(type.equals("package")){
			outSc = noSuccClasses;
			outPc = noPreClasses;
			out = new int[5];
			out[0] = weight; out[1] = outSc; out[2] = outSm; out[3] = outPc; out[4] = outPm;
		} else if(type.equals("class")){
			out = new int[3];
			out[0] = weight; out[1] = outSm; out[2] = outPm;
		} else{
			out = new int[1]; out[0] = weight;}
		return out;
	}

	
	public String toString(){
		return sig.toString() + "(" + weight + ")";
	}
	
	/**
	 * Returns true if the signatures are equal,
	 * even if the weight is not the same.
	 * @param ae
	 * @return true if the signatures are equal (their names)
	 */
	public boolean equals(AdjacenceElement ae){
		return ae.getSignature().equals(sig);
	}
	
	/**
	 * See {@link #equals(AdjacenceElement)}
	 * @param sig
	 * @return true if the signatures are equal (their names)
	 */
	public boolean equals(Signature sig){
		return sig.equals(this.sig);
	}
	
	/**
	 * Passing a {@link callgraph.AdjacenceElement} or a {@link Signature}
	 * will result in a call to corresponding equals method. All other
	 * objects cause a ClassCastException 
	 */
	public boolean equals(Object o){
		if( o instanceof AdjacenceElement)
			return this.equals( (AdjacenceElement) o );
		else if( o instanceof Signature )
			return this.equals( (Signature) o );
		else
			throw(new ClassCastException());
	}

}
