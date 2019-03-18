package testprogram;

import java.io.File;

import callgraph.*;

/**
 * Almost the same as used to instrument Rhino, slight adjustments
 * to fit the needs here.
 * @author Christopher Oßner
 */
public aspect Trace {
    /**
     * This pointcut catches the joinpoints of calls of each method that belongs to 
     * the instrumented program. Executions of jre methods are not considered, 
     * as well as any execution of methods that belong to other packages of this 
     * framework.
     */
    pointcut methodCall(): call(* *(..))
    					   || call(new(..));
    
    pointcut main():
    	execution(* main(String[]))
    	&& !within(bl..*);
    
    /**
     * Used to model the call-graph.
     */
    private AdjacenceList al;
    
    /**
     * Used to provide a output directory for {@link #gw}.
     */
    private static String file = "test_graphs";
    
    /**
     * Used to store a name for the output graph. {@link #gw} uses it as filename.
     */
    private static String graphname = "test_graph";
    
    /**
     * Is used here to write the serialized objects (adjacency lists).
     */
    private GraphWriter gw;
    
    before(): main()
    			&& !within(callgraph.*)
	   			&& !within(Trace)
	   			&& !within(tests.*)
	   			&& !within(bl..*){
    	al = new AdjacenceList();
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
	   			&& !within(Trace)
	   			&& !within(tests.*)
				&& !within(bl..*){
     	org.aspectj.lang.Signature sig = thisJoinPointStaticPart.getSignature();
     	org.aspectj.lang.Signature caller = thisEnclosingJoinPointStaticPart.getSignature();
   		al.addCall(caller, sig);
    }
    
    after(): main()
    		&& !within(callgraph.*)
			&& !within(Trace)
			&& !within(tests.*)
			&& !within(bl..*){
    	gw = new GraphWriter(al, file, graphname);
    	File f = new File(file);
    	f.mkdir();
    	gw.writeAdjacence();
    }
}