/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.io;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class TopicsFileWriter implements TopicsWriter {
	

	private String workingDir;


	public TopicsFileWriter(String workingDir) {
		this.workingDir = workingDir;
	}
	
	public void writeWordCountByTopicAndTerm(int[][] wordCountByTopicAndTerm, int K, int V, int iter) 
	throws FileNotFoundException {
		PrintStream file = new PrintStream(workingDir + iter + "-topics.dat");
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++)
				file.format("%05d ",wordCountByTopicAndTerm[k][w]);
			file.println();
		}
		file.close();
	}
	
	
}
