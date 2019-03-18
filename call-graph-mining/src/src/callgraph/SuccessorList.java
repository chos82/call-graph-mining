package callgraph;

/**
 * This structure is used to hold the vertices, that succeed another vertex
 * within {@link AdjacenceList}.
 * @author Christopher Oßner
 *
 */
public class SuccessorList extends ArrayListSet {

	private static final long serialVersionUID = 1L;
	
	public SuccessorList(){
		super();
	}
	
	/**
	 * Increments the call frequency of the corresponding {@link AdjacenceElement} 
	 * for the passed signature, or adds a new element, if it is the first call
	 * to the passed signature.
	 * @param sig the signature which is called
	 */
	public void addCall(Signature sig){
		int index = this.indexOf( sig );
		if( index != -1 ){
			AdjacenceElement ae = getElement(index);
			ae.addCall();
			set(index, ae);
		}
		else{
			AdjacenceElement elem = new AdjacenceElement(sig);
			this.addUnchecked(elem);			
		}
	}
	
	/**
	 * Useful during conversion.
	 * @param sig successor in a graph
	 * @param weight the successors call-frequency
	 */
	public void addElement(Signature sig, int weight){
		int index = this.indexOf( sig );
		if( index != -1 ){
			AdjacenceElement ae = getElement(index);
			int oldWeight = ae.getWeight();
			ae.setWeight(weight + oldWeight);
			set(index, ae);
		}
		else{
			AdjacenceElement elem = new AdjacenceElement(sig);
			elem.setWeight(weight);
			this.addUnchecked(elem);			
		}
	}
	
	/**
	 * Convenience method - does a type cast
	 * @param i index of the Element, that is wanted
	 * @return the AdjacenceElement at the specified position
	 */
	public AdjacenceElement getElement(int i){
		return (AdjacenceElement) get(i);
	}
	
	public String toString(){
		String out = "";
		for(int i = 0; i < size(); i++){
			out += "\t" + getElement(i).toString()+"\n";
		}
		return out;
	}
	
}
