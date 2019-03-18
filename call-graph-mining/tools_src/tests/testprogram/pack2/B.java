package testprogram.pack2;

/**
 * Just a dummy for testing.
 * @author CO
 *
 */
public class B {
	
	public void methB(){
		;
	}
	
	/**
	 * 10 times calls {@link D#methA()}
	 * @param x
	 */
	public void methB(int x){
		for(int i = 0; i < 10; i++){
			D.methA();
		}
	}
	
	/**
	 * Calls {@link #internalA()}
	 */
	public void methA(){
		internalA();
	}
	
	private void internalA(){
		;
	}

}
