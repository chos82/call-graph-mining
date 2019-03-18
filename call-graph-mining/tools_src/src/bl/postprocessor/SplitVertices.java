package bl.postprocessor;

import java.util.*;
import java.io.*;

public class SplitVertices {
	
	class EmbeddedFragment{
		private HashSet<String> fragVertices;
		private int countInCorr;
		private int countInFail;
		EmbeddedFragment(HashSet<String> fragVertices,int countInCorr, int countInFail){
			this.fragVertices = fragVertices;
			this.countInCorr = countInCorr;
			this.countInFail = countInFail;
		}
		public int getCountInCorr(){ return this.countInCorr; }
		public int getCountInFail(){ return this.countInFail; }
		public HashSet<String> getEmbeddedFragment(){ return fragVertices; }
	}
	
	/**
	 * Maps each fragment in S_f (fragments that occur in failing executions only)
	 * to a set of vertices.
	 */
	private HashMap<String, HashSet<String>> fragments;
	
	/**
	 * List of sets of vertices of each fragment in S_corr (fragments that occur in correct executions only)
	 * to a set of vertices.
	 */
	private ArrayList<EmbeddedFragment> embeddings;
	
	/**
	 * Set of all graphs in D_corr. Serves to get mightiness.
	 */
	private HashSet<String> graphsCorr = new HashSet<String>();
	
	/**
	 * Set of all graphs in D_corr. Serves to get mightiness.
	 */
	private HashSet<String> graphsFail = new HashSet<String>();
	
	/**
	 * ParSeMiS fragments file
	 */
	private String inFile;
	
	public SplitVertices(String inFile){
		this.inFile = inFile;
	}
	
	/**
	 * Starts the whole parse process.
	 */
	public void exec(){
		fragments = new HashMap<String, HashSet<String>>();
		embeddings = new ArrayList<EmbeddedFragment>();
		FileReader fr = null;
		try {
			fr = new FileReader(inFile);
		} catch (FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		}
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		ArrayList<String> fragment = new ArrayList<String>();
		boolean analyze = false;
		while(true){
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.err.println(e);
				System.exit(1);
			}
			if(line == null)
				break;
			if(line.startsWith("t # ")){
				if(analyze)
					analyzeFragment(fragment);
				fragment = new ArrayList<String>();
				fragment.add(line);
			} else{
				analyze = true;
				fragment.add(line);
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
	}
	
	/**
	 * Executes {@link #exec()} if it has not been done before.
	 * @return maps each fragment in SG_fail to a set of vertices that this fragment contains
	 */
	public HashMap<String, HashSet<String>> getFailing(){
		if(fragments == null)
			exec();
		return fragments;
	}
	
	/**
	 * @return {@link #embeddings}
	 */
	public ArrayList<EmbeddedFragment> getEmbeddedFragments(){
		if(embeddings == null)
			exec();
		return this.embeddings;
	}
	
	public int getNoCorrGraphs(){
		if(embeddings == null)
			exec();
		return this.graphsCorr.size();
	}
	
	public int getNoFailGraphs(){
		if(embeddings == null)
			exec();
		return this.graphsFail.size();
	}
	
	/**
	 * Analyzes a single fragment and puts it`s vertices to {@link #fragments}, if it is in S_f.
	 * @param fragment the fragment to analyze
	 */
	private void analyzeFragment(ArrayList<String> fragment){
		String fragName = fragment.get(0);
		HashSet<String> vertices = new HashSet<String>();
		// check for the P_fail score
		for(int i = fragment.size() -1; i >= 0; i--){
			String line = fragment.get(i);
			if(line.startsWith("#=>"))
				if(!line.matches(".*_failed.*"))
					break;
			else if(line.startsWith("v "))
				vertices.add(line.split(" ")[2]);
		}
		fragments.put(fragName, vertices);
		// check for the P_fail-corr score
		vertices = new HashSet<String>();
		int noEmbCorr = 0, noEmbFail = 0;
		for(int i = fragment.size() -1; i >= 0; i--){
			String line = fragment.get(i);
			if(line.startsWith("#=>")){
				if( line.matches(".*_failed.*") ){
					noEmbFail++;
					graphsFail.add(fragName);
				} else{
					noEmbCorr++;
					graphsCorr.add(fragName);
				}
			} else if(line.startsWith("v ")){
				vertices.add(line.split(" ")[2]);
			}
		}
		embeddings.add( new EmbeddedFragment(vertices, noEmbCorr, noEmbFail) );
	}
	
	public String toString(){
		String out = "";
		for(Iterator<Map.Entry<String, HashSet<String>>> iter = fragments.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry<String, HashSet<String>> currentEntry = iter.next();
			out += currentEntry.getKey() + "\n";
			for(Iterator<String> iter2 = currentEntry.getValue().iterator(); iter2.hasNext(); ){
				out += "\t" + iter2.next() + "\n";
			}
		}
		return out;
	}

}
