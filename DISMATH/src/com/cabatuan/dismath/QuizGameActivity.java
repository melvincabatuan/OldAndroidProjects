package com.cabatuan.dismath;

import java.io.IOException;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class QuizGameActivity extends QuizActivity {

	SharedPreferences mGameSettings;
	Hashtable<Integer, Question> mQuestions;

	private TextSwitcher mQuestionText;
	private WebView mQuestionEqn;
	private Button yesButton;
	private Button noButton;
	private int startingQuestionNumber;

	/**
	 * 
	 * Helper method to record the answer the user gave and load up the next
	 * question.
	 * 
	 * @param bAnswer
	 *            The answer the user gave
	 */
	private void handleAnswerAndShowNextQuestion(boolean bAnswer) {
		int curScore = mGameSettings.getInt(GAME_PREFERENCES_SCORE, 0);
		int nextQuestionNumber = mGameSettings.getInt(
				GAME_PREFERENCES_CURRENT_QUESTION, 1) + 1;

		Editor editor = mGameSettings.edit();
		editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, nextQuestionNumber);

		// Log the number of "yes" answers only
		if (bAnswer == true) {
			editor.putInt(GAME_PREFERENCES_SCORE, curScore + 1);
		}
		editor.commit();
		
		// Check answers, T or F encoded odd-even scheme
		int messageResId = 0;
		
		if (nextQuestionNumber%2 == 0) {
		    messageResId = R.string.correct_toast;
		} 
		else {
		    messageResId = R.string.incorrect_toast;
		}
		Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
		

		if (mQuestions.containsKey(nextQuestionNumber) == false) {
			// Load next batch
			try {
				loadQuestionBatch(nextQuestionNumber);
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Loading updated question batch failed", e);
			}
		}

		if (mQuestions.containsKey(nextQuestionNumber) == true) {

			// Update question equation
			WebView w = (WebView) findViewById(R.id.webview);
			w.loadUrl("javascript:document.getElementById('math').innerHTML='\\\\["
					+ doubleEscapeTeX(getQuestionEqn(nextQuestionNumber))
					+ "\\\\]';");
			w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");

			// Update question text
			TextSwitcher questionTextSwitcher = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
			questionTextSwitcher.setText(getQuestionText(nextQuestionNumber));

		} else {
			// Tell the user we don't have any new questions at this time
			handleNoQuestions();
			}

	}

	/**
	 * Helper method to configure the question screen when no questions were
	 * found. Could be called for a variety of error cases, including no new
	 * questions, IO failures, or parser failures.
	 */
	private void handleNoQuestions() {
		TextSwitcher questionTextSwitcher = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
		questionTextSwitcher.setText(getResources().getText(
				R.string.no_questions));

		WebView w = (WebView) findViewById(R.id.webview);
		String htmlString = "<h1>End</h1>";
		w.loadData(htmlString, "text/html", "utf-8");

		// Disable yes button
		yesButton.setEnabled(false);

		// Disable no button
		noButton.setEnabled(false);
	}

	/**
	 * Returns a {@code String} representing the text for a particular question
	 * number
	 * 
	 * @param questionNumber
	 *            The question number to get the text for
	 * @return The text of the question, or null if {@code questionNumber} not
	 *         found
	 */
	private String getQuestionText(Integer questionNumber) {
		String text = null;
		Question curQuestion = mQuestions.get(questionNumber);
		if (curQuestion != null) {
			text = curQuestion.mText;
		}
		return text;
	}

	/**
	 * Returns a {@code String} representing the tex equation for a particular
	 * question
	 * 
	 * @param questionNumber
	 *            The question to get the tex equation for
	 * @return A {@code String} for the equation or null if none found
	 */
	private String getQuestionEqn(Integer questionNumber) {
		String eqn = null;
		Question curQuestion = mQuestions.get(questionNumber);
		if (curQuestion != null) {
			eqn = curQuestion.mTexEqn;
		}
		return eqn;
	}

	private String doubleEscapeTeX(String s) {
		String t = "";
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '\'')
				t += '\\';
			if (s.charAt(i) != '\n')
				t += s.charAt(i);
			if (s.charAt(i) == '\\')
				t += "\\";
		}
		return t;
	}

	/**
	 * Loads the XML into the {@see mQuestions} class member variable
	 * 
	 * @param startQuestionNumber
	 *            TODO: currently unused
	 * @throws XmlPullParserException
	 *             Thrown if XML parsing errors
	 * @throws IOException
	 *             Thrown if errors loading XML
	 */
	private void loadQuestionBatch(int startQuestionNumber)
			throws XmlPullParserException, IOException {
		// Remove old batch
		mQuestions.clear();

		// TODO: Contact the server and retrieve a batch of question data,
		// beginning at startQuestionNumber
		XmlResourceParser questionBatch;

		// BEGIN MOCK QUESTIONS
		if (startQuestionNumber < 16) {
			questionBatch = getResources().getXml(R.xml.samplequestions);
		} else {
			questionBatch = getResources().getXml(R.xml.samplequestions2);
		}
		// END MOCK QUESTIONS

		// Parse the XML
		int eventType = -1;

		// Find Score records from XML
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {

				// Get the name of the tag (eg questions or question)
				String strName = questionBatch.getName();

				if (strName.equals(XML_TAG_QUESTION)) {

					String questionNumber = questionBatch.getAttributeValue(
							null, XML_TAG_QUESTION_ATTRIBUTE_NUMBER);
					Integer questionNum = new Integer(questionNumber);
					String questionText = questionBatch.getAttributeValue(null,
							XML_TAG_QUESTION_ATTRIBUTE_TEXT);
					String questionTexEqn = questionBatch.getAttributeValue(
							null, XML_TAG_QUESTION_ATTRIBUTE_TEXEQN);

					// Save data to our hashtable
					mQuestions.put(questionNum, new Question(questionNum,
							questionText, questionTexEqn));
				}
			}
			eventType = questionBatch.next();
		}
	}

	/**
	 * A switcher factory for use with the question text. Creates the next
	 * {@code TextView} object to animate to
	 * 
	 */
	private class MyTextSwitcherFactory implements ViewSwitcher.ViewFactory {
		@Override
		public View makeView() {
			TextView textView = (TextView) LayoutInflater.from(
					getApplicationContext()).inflate(
					R.layout.text_switcher_view, mQuestionText, false);
			return textView;
		}
	}

	/**
	 * Object to manage the data for a single quiz question
	 * 
	 */
	private class Question {
		@SuppressWarnings("unused")
		int mNumber;
		String mText;
		String mTexEqn;

		/**
		 * 
		 * Constructs a new question object
		 * 
		 * @param questionNum
		 *            The number of this question
		 * @param questionText
		 *            The text for this question
		 * @param questionTexEqn
		 *            A tex equation to display with this question
		 */
		public Question(int questionNum, String questionText,
				String questionTexEqn) {
			mNumber = questionNum;
			mText = questionText;
			mTexEqn = questionTexEqn;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(DEBUG_TAG, "onCreate(Bundle) called");
		setContentView(R.layout.game); // The layout consists of a WebView and
										// TextSwitcher

		// / Set up WebView for the math equation:
		/***************************************************************/
		mQuestionEqn = (WebView) findViewById(R.id.webview);
		mQuestionEqn.getSettings().setJavaScriptEnabled(true); // note:
																// JavaScript
																// disabled by
																// default
		mQuestionEqn.getSettings().setBuiltInZoomControls(false);
		mQuestionEqn.setWebViewClient(new WebViewClient());
		mQuestionEqn.loadDataWithBaseURL("http://bar",
				"<script type='text/x-mathjax-config'>"
						+ "MathJax.Hub.Config({ "
						+ "showProcessingMessages: false, "
						+ "messageStyle: 'none', "
						+ "showMathMenu: false, "
						+ "jax: ['input/TeX','output/SVG'], "
						+ "extensions: ['tex2jax.js'], "
						+ "TeX: {extensions: ['AMSmath.js','AMSsymbols.js',"
						+ " 'noErrors.js','noUndefined.js' ]} "
						+ "});</script>" + "<script type='text/javascript' "
						+ "src='file:///android_asset/MathJax/MathJax.js'"
						+ "></script><span id='math'></span>", "text/html",
				"utf-8", "");
		// mQuestionEqn.loadUrl("javascript:MathJax.Hub.Typeset(MathJax.Hub);");
		// mQuestionEqn.loadUrl("javascript:MathJax.Hub.Startup.onload(MathJax.Hub);");

		// Set up Text Switcher
		mQuestionText = (TextSwitcher) findViewById(R.id.TextSwitcher_QuestionText);
		mQuestionText.setFactory(new MyTextSwitcherFactory());

		// Retrieve the shared preferences
		mGameSettings = getSharedPreferences(GAME_PREFERENCES,
				Context.MODE_PRIVATE);

		// Initialize question batch
		mQuestions = new Hashtable<Integer, Question>(QUESTION_BATCH_SIZE);

		// Load the questions
		startingQuestionNumber = mGameSettings.getInt(
				GAME_PREFERENCES_CURRENT_QUESTION, 0);

		// If we're at the beginning of the quiz, initialize the Shared
		// preferences
		if (startingQuestionNumber == 0) {
			Editor editor = mGameSettings.edit();
			editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, 1);
			editor.commit();
			startingQuestionNumber = 1;
		}

		try {
			loadQuestionBatch(startingQuestionNumber);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Loading initial question batch failed", e);
		}

		// If the question was loaded properly, display it
		if (mQuestions.containsKey(startingQuestionNumber) == true) {

			// Set the text of the textswitcher
			mQuestionText.setCurrentText(getQuestionText(startingQuestionNumber));

			// Set the equation of the WebView
			 
			  mQuestionEqn.setWebViewClient(new WebViewClient() {
			  
			  @Override public void onPageFinished(WebView view, String url) {
			  super.onPageFinished(view, url); if
			  (!url.startsWith("http://bar")) return; mQuestionEqn.loadUrl(
			  "javascript:document.getElementById('math').innerHTML='\\\\[" +
			  doubleEscapeTeX(getQuestionEqn(startingQuestionNumber)) +
			  "\\\\]';");
			  mQuestionEqn.loadUrl("javascript:MathJax.Hub.Typeset(MathJax.Hub);"
			  ); } });
			 

		} else {
			// Tell the user we don't have any new questions at this time
			handleNoQuestions();
		}

		// Set buttons
		yesButton = (Button) findViewById(R.id.Button_Yes);
		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleAnswerAndShowNextQuestion(true);
			}
		});
		/*
		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(QuizGameActivity.this, R.string.correct_toast,
						Toast.LENGTH_SHORT).show();
			}
		});
		*/

		noButton = (Button) findViewById(R.id.Button_No);
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleAnswerAndShowNextQuestion(false);
			}
		});
		/*
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(QuizGameActivity.this, R.string.incorrect_toast,
						Toast.LENGTH_SHORT).show();
			}
		});
       */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.gameoptions, menu);
		menu.findItem(R.id.help_menu_item).setIntent(
				new Intent(this, QuizHelpActivity.class));
		menu.findItem(R.id.settings_menu_item).setIntent(
				new Intent(this, QuizSettingsActivity.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}

}