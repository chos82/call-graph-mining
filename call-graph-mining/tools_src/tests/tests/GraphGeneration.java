package tests;

import static org.junit.Assert.*;

import org.junit.*;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphReader;
import callgraph.Signature;
import callgraph.SuccessorList;
import testprogram.pack1.*;


public class GraphGeneration {
	
	private AdjacenceList graph;
	
	/**
	 * Executes {@link Harness#main(String[])} and tests on the generated graph.
	 * Test if the generated call-graph is correct.
	 * Note: Not every edge in the graph is tested.
	 * We only tests for the arguments to A.
	 */
	@Test public void testCallFrequency(){
		Signature sigExec = new Signature("method", "testprogram.pack1.A.exec()");
		Signature sigA = new Signature("method", "testprogram.pack1.A.methA()");
		Signature sigB = new Signature("method", "testprogram.pack1.A.methB()");
		Signature foreignA = new Signature("method", "testprogram.pack2.B.methA()");
		Signature foreignB = new Signature("method", "testprogram.pack2.B.methB()");
		Signature innerClass = new Signature("method", "testprogram.pack1.A$InnerClass.foo()");
		Signature interfaceClass = new Signature("method", "testprogram.pack1.A$Goo.inc(int)");
		Signature jre1Class = new Signature("method", "java.io.PrintStream.println()");
		Signature jre2Class = new Signature("method", "java.util.Collections.sort(java.util.List)");
		Signature sigC = new Signature("method", "testprogram.pack1.C.recursion(int)");
		Signature sigBmethA = new Signature("method", "testprogram.pack2.B.methA()");
		Signature sigBinternalA = new Signature("method", "testprogram.pack2.B.internalA()");
		Signature foreignBpolymorph = new Signature("method", "testprogram.pack2.B.methB(int)");
		SuccessorList succA = graph.getSuccessors( sigExec );
		SuccessorList succC = graph.getSuccessors( sigC );
		SuccessorList succB = graph.getSuccessors( sigBmethA );
		//A.exec -> A.methA
		int ind = succA.indexOf( sigA );
		int callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.timesA );
		// A.exec() -> A.methB
		ind = succA.indexOf(sigB);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.timesB );
		// A.exec() -> B.methA()
		ind = succA.indexOf(foreignA);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.foreignA );
		// A.exec() -> B.methB()
		ind = succA.indexOf(foreignB);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.foreignB );
		// A.exec() -> A$InnerClass.foo()
		ind = succA.indexOf(innerClass);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.innerClassCalls );
		// A.exec() -> A$Goo.inc(int)
		ind = succA.indexOf(interfaceClass);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.interfaceCalls );
		// A.exec() -> java.io.PrintStream.println()
		ind = succA.indexOf(jre1Class);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.jre1Calls );
		// A.exec() -> java.util.Collections.sort(java.util.List)
		ind = succA.indexOf(jre2Class);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.jre2Calls );
		// testprogram.pack1.C.recursion(int) -> testprogram.pack1.C.recursion(int)
		ind = succC.indexOf(sigC);
		callFrequency = ( (AdjacenceElement) succC.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.foreignClass );
		// testprogram.pack2.B.methA() -> testprogram.pack2.B.internalA()
		ind = succB.indexOf(sigBinternalA);
		callFrequency = ( (AdjacenceElement) succB.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.foreignA );
		// testprogram.pack1.A.exec() -> testprogram.pack2.B.methB(int)
		ind = succA.indexOf(foreignBpolymorph);
		callFrequency = ( (AdjacenceElement) succA.get(ind) ).getWeight();
		assertTrue( callFrequency == Harness.foreignB *2 );
	}
	
	@Before public void setUp(){
		String[] foo = {};
		Harness.main(foo);
		GraphReader gr = new GraphReader();
		graph = gr.readGraph("testIO\\graph_data\\test_graph.ser");
	}
	
}
