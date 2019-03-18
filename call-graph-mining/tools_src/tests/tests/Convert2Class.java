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

public class Convert2Class {
	
	private AdjacenceList graph;
	
	@Test public void main(){
		Signature sigA = new Signature("class", "testprogram.pack1.A");
		Signature sigDummyPack = new Signature("class", "foreign.Package", true);
		Signature sigDummyClass = new Signature("class", "foreign.Class", true);
		Signature sigDummyJre = new Signature("class", "jre.Class", true);
		SuccessorList succA = graph.getSuccessors( sigA );
		SuccessorList succDummyPack = graph.getSuccessors( sigDummyPack );
		SuccessorList succDummyClass = graph.getSuccessors( sigDummyClass );
		
		// A -> A
		int ind = succA.indexOf( sigA );
		AdjacenceElement ae = ( (AdjacenceElement) succA.get(ind) );
		int expectedCalls = 
			Harness.timesA + 				// methA()
		 	Harness.timesB;					// methB()
		int callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		int expectedCalledMeth = 2;  		// A.methA(), A.methB()
		int calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		int expectedCallingMeth = 1;		/* A.exec() */
		int callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// A -> ForeignPackage (dummy)
		ind = succA.indexOf( sigDummyPack );
		ae = ( (AdjacenceElement) succA.get(ind) );
		expectedCalls = 
			1 +								// B.<init>
			Harness.foreignA + 				// B.methA()
		 	Harness.foreignB *3;			// B.methB(), 2* B.methB(int)
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 4;  			/* B.methA(), B.methB(), B.<init>,
										   	   B.methB(int) */
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 1;			/* A.exec() */
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// A -> ForeignClass (dummy)
		ind = succA.indexOf( sigDummyClass );
		ae = ( (AdjacenceElement) succA.get(ind) );
		expectedCalls = 
			4 +								/* C.<init>(int), C.recursion(int),
											   A.InnerClass<init>, A$1 (Goo.<init>) */
			Harness.innerClassCalls	+		// InnerClass.foo()
			Harness.interfaceCalls;			// Goo.inc()
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 6;  			/* C.<init>(int), C.recursion(int),
											   A.InnerClass<init>, A$1 (Goo.<init>),
											   InnerClass.foo()
											   Goo.inc() */
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 1;			/* A.exec() */
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// a -> JRE (dummy)
		ind = succA.indexOf( sigDummyJre );
		ae = ( (AdjacenceElement) succA.get(ind) );
		expectedCalls = 
			1 +								// ArrayList.<init> 
			Harness.jre1Calls	+			// System.out.println()
			Harness.jre2Calls;				// Collections.sort(..)
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 3;  			/* ArrayList.<init> 
											   System.out.println()
											   Collections.sort(..) */
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 1;			/* A.exec() */
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// ForeignPackage -> ForeignPackage (dummy)
		ind = succDummyPack.indexOf( sigDummyPack );
		ae = ( (AdjacenceElement) succDummyPack.get(ind) );
		expectedCalls = 
			Harness.foreignB *20 +			/* B.methB(int) is called Harness.foreignB *2 and calls
											   10* D.methA() */
			Harness.foreignA;				// B.methA -> B.internalA
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 2;  			// B.innternalA(), D.methA()
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 2;			// B.methA(), B.methB(int)
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// ForeignClass -> A
		ind = succDummyClass.indexOf( sigA );
		ae = ( (AdjacenceElement) succDummyClass.get(ind) );
		expectedCalls = 2;					// Harness.main() -> {A.<init>(), A.exec()}
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 2;
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 1;
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// ForeignClass -> ForeignClass
		ind = succDummyClass.indexOf( sigDummyClass );
		ae = ( (AdjacenceElement) succDummyClass.get(ind) );
		expectedCalls = 88;					// C.recursion(int), C.staticMeth()
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 2;
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 2;
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
		
		// ForeignClass -> JRE
		ind = succDummyClass.indexOf( sigDummyJre );
		ae = ( (AdjacenceElement) succDummyClass.get(ind) );
		expectedCalls = 1;					// Harness.main() -> System.out.println
		callFrequency = ae.getWeight();
		assertTrue( callFrequency == expectedCalls );
		
		expectedCalledMeth = 1;
		calledMeth = ae.getNoSuccMethods();
		assertTrue( calledMeth == expectedCalledMeth );
		
		expectedCallingMeth = 1;
		callingMeth = ae.getNoPreMethods();
		assertTrue( callingMeth == expectedCallingMeth );
	}
	
	/**
	 * Runs {@link Harness#main(String[])} reads in the generated graph, converts it 
	 * to class level ({@link GraphConverter#methods2class(java.util.HashSet)}), where
	 * packages are set to {@code testprogram.pack1} and the included vertices are set
	 * to {@code testprogram.pack1.A}.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		String[] foo = {};
		Harness.main(foo);
		GraphReader gr = new GraphReader();
		AdjacenceList graphTemp = gr.readGraph("ttestIO\\graph_data\\test_graph.ser");
		GraphConverter gc = new GraphConverter(graphTemp);
		HashSet<Signature> packs = new HashSet<Signature>();
		HashSet<Signature> incClasses = new HashSet<Signature>();
		incClasses.add( new Signature( "class", "testprogram.pack1.A" ) );
		packs.add( new Signature( "package", "testprogram.pack1" ) );
		gc.includeVertices(incClasses);
		graph = gc.methods2class(packs);
		GraphWriter gw = new GraphWriter(graph, "testIO\\graph_data", "test_graph_class");
		gw.writeAdjacence();
	}

}
