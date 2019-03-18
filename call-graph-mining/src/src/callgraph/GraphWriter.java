package callgraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * As the name suggests, graphs can be written here.
 * Directory delimiters are windows style - sorry.
 */
public class GraphWriter {
	
	private static final String NEW_LINE = "\n";//System.getProperty("line.seperator");
	
	/**
	 * The object of interest - gets written to disk.
	 */
	private AdjacenceList al;
	
	/**
	 * Must be a directory - no file extension. Not really pretty...
	 * But serves needs here well.
	 * Is used to build all output names.
	 * Serialized objects will be stored in filname + "\\data"
	 * LG in filename + '.lg'
	 */
	private String filename;
	
	/**
	 * The name that will be given to graphs. As JavaScript tests might serve as 
	 * names (and those are in multiple directories),
	 * '\\' is replaced by '_', to exclude directory structures.
	 */
	private String graphname;
	
	/**
	 * Responsible for the text-graph files (.lg)
	 */
	private FileWriter fw;
	
	private BufferedWriter bw;
	
	/**
	 * Got a name for your output graph? Then start here. If called from the modified
	 * version of jsDriver.pl of this framework, the JavaScript filenames will end up here.
	 * @param al graph to be written
	 * @param filename output directory
	 * @param graphname name for the graph to be written
	 */
	public GraphWriter(AdjacenceList al, String filename, String graphname){
		this(al, filename);
		this.graphname = graphname;
	}
	
	/**
	 * Leaves the graph name out - that will be set to "Interactive_SHELL_graph"
	 * @param al graph to be written
	 * @param filename output directory
	 */
	public GraphWriter(AdjacenceList al, String filename){
		this.al = al;
		this.filename = filename;
	}
	
	/**
	 * Writes {@link #al} as serialized object to disk. It will be placed in
	 * the directory {@link #filename}. And has {@link #graphname} + '.ser' as name.
	 */
	public void writeAdjacence(){
		String datafolder = filename;
		File f = new File(datafolder);
		f.mkdir();
		String name = "";
		if(graphname == null){
			graphname = "Interactive_SHELL_graph";
			name = graphname;
		}
		else{
			name = graphname.replace('/', '_');
			name = name.replace('\\', '_');
		}
		String filename = datafolder + "\\" + name + ".ser";
		FileOutputStream fos = null;
		try 
		{ 
		  fos = new FileOutputStream( filename ); 
		  ObjectOutputStream o = new ObjectOutputStream( fos ); 
		  o.writeObject( al );
		} 
		catch ( IOException e ) { System.err.println( e ); } 
		finally { try { fos.close(); } catch ( Exception e ) { } }
	}
	
