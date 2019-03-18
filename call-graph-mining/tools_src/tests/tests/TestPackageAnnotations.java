package tests;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphConverter;
import callgraph.Signature;
import callgraph.SuccessorList;
import junit.framework.TestCase;

/**
 * Uses a hand crafted graph to test primary the GraphConverter.
 * JRE calls and the dealing with dummies is NOt tested.
 * @author CO
 *
 */
public class TestPackageAnnotations extends TestCase {
	
	private AdjacenceList in = new AdjacenceList(), out = new AdjacenceList();
	
    public void setUp() {
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "a.A.b()"));
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "a.A.b()"));
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "a.A.b()"));
       
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "a.B.a()"));
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "a.B.a()"));
       
       in.addCall(new Signature("method", "a.A.a()"), new Signature("method", "b.A.a()"));
       
       in.addCall(new Signature("method", "a.B.a()"), new Signature("method", "b.A.a()"));
       in.addCall(new Signature("method", "a.B.a()"), new Signature("method", "b.A.a()"));
       in.addCall(new Signature("method", "a.B.a()"), new Signature("method", "b.A.a()"));
       
       in.addCall(new Signature("method", "a.B.a()"), new Signature("method", "a.B.b()"));
       
       in.addCall(new Signature("method", "a.B.b()"), new Signature("method", "b.A.a()"));
       in.addCall(new Signature("method", "a.B.b()"), new Signature("method", "b.A.a()"));
       // a -> a (6,2,3,2,3); a -> b (6,1,1,2,2)
       //System.out.println("IN:\n\n"+in);
       
       out = (new GraphConverter(in).methods2Package());
       
       //System.out.println("OUT:\n\n"+out);
    }
    
    public void testCallFrequency() {
       SuccessorList a = (SuccessorList) out.get(new Signature("package", "a"));
       AdjacenceElement ae_a1 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "a")));
       int ae_a1_f = ae_a1.getWeight();
       assertEquals(6, ae_a1_f);
       AdjacenceElement ae_a2 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "b")));
       int ae_a2_f = ae_a2.getWeight();
       assertEquals(6, ae_a2_f);
    }
    
    public void testSuccClassesFrequency() {
        SuccessorList a = (SuccessorList) out.get(new Signature("package", "a"));
        AdjacenceElement ae_a1 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "a")));
        int ae_a1_f = ae_a1.getNoSuccClasses();
        assertEquals(2, ae_a1_f);
        AdjacenceElement ae_a2 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "b")));
        int ae_a2_f = ae_a2.getNoSuccClasses();
        assertEquals(1, ae_a2_f);
     }
    
    public void testSuccMethFrequency() {
        SuccessorList a = (SuccessorList) out.get(new Signature("package", "a"));
        AdjacenceElement ae_a1 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "a")));
        int ae_a1_f = ae_a1.getNoSuccMethods();
        assertEquals(3, ae_a1_f);
        AdjacenceElement ae_a2 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "b")));
        int ae_a2_f = ae_a2.getNoSuccMethods();
        assertEquals(1, ae_a2_f);
     }
    
    public void testPreClassesFrequency() {
        SuccessorList a = (SuccessorList) out.get(new Signature("package", "a"));
        AdjacenceElement ae_a1 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "a")));
        int ae_a1_f = ae_a1.getNoPreClasses();
        assertEquals(2, ae_a1_f);
        AdjacenceElement ae_a2 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "b")));
        int ae_a2_f = ae_a2.getNoPreClasses();
        assertEquals(2, ae_a2_f);
     }
    
    public void testPreMethodsFrequency() {
        SuccessorList a = (SuccessorList) out.get(new Signature("package", "a"));
        AdjacenceElement ae_a1 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "a")));
        int ae_a1_f = ae_a1.getNoPreMethods();
        assertEquals(2, ae_a1_f);
        AdjacenceElement ae_a2 = (AdjacenceElement)a.get(a.indexOf(new Signature("package", "b")));
        int ae_a2_f = ae_a2.getNoPreMethods();
        assertEquals(3, ae_a2_f);
     }

}
