package org.opencv.samples.tutorial2;

import java.util.ArrayList;

import android.util.Log;

public class ListItem {
	public int questionNum;
	public boolean choiceA;
	public boolean choiceB;
	public boolean choiceC;
	public boolean choiceD;

	// default constructor
	public ListItem() {
		this(0, false, false, false, false);
	}

	public static void printListItemArrayList(ArrayList<ListItem> l) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < l.size(); i++) {
			buf.append(l.get(i).toString() + "\n");
		}
		Log.v("Vaibhav", "List contents:\n" + buf.toString());

	}

	// main constructor
	public ListItem(int questionNum, boolean choiceA, boolean choiceB, boolean choiceC, boolean choiceD) {
		super();
		this.questionNum = questionNum;
		this.choiceA = choiceA;
		this.choiceB = choiceB;
		this.choiceC = choiceC;
		this.choiceD = choiceD;
	}

	public boolean get(int optionNum) {
		boolean option = false;
		switch (optionNum) {
		case 0:
			option = this.choiceA;
			break;
		case 1:
			option = this.choiceB;
			break;
		case 2:
			option = this.choiceC;
			break;
		case 3:
			option = this.choiceD;
			break;
		default:
			break;
		}
		return option;
	}

	// String representation
	public String toString() {
		return "" + this.questionNum + " - " + this.choiceA + "  " + this.choiceB + "  " + this.choiceC + "  " + this.choiceD;
	}
}