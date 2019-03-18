package bl.postprocessor;

import java.io.*;
import java.util.*;
import callgraph.*;

/**
 * Reads in a {@code -outputFile} of ParSeMiS and enhances
 * it with ClassNumber and EdgeWeights, similar to the ParSeMiS
 * output of M. Huber`s versions. This class is not used anymore,
 * as the writing to a file is omitted. 
 * @author Christopher Oßner
 * @deprecated
 *
 */
public class FragmentsEnhancer {
	
	/**
	 * String that indicates that a graph is from a failing execution. Supposed to
	 * be found at the end of a graph-name. Graph-names matching this String will be
	 * set to {@code ClassNumber 0}.
	 */
	private static final String FAILING_INDICATOR = "_failed";
	
	/**
	 * For the ParSeMiS output (unified)
	 */
	private BufferedReader inFragBr;
	
	/**
	 * For the enhanced output.
	 */
	private BufferedWriter outFragBw;
	
	/**
	 * Location of serialized graphs corresponding to the passed ParSeMiS output.
	 */
	private String graphDB;
	
	/**
	 * Initializes all Readers / Writers.
	 * @param inFragments ParSeMiS output (unified)
	 * @param graphDB path to serialized graphs used to produce the ParSeMiS output
	 * @param outFragments location to store output
	 */
	public FragmentsEnhancer( String inFragments, String graphDB, String outFragments ){
		FileReader inFragFr = null;
		FileWriter outFragFw = null;
		try{
			inFragFr = new FileReader(inFragments);
			outFragFw = new FileWriter(outFragments);
		} catch( IOException e ){
			System.out.println("IOException: " + e.getMessage());
			System.exit(1);
		}
		inFragBr = new BufferedReader(inFragFr);
		outFragBw = new BufferedWriter(outFragFw);
		this.graphDB = graphDB;
	}
	
	public static void main(String[] args){
		if(args.length != 3){
			System.out.println("Read the doc!");
			System.exit(1);
		}
		FragmentsEnhancer fe = new FragmentsEnhancer(args[0], args[1], args[2]);
		int cont = fe.exec();
		System.out.println("Wrote the annotations for " + cont + " fragments to '" + args[2] + "'.");
	}
	
	/**
	 * For each fragment the corresponding serialized object is looked up to get the annotations for
	 * each edge in it`s super-graph.
	 * The produced output contains the fragment (nothing changed here) but embeddings got the form:
	 * <code>
	 * # lg representation of a fragment
	 * # name = name of the 1st super-graph this fragment was found in
	 * # c = name.endWith("_failing") ? 1 : 0
	 * # a1 = annotations of edge_1 (the first listed in the fragment) in the supergraph <name>
	 * #=> <name> NodeIDs <Node IDs just like those in the original>
	 * fragments file>
	 * #=> <name> ClassNumber c
	 * #=> <name> EdgeWeights a1 a2 a3 [...]
	 * # next embedding ...
	 * </code>
	 * @return number of fragments
	 */
	public int exec(){
		String line = null;
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<Edge> al = new ArrayList<Edge>();
		int i = 0;
		do{
			try{
				line = inFragBr.readLine();
			} catch( IOException e ){
				System.out.println("IOException: " + e.getMessage());
				System.exit(1);
			}
			if(line == null)
				break;
			else if( line.startsWith("t #") ){
				i++;
				list = new ArrayList<String>();
				al = new ArrayList<Edge>();
			} else if( line.startsWith("v ") ){
				String sig = line.split(" ")[2];
				list.add(sig);
			} else if ( line.startsWith("e ") ){
				String[] pieces = line.split(" ");
				int start = Integer.parseInt(pieces[1]);
				int end = Integer.parseInt(pieces[2]);
				String startSig = list.get(start);
				String endSig = list.get(end);
				Edge edge = new Edge(startSig, endSig);
				al.add(edge);
			} else{
				String graphname = line.split(" ")[1];
				int classNo = graphname.endsWith(FAILING_INDICATOR) ? 1 : 0;
				AdjacenceList graph = new GraphReader().readGraph( graphDB + "\\" + graphname + ".ser" );
				String annotations = getFragmentsAnnotations( al, graph );
				line = "#=> " + graphname + " NodeIDs " + line.substring( 5 + graphname.length() );
				line += "\n#=> " + graphname + " ClassNumber " + classNo;
				line += "\n#=> " + graphname + " EdgeWeights " + annotations;
			}
			try{
				outFragBw.write( line + "\n");
			} catch( IOException e ){
				System.out.println("IOException: " + e.getMessage() );
				System.exit(1);
			}
		}while( true );
		try {
			inFragBr.close();
			outFragBw.close();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		return i;
	}
	
	/**
	 * Generates a list of annotations.
	 * @param al a list of all edges for one fragment
	 * @param graph the super-graph for which to look up the annotations
	 * @return all annotations (returned by {@link AdjacenceList#getAnnotations(Signature, Signature)} for 
	 * the fragment within the passed super-graph
	 * @throws RuntimeException
	 */
	private String getFragmentsAnnotations( ArrayList<Edge> al, AdjacenceList graph ) throws RuntimeException{
		String annotations = "";
		for( int i = 0; i < al.size(); i++ ){
			Edge edge = (Edge) al.get(i);
			Signature start = edge.getStart();
			Signature end = edge.getEnd();
			int[] fragmentAnnotation = graph.getAnnotations( start, end );
			if(fragmentAnnotation == null)
				throw new RuntimeException("Edge '" + start + "' -> '" + end + "' of a fragment was not found in corresponding supergraph.");
			
			for(int j = 0; j < fragmentAnnotation.length; j++){
				annotations += fragmentAnnotation[j];
				if(j < fragmentAnnotation.length -1)
					annotations += ",";
			}
			if( i < al.size() -1 )
				annotations += " ";
		}
		return annotations;
	}
	
}
