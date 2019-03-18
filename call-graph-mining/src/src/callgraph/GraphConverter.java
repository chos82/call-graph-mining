package callgraph;

import java.util.*;

/**
 * Converts graphs to different hierarchy levels (method, class or package).
 * And calculates annotations.
 *
 * @author Christopher Oßner
 * TODO should be moved to bl.tools as java1.6 can be used there, we really would
 * benefit from generic classes. No need to have this class here (it is not used during the
 * trace). Maybe different converters for the different hierarchy levels...
 */
public class GraphConverter {
	
	/**
	 * The graph to be converted.
	 */
	private AdjacenceList input;
	
	/**
	 * @param in the graph to be converted
	 */
	public GraphConverter(AdjacenceList in){
		input = in;
	}
	
	/**
	 * If only specific vertices wanted they can be placed here.
	 * Used in methods2class.
	 */
	private HashSet verticesIncluded = new HashSet();
	
	/**
	 * Set true if signatures, belonging to a constructor should be left out.
	 */
	private boolean omitConstructors = false;
	
	/**
	 * Set true if not all vertices should be considered.
	 * verticesIncluded must be set in that case.
	 * Used in methods2class.
	 */
	private boolean sampleVertices = false;
	
	/**
	 * Set the signatures of classes to include. Those not within the passed
	 * set, will become a dummy-signature 'ForeignClass' when the converter is
	 * executed.
	 * @param include set of signatures to include
	 */
	public void includeVertices(HashSet include){
		verticesIncluded = include;
		sampleVertices = true;
	}
	
	public void omitConstructors(){
		this.omitConstructors = true;
	}
	
