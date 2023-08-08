/*
 * ProteinRecord class
 * 
 * 단백질/유전자 개체를 나타내는 클래스 입니다.
 * This class contains single entity of protein/gene, in this project, entry 
 * unit provided in the raw file by the Uniprot.
 * 
 * - This class inherits HashMap
 * - key is two-capital-alphabet string, which stands for field a protein 
 * 	entry contains
 * - Value is arrayed string that containing the field attribute in each line
 * 	as an element of the array
 */

package proteinJavanise;

import java.util.ArrayList;
import java.util.HashMap;

public class ProteinRecord extends HashMap<String, ArrayList<String>> {
	private static final long serialVersionUID = 1L;
	
}