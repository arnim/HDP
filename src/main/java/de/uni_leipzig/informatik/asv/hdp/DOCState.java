/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

import de.uni_leipzig.informatik.asv.io.Document;


/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class DOCState {
	
	static int idCounter = 0;
	
	int docID, documentLength, numberOfTables;

	int[] tableToTopic; 
    int[] wordCountByTable;
	WordState[] words;

	
	public DOCState(Document doc, int docID){  
		this.docID = docID;
	    numberOfTables = 0;  
	    documentLength = doc.getLength();
	    words = new WordState[documentLength];	
	    wordCountByTable = new int[2];
	    tableToTopic = new int[2];
	    int word, count, m = 0;
	    for (int n = 0; n < doc.getNumberOfUniquTerms(); n++) {
	        word  = doc.getTerm(n);
	        count = doc.getCount(n);
	        for (int j = 0; j < count; j++) {
	            words[m] = new WordState(word, -1);
	            m++;
	        }
	    }
	}

	
	public void defragment(int[] kOldToKNew) {
	    int[] tOldToTNew = new int[numberOfTables];
	    int t, newNumberOfTables = 0;
	    for (t = 0; t < numberOfTables; t++){
	        if (wordCountByTable[t] > 0){
	            tOldToTNew[t] = newNumberOfTables;
	            tableToTopic[newNumberOfTables] = kOldToKNew[tableToTopic[t]];
	            Utils.swap(wordCountByTable, newNumberOfTables, t);
	            newNumberOfTables ++;
	        } else 
	        	tableToTopic[t] = -1;
	    }
	    numberOfTables = newNumberOfTables;
	    for (int i = 0; i < documentLength; i++)
	        words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
	}

	
	
}
