/*
 * Copyright 2011 Arnim Bleier
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;

import de.uni_leipzig.informatik.asv.io.Corpus;
import de.uni_leipzig.informatik.asv.io.SVNCorpus;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class HDP {


	public static void main(String[] args) throws FileNotFoundException {
		if (args.length!=2) {
			System.out.println("The application needs to params.");
			System.out.println("Use program arguments: src.corpus ~target/");;
			return;
		}
		

		Corpus corpus = new SVNCorpus();
		((SVNCorpus) corpus).read(args[0]);
		HDPGibbsSampler state = new HDPGibbsSampler();
		
		state.numberOfTopics = 1;
		state.beta = .05;
		
		state.initGibbsState(corpus);
		
		System.out.println("numberOfTopics="+state.numberOfTopics);
		System.out.println("sizeOfVocabulary="+state.sizeOfVocabulary);
		System.out.println("totalNumberOfWords="+state.totalNumberOfWords);
		System.out.println("NumberOfDocs="+state.docStates.length);

		state.run(args[1], true, 10, 2001, 1000);

	}

}
