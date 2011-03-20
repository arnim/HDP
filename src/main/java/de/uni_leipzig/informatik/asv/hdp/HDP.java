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
		if (args.length!=2) {
			System.out.println("The application needs to params.");
			System.out.println("Use program arguments: src.corpus ~target/");;
			return;
		}
		

		Corpus corpus = new CLDACorpus();
		((CLDACorpus) corpus).read(args[0]);
		HDPGibbsSampler state = new HDPGibbsSampler();
		
		state.numberOfTopics = 1;
		state.beta = .5;
		
		state.initGibbsState(corpus);
		
		System.out.println("numberOfTopics="+state.numberOfTopics);
		System.out.println("sizeOfVocabulary="+state.sizeOfVocabulary);
		System.out.println("totalNumberOfWords="+state.totalNumberOfWords);
		System.out.println("NumberOfDocs="+state.docStates.length);

		state.run(true, 10, 2001, 1000, System.out, new TopicsFileWriter(args[1]), new WordAssignmentsFileWriter(args[1]));

	}

}
