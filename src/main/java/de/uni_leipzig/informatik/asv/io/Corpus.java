/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */
package de.uni_leipzig.informatik.asv.io;


import java.util.List;


/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public interface Corpus extends List<Document> {
	
	public int getVocabularySize();
	
	public int getTotalNumberOfWords();
	

}
