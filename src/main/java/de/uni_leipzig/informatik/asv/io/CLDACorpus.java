/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Reader for data following the format described by <a href="http://www.cs.princeton.edu/~blei/">David Blei</a> 
 * in the <a href="http://www.cs.princeton.edu/~blei/lda-c/readme.txt">lda-c/readme.txt</a>
 * "Under LDA, the words of each document are assumed exchangeable. Thus, 
 * each document is succinctly represented as a sparse vector of word 
 * counts. The data is a file where each line is of the form:
 * 
 *   [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count] 
 * 
 * where [M] is the number of unique terms in the document, and the 
 * [count] associated with each term is how many times that term appeared 
 * in the document.  Note that [term_1] is an integer which indexes the 
 * term; it is not a string."
 * <p>
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class CLDACorpus extends ArrayList<Document> implements Corpus   {
	

	private static final long serialVersionUID = -8568648610437635401L;
	public int sizeVocabulary = 0;
	public int totalNumberOfWords = 0;

	
	
	public void read(InputStream is) {
		int length, word;
		SimpleDocument d;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				try {
					String[] fields = line.split(" ");
					length = Integer.parseInt(fields[0]);
					d = new SimpleDocument(length);
					for (int n = 0; n < length; n++) {
						String[] wordCounts = fields[n + 1].split(":");
						word = Integer.parseInt(wordCounts[0]);
						d.words[n] = word;
						d.counts[n] = Integer.parseInt(wordCounts[1]);
						d.total += Integer.parseInt(wordCounts[1]);
						if (word >= sizeVocabulary)
							sizeVocabulary = word + 1;
					}
					totalNumberOfWords += d.total;
					add(d);
				} catch (Exception e) {
					System.err.println(e.getMessage() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void read(String filename) throws FileNotFoundException {
		read(new FileInputStream(filename));
	}


	@Override
	public int getVocabularySize() {
		return sizeVocabulary;
	}

	@Override
	public int getTotalNumberOfWords() {
		return totalNumberOfWords;
	}



}
