package paperJavanise;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayedPaperRecord extends ArrayList<PaperRecord> {
	private static final long serialVersionUID = 1L;

	public ArrayedPaperRecord() {
	}
	
	public ArrayedPaperRecord(Collection<PaperRecord> paperRecords) {
		super(paperRecords);
	}
}
