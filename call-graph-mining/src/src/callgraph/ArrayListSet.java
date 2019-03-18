package callgraph;

import java.util.*;
import java.io.*;

/**
 * This data structure combines the advantages of {@link ArrayList} with a set behavior.
 * In contrast to {@link ArrayList} it is a light-weight on-purpose implementation.
 * Code is partwise c&p-ed from ArrayList.
 * @author Christopher Oßner
 */
public class ArrayListSet extends AbstractList implements Set, Serializable{

	private static final long serialVersionUID = 1L;
	
	private int size = 0;
	
	private Object[] elementData;
	
	/**
	 * @param initialCapacity the initial capacity
	 */
	public ArrayListSet(int initialCapacity) {
		super();
	        if (initialCapacity < 0)
	            throw new IllegalArgumentException("Illegal Capacity: "+
	                                               initialCapacity);
		this.elementData = new Object[initialCapacity];
    }

	/**
	 * By default the initial capacity is set to 10.
	 */
	public ArrayListSet() {
		this(10);
    }
	
	/**
	 * Adds elements to the structure if they are not already present.
	 * @return true if the element was added, false otherwise
	 */
	public boolean add(Object o){
		if( !this.contains(o) ){
			ensureCapacity(size + 1);  // Increments modCount!!
			elementData[size++] = o;
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Adds elements to the structure without checking their presence.
	 * May be used if {@link #contains(Object)} is called before and
	 * returns false. CAUTION set behavior must be ensured by YOU!
	 * @param o the element to add
	 */
	public void addUnchecked(Object o){
		ensureCapacity(size + 1);
		elementData[size++] = o;
	}
	
	public Object set(int index, Object element) {
		RangeCheck(index);

		Object oldValue = elementData[index];
		elementData[index] = element;
		return oldValue;
	}
	
	public boolean contains(Object o){
		return indexOf(o) >= 0;
	}
	
	public void ensureCapacity(int minCapacity) {
		modCount++;
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
		    Object oldData[] = elementData;
		    int newCapacity = (oldCapacity * 3)/2 + 1;
	    	    if (newCapacity < minCapacity)
			newCapacity = minCapacity;
		    elementData = new Object[newCapacity];
		    System.arraycopy(oldData, 0, elementData, 0, size);
		}
	}
	
	public int size() {
		return size;
	}
	
	public Object get(int index) {
		this.RangeCheck(index);

		return elementData[index];
	}
	
	public int indexOf(Object elem) {
		if (elem == null) {
		    for (int i = 0; i < size; i++)
			if (elementData[i]==null)
			    return i;
		} else {
		    for (int i = 0; i < size; i++){
			if (elem.equals(elementData[i]))
			    return i;}
		}
		return -1;
	}
	
	private void RangeCheck(int index) {
		if (index >= size || index < 0)
		    throw new IndexOutOfBoundsException(
			"Index: "+index+", Size: "+size);
	}

}
