/*
 * Copyright 2011 Arnim Bleier
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */
package de.uni_leipzig.informatik.asv.io;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public interface Document {
	
	public int getLength();
	
	public int getNumberOfUniquTerms();
	
	public int getTerm(int n);
	
	public int getCount(int n);

}
