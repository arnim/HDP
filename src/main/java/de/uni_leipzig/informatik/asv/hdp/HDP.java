/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

import java.io.IOException;

import de.uni_leipzig.informatik.asv.io.CLDACorpus;
import de.uni_leipzig.informatik.asv.io.Corpus;
import de.uni_leipzig.informatik.asv.io.TopicsFileWriter;
import de.uni_leipzig.informatik.asv.io.WordAssignmentsFileWriter;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class HDP {


	public static void main(String[] args) throws IOException {
		 int saveLag = 0, iter = 0;
		 String inputFile = null, outputDir = null;
		 Corpus corpus = new CLDACorpus();
		 HDPGibbsSampler state = new HDPGibbsSampler();
		try {
			state.beta = Double.parseDouble(args[0]);
			state.alpha = Double.parseDouble(args[1]);
			state.gamma = Double.parseDouble(args[2]);
			iter = Integer.parseInt(args[3]);
			saveLag = Integer.parseInt(args[4]);
			inputFile = args[5];
			outputDir = args[6];
			state.numberOfTopics = Integer.parseInt(args[7]);
		} catch (Exception e) {
			System.out.println("CRF Gibbs sampling for the Hierarchical Dirichlet Processes");
			System.out.println("The application nees the folowing params in exact order");
			System.out.println("beta alpha gamma iterations saveLag inputFile outputDir initialNumberOfTOpics");
			System.out.println("Example:");
			System.out.println("HDP 0.5 1.5 1.0 2001 500 ./input.corpus ./outputdir/ 5");
			System.exit(0);
		}
		
		((CLDACorpus) corpus).read(inputFile);
		state.initGibbsState(corpus);
		
		System.out.println("sizeOfVocabulary="+state.sizeOfVocabulary);
		System.out.println("totalNumberOfWords="+state.totalNumberOfWords);
		System.out.println("NumberOfDocs="+state.docStates.length);

		state.run(true, 10, iter, saveLag, System.out, 
				new TopicsFileWriter(outputDir), new WordAssignmentsFileWriter(outputDir));
	}

}
