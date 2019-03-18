package callgraph;

import java.io.*;

/**
 * Reads in a serialized graph.
 * @author Christopher Oßner
 *
 */
public class GraphReader {
	
	/**
	 * @param filename filename of the serialized adjacency list to be read
	 * @return object representation of the read file
	 */
	public AdjacenceList readGraph(String filename){
		InputStream fis = null;
		AdjacenceList al = null;
		try 
		{ 
		  fis = new FileInputStream( filename ); 
		  ObjectInputStream o = new ObjectInputStream( fis );
		  Object foo = o.readObject();
		  al = (AdjacenceList) foo;
		} 
		catch ( IOException e ) { System.err.println( e ); } 
		catch ( ClassNotFoundException e ) { System.err.println( e ); } 
		finally { try { fis.close(); } catch ( Exception e ) { } }
		return al;
	}
	 	
}
