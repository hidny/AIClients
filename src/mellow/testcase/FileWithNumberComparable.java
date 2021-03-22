package mellow.testcase;

import java.io.File;

public class FileWithNumberComparable implements Comparable {

	private int number = -1;
	private File file;
	
	public FileWithNumberComparable(File file, int number) {
		
		this.file = file;
		this.number = number;
	}
	
	@Override
	public int compareTo(Object o) {
		
		if(((FileWithNumberComparable)o).number < this.number) {
			return +1;
		
		//No need:
		//} else if(((FileWithNumber)o).number == this.number) {
		//	return 0;
		
		} else {
			return -1;
		}
	}

	public File getFile() {
		return file;
	}


	
}
