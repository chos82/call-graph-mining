package callgraph;

/**
 * Serves as fallback, if the pointcuts within the instrumentation
 * can`t catch the end of an execution. This thread is used only to 
 * pass it to {@link Runtime#addShutdownHook(Thread hook)}. 
 * Still some tests are not written to disk. The reason might be, 
 * that the operations done here should be not time consuming, but 
 * we have to do the writing.
 */
public class GraphWriterThread extends Thread {
	
	/**
	 * Used to pass it to {@link GraphWriter}
	 */
	private String graphname;
	
	/**
	 * Intern representation of the graph
	 */
	private AdjacenceList al;
	
	/**
	 * Directory of the output
	 */
	private String file;
	
	/**
	 * Responsible for writing serialized versions of {@link #al}
	 */
	private GraphWriter gw;
	
	/**
	 * Used to prevent 
	 */
	private boolean wroteResults;
	
	public void init(String file, String graphname){
		this.file = file;
		this.graphname = graphname;
	}
	
	/**
	 * Used to update {@link #al}. 
	 * 
	 * Since CyclicBarrier is not available within java 1.4, passing the adjacencey list 
     * around seems to be the only option (maybe it could be done with a {@link Thread#join})
	 * @param al
	 */
	public void setAdjacence(AdjacenceList al){
		this.al = al;
	}
	
	/**
	 * Setter method. Set to avoid manifold writing of the graph.
	 * @param wr
	 */
	public void setWroteResults(boolean wr){
		wroteResults = wr;
	}
	
	public void run() { 
		writeResults();
	}
	
	/**
     * Initialize {@link #gw} with the proper values and write {@link #al}
     * as serialized object to disk.
     */
	private void writeResults(){
		if(!wroteResults){
	       	if(graphname != null)
	       		gw = new GraphWriter(al, file, graphname);
	       	else
	       		gw = new GraphWriter(al, file);
	       	gw.writeAdjacence();
		}
    }

}
