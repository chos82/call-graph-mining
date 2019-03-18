package testprogram.pack1;

/**
 * Just a dummy for testing.
 * @author CO
 *
 */
public class C {
	
	public C(int x){
		for(int i = 0; i < x; i++){
			staticMeth();
		}
	}
	
	void recursion(int times){
		if( times > 0)
			recursion(times -1);
	}
	
	public static void staticMeth(){
		;
	}

}
