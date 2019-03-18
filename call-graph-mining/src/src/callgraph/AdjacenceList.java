package callgraph;

import java.util.*;

/**
 * This extended HashMap is used to model the representation of the total-reduced
 * call-graphs (documentation of total reduction, see Eichinger et al.). As the name
 * suggests, its a adjacency list.
 * The key of each entry is the start of a vertex, the value contains all
 * its successors (once).
 * A HashMap is used, although it is not order preserving, since its contains
 * method is much more effective (and the set behavior is inherent), 
 * than the one of a {@link ArrayList}. Since with each
 * method invocation of the instrumented program, a adding of a call is 
 * needed, a the efficient {@link HashMap#put} is useful. A ordering has to be achieved
 * again, to write the graph (e.g. {@link GraphWriter#writeR_w_total2lg(boolean, boolean)}), 
 * but that is done only once per test.
 * The successors of each vertex (key) are modeled as {@link SuccessorList}, since a get method
 * is useful in this case. The less effective contains method causes no major speed-down.
 * @author Christopher Oßner
 *
 */
public class AdjacenceList extends HashMap{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Since for most total reduced call-graphs (on method level), in Rhino, 256 seems
	 * to be the closest 2^n value, this is a good value to initialize the structures
	 * initial capacity.
	 */
	private static final int INIT_CAP = 256;
	
	/**
	 * Initialize the inner HashMap
	 * @param initialCapacy capacity for the HashMap.
	 */
	public AdjacenceList(int initialCapacy){
		super(initialCapacy);
		if(initialCapacy < 0)
			throw new IllegalArgumentException("Illegal Capacy* " +
											   initialCapacy);
	}
	
	/**
	 * Initialize the inner HashMap with {@link #INIT_CAP}.
	 */
	public AdjacenceList(){
		this(INIT_CAP);
	}
	
	/**
	 * The method signatures, which are available within AspectJ`s advises 
	 * (thisJoinPointStaticPart) can be passed to this method. If the start vertex had no successors till now
	 * a new {@link SuccessorList} is added, otherwise the existing one is used, the call is added via
	 * {@link SuccessorList#addCall(Signature)} and the result is passed to {@link HashMap#put}.
	 * @param caller the calling methods`s signature
	 * @param callee the called methods`s signature
	 */
	public void addCall(org.aspectj.lang.Signature caller, org.aspectj.lang.Signature callee){
		Signature ce = new Signature(callee);
		Signature cr = new Signature(caller);
		SuccessorList succ = getSuccessors(cr);
		SuccessorList list = succ == null ? new SuccessorList() : succ;
		list.addCall(ce);
		put(cr, list);
	}
	
	/**
	 * Same as {@link #addCall(Signature, Signature)}, but works with {@link Signature}
	 * @param caller the calling methods`s signature
	 * @param callee the called methods`s signature
	 */
	public void addCall(Signature caller, Signature callee){
		//Signature cr = new Signature(caller.getType(), caller.getSignature());
		SuccessorList succ = getSuccessors(caller);
		succ = succ == null ? new SuccessorList() : succ;
		succ.addCall(callee);
		put(caller, succ);
	}
	
	/**
	 * Used to add called methods during the converting process ({@link GraphConverter}).
	 * During test runs called methods are available always pairwise (caller, callee). So this 
	 * method is not needed there.
	 * @param callee a called method (a successor within another AdjacenceList or similar structure)
	 */
	/*public void addCallee(Signature callee){
		//Signature cr = new Signature(callee.getType(), callee.getSignature());
		put(callee, new SuccessorList());
	}*/
	
	/**
	 * If we have to addEdges (a call with a weight different than one)
	 * This method is used. It calls {@link SuccessorList#addElement(Signature, int)}.
	 */
	public void addEdge(Signature caller, Signature callee, int x){
		SuccessorList succ = getSuccessors(caller);
		succ = succ == null ? new SuccessorList() : succ;
		succ.addElement(callee, x);
		put(caller, succ);
	}
	
	/**
	 * @param key for which the succeeding vertices are wanted
	 * @return successors for key
	 */
	public SuccessorList getSuccessors(Object key){
		return (SuccessorList) get(key);
	}
	
	/**
	 * Looks up the annotations for a given edge.
	 * @param start start vertex of the wanted edge
	 * @param end end vertex of the wanted edge
	 * @return the annotations by calling {@link AdjacenceElement#getAnnotations()}
	 */
	public int[] getAnnotations(Signature start, Signature end){
		SuccessorList succ = getSuccessors(start);
		RuntimeException exp = new RuntimeException("Wanted to look up annotations for '" + 
				start.getSignature() + " -> " + end.getSignature() + "', but that "+
				"edge is not present in the graph!");
		if(succ == null){
			System.out.println(toString());
			throw exp;}
		int ind = succ.indexOf(end);
		if(ind == -1){
			System.out.println(toString());
			throw exp;}
		return succ.getElement(ind).getAnnotations();
	}
	
	/**
	 * @return an {@link Iterator} over the entries in the Map
	 */
	public Iterator iterator()   {
        return entrySet().iterator();
    }
	
	/**
	 * @return an {@link Iterator} over the keys only
	 */
	public Iterator keyIterator(){
		return keySet().iterator();
	}
	
	public String toString(){
		String out = "";
		for(Iterator iter = iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry) iter.next();
			out += entry.getKey() + ":\n";
			SuccessorList succ = (SuccessorList) entry.getValue();
			for(int i = 0; i < succ.size(); i++){
				out += "\t" + succ.get(i).toString() +"\n";
			}
		}
		return out;
	}

}
