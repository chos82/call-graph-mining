package bl.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphReader;
import callgraph.Signature;
import callgraph.SuccessorList;

/**
 * Bored of analyzing graphs you never saw? Here we go...
 * @author CO
 *
 */
public class DotWriter {
	
	private String inFileName, outFileName, graphName;
	
	/**
	 * @param inFileName some serialized {@link AdjacenceList}
	 * @param outFileName a DOT ready to visualize;)
	 */
	DotWriter(String inFileName, String outFileName){
		this.inFileName = inFileName;
		this.outFileName = outFileName;
		File inFile = new File(inFileName);
		File outFile = new File(outFileName);
		if( ! inFile.exists() ){
			System.out.println("Input file does not exist.");
			System.exit(1);
		}
		if( outFile.exists() ){
			System.out.println("Terminated as output file already exists.");
			System.exit(1);
		}
		String temp = outFile.getName();
		this.graphName = temp.substring(0, temp.length() - 4);
	}
	
	/**
	 * @param args in, out
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Input graph is: " + args[0]);
		System.out.println("Output DOT is: " + args[1]);
		DotWriter dw = new DotWriter(args[0], args[1]);
		dw.exec();
	}
	
	/**
	 * Trigger it.
	 * @throws IOException
	 */
	public void exec() throws IOException{
		GraphReader gr = new GraphReader();
		AdjacenceList inGraph = gr.readGraph(inFileName);
		writeDot(inGraph, outFileName);
	}
	
	/**
	 * Where the work is done...
	 * @param graph input
	 * @param outFileName out DOT
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void writeDot(AdjacenceList graph, String outFileName) throws IOException{
		FileWriter fw = new FileWriter(outFileName);
		BufferedWriter bw = new BufferedWriter(fw);
		String out = "digraph " + graphName + "{\n";
		bw.write(out);
		HashMap<Signature, Integer> vertices = vertexId(graph);
		for( Entry<Signature, Integer> entry : vertices.entrySet() ){
			Signature vertex = entry.getKey();
			out = "\t"+entry.getValue()+" [label=\""+vertex.getSignature()+"\"";
			if(vertex.isDummy())
				out += " shape=\"box\"";
			out += "];\n";
			bw.write(out);
		}
		for(Iterator<Entry<Signature, SuccessorList>> iter = graph.iterator();
			iter.hasNext(); ){
			Entry<Signature, SuccessorList> entry = iter.next();
			Signature start = entry.getKey();
			SuccessorList succ = entry.getValue();
			for(Iterator<AdjacenceElement> iterSucc = succ.iterator(); iterSucc.hasNext(); ){
				AdjacenceElement ae = iterSucc.next();
				Signature end = ae.getSignature();
				Integer startId = vertices.get(start);
				Integer endId = vertices.get(end);
				int[] annotations = ae.getAnnotations();
				out = "\t"+startId+" -> "+endId+" [label=\"";
				for(int i = 0; i < annotations.length; i++){
					out += annotations[i];
					if(i < annotations.length -1)
						out += ", ";
				}
				out += "\"]\n";
				bw.write(out);
			}
		}
		bw.write("}");
		System.out.println("Wrote DOT.");
		bw.close();
	}
	
	/**
	 * To add an ID to each vertex (used as name within the DOT)
	 * @param graph our graph to print
	 * @return signature -> ID
	 */
	@SuppressWarnings("unchecked")
	private HashMap<Signature, Integer> vertexId(AdjacenceList graph){
		HashMap<Signature, Integer> out = new HashMap<Signature, Integer>();
		int i = 0;
		for(Iterator<Entry<Signature, SuccessorList>> iter = graph.iterator();
			iter.hasNext(); ){
			Entry<Signature, SuccessorList> entry = iter.next();
			Signature vertex = entry.getKey();
			if( ! out.containsKey(vertex) ){
				out.put(vertex, i);
				i++;
			}
			SuccessorList succ = entry.getValue();
			for(Iterator<AdjacenceElement> succIter = succ.iterator(); succIter.hasNext(); ){
				AdjacenceElement ae = succIter.next();
				vertex = ae.getSignature();
				if( ! out.containsKey(vertex)){
					out.put(vertex, i);
					i++;
				}
			}
		}
		return out;
	}

}
