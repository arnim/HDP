/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
class Utils {

	
	public static void swap(int[] arr, int arg1, int arg2){
		   int t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static void swap(int[][] arr, int arg1, int arg2) {
		   int[] t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static double[] ensureCapacity(double[] arr, int min){
		int length = arr.length;
		if (min < length)
			return arr;
		double[] arr2 = new double[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}

	public static int[] ensureCapacity(int[] arr, int min) {
		int length = arr.length;
		if (min < length)
			return arr;
		int[] arr2 = new int[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}


	public static int[][] add(int[][] arr, int[] newElement, int index) {
		int length = arr.length;
		if (length <= index){
			int[][] arr2 = new int[index*2][];
			for (int i = 0; i < length; i++) 
				arr2[i] = arr[i];
			arr = arr2;
		}
		arr[index] = newElement;
		return arr;
	}
	
	
}