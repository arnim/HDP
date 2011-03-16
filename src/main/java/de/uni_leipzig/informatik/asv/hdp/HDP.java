package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;

public class HDP {


	public static void main(String[] args) throws FileNotFoundException {
		if (args.length!=2) {
			System.out.println("The application needs to params.");
			System.out.println("Use program arguments: src.corpus ~target/");;
			return;
		}
		

		Corpus corpus = new Corpus();
		corpus.read(args[0]);
		HDPGibbsSampler state = new HDPGibbsSampler();
		
		state.numberOfTopics = 1;
		state.beta = .1;
		
		state.initGibbsState(corpus);
		
		System.out.println("numberOfTopics="+state.numberOfTopics);
		System.out.println("sizeOfVocabulary="+state.sizeOfVocabulary);
		System.out.println("totalNumberOfWords="+state.totalNumberOfWords);
		System.out.println("NumberOfDocs="+state.docStates.length);

		state.run(args[1], true, 10, 2001, 100);

	}

}
