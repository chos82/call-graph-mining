package tests;


import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import testprogram.pack1.Harness;
import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphConverter;
import callgraph.GraphReader;
import callgraph.GraphWriter;
import callgraph.Signature;
import callgraph.SuccessorList;

public class Convert2Method {
	
	private AdjacenceList graph;

	@Test
	public void main(){
		Signature sigMethA = new Signature("method", "testprogram.pack2.B.methA()");
		Signature sigMethB = new Signature("method", "testprogram.pack2.B.methB()");
		Signature sigMethB2 = new Signature("method", "testprogram.pack2.B.methB(int)");
		Signature sigDummy = new Signature("method", "foreign.Class.method(..)", true);
		Signature sigDummyJre = new Signature("method", "jre.Class.method(..)", true);
		Signature sigInternalA = new Signature("method", "testprogram.pack2.B.internalA()");
		SuccessorList succMethB2 = graph.getSuccessors( sigMethB2 );
		SuccessorList succDummy = graph.getSuccessors( sigDummy );
		SuccessorList succMethA = graph.getSuccessors( sigMethA );
		
		// B.methA() -> B.internalA()
		int ind = succMethA.indexOf( sigInternalA );
		AdjacenceElement ae = ( (AdjacenceElement) succMethA.get(ind) );
		int expectedCalls = Harness.foreignA;
		int callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		// foreign.Class.method() -> B.methA()
		ind = succDummy.indexOf( sigMethA );
		callFrequency = ( (AdjacenceElement) succDummy.get(ind) ).getWeight();
		expectedCalls = Harness.foreignA;
		assertTrue( callFrequency == expectedCalls );
		
		// foreign.Class.method(..) -> B.methB()
		ind = succDummy.indexOf( sigMethB );
		callFrequency = ( (AdjacenceElement) succDummy.get(ind) ).getWeight();
		expectedCalls = Harness.foreignB;
		assertTrue( callFrequency == expectedCalls );
		
		// foreign.Class.method(..) -> B.methB(int)
		ind = succDummy.indexOf( sigMethB2 );
		callFrequency = ( (AdjacenceElement) succDummy.get(ind) ).getWeight();
		expectedCalls = Harness.foreignB *2;
		assertTrue( callFrequency == expectedCalls );
		
		// foreign.Class.method(..) -> jre.Class.method(..)
		ind = succDummy.indexOf( sigDummyJre );
		callFrequency = ( (AdjacenceElement) succDummy.get(ind) ).getWeight();
		expectedCalls = 
			2 +						// Harness.main() -> System.out.println, A.exec() -> ArrayList<init>
			Harness.jre1Calls +
			Harness.jre2Calls;
		assertTrue( callFrequency == expectedCalls );
		
		// B.methB(int) -> foreign.Class.method(..)
		ind = succMethB2.indexOf( sigDummy );
		callFrequency = ( (AdjacenceElement) succMethB2.get(ind) ).getWeight();
		expectedCalls = Harness.foreignB *20;
		assertTrue( callFrequency == expectedCalls );
		
		// foreign.Class.method(..) -> foreign.Class.method(..)
		ind = succDummy.indexOf( sigDummy );
		callFrequency = ( (AdjacenceElement) succDummy.get(ind) ).getWeight();
		expectedCalls = 
			6 +							/* A.<init>, A.exec(), C.<init>,
			 							   A$InnerClass.<init>, A$1.<init>
			 							   C.recursion(int) */
			Harness.timesA +			// A.methA()
			Harness.timesB +			// A.methB()
			Harness.foreignClass *2 + 	// C.staticMeth(), C.recursion
			Harness.innerClassCalls +	// Goo.inc()
			Harness.interfaceCalls;		// InnerClass.foo()
		assertTrue( callFrequency == expectedCalls );
	}
	
	/**
	 * Runs {@link Harness#main(String[])} reads in the generated graph, converts it 
	 * to method level ({@link GraphConverter#methods2class(java.util.HashSet)}), where
	 * classes are set to {@code testprogram.pack2.B}.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		String[] foo = {};
		Harness.main(foo);
		GraphReader gr = new GraphReader();
		AdjacenceList graphTemp = gr.readGraph("testIO\\graph_data\\test_graph.ser");
		GraphConverter gc = new GraphConverter(graphTemp);
		HashSet<Signature> classes = new HashSet<Signature>();
		classes.add( new Signature( "package", "testprogram.pack2.B" ) );
		graph = gc.methods(classes);
		GraphWriter gw = new GraphWriter(graph, "testIO\\graph_data", "test_graph_methods");
		gw.writeAdjacence();
		System.out.println(graphTemp);
	}

}
