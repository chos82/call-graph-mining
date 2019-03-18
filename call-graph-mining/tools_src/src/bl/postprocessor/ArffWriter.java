package bl.postprocessor;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphReader;
import callgraph.Signature;
import callgraph.SuccessorList;

/**
 * Generates an ARFF file out of an ParSeMiS fragments file. Each annotation of each edge
 * (the set of all edges of all fragments)
 * becomes an attribute. Each super-graph (the embedding) becomes a row. The  value in a cell
 * is the value of the annotation of that edge (column) in an embedding (row).
 * 
 * @author Christopher Oßner
 *
 */
public class ArffWriter {
	
	/**
	 * String that indicates that a graph is from a failing execution. Supposed to
	 * be found at the end of a graph-name. Graph-names matching this String will be
	 * <code>class failed</code>.
	 */
	private static final String FAILING_INDICATOR = "_failed";
	
	/**
	 * To handle the input fragments file.
	 */
	private BufferedWriter outBw;
	
	/**
	 * To handle the output ARFF.
	 */
	private BufferedReader inBr;
	
	/**
	 * Set of all attributes. 
	 */
	HashMap<String, Integer> allAttributes = new HashMap<String, Integer>(1024);
	
	/**
	 * Maps graph name -> occurring attributes
	 */
	HashMap<String, HashMap<Integer, Integer>> table = new HashMap<String, HashMap<Integer, Integer>>();
	
	/**
	 * Path to corresponding graph DB. If set dummies will be re-included.
	 */
	private String graphDB = null; 
	
	/**
	 * If dummy vertices (foreign package, foreign class) were omitted during the graph mining step
	 * (when the LG was wrote),
	 * they can be re-included through setting this variable true.
	 */
	private boolean reincludeDummies = false;
	
	/**
	 * Calls to JRE can be inlcuded by setting this variable true.
	 */
	private boolean reincludeJre = false;
	
	/**
	 * Maps fragment name -> stats
	 */
	private Statistics statistics = null;

	/**
	 * Holding a unique ID for attributes (of the ARFF). Actually the same as {@link #allAttributes} size,
	 * but cheaper to retrieve. 
	 */
	private int attributeId = 0;
	
	class Statistics extends HashMap<String, FragmentStatistic>{

		private static final long serialVersionUID = 1L;

		public FragmentStatistic incVertices(String fragName){
			FragmentStatistic oldValue;
			if(super.containsKey(fragName)){
				oldValue = super.put(fragName, super.get(fragName).incVertex());
				return oldValue;
			} else {
				super.put(fragName, ( new FragmentStatistic() ).incVertex() );
				return null;
			}
		}
		
		public FragmentStatistic incEdges(String fragName){
			FragmentStatistic oldValue;
			if(super.containsKey(fragName)){
				oldValue = super.put(fragName, super.get(fragName).incEdges());
				return oldValue;
			} else {
				super.put(fragName, ( new FragmentStatistic() ).incEdges() );
				return null;
			}
		}
		
		public FragmentStatistic incEmbeddings(String fragName){
			FragmentStatistic oldValue;
			if(super.containsKey(fragName)){
				oldValue = super.put(fragName, super.get(fragName).incEmbeddings());
				return oldValue;
			} else {
				super.put(fragName, ( new FragmentStatistic() ).incEmbeddings() );
				return null;
			}
		}
	}
	
	private class FragmentStatistic{
		private int noVertices;
		private int noEdges;
		private int noEmbeddings;
		FragmentStatistic(){
			noVertices = 0; noEdges = 0; noEmbeddings = 0;
		}
		FragmentStatistic  incVertex(){ noVertices++; return this; }
		FragmentStatistic incEdges(){ noEdges++; return this; }
		FragmentStatistic incEmbeddings(){ noEmbeddings++; return this; }
		int getNoVertices(){ return noVertices; }
		int getNoEdges(){ return noEdges; }
		int getNoEmbeddings(){ return noEmbeddings; }
	}
	
