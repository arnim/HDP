/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.io;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class WordAssignmentsFileWriter implements WordAssignmentsWriter {
	
	private String workingDir;
	private PrintStream file = null;


	public WordAssignmentsFileWriter(String workingDir) {
		this.workingDir = workingDir;
	}


	@Override
	public void writeAssignment(int docID, int term, int topic, int table) {
		file.println(docID + " " + term + " " + topic + " " + table);
	}


	@Override
	public void openForIteration(int iter) throws IOException {
		if (file != null)
			file.close(); // throw new IOException("closeIteration() must be called before opening an new iteration");
		this.file = new PrintStream(workingDir + iter + "-word-assignments.dat");
		file.println("d w z t");
	}


	@Override
	public void closeIteration() throws IOException {
		file.close();
		file = null;
	}

}
