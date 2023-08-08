/*
 * PaperRecord class
 * 
 * 대사공학 논문 개체를 나타내는 클래스 입니다.
 * This class contains a single entity of paper, in this project, entry 
 * unit provided in the raw file by the Uniprot.
 * 
 * - This class inherits HashMap
 * - key is two-capital-alphabet string, which stands for field a protein 
 * 	entry contains
 * - Value is arrayed string that containing the field attribute in each line
 * 	as an element of the array
 */

package paperJavanise;

import java.util.ArrayList;
import java.util.HashMap;

public class PaperRecord extends HashMap<String, ArrayList<String>> {
	private static final long serialVersionUID = -9026096314267249958L;
	void printRecord() {
		this.forEach((k, v) -> {v.add(0, k); System.out.println(v);} );
	}
	static ArrayList<String> mergeValue (
	  ArrayList<String> originList, ArrayList<String> newcomerList) {
		originList.addAll(newcomerList);
		return originList;
	}
}