	/**
	 * Sets up the file handlers.
	 * @param input location of the input, an enhanced fragments file
	 * @param output where to store the output ARFF
	 * @deprecated
	 */
	ArffWriter(String input, String output){
		FileReader inFr = null;
		FileWriter outFw = null;
		try {
			inFr = new FileReader(input);
		} catch (FileNotFoundException e) {
			System.out.println("IOException: " +e.getMessage());
			System.exit(1);
		}
		try {
			outFw = new FileWriter(output);
		} catch (IOException e) {
			System.out.println("IOException: " +e.getMessage());
			System.exit(1);
		}
		inBr = new BufferedReader(inFr);
		outBw = new BufferedWriter(outFw);
	}
	
	ArffWriter(String input, String output, String graphDB){
		this(input, output);
		this.graphDB = graphDB;
	}
	
	/**
	 * Sets up the file handlers.
	 * @param input location of the input, an (unified) ParSeMiS fragments file
	 * @param output where to store the output ARFF
	 * @param graphDB path to the serialized graph objects that were used to create the fragments file
	 * @param reincludeDummies If dummy vertices were omitted during the graph mining step
	 * @param reincludeJre if calls to JRE shall be included to the ARFF, set true
	 * (when the LG was wrote), they can be re-included through setting this variable true.
	 */
	public ArffWriter(String input, String output,
			String graphDB, boolean reincludeDummies,
			boolean reincludeJre){
		this(input, output);
		this.graphDB = graphDB;
		this.reincludeDummies = reincludeDummies;
		this.reincludeJre = reincludeJre;
	}
	
