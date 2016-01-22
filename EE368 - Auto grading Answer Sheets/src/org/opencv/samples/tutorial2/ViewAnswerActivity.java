package org.opencv.samples.tutorial2;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.TextView;
import android.app.ListActivity;

public class ViewAnswerActivity extends ListActivity {
	// Define the data source
	// Made it static and public so that answerActivity can access it. Otherwise parceable interface needs to be created
	// which is extremely complex

	public static ArrayList<ListItem> studentAnswerData = new ArrayList<ListItem>();
	public static ArrayList<ListItem> referenceAnswerData = new ArrayList<ListItem>();

	private String getScore() {
		StringBuffer buf = new StringBuffer();
		int total = ViewAnswerActivity.referenceAnswerData.size();
		int correct = 0;
		for (int q = 0; q < total; q++) {
			ListItem refAnswer = ViewAnswerActivity.referenceAnswerData.get(q);
			ListItem studentAnswer = ViewAnswerActivity.studentAnswerData.get(q);
			// Compare each option from both sets and if result can be concluded break
			for (int o = 0; o < 4; o++) {
				if (refAnswer.get(o) && studentAnswer.get(o)) {
					correct++;
					break;
				} else if (refAnswer.get(o) && !studentAnswer.get(o)) {
					break;
				} else if (!refAnswer.get(o) && studentAnswer.get(o)) {
					break;
				}
			}
		}

		buf.append("" + correct + " / " + total);
		return buf.toString();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// this.data = OMR_reader.answerData; //Other activity should set this array list

		setContentView(R.layout.activity_answer);

		// Add an answer so that if clicked without any answer sheet it will not crash and show something on screen

		if (ViewAnswerActivity.studentAnswerData.size() == 0 || ViewAnswerActivity.referenceAnswerData.size() == 0) {
			ViewAnswerActivity.referenceAnswerData = new ArrayList<ListItem>();
			ViewAnswerActivity.referenceAnswerData.add(new ListItem(1, false, true, false, false));
			ViewAnswerActivity.referenceAnswerData.add(new ListItem(2, false, false, true, false));
			ViewAnswerActivity.referenceAnswerData.add(new ListItem(2, true, false, false, false));
			ViewAnswerActivity.referenceAnswerData.add(new ListItem(2, false, false, false, true));

			ViewAnswerActivity.studentAnswerData = new ArrayList<ListItem>();
			ViewAnswerActivity.studentAnswerData.add(new ListItem(1, false, true, false, false));
			ViewAnswerActivity.studentAnswerData.add(new ListItem(2, false, true, false, false));
			ViewAnswerActivity.studentAnswerData.add(new ListItem(2, true, false, false, false));
			ViewAnswerActivity.studentAnswerData.add(new ListItem(2, false, false, false, true));

		}

		TextView scoreView = (TextView) findViewById(R.id.score);
		scoreView.setText("Your Score: " + getScore());

		// setup the data adaptor
		ArrayList<ArrayList<ListItem>> answerData = new ArrayList<ArrayList<ListItem>>();
		answerData.add(ViewAnswerActivity.referenceAnswerData);
		answerData.add(ViewAnswerActivity.studentAnswerData);

		CustomAdapter adapter = new CustomAdapter(this, R.layout.list_item, answerData);

		// specify the list adaptor
		setListAdapter(adapter);

	}
}