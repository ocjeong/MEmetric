package proteinJavanise;

import java.util.Comparator;

public class PatternComparator<T> implements Comparator<T> {
	@Override
	public int compare(T arg0, T arg1) { 
		// TODO Auto-generated method stub
		return arg0.toString().compareTo(arg1.toString());
	}
}