	/**
	 * Triggers the process.
	 */
	public void exec(){
		String line = null;
		statistics = new Statistics();
		ArrayList<String> vertices = new ArrayList<String>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		String fragmentName = null;
		while(true){
			try {
				line = inBr.readLine();
			} catch (IOException e) {
				System.out.println("IOException: " +e.getMessage());
				System.exit(1);
			}
			if(line == null)
				break;
			if( line.startsWith("t #") ){
				fragmentName = line.substring(4);
				vertices = new ArrayList<String>();
				edges = new ArrayList<Edge>();
			} else if( line.startsWith("v ") ){
				String sig = line.replaceFirst("^(v \\d* )", "");
				vertices.add(sig);
				statistics.incVertices(fragmentName);
			} else if ( line.startsWith("e ") ){
				String[] pieces = line.split(" ");
				int start = Integer.parseInt(pieces[1]);
				int end = Integer.parseInt(pieces[2]);
				String startSig = vertices.get(start);
				String endSig = vertices.get(end);
				Edge edge = new Edge(startSig, endSig);
				edges.add(edge);
				statistics.incEdges(fragmentName);
			} else if(line.startsWith("#=> ")){
				String graphname = line.substring(4); // "#=> ".length = 4
				graphname = graphname.substring(0, graphname.indexOf(' '));
				table.put( graphname, addCells(graphname, edges, fragmentName) );
				statistics.incEmbeddings(fragmentName);
			}
		}
		try {
			writeOutput(table);
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		
	}
	
	/**
	 * If {@link #statistics} is null, {@link #exec()} will be called.
	 * @return statistics unordered
	 */
	public ArrayList<Entry<String, FragmentStatistic>> getStatistics(){
		if(statistics == null)
			exec();
		ArrayList<Entry<String, FragmentStatistic>> a = new ArrayList<Entry<String, FragmentStatistic>>();
		for(Iterator<Entry<String, FragmentStatistic>> iter = statistics.entrySet().iterator();
			iter.hasNext(); ){
			Entry<String, FragmentStatistic> entry = iter.next();
			a.add(entry);
		}
		return a;
	}
	
	public String toStringStatistics(){
		String out = null;
		ArrayList<Entry<String, FragmentStatistic>> a = getStatistics();
		out = "#fragments: " + statistics.size() + "\n";
		Comparator<Entry<String, FragmentStatistic>> c = new Comparator<Entry<String, FragmentStatistic>>() {
			@Override
			public int compare(Entry<String, FragmentStatistic> o1,
					Entry<String, FragmentStatistic> o2) {
				if(o1.getValue().getNoVertices() < o2.getValue().getNoVertices()) return -1;
				if(o1.getValue().getNoVertices() == o2.getValue().getNoVertices()) return 0;
				else return 1;
			}
		};
		Collections.sort(a, c);
		Entry<String, FragmentStatistic> maxEntry = a.get(a.size() -1);
		Entry<String, FragmentStatistic> minEntry = a.get(0);
		out += "Biggest fragment (by #vertices): t # " + maxEntry.getKey() + 
			   "\n\t#vertices: " + maxEntry.getValue().getNoVertices() +
			   "\n\t#edges: " + maxEntry.getValue().getNoEdges() + 
			   "\nSmallest fragment (by #vertices): t # " + minEntry.getKey() + 
			   "\n\t#vertices: " + minEntry.getValue().getNoVertices() +
			   "\n\t#edges: " + minEntry.getValue().getNoEdges();
		c = new Comparator<Entry<String, FragmentStatistic>>() {
			@Override
			public int compare(Entry<String, FragmentStatistic> o1,
					Entry<String, FragmentStatistic> o2) {
				if(o1.getValue().getNoEmbeddings() < o2.getValue().getNoEmbeddings()) return -1;
				if(o1.getValue().getNoEmbeddings() == o2.getValue().getNoEmbeddings()) return 0;
				else return 1;
			}
		};
		Collections.sort(a, c);
		maxEntry = a.get(a.size() -1);
		minEntry = a.get(0);
		out += "\nMost frequent embedded fragment: t#" + maxEntry.getKey() + 
			   " (" + maxEntry.getValue().getNoEmbeddings() + ")" +
			   "\nLeast frequent embedded fragment: t#" + minEntry.getKey() +
			   " (" + minEntry.getValue().getNoEmbeddings() + ")";
		return out;
	}
	
	/**
	 * We add only those cells per row, that are not NULL. We also add each attribute
	 * to {@link #allAttributes}. Cells resulting from the current fragment are added
	 * to {@link #table} to those, that are already present there.
	 * @param graphname name of the embedding graph
	 * @param edges all edges of the current fragment
	 * @param fragmentName name of the current fragment
	 * @return graphname -> attributes (attributes from {@link #table} plus those
	 * new in the current fragment)
	 */
	private HashMap<Integer, Integer> addCells(String graphname,
			ArrayList<Edge> edges,
			String fragmentName){
		
		HashMap<Integer, Integer> cells;
		cells = table.get(graphname);
		cells = cells == null ?  new HashMap<Integer, Integer>() : cells;
		AdjacenceList graph = new GraphReader().readGraph( graphDB + "\\" + graphname + ".ser" );
		//check for hierarchy level && set annotation names
		String[] annotationNames = null;
		
		if(reincludeDummies || reincludeJre)
			edges = reincludeDummies(graph, edges);
		if(edges.size() > 0){
			Edge edge = edges.get(0);
			Signature sig = edge.getStart();
			if( sig.getType().equals("package") ) annotationNames = AdjacenceElement.PACKAGE_ANNOTATIONS;
			else if( sig.getType().equals("class") ) annotationNames = AdjacenceElement.CLASS_ANNOTATIONS;
			else annotationNames = AdjacenceElement.METHOD_ANNOTATIONS;
		}
		for( int i = 0; i < edges.size(); i++ ){
			Edge currentEdge = edges.get(i);
			if(annotationNames == null)
			System.out.println("edge == null");
			int[] annotations = graph.getAnnotations(currentEdge.getStart(), currentEdge.getEnd());
			for( int j = 0; j < annotations.length; j++ ){
				String attribute = fragmentName+":"+annotationNames[j]+"("+currentEdge.toString()+")";
				if(allAttributes.get( attribute ) == null){
					attributeId++;
					allAttributes.put(attribute, attributeId);
				}
				Integer id = allAttributes.get(attribute);
				cells.put( id, annotations[j] );
			}
		}
		return cells;
	}
	
	/**
	 * Looks up dummies in the serialized graphs and generated additional attributes
	 * for each edge to and from the dummies. Dummies (foreign package, foreign class)
	 * are included if {@link #reincludeDummies} is set. Calls to JRE are included, if
	 * {@link #reincludeJre} is set.
	 * @param graphname name of the graph to look up
	 * @param edges the current edges for that graph
	 * @return the edges passed as parameter + the dummy additions
	 */
	@SuppressWarnings("unchecked") //java 1.4 (needed, because of rhino) knows no generics
	private ArrayList<Edge> reincludeDummies(AdjacenceList graph,
			ArrayList<Edge> edgesIn) {
		ArrayList<Edge> edges = new ArrayList<Edge>(edgesIn);
		for(Iterator<Map.Entry<Signature, SuccessorList>> iter = graph.iterator(); iter.hasNext(); ){
			Map.Entry<Signature, SuccessorList> entry = iter.next();
			SuccessorList succ = entry.getValue();
			Signature start = entry.getKey();
			for(Iterator<AdjacenceElement> succIter = succ.iterator(); succIter.hasNext(); ){
				Signature currentSuccessor = succIter.next().getSignature();
				if( ( reincludeDummies && start.isDummy() && !start.isJre() )
						&& ( reincludeJre || !currentSuccessor.isJre() ) )
					edges.add(  new Edge( start, currentSuccessor )  );
				else if( ( reincludeDummies && currentSuccessor.isDummy() && ! currentSuccessor.isJre() )
						 || ( reincludeJre && currentSuccessor.isJre() 
								 && ( reincludeDummies || !start.isDummy() )  ) )
					edges.add(  new Edge( start, currentSuccessor )  );
			}
		}
		return edges;
	}
	
	private void writeOutput(HashMap<String, HashMap<Integer, Integer>> table) throws IOException{
		int noSuperGraphs = 0;
		int noColumns = allAttributes.size();
		Entry<String, HashMap<Integer, Integer>> currentRow = null;
		outBw.write("@RELATION " + "EntropyScoring\n\n");
		
		for(Iterator<String> iter = allAttributes.keySet().iterator(); iter.hasNext(); ){
			String attr = iter.next();
			outBw.write("@ATTRIBUTE " + attr.replace(',', '/') + " INTEGER\n");
		}
		
		outBw.write("@ATTRIBUTE class {failed,passed}\n\n@DATA\n");
		
		for(Iterator<Entry<String, HashMap<Integer, Integer>>> rowIter = table.entrySet().iterator();
			rowIter.hasNext(); ){
			
			currentRow = rowIter.next();
			HashMap<Integer, Integer> currentRowValues = currentRow.getValue();
			String currentGraphClass;
			if(currentRow.getKey().endsWith(FAILING_INDICATOR))
				currentGraphClass = "failed";
			else
				currentGraphClass = "passed";
			for( Iterator<Entry<String, Integer>> attrIter = allAttributes.entrySet().iterator(); 
				 attrIter.hasNext(); ){
				Entry<String, Integer> attrEntry = attrIter.next();
				Integer id = attrEntry.getValue();
				Integer value = currentRowValues.get(id);
				String valueString = value == null ? "?" : value.toString();
				outBw.write(valueString + ",");
			}
			
			outBw.write(currentGraphClass + "\n");
			noSuperGraphs++;
			
		}
		
		outBw.close();
		System.out.println("Wrote " + noColumns + " attributes for " + noSuperGraphs + " super-graphs.");
	}
	
}
