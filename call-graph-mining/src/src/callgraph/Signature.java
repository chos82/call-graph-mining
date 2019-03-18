package callgraph;

import java.io.*;

import org.aspectj.lang.reflect.CodeSignature;

/**
 * Is the representation of the signatures to work with. To facilitate
 * graphs that trade with specific classes or methods, signatures can be
 * dummy signatures. All call to other classes or methods (those which are
 * not specified to be of special interest, what is done in the {@link GraphConverter})
 * may be modeled as dummy signatures. 
 * @author Christopher Oßner
 * TODO There really should be an Signature interface and some specific Signatures implementing it! 
 */
public class Signature implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * String representation of the signature.
	 */
	private String sig;
	
	/**
	 * 'package', 'class' and 'method' are valid types
	 */
	private String type;
	
	/**
	 * True if we got a dummy signature.
	 */
	private boolean dummy;
	
	/**
	 * 
	 * @return true if the signature has a name that starts
	 * with a prefix that belongs to the JRE(v1.4)
	 */
	public boolean isJre(){
		return(  this.sig.startsWith("java") ||
				 this.sig.startsWith("javax") ||
				 this.sig.startsWith("org.ietf") ||
				 this.sig.startsWith("org.omg") ||
				 this.sig.startsWith("org.w3c") ||
				 this.sig.startsWith("org.xml") ||
				 this.sig.toLowerCase().startsWith("jre"));
	}
	
	/**
	 * Produces a dummy signature.
	 * @param type of the wanted dummy ('package', 'class' or 'method')
	 */
	public Signature(String type, String name, boolean dummy){
		checkType(type);
		this.type = type;
		this.sig = name;
		this.dummy = true;
	}
	
	/**
	 * Initializes a usual signature.
	 * @param type
	 * @param sig
	 */
	public Signature(String type, String sig){
		checkType(type);
		this.type = type;
		this.sig = sig;
		this.dummy = false;
	}
	
	/**
	 * Initializes an instance from a {@link org.aspectj.lang.Signature}. Useful
	 * especially if called within aspects.
	 * @param sig
	 */
	public Signature(org.aspectj.lang.Signature sig){
		this.type = "method";
		this.sig = sig.getDeclaringType().getName() + "." + sig.getName() + "(";
		Class[] parameters = ( (CodeSignature) sig ).getParameterTypes();
		String typeName;
		for( int i = 0; i < parameters.length; i++ ){
			typeName = parameters[i].getName();
			if(parameters[i].isArray()){
				this.sig += parameters[i].getComponentType().getName();
				while( typeName.startsWith("[") ){
					typeName = typeName.substring(1);
					this.sig += "[]";
				}
			} else{
				this.sig += parameters[i].getName();
			}
			if( i < parameters.length -1 )
				this.sig += ",";
		}
		this.sig += ")";
		this.dummy = false;
	}
	
	/**
	 * Copy constructor
	 */
	public Signature( Signature sig ){
		this.type = sig.getType();
		this.dummy = sig.isDummy();
		this.sig = sig.getSignature();
	}
	
	/**
	 * Throws the exception, if the passed argument is not within
	 * {'package', 'class', 'method'}
	 * @param type
	 */
	private void checkType(String type){
		if(type.equals("package") ||
		   type.equals("class") ||
		   type.equals("method"))
			;
		else
			throw(new IllegalArgumentException("'"+type+"' is no valid Signature.type!"));
	}
	
	/**
	 * 
	 * @return {@link #type}
	 */
	public String getType(){
		return type;
	}
	
	/**
	 * 
	 * @return {@link #sig}
	 */
	public String getSignature(){
		return sig;
	}
	
	/**
	 * @return true if the instance is a dummy
	 */
	public boolean isDummy(){
		return dummy;
	}
	
	/**
	 * 
	 * @return true if signature appears to be a constructor (contains the substring {@code <init>},
	 * false otherwise
	 */
	public boolean isConstructor(){
		if(this.sig.indexOf("<init>") != -1)
			return true;
		return false;
	}
	
	public String toString(){
		String out = "";
		if(this.dummy)
			out += "dummy-";
		out += type + ": " + sig;
		return out;
	}
	
	/**
	 * @param sig to compare with
	 * @return true if {@link #sig} is equal, false otherwise
	 */
	public boolean equals(Signature sig){
		return (sig.getSignature().equals(this.sig));
	}
	
	/**
	 * @param sig to compare with
	 * @return true if {@link #sig} is equal, false otherwise
	 */
	public boolean equals(String sig){
		return sig.equals(this.sig);
	}
	
	/* Not so easy ;)
	 * public boolean equals(org.aspectj.lang.Signature sig){
		if(this.sig == sig.getDeclaringType().getName() + "." + sig.getName())
			return true;
		else
			return false;
	}*/
	
	/**
	 * @param ae to compare with
	 * @return true if {@link #sig} is equal, false otherwise
	 */
	public boolean equals(AdjacenceElement ae){
		return this.equals(ae.getSignature());
	}
	
	/**
	 * Calls the corresponding equals method, if the passed argument
	 * has one. Exception is thrown otherwise.
	 * @param o object to compare with 
	 */
	public boolean equals(Object o){
		if(o instanceof String)
			return( this.equals( (String) o ) );
		else if(o instanceof Signature)
			return( this.equals( (Signature) o ) );
		else if(o instanceof AdjacenceElement)
			return( this.equals( (AdjacenceElement) o ) );
		else 
			throw(new IllegalArgumentException("Can not compare to '"+o.toString()+"'."));
	}
	
	public int hashCode(){
		return sig.hashCode();
	}
	
	/**
	 * Convert the signature to package level, by simply removing the last
	 * part of {@link #sig} and adjusting {@link #type}.
	 * @return the instance on package level or <code>this</code> if the instance was
	 * on package level before
	 */
	public Signature toPackage(){
		String sig = this.sig, type = this.type;
		if(type.equals("method")){
			sig = sig.substring(0, sig.indexOf("("));
			sig = sig.substring(0, sig.lastIndexOf("."));
			type = "class";}
		if(type.equals("class")){
			sig = sig.substring(0, sig.lastIndexOf("."));
			type = "package";}
		return new Signature(type, sig);
	}
	
	/**
	 * Same as {@link #toPackage()} but converts to a signature on class level.
	 * @return the signature converted to class level
	 */
	public Signature toClass(){
		String sig = this.sig, type = this.type;
		if(type.equals("method")){
			sig = sig.substring(0, sig.indexOf("("));
			sig = sig.substring(0, sig.lastIndexOf("."));
			type = "class";
		}
		if(type.equals("package"))
			throw(new UnsupportedOperationException("'" +
						this.toString() + 
						"' can not be converted into a class Signature"));
		return new Signature(type, sig);
	}

}
