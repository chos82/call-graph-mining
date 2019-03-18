package testprogram.pack1;

/**
 * Triggers the test: Executes a little dummy program to check for 
 * correct graph generation and so on.
 * @author CO
 * TODO currently it is a very simple program, not reflecting all
 * aspects of real world coding
 */
public class Harness {
	
	public static final int timesA = 10;
	public static final int timesB = 83;
	public static final int foreignClass = 44;
	public static final int foreignA = 17;
	public static final int foreignB = 3;
	public static final int innerClassCalls = 9;
	public static final int interfaceCalls = 12;
	public static final int jre1Calls = 101;
	public static final int jre2Calls = 202;
	
	public static void main(String[] args){
		A a = new A(timesA, timesB, foreignClass,
				foreignA, foreignB,
				innerClassCalls, interfaceCalls,
				jre1Calls, jre2Calls );
		a.exec();
		System.out.println("Running test suite..."+
				"\nwe instantiate A with the following values:"+
				"\n#calls to A.methA(): " + timesA+
				"\n#calls to A.methB(): "+ timesB+
				"\n#calls to B.methA(): "+ foreignA+
				"\n#calls to B.methB(): "+ foreignB+
				"\n#calls to B.methB(int): "+ foreignB+
				"\n#call C( "+foreignClass+ " )"+
				"\n#call C.recursion( "+foreignClass+ " )"+
				"\n#calls to A$Goo.inc():"+ innerClassCalls+
				"\n#calls to A$1.foo(): "+ interfaceCalls+
				"\n#calls to System.out.println(): "+ jre1Calls+
				"\n#calls to Collections.sort(List l): " +jre2Calls);
	}
	
}
