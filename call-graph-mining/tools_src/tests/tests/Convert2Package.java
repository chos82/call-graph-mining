package tests;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphConverter;
import callgraph.GraphReader;
import callgraph.GraphWriter;
import callgraph.Signature;
import callgraph.SuccessorList;
import testprogram.pack1.Harness;

/**
 * Executes {@link Harness#main(String[])} and tests on the generated graph.
 * Converts it to package level.
 * @author CO
 *
 */
public class Convert2Package {
	
	private AdjacenceList graph;
	
	/**
	 * We have to take care to not forget about the calls from {@link Harness#main(String[])}
	 */
	@Test public void main(){
		Signature sigPack1 = new Signature("package", "testprogram.pack1");
		Signature sigPack2 = new Signature("package", "testprogram.pack2");
		Signature sigJre = new Signature("method", "jre");
		SuccessorList succPack1 = graph.getSuccessors( sigPack1 );
		SuccessorList succPack2 = graph.getSuccessors( sigPack2 );
		SuccessorList succJre = graph.getSuccessors( sigJre );
		
		//pack1 -> pack1
		int ind = succPack1.indexOf( sigPack1 );
		int callFrequency = ( (AdjacenceElement) succPack1.get(ind) ).getWeight();
		int expectedCalls = 6 + 			/* C<init>, A$1<init> (new Goo()), A$InnerClass<init>,
		 									   C.recursion, Harnes -> {A<innit>, A.exec() */
		 	Harness.foreignClass *2 + 		// C<init> C.staticMeth, C.recursion(..)
		 	Harness.timesA + 				// methA()
		 	Harness.timesB + 				// methB()
		 	Harness.innerClassCalls +	    // A$InnerClass.foo()
		 	Harness.interfaceCalls; 		// Goo.inc()
		assertTrue( callFrequency == expectedCalls );
		
		int expectedCalledClasses = 5; 		// A, A$Goo, A$InnerClass, C, A$1 (new Goo())
		int calledClasses = ( (AdjacenceElement) succPack1.get(ind) ).getNoSuccClasses();
		assertTrue( calledClasses == expectedCalledClasses );
		
		int expectedCalledMeth = 11;  		/* A<init>, A.exec(), A$1<init>,
		 									   A$Goo.inc(), A$InnerClass<init>, A$InnerClass.foo(),
		 									   A.methA(), A.metB(), C<init>, C.recursion(), C.staticMeth() */
		int calledMeth = ( (AdjacenceElement) succPack1.get(ind) ).getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		int expectedCallingClasses = 3;				// A, A$Goo, A$InnerClass
		int callingClasses = ( (AdjacenceElement) succPack1.get(ind) ).getNoPreClasses();
		assertTrue( callingClasses == expectedCallingClasses );
		
		int expectedCallingMeth = 4;		/* A.exec(), C<init>, C.recursion(), Harness.main() */
		int callingMeth = ( (AdjacenceElement) succPack1.get(ind) ).getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// pack1 -> pack2
		ind = succPack1.indexOf( sigPack2 );
		AdjacenceElement ae = ( (AdjacenceElement) succPack1.get(ind) );
		
		expectedCalls = Harness.foreignA +		// B.methB()
						Harness.foreignB *3 +   // B.methB() + 2* B.methB(int)
						1;						// B<init>
		callFrequency = ae.getWeight();
		assertTrue( expectedCalls == callFrequency );
		
		expectedCalledClasses = 1;				// B;
		calledClasses = ae.getNoSuccClasses();
		assertTrue( expectedCalledClasses == calledClasses );
		
		expectedCalledMeth = 4;					// B<init>, B.methA(), B.methB(), B.methB(int)
		calledMeth = ae.getNoSuccMethods();
		assertTrue( expectedCalledMeth == calledMeth );
		
		expectedCallingClasses = 1;				// A
		callingClasses = ae.getNoSuccClasses();
		assertTrue( expectedCallingMeth == callingMeth );
		
		expectedCallingMeth = 1;				// A.exec()
		callingMeth = ae.getNoPreMethods();
		assertTrue( expectedCallingMeth == callingMeth );
		
		// pack1 -> jre
		ind = succPack1.indexOf( sigJre );
		ae = ( (AdjacenceElement) succPack1.get(ind) );
		
		// println(String) is called once in Harness.main(), ArrayList<init>
		expectedCalls = Harness.jre1Calls + Harness.jre2Calls +2; 
		callFrequency = ae.getWeight();
		assertTrue( expectedCalls == callFrequency );
		
		calledClasses = ae.getNoSuccClasses();
		expectedCalledClasses = 3; 				// Collections, PrintStream, ArrayList
		assertTrue( calledClasses == expectedCalledClasses );
		
		calledMeth = ae.getNoSuccMethods();
		expectedCalledMeth = 4;					/* Collections.sort(..), PrintStream.println(),
												   PrintStream.println(String), ArrayList<init> */
		assertTrue( calledMeth == expectedCalledMeth );
		
		callingClasses = ae.getNoPreClasses();
		expectedCallingClasses = 2;				// A, Harness
		assertTrue( callingMeth == expectedCallingMeth );
		
		callingMeth = ae.getNoPreMethods();
		expectedCallingMeth = 2; 				// A.exec(), Harness.main()
		assertTrue( callingMeth == expectedCallingMeth );
		
		// pack2 -> pack2
		ind = succPack2.indexOf( sigPack2 );
		ae = ( (AdjacenceElement) succPack2.get(ind) );
		
		// println(String) is called once in Harness.main()
		expectedCalls = Harness.foreignA + 		// B.internalA()
						Harness.foreignB *20;	// B.methB(int) -> D.methA()
		callFrequency = ae.getWeight();
		assertTrue( expectedCalls == callFrequency );
		
		calledClasses = ae.getNoSuccClasses();
		expectedCalledClasses = 2; 				// B, D
		assertTrue( calledClasses == expectedCalledClasses );
		
		calledMeth = ae.getNoSuccMethods();
		expectedCalledMeth = 2;					// B.internalA(), D.methA()
		assertTrue( calledMeth == expectedCalledMeth );
		
		callingClasses = ae.getNoPreClasses();
		expectedCallingClasses = 1;				// B
		assertTrue( callingMeth == expectedCallingMeth );
		
		callingMeth = ae.getNoPreMethods();
		expectedCallingMeth = 2; 				// B.methA(), B.methB(int)
		assertTrue( callingMeth == expectedCallingMeth );
		
		// jre never has a successor
		assertTrue( succJre == null );
	}
	
	@Before public void setUp(){
		String[] foo = {};
		Harness.main(foo);
		GraphReader gr = new GraphReader();
		AdjacenceList graphTemp = gr.readGraph("testIO\\graph_data\\test_graph.ser");
		GraphConverter gc = new GraphConverter(graphTemp);
		graph = gc.methods2Package();
		GraphWriter gw = new GraphWriter(graph, "testIO\\graph_data", "test_graph_package");
		gw.writeAdjacence();
	}

}
