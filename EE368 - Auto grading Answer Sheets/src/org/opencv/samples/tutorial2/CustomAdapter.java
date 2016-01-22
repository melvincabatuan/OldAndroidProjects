package org.opencv.samples.tutorial2;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
	// store the context (as an inflated layout)
	private LayoutInflater inflater;
	// store the resource (typically list_item.xml)
	private int resource;
	// store (a reference to) the data
	private ArrayList<ListItem> referenceData;
	private ArrayList<ListItem> studentData;

	/**
	 * Default constructor. Creates the new Adaptor object to provide a ListView with data.
	 * 
	 * @param context
	 * @param resource
	 * @param data
	 */
	public CustomAdapter(Context context, int resource, ArrayList<ArrayList<ListItem>> data) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.resource = resource;
		this.referenceData = data.get(0);
		this.studentData = data.get(1);
	}

	/**
	 * Return the size of the data set.
	 */
	public int getCount() {
		return this.studentData.size();
	}

	/**
	 * Return an object in the data set.
	 */
	public Object getItem(int position) {
		return this.studentData.get(position);
	}

	/**
	 * Return the position provided.
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Return a generated view for a position.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// reuse a given view, or inflate a new one from the xml
		View view;

		if (convertView == null) {
			view = this.inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		// bind the data to the view object
		return this.bindData(view, position);
	}

	private void colorOption(boolean studentChoice, boolean referenceChoice, TextView tv) {
		// If both options are true - student has answered it correct. So make it green
		if (referenceChoice && studentChoice) {
			tv.setBackgroundColor(Color.parseColor("#00ff00"));
			tv.setTextColor(Color.parseColor("#006600"));
		} else if (referenceChoice && !studentChoice) {
			// If reference is true and student is false. Mark the option as gray
			tv.setBackgroundColor(Color.parseColor("#666666"));
			tv.setTextColor(Color.parseColor("#cccccc"));
		} else if (!referenceChoice && studentChoice) {
			// If reference is false and student is true. Mark the option as red
			tv.setBackgroundColor(Color.parseColor("#ff0000"));
			tv.setTextColor(Color.parseColor("#660000"));
		} else {
			tv.setBackgroundColor(Color.parseColor("#ffffff"));
			tv.setTextColor(Color.parseColor("#000000"));
		}
	}

	/**
	 * Bind the provided data to the view. This is the only method not required by base adapter.
	 */
	public View bindData(View view, int position) {
		// Log.v("Vaibhav","1");
		// make sure it's worth drawing the view
		if (this.studentData.get(position) == null) {
			return view;
		}

		// pull out the object
		ListItem studentAnswer = this.studentData.get(position);
		ListItem referenceAnswer = this.referenceData.get(position);

		// Log.v("Vaibhav","position:" + position + "\n" + item.toString());

		// Log.v("Vaibhav","2");

		// extract the view object
		View viewElement = view.findViewById(R.id.choice_a);
		// cast to the correct type
		TextView tv = (TextView) viewElement;
		// set the background color to green if true
		colorOption(studentAnswer.choiceA, referenceAnswer.choiceA, tv);

		viewElement = view.findViewById(R.id.choice_b);
		tv = (TextView) viewElement;

		colorOption(studentAnswer.choiceB, referenceAnswer.choiceB, tv);

		// Log.v("Vaibhav","4");

		viewElement = view.findViewById(R.id.choice_c);
		tv = (TextView) viewElement;

		colorOption(studentAnswer.choiceC, referenceAnswer.choiceC, tv);

		// Log.v("Vaibhav","5");

		viewElement = view.findViewById(R.id.choice_d);
		tv = (TextView) viewElement;

		colorOption(studentAnswer.choiceD, referenceAnswer.choiceD, tv);

		// Log.v("Vaibhav","6");

		viewElement = view.findViewById(R.id.question_num);
		tv = (TextView) viewElement;
		String qNum = "" + studentAnswer.questionNum;
		tv.setText(qNum);
		// Log.v("Vaibhav","7");

		// return the final view object
		return view;
	}
}