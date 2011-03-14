package de.uni_leipzig.informatik.asv.hdp;

public class WordState {   
	
	int termIndex;
	int tableAssignment;

	
	public WordState(int wordIndex, int tableAssignment){
		this.termIndex = wordIndex;
		this.tableAssignment = tableAssignment;
	}


}