	/**
	 * Writes the total reduced call-graph held in {@link #al} to 
	 * file {@link #filename} + ".lg". As in this framework only 
	 * total reduced graphs are used, {@link ArrayListSet} is used to
	 * generate a order preserving structure out of the Hash based
	 * AdjacenceList. The ordering is important, as the indices are used
	 * for the identification numbers in LG.
	 * @param writeWeight if set to true annotations will be written. Resulting in
	 * a extended LG graph (see Matthias Huber for more information on extended LG).
	 * Rarely needed (within current setup)
	 * @param skipDummies set true if LG should contain no dummies
	 */
	public void writeR_w_total2lg(boolean writeWeight, boolean skipDummies, boolean skipJre){
		setFileWriter();
		bw = new BufferedWriter(fw);
		writeGraphName2lg();
		String print;
		ArrayList vertices = vertexId(al, skipDummies, skipJre);
		for(int i = 0; i < vertices.size(); i++){
			Signature sig = (Signature) vertices.get(i);
					
			print = "v " + i + " " + sig.getSignature() + NEW_LINE;
			try {
				bw.write(print);
			} catch (IOException e) {
				System.out.println("I/O Exception: " + e.getMessage());
				System.exit(1);
			}
		}
		for(Iterator iter = al.iterator(); iter.hasNext(); ){
			Entry entry = (Entry) iter.next();
			Signature sig = (Signature) entry.getKey();
			SuccessorList succ = (SuccessorList) entry.getValue();
			int startInd = vertices.indexOf(sig);
			String start = "e " + ( startInd ) + " ";
			if(startInd == -1)
				continue;
			if(succ == null)
				continue;
			for(int j = 0; j < succ.size(); j++){
				AdjacenceElement ae = succ.getElement(j);
				Signature succsig = ae.getSignature();
				int end = vertices.indexOf(succsig);
				if(end == -1)
					continue;
				print = start + end + " call";
				if(writeWeight){
					print += " ";
					int[] annot = ae.getAnnotations();
					for(int k = 0; k < annot.length; k++){
						print += annot[k];
						if(k < annot.length -1)
							print += ",";
					}
				}
				print += NEW_LINE;
				try {
					bw.write(print);
				} catch (IOException e) {
					System.out.println("I/O Exception: " + e.getMessage());
					System.exit(1);
				}
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			System.out.println("I/O Exception: " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * To add an ID to each vertex (used as name within the DOT)
	 * @param graph our graph to print
	 * @param skipJre 
	 * @param skipDummies 
	 * @return signature -> ID
	 */
	private ArrayList vertexId(AdjacenceList graph, boolean skipDummies, boolean skipJre){
		ArrayList out = new ArrayList();
		HashSet temp = new HashSet();
		for(Iterator iter = graph.iterator();
			iter.hasNext(); ){
			Entry entry = (Entry) iter.next();
			Signature vertex = (Signature) entry.getKey();
			if( temp.add(vertex) ){
				if( skipDummies && ( ( vertex.isDummy() && !vertex.isJre() ) || onlyDummyPreccessors(vertex)) )
					continue;
				if( skipJre && ( vertex.isJre() || onlyJrePreccessors(vertex)) )
					continue;
				out.add(vertex);
			}
				
			SuccessorList succ = (SuccessorList) entry.getValue();
			for(Iterator succIter = succ.iterator(); succIter.hasNext(); ){
				AdjacenceElement ae = (AdjacenceElement) succIter.next();
				vertex = ae.getSignature();
				if( temp.add(vertex) ){
					if( skipDummies && ( ( vertex.isDummy() && !vertex.isJre() ) || onlyDummyPreccessors(vertex)) )
						continue;
					if( skipJre && ( vertex.isJre() || onlyJrePreccessors(vertex)) )
						continue;
					out.add(vertex);
				}
			}
		}
		return out;
	}

	
	private boolean onlyJrePreccessors(Signature sig) {
		for(Iterator iter = al.iterator(); iter.hasNext(); ){
			Entry entry = (Entry) iter.next();
			Signature start = (Signature) entry.getKey();
			SuccessorList succ = (SuccessorList) entry.getValue();
			for(Iterator succIter = succ.iterator(); succIter.hasNext(); ){
				AdjacenceElement end = (AdjacenceElement) succIter.next();
				if(  ( end.getSignature().equals(sig) &&  ! start.isJre() ) ||
						( start.equals(sig) && ! end.getSignature().isJre() )  )
					return false;
			}
		}
		return true;
	}

	private boolean onlyDummyPreccessors(Signature sig){
		for(Iterator iter = al.iterator(); iter.hasNext(); ){
			Entry entry = (Entry) iter.next();
			Signature start = (Signature) entry.getKey();
			SuccessorList succ = (SuccessorList) entry.getValue();
			for(Iterator succIter = succ.iterator(); succIter.hasNext(); ){
				AdjacenceElement end = (AdjacenceElement) succIter.next();
				if(  ( end.getSignature().equals(sig) &&  ! start.isDummy() ) ||
						( start.equals(sig) && ! end.getSignature().isDummy() )  )
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Helper for printing graph names within LG
	 */
	private void writeGraphName2lg(){
		String name;
		if(graphname == null){
			//System.out.println("Writing to '" + filename + "'...");
			name = "t # Interactive_SHELL_graph" + NEW_LINE;
		}
		else{
			//System.out.println("Writing Test '" + graphname + "' to '" + filename + "'...");
			name = "t # " + graphname + NEW_LINE;
		}
		try{
			bw.write(name);
		}
		catch (IOException e) {
			System.out.println("IO/Exception: " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Helper to initialize the {@link FileWriter}
	 */
	private void setFileWriter(){
		boolean apnd = false;
		File file = new File(filename + ".lg");
		if (file.exists())
			apnd = true;
		try {
			fw = new FileWriter(file, apnd);
		} catch (IOException e) {
			System.out.println("I/O Exception: " + e.getMessage());
		}
	}
	
}
