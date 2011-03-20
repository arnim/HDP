/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.io;

import java.io.IOException;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public interface WordAssignmentsWriter {
	
	public void openForIteration(int iter) throws IOException;
	
	public void writeAssignment(int docID, int term, int topic, int table) throws IOException;
	
	public void closeIteration() throws IOException;

}