	/**
	 * Converts graph objects to a cutout on method level. Methods not within 
	 * the classes passed, will be represented as dummies.
	 * @param classes classes to consider
	 * @return the graph hold in {@link #input} but methods not with in the
	 * argument are replaced by dummies
	 */
	public AdjacenceList methods(HashSet classes){
		AdjacenceList out = new AdjacenceList();
		Signature dummy = new Signature("method", "foreign.Class.method(..)", true);
		Signature jreDummy = new Signature("method", "jre.Class.method(..)", true);
		for(Iterator iter = input.iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry) iter.next();
			SuccessorList succ = (SuccessorList) entry.getValue();
			Signature sig = ( (Signature) entry.getKey() );
			if(omitConstructors && sig.isConstructor()) continue;
			if(  ! classes.contains( sig.toClass() )  ) sig = dummy;
				
			for(int i = 0; i < succ.size(); i++){
				AdjacenceElement ae = (AdjacenceElement) succ.get(i);
				Signature succsig = ae.getSignature();
				if(omitConstructors && succsig.isConstructor()) continue;
				int weight = ae.getWeight();
				if(succsig.isJre())
					succsig = jreDummy;
				else if(  ! classes.contains( succsig.toClass() )  )
					succsig = dummy;
				out.addEdge(sig, succsig, weight);
			}
			
		} return out;
	}
	
	/**
	 * Converts to a graph on class level and calculates the annotations.
	 * (called-methods, calling-methods). Classes in packages not in the
	 * passed set will become a dummy-signature 'ForeignPackage'. We do
	 * not deal with the information, that inner and anonymous classes
	 * are part of the encapsulating class. So {@code SomeClass} and
	 * {@code SomeClass$InnerClass} are different signatures.
	 * @see {@link #verticesIncluded}
	 * @param packnames
	 * @return {@link #input} converted to a graph on class level
	 */
	public AdjacenceList methods2class(HashSet packnames){
		AdjacenceList out = new AdjacenceList();
		//maps each class to succeeding methods
		HashMap toMeth = new HashMap();
		//maps each method to classes that call it
		HashMap fromMeth = new HashMap();
		Signature dummy = new Signature("class", "foreign.Package", true);
		Signature dummyClass = new Signature("class", "foreign.Class", true);
		Signature jreDummy = new Signature("class", "jre.Class", true);
		for(Iterator iter = input.iterator(); iter.hasNext(); ){
			//the methods succeeding the current method
			HashSet toSuccMeth = new HashSet();
			//the classes preceding current method
			HashSet fromMethClasses = new HashSet();
			Map.Entry entry = (Map.Entry) iter.next();
			SuccessorList succ = (SuccessorList) entry.getValue();
			Signature sigMeth = ( (Signature) entry.getKey() );
			if(omitConstructors && sigMeth.isConstructor()) continue;
			Signature sigPackage = sigMeth.toPackage();
			Signature sig = sigMeth.toClass();
			if(packnames.contains(sigPackage)){
				if(sampleVertices){
					// current signature is not in considered classes
					if(! verticesIncluded.contains(sig)){
						sig = dummyClass;
						sigMeth = new Signature("method", sigMeth.getSignature(), true);
					}
				}
			} else{
				sig = dummy;
				sigMeth = new Signature("method", sigMeth.getSignature(), true);
			}
			if( toMeth.containsKey(sig) )
				toSuccMeth = (HashSet) toMeth.get(sig);
				
			for(int i = 0; i < succ.size(); i++){
				AdjacenceElement ae = (AdjacenceElement) succ.get(i);
				Signature succsig = ae.getSignature();
				if(omitConstructors && succsig.isConstructor()) continue;
				int weight = ae.getWeight();
				Signature succMeth = new Signature(succsig);
				succsig = succsig.toClass();
				if(succsig.isJre()) {
					succsig = jreDummy;
					succMeth = new Signature("method", succMeth.getSignature(), true);
				} else if(packnames.contains(succsig.toPackage())){
					if(sampleVertices){
						// current signature is not in considered classes
						if(! verticesIncluded.contains(succsig)){
							succsig = dummyClass;
							succMeth = new Signature("method", succMeth.getSignature(), true);
						}
					}
				} else{
					succsig = dummy;
					succMeth = new Signature("method", succMeth.getSignature(), true);
				}
				toSuccMeth.add(succMeth);
				fromMethClasses.add( new Signature(succsig) );
				out.addEdge(sig, succsig, weight);
			}
			toMeth.put(sig, toSuccMeth);
			fromMeth.put(sigMeth, fromMethClasses);
		}
		out = calculateClassAnnotations( out, toMeth, fromMeth, packnames );
		return out;
	}
	
	/**
	 * Converts the graph on method level in {@link #input} to a graph on
	 * package level. 
	 * @return the converted graph
	 */
	public AdjacenceList methods2Package(){
		AdjacenceList out = new AdjacenceList();
		// called methods
		HashMap toMeth = new HashMap();
		// called classes
		HashMap toClass = new HashMap();
		// calling methods
		HashMap fromMeth = new HashMap();
		// calling classes
		HashMap fromClass = new HashMap();
		Signature jreDummy = new Signature("package", "jre", true);
		for(Iterator iter = input.iterator(); iter.hasNext(); ){
			// methods called by currently analyzed method
			HashSet toSuccMeth = new HashSet();
			HashSet toSuccClass = new HashSet();
			// classes that currently analyzed method calls
			HashSet fromClassPackages = new HashSet();
			HashSet fromMethPackages = new HashSet();
			Map.Entry entry = (Map.Entry) iter.next();
			SuccessorList succ = (SuccessorList) entry.getValue();
			Signature sigMeth = ( (Signature) entry.getKey() );
			if(omitConstructors && sigMeth.isConstructor()) continue;
			Signature sigClass = sigMeth.toClass();
			if( fromClass.containsKey(sigClass) )
				fromClassPackages = (HashSet) fromClass.get(sigClass);
			Signature sig = sigMeth.toPackage();
			if( toMeth.containsKey(sig) )
				toSuccMeth = (HashSet) toMeth.get(sig);
			if( toClass.containsKey(sig) )
				toSuccClass = (HashSet) toClass.get(sig);
				
			for(int i = 0; i < succ.size(); i++){
				AdjacenceElement ae = (AdjacenceElement) succ.get(i);
				Signature succsig = ae.getSignature();
				if(omitConstructors && succsig.isConstructor()) continue;
				int weight = ae.getWeight();
				Signature succMeth = new Signature(succsig);
				Signature succClass = new Signature(succsig.toClass());
				if(succsig.isJre()) {
					succsig = jreDummy;
					succMeth = new Signature("method", succMeth.getSignature(), true);
					succClass = new Signature("class", succClass.getSignature(), true);
				} else
					succsig = succsig.toPackage();
				toSuccMeth.add(succMeth);
				toSuccClass.add(succClass);
				fromClassPackages.add( new Signature(succsig) );
				fromMethPackages.add( new Signature(succsig) );
				out.addEdge(sig, succsig, weight);
			}
			
			toMeth.put(sig, toSuccMeth);
			toClass.put(sig, toSuccClass);
			fromMeth.put(sigMeth, fromMethPackages);
			fromClass.put(sigClass, fromClassPackages);
		}
		out = calculateAnnotations( out, toMeth, toClass, fromMeth, fromClass );
		return out;
	}
	
	/**
	 * Calculates annotations for the adjacency list passed as first argument. Keep in mind
	 * that calls to a method implemented by an anonymous class and the instantiation
	 * of such are calls to two different classes. E.g. {@code new Comparator<..>(){
	 * private void meth(){;}};} within 
	 * a class {@code Enc} will cause a signature like {@code Enc$1<init>()}, while a call to
	 * the implemented method {@code meth()} will cause a signature {@code Enc$Comparator.meth()}.
	 * @param al graph to calculate annotations for
	 * @param toMeth map of package -> {<all methods this package calls>}, needed for called-methods
	 * @param toClass map of package -> {<all Classes this package calls>}, needed for called-classes
	 * @param fromMeth map of method -> {<all packages this method calls>}, needed for calling-methods
	 * @param fromClass map of class -> {<all packages this method calls>}, needed for calling-classes
	 * @return al, with annotations
	 */
	private AdjacenceList calculateAnnotations( AdjacenceList al, HashMap toMeth, HashMap toClass,
												HashMap fromMeth, HashMap fromClass ){
		for(Iterator iter = al.iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry) iter.next();
			SuccessorList succ = (SuccessorList) entry.getValue();
			Signature sig = ( (Signature) entry.getKey() );
			HashSet succMeth = (HashSet) toMeth.get(sig);
			HashSet succClass = (HashSet) toClass.get(sig);
			SuccessorList newSucc = new SuccessorList();
			for(Iterator succIter = succ.iterator(); succIter.hasNext();){
				AdjacenceElement element = (AdjacenceElement) succIter.next();
				Signature succPackage = element.getSignature();
				int noSuccMeth = 0;
				// called methods
				for(Iterator succMethIter = succMeth.iterator(); succMethIter.hasNext();){
					Signature meth = (Signature) succMethIter.next();
					/* we have a called method, if we look at a 
					 * method that is in the same package as the 
					 * currently analyzed package of the input graph
					 * or if we have got a call to a dummy */ 
					if(meth.toPackage().equals(succPackage) ||
					  ( succPackage.isDummy() && meth.isDummy() )  )
						noSuccMeth++;
				}
				// called classes
				int noSuccClasses = 0;
				for(Iterator succClassIter = succClass.iterator(); succClassIter.hasNext();){
					Signature classSig = (Signature) succClassIter.next();
					if(classSig.toPackage().equals(succPackage) ||
					  ( succPackage.isDummy() && classSig.isDummy() ))
						noSuccClasses++;
				}
				HashSet succPackages;
				int noPreMethods = 0;
				Signature sigMeth;
				// calling methods
				for(Iterator fromMethIter = fromMeth.entrySet().iterator(); fromMethIter.hasNext(); ){
					Map.Entry tme = (Map.Entry) fromMethIter.next();
					succPackages = (HashSet) tme.getValue();
					sigMeth = ( (Signature) tme.getKey() );
					for( Iterator succPackagesIter = succPackages.iterator(); succPackagesIter.hasNext(); ){
						Signature currentPackage = (Signature) succPackagesIter.next();
						if( currentPackage.equals(succPackage) && sigMeth.toPackage().equals(sig) )
							noPreMethods++;
					}
				}
				// calling classes
				int noPreClasses = 0;
				Signature sigClass;
				for(Iterator fromClassIter = fromClass.entrySet().iterator(); fromClassIter.hasNext(); ){
					Map.Entry tce = (Map.Entry) fromClassIter.next();
					succPackages = (HashSet) tce.getValue();
					sigClass = ( (Signature) tce.getKey() );
					for( Iterator succPackagesIter = succPackages.iterator(); succPackagesIter.hasNext(); ){
						Signature currentPackage = (Signature) succPackagesIter.next();
						if( currentPackage.equals(succPackage) && sigClass.toPackage().equals(sig) )
							noPreClasses++;
					}
				}
				element.setNoSuccClasses(noSuccClasses);
				element.setNoSuccMethods(noSuccMeth);
				element.setNoPreClasses(noPreClasses);
				element.setNoPreMethods(noPreMethods);
				newSucc.add(element);
			}
			al.put(sig, newSucc);
		}
		return al;
	}
	
	/**
	 * Calculates annotations for the adjacency list passed as first argument.
	 * @param al
	 * @param toMeth toMeth map of package -> {<all methods this package calls>}, needed for called-methods
	 * @param fromMeth map of method -> {<all packages this method calls>}, needed for calling-methods
	 * @param packnames packages to considered, used to look up if we deal with a 'ForeignPackage' method (not
	 * very clean implementation - caused by bad design of Signature)
	 * @return al with annotations
	 */
	private AdjacenceList calculateClassAnnotations(AdjacenceList al,
			HashMap toMeth, HashMap fromMeth, HashSet packnames) {
		for(Iterator iter = al.iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry) iter.next();
			SuccessorList succ = (SuccessorList) entry.getValue();
			Signature sig = ( (Signature) entry.getKey() );
			HashSet succMeth = (HashSet) toMeth.get(sig);
			SuccessorList newSucc = new SuccessorList();
			for(Iterator succIter = succ.iterator(); succIter.hasNext();){
				AdjacenceElement element = (AdjacenceElement) succIter.next();
				Signature succClass = element.getSignature();
				int noSuccMeth = 0;
				// calculate called-methods
				for(Iterator succMethIter = succMeth.iterator(); succMethIter.hasNext();){
					Signature meth = (Signature) succMethIter.next();
					/*
					 * we handle a method that is within same class like currently analyzed
					 * class, or some kind of dummy
					 */
					if(  meth.toClass().equals(succClass) || 		// usual vertex
						( meth.isDummy()
						  && succClass.equals("foreign.Package")
						  && !packnames.contains(meth.toPackage())
						  && !meth.isJre() ) ||						// call to foreign package
						( meth.isDummy()
						  && succClass.equals("foreign.Class")
						  && !verticesIncluded.contains(meth.toClass())
						  && packnames.contains(meth.toPackage())
						  && !meth.isJre() ) ||						// call to foreign class
						( meth.isDummy()
						  && succClass.equals("jre.Class")
						  && meth.isJre() )   )						// call to JRE
						noSuccMeth++;
				}
				HashSet succClasses;
				int noPreMethods = 0;
				Signature sigMeth;
				// calculate calling-methods
				for(Iterator fromMethIter = fromMeth.entrySet().iterator(); fromMethIter.hasNext(); ){
					Map.Entry tme = (Map.Entry) fromMethIter.next();
					succClasses = (HashSet) tme.getValue();
					sigMeth = ( (Signature) tme.getKey() );
					if( sigMeth.toClass().equals(sig) || 
					  ( sig.isDummy() && sigMeth.isDummy() )  ){
						for( Iterator succClassesIter = succClasses.iterator(); succClassesIter.hasNext(); ){
							Signature currentClass = (Signature) succClassesIter.next();
							if( currentClass.equals(succClass) )
								noPreMethods++;
						}
					}
				}
				element.setNoSuccMethods(noSuccMeth);
				element.setNoPreMethods(noPreMethods);
				newSucc.add(element);
			}
			al.put(sig, newSucc);
		}
		return al;
	}

	
}
