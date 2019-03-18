package instrumentation;

import callgraph.*;

/**
 * This aspcet is needed to instrument Rhino for call-graph generation.
 * The instrumentation is program specific. It may serve as draft for the
 * instrumentation of other programs.
 * @author Christopher Oßner
 */
public aspect Trace {
    /**
     * This pointcut catches the joinpoints of the calls to each method (including constructors)
     * that belongs to 
     * the instrumented program. Calls of methods that belong to other packages of this 
     * framework are not considered.
     */
    pointcut methodCall(): call(* *(..)) || call(new(..));
    
    /**
     * This poitcut is needed to catch the beginning and the end of a trace. As within Rhino 
     * it does not catch the end in each case, further agents are needed (see corresponding 
     * advices).
     */
    pointcut shellMain(String args[]):
    	execution(public static void org.mozilla.javascript.tools.shell.Main.main(String[]))
    	&& args(args);
    
    /**
     * Since the invocation of 'js>quit()' (Rhino`s javascript shell) does not end with the main(),
     * but with a System.exit() (quite rough in my opinon), an extra pointcut is needed.
     * It is not needed to trace the tests in Rhino`s test-suite, but the shell is nice for ad-hoc 
     * testing.
     */
    pointcut shellQuit():
    	execution(public static void org.mozilla.javascript.tools.shell.Global.quit(..));
    
    /**
     * Actually this pointcut is not used. But as most Rhino tests, that do not end with the shell`s 
     * main(), seem to end here (no proper evaluation was done to verify that), you may want to use 
     * it. 
     */
    pointcut context():
    	execution(* org.mozilla.javascript.Context.getCurrentContext(..) );
    
    /**
     * Used to model the call-graph.
     */
    private AdjacenceList al;
    
    /**
     * Used to provide a output directory for {@link #gw}.
     */
    private String file;
    
    /**
     * Used to store a name for the output graph. {@link #gw} uses it as filename.
     */
    private String graphname;
    
    /**
     * As not all Rhino tests end with main(), this thread is used to add a
     * {@link Runtime#addShutdownHook(Thread hook)}. Thanks to Vallentin Dallmeier 
     * for his hint, that program ends might be caught like this.
     */
    private GraphWriterThread gwt = new GraphWriterThread();
    
    /**
     * Is used here to write the serialized objects (adjacency lists).
     */
    private GraphWriter gw;
    
    /**
     * Used to avoid manifold writing of the graph.
     */
    private boolean wroteResults = false;

    /**
     * Called when Rhino`s js-shell is opened. Mainly gets the output directory, 
     * passed as argument to the modified main() of Rhino`s shell.
     * @param args at pos[0] is the output directory, the whole array is passed
     * to {@link #getGraphName} 
     */
    before(String args[]): shellMain(args){
    	al = new AdjacenceList();
    	wroteResults = false;
    	if(args.length < 1 ){
    		System.out.println("An output file must be given!");
    		System.exit(1);
    	}
    	file = args[0];
    	graphname = getGraphName(args);
    	gwt.init(file, graphname);
    	Runtime.getRuntime().addShutdownHook( gwt ); 
    }
    
    /**
     * Adds the signatures of the called method and it`s enclosing signature
     * to the adjacency list.
     * 
     * Since CyclicBarrier is not available within java 1.4, passing the adjacence list 
     * around seems to be the only option (maybe it could be done with a {@link Thread#join})
     */
    before(): methodCall()
	   			&& !within(callgraph.*)
	   			&& !within(instrumentation.*){
     	org.aspectj.lang.Signature sig = thisJoinPointStaticPart.getSignature();
     	org.aspectj.lang.Signature caller = thisEnclosingJoinPointStaticPart.getSignature();
    	//System.out.println(sig.getDeclaringType().getName() + "." + sig.getName());
   		al.addCall(caller, sig);
    	gwt.setAdjacence(al);
    }
    
    /**
     * Invoke {@link #writeResults}.
     */
    before(): shellQuit() {
    	writeResults();
    	gwt.setWroteResults(wroteResults);
    }
    
    /**
     * Invoke {@link #writeResults}.
     */
    after(String[] args): shellMain(args){
    	writeResults();
    	gwt.setWroteResults(wroteResults);
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
    
    /**
     * Reads out the options, that can be passed to Rhino-shell`s main() and 
     * sets {@link #graphname} to the filename of the javascript file that 
     * can be passed to the shell (-f option). Jvascript files that include 
     * the 'shell' are ignored.
     */
    private String getGraphName(String[] args){
    	String graphname = null;
    	for(int i = 0; i < args.length; i++){
    		if (args[i].equals("-f")) {
                if (++i == args.length){
                    System.out.println("No js inputfile secified!");
                    System.exit(1);
                }
                else{
               		for(int j = i; j < args.length; j++){
               			if(!args[j].matches(".*shell.*") && !args[j].equals("-f")){
               				int ind = args[j].indexOf("tests");
               				if(ind != -1){
               					ind += 6;
               					graphname = args[j].substring(ind);
                    			return graphname;
               				}
               				else{
               					ind = args[j].indexOf("./");
               					if(ind != -1){
               						ind += 2;
                   					graphname = args[j].substring(ind);
                        			return graphname;
               					}
               					else{
               						graphname = args[j];
               						return graphname;
               					}
               				}
               			}
                	}
                }
            }
    	}
    	return graphname;
    }
    
}
