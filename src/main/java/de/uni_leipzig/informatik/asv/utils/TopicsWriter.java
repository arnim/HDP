/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.utils;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class TopicsWriter  {
	

	private String outFileStr;


	public TopicsWriter(String workingDir) {
		this.outFileStr = workingDir;
	}
	
	public void writeWordCountByTopicAndTerm(int[][] wordCountByTopicAndTerm, int K, int V) 
	throws FileNotFoundException {
		PrintStream file = new PrintStream(outFileStr);
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++)
				file.format("%05d ",wordCountByTopicAndTerm[k][w]);
			file.println();
		}
		file.close();
	}
	
	
}
