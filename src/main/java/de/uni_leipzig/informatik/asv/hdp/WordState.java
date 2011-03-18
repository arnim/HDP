/*
 * Copyright 2011 Arnim Bleier
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class WordState {   
	
	int termIndex;
	int tableAssignment;

	
	public WordState(int wordIndex, int tableAssignment){
		this.termIndex = wordIndex;
		this.tableAssignment = tableAssignment;
	}


}
