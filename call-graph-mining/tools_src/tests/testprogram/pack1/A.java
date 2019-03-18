package testprogram.pack1;

import java.util.ArrayList;
import java.util.Collections;

import testprogram.pack2.B;

/**
 * Just a dummy for testing.
 * @author CO
 *
 */
public class A {
	
	/**
	 * To create an abstract class
	 * @author CO
	 */
	interface Goo{
		public int inc(int x);
	}
	
	class InnerClass{
		void foo(){
			;
		}
	}
	
	private int timesA = 0;
	private int timesB = 0;
	private int foreignA;
	private int foreignB;
	private int interfaceCalls;
	private int innerClassCalls;
	private int jre1Calls;
	private int jre2Calls;
	private int foreignClass;
	
	/**
	 * @param timesA calls to methA
	 * @param timesB calls to methB
	 * @param foreignClass used as parameter to one call of {@link C#recursion(int)}.
	 * And as parameter to {@link C#C(int)} is also called as often.
	 * @param foreignA calls to B.methA, which calls B.internalA() (one time)
	 * @param foreignB calls to B.methB, B.methB(int) will get called 2* argument and calls 10 times D.methA()
	 * @param innerClassCalls calls to InnerClass
	 * @param interfaceCalls calls to Goo
	 * @param jre1Calls calls to System.out.println
	 * @param jre2Calls calls to Collections.sort
	 */
	A(int timesA, int timesB, int foreignClass, int foreignA, int foreignB,
	  int innerClassCalls, int interfaceCalls, int jre1Calls,
	  int jre2Calls	){
		this.timesA = timesA;
		this.timesB = timesB;
		this.foreignA = foreignA;
		this.foreignB = foreignB;
		this.innerClassCalls = innerClassCalls;
		this.interfaceCalls = interfaceCalls;
		this.jre1Calls = jre1Calls;
		this.jre2Calls = jre2Calls;
		this.foreignClass = foreignClass;
	}
	
	void exec(){
		B b = new B();
		C c = new C(foreignClass);
		InnerClass inner = new InnerClass();
		Goo goo = new Goo(){
			@Override
			public int inc(int x) {
				return x++;
			}
		};
		for(int i = 0; i < timesA; i++){
			methA();
		}
		for(int i = 0; i < timesB; i++){
			methB();
		}
		for(int i = 0; i < foreignA; i++){
			b.methA();
		}
		for(int i = 0; i < foreignB; i++){
			b.methB();
			b.methB(10);
			b.methB(10);
		}
		for(int i = 0; i < innerClassCalls; i++){
			inner.foo();
		}
		for(int i = 0; i < interfaceCalls; i++){
			goo.inc(10);
		}
		for(int i = 0; i < jre1Calls; i++){
			System.out.println();
		}
		ArrayList<Integer> al = new ArrayList<Integer>();
		for(int i = 0; i < jre2Calls; i++){
			Collections.sort(al);
		}
		c.recursion(foreignClass);
	}
	
	/**
	 * Dummy
	 */
	private void methA(){
		;
	}
	
	/**
	 * Dummy
	 */
	public void methB(){
		;
	}

}
