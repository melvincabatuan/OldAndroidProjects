package com.example.hello_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.scilab.forge.jlatexmath.TeXConstants; 
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;


public class DisplayMessageActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// Get the message from the intent
	Intent intent = getIntent();
	String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
	// Create the text view
	TextView textView = new TextView(this);
	textView.setTextSize(40);
	textView.setText(message);
	setContentView(textView);
	
	}
}
