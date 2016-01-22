package com.cabatuan.dismath;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class QuizSettingsActivity extends QuizActivity {
	SharedPreferences mGameSettings;
	static final int DATE_DIALOG_ID = 0;
	static final int PASSWORD_DIALOG_ID = 1;

	static final int TAKE_AVATAR_CAMERA_REQUEST = 1;
	static final int TAKE_AVATAR_GALLERY_REQUEST = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		// Retrieve the shared preferences
		mGameSettings = getSharedPreferences(GAME_PREFERENCES,
				Context.MODE_PRIVATE);

		// Initialize the avatar button
		initAvatar();
		// Initialize the nickname entry
		initNicknameEntry();
		// Initialize the email entry
		initEmailEntry();
		// Initialize the Password chooser
		initPasswordChooser();
		// Initialize the Date picker
		initDatePicker();
		// Initialize the spinner
		initGenderSpinner();
	}

	@Override
	protected void onPause() {
		super.onPause();

		EditText nicknameText = (EditText) findViewById(R.id.EditText_Nickname);
		EditText emailText = (EditText) findViewById(R.id.EditText_Email);

		String strNickname = nicknameText.getText().toString();
		String strEmail = emailText.getText().toString();

		Editor editor = mGameSettings.edit();
		editor.putString(GAME_PREFERENCES_NICKNAME, strNickname);
		editor.putString(GAME_PREFERENCES_EMAIL, strEmail);

		editor.commit();
	}

	@Override
	protected void onDestroy() {
		Log.d(DEBUG_TAG, "SHARED PREFERENCES");
		Log.d(DEBUG_TAG,
				"Nickname is: "
						+ mGameSettings.getString(GAME_PREFERENCES_NICKNAME,
								"Not set"));
		Log.d(DEBUG_TAG,
				"Email is: "
						+ mGameSettings.getString(GAME_PREFERENCES_EMAIL,
								"Not set"));
		Log.d(DEBUG_TAG,
				"Gender (M=1, F=2, U=0) is: "
						+ mGameSettings.getInt(GAME_PREFERENCES_GENDER, 0));
		// We are not saving the password yet
		Log.d(DEBUG_TAG,
				"Password is: "
						+ mGameSettings.getString(GAME_PREFERENCES_PASSWORD,
								"Not set"));
		// We are not saving the date of birth yet
		Log.d(DEBUG_TAG,
				"DOB is: "
						+ DateFormat.format("MMMM dd, yyyy",
								mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)));

		Log.d(DEBUG_TAG,
				"Avatar is: "
						+ mGameSettings.getString(GAME_PREFERENCES_AVATAR,
								"Not set"));

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case TAKE_AVATAR_CAMERA_REQUEST:

			if (resultCode == Activity.RESULT_CANCELED) {
				// Avatar camera mode was canceled.
			} else if (resultCode == Activity.RESULT_OK) {

				// Took a picture, use the downsized camera image provided by
				// default
				Bitmap cameraPic = (Bitmap) data.getExtras().get("data");
				if (cameraPic != null) {
					try {
						saveAvatar(cameraPic);
					} catch (Exception e) {
						Log.e(DEBUG_TAG,
								"saveAvatar() with camera image failed.", e);
					}
				}
			}
			break;
		case TAKE_AVATAR_GALLERY_REQUEST:

			if (resultCode == Activity.RESULT_CANCELED) {
				// Avatar gallery request mode was canceled.
			} else if (resultCode == Activity.RESULT_OK) {

				// Get image picked
				Uri photoUri = data.getData();
				if (photoUri != null) {
					try {
						int maxLength = 150;
						// Full size image likely will be large. Let's scale the
						// graphic to a more appropriate size for an avatar
						Bitmap galleryPic = Media.getBitmap(
								getContentResolver(), photoUri);
						Bitmap scaledGalleryPic = createScaledBitmapKeepingAspectRatio(
								galleryPic, maxLength);
						saveAvatar(scaledGalleryPic);
					} catch (Exception e) {
						Log.e(DEBUG_TAG,
								"saveAvatar() with gallery picker failed.", e);
					}
				}
			}
			break;
		}
	}

	public void onLaunchCamera(View v) {
		String strAvatarPrompt = "Take your picture to store as your avatar!";
		Intent pictureIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(
				Intent.createChooser(pictureIntent, strAvatarPrompt),
				TAKE_AVATAR_CAMERA_REQUEST);
	}

	/**
	 * Scale a Bitmap, keeping its aspect ratio
	 * 
	 * @param bitmap
	 *            Bitmap to scale
	 * @param maxSide
	 *            Maximum length of either side
	 * @return a new, scaled Bitmap
	 */
	private Bitmap createScaledBitmapKeepingAspectRatio(Bitmap bitmap,
			int maxSide) {
		int orgHeight = bitmap.getHeight();
		int orgWidth = bitmap.getWidth();

		// scale to no longer any either side than 75px
		int scaledWidth = (orgWidth >= orgHeight) ? maxSide
				: (int) (maxSide * ((float) orgWidth / (float) orgHeight));
		int scaledHeight = (orgHeight >= orgWidth) ? maxSide
				: (int) (maxSide * ((float) orgHeight / (float) orgWidth));

		// create the scaled bitmap
		Bitmap scaledGalleryPic = Bitmap.createScaledBitmap(bitmap,
				scaledWidth, scaledHeight, true);
		return scaledGalleryPic;
	}

	private void saveAvatar(Bitmap avatar) {
		String strAvatarFilename = "avatar.jpg";
		try {
			avatar.compress(CompressFormat.JPEG, 100,
					openFileOutput(strAvatarFilename, MODE_PRIVATE));
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Avatar compression and save failed.", e);
		}

		Uri imageUriToSaveCameraImageTo = Uri.fromFile(new File(
				QuizSettingsActivity.this.getFilesDir(), strAvatarFilename));

		Editor editor = mGameSettings.edit();
		editor.putString(GAME_PREFERENCES_AVATAR,
				imageUriToSaveCameraImageTo.getPath());
		editor.commit();

		// Update the settings screen
		ImageButton avatarButton = (ImageButton) findViewById(R.id.ImageButton_Avatar);
		String strAvatarUri = mGameSettings
				.getString(GAME_PREFERENCES_AVATAR,
						"android.resource://com.androidbook.btdt.hour13/drawable/avatar");
		Uri imageUri = Uri.parse(strAvatarUri);
		avatarButton.setImageURI(null); // Workaround for refreshing an
										// ImageButton, which tries to cache the
										// previous image Uri. Passing null
										// effectively resets it.
		avatarButton.setImageURI(imageUri);
	}

	/**
	 * Initialize the Avatar
	 */
	private void initAvatar() {
		// Handle password setting dialog
		ImageButton avatarButton = (ImageButton) findViewById(R.id.ImageButton_Avatar);

		if (mGameSettings.contains(GAME_PREFERENCES_AVATAR)) {
			String strAvatarUri = mGameSettings
					.getString(GAME_PREFERENCES_AVATAR,
							"android.resource://com.androidbook.peakbagger/drawable/avatar");
			Uri imageUri = Uri.parse(strAvatarUri);
			avatarButton.setImageURI(imageUri);
		} else {
			avatarButton.setImageResource(R.drawable.avatar);
		}

		avatarButton.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				String strAvatarPrompt = "Choose a picture to use as your avatar!";
				Intent pickPhoto = new Intent(Intent.ACTION_PICK);
				pickPhoto.setType("image/*");
				startActivityForResult(
						Intent.createChooser(pickPhoto, strAvatarPrompt),
						TAKE_AVATAR_GALLERY_REQUEST);
				return true;
			}
		});

	}

	/**
	 * Initialize the nickname entry
	 */
	private void initNicknameEntry() {
		EditText nicknameText = (EditText) findViewById(R.id.EditText_Nickname);
		if (mGameSettings.contains(GAME_PREFERENCES_NICKNAME)) {
			nicknameText.setText(mGameSettings.getString(
					GAME_PREFERENCES_NICKNAME, ""));
		}
	}

	/**
	 * Initialize the email entry
	 */
	private void initEmailEntry() {
		EditText emailText = (EditText) findViewById(R.id.EditText_Email);
		if (mGameSettings.contains(GAME_PREFERENCES_EMAIL)) {
			emailText.setText(mGameSettings.getString(GAME_PREFERENCES_EMAIL,
					""));
		}
	}

	/**
	 * Initialize the Password chooser
	 */
	private void initPasswordChooser() {
		// Set password info
		TextView passwordInfo = (TextView) findViewById(R.id.TextView_Password_Info);
		if (mGameSettings.contains(GAME_PREFERENCES_PASSWORD)) {
			passwordInfo.setText(R.string.settings_pwd_set);
		} else {
			passwordInfo.setText(R.string.settings_pwd_not_set);
		}
	}

	/**
	 * Called when the user presses the Set Password button
	 * 
	 * @param view
	 *            the button
	 */
	public void onSetPasswordButtonClick(View view) {
		showDialog(PASSWORD_DIALOG_ID);
	}

	/**
	 * Initialize the Date picker
	 */
	private void initDatePicker() {
		// Set password info
		TextView dobInfo = (TextView) findViewById(R.id.TextView_DOB_Info);
		if (mGameSettings.contains(GAME_PREFERENCES_DOB)) {
			dobInfo.setText(DateFormat.format("MMMM dd, yyyy",
					mGameSettings.getLong(GAME_PREFERENCES_DOB, 0)));
		} else {
			dobInfo.setText(R.string.settings_dob_not_set);
		}
	}

	/**
	 * Called when the user presses the Pick Date button
	 * 
	 * @param view
	 *            The button
	 */
	public void onPickDateButtonClick(View view) {
		showDialog(DATE_DIALOG_ID);
	}

	/**
	 * Initialize the spinner
	 */
	private void initGenderSpinner() {
		// Populate Spinner control with genders
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_Gender);
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this,
				R.array.genders, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (mGameSettings.contains(GAME_PREFERENCES_GENDER)) {
			spinner.setSelection(mGameSettings.getInt(GAME_PREFERENCES_GENDER,
					0));
		}
		// Handle spinner selections
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View itemSelected, int selectedItemPosition, long selectedId) {
				Editor editor = mGameSettings.edit();
				editor.putInt(GAME_PREFERENCES_GENDER, selectedItemPosition);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			final TextView dob = (TextView) findViewById(R.id.TextView_DOB_Info);
			Calendar now = Calendar.getInstance();

			DatePickerDialog dateDialog = new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {

							Time dateOfBirth = new Time();
							dateOfBirth.set(dayOfMonth, monthOfYear, year);
							long dtDob = dateOfBirth.toMillis(true);
							dob.setText(DateFormat.format("MMMM dd, yyyy",
									dtDob));

							Editor editor = mGameSettings.edit();
							editor.putLong(GAME_PREFERENCES_DOB, dtDob);
							editor.commit();
						}
					}, now.get(Calendar.YEAR), now.get(Calendar.MONTH),
					now.get(Calendar.DAY_OF_MONTH));
			return dateDialog;
		case PASSWORD_DIALOG_ID:
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.password_dialog,
					(ViewGroup) findViewById(R.id.root));
			final EditText p1 = (EditText) layout
					.findViewById(R.id.EditText_Pwd1);
			final EditText p2 = (EditText) layout
					.findViewById(R.id.EditText_Pwd2);
			final TextView error = (TextView) layout
					.findViewById(R.id.TextView_PwdProblem);
			p2.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					String strPass1 = p1.getText().toString();
					String strPass2 = p2.getText().toString();
					if (strPass1.equals(strPass2)) {
						error.setText(R.string.settings_pwd_equal);
					} else {
						error.setText(R.string.settings_pwd_not_equal);
					}
				}

				// ... other required overrides need not be implemented
				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
			});
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			// Now configure the AlertDialog
			builder.setTitle(R.string.settings_button_pwd);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// We forcefully dismiss and remove the Dialog, so
							// it
							// cannot be used again (no cached info)
							QuizSettingsActivity.this
									.removeDialog(PASSWORD_DIALOG_ID);
						}
					});
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TextView passwordInfo = (TextView) findViewById(R.id.TextView_Password_Info);
							String strPassword1 = p1.getText().toString();
							String strPassword2 = p2.getText().toString();
							if (strPassword1.equals(strPassword2)) {
								Editor editor = mGameSettings.edit();
								editor.putString(GAME_PREFERENCES_PASSWORD,
										strPassword1);
								editor.commit();
								passwordInfo.setText(R.string.settings_pwd_set);
							} else {
								Log.d(DEBUG_TAG,
										"Passwords do not match. Not saving. Keeping old password (if set).");
							}
							// We forcefully dismiss and remove the Dialog, so
							// it
							// cannot be used again
							QuizSettingsActivity.this
									.removeDialog(PASSWORD_DIALOG_ID);
						}
					});
			// Create the AlertDialog and return it
			AlertDialog passwordDialog = builder.create();
			return passwordDialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DATE_DIALOG_ID:
			// Handle any DatePickerDialog initialization here
			DatePickerDialog dateDialog = (DatePickerDialog) dialog;
			int iDay,
			iMonth,
			iYear;
			// Check for date of birth preference
			if (mGameSettings.contains(GAME_PREFERENCES_DOB)) {
				// Retrieve Birth date setting from preferences
				long msBirthDate = mGameSettings.getLong(GAME_PREFERENCES_DOB,
						0);
				Time dateOfBirth = new Time();
				dateOfBirth.set(msBirthDate);

				iDay = dateOfBirth.monthDay;
				iMonth = dateOfBirth.month;
				iYear = dateOfBirth.year;
			} else {
				Calendar cal = Calendar.getInstance();
				// Today's date fields
				iDay = cal.get(Calendar.DAY_OF_MONTH);
				iMonth = cal.get(Calendar.MONTH);
				iYear = cal.get(Calendar.YEAR);
			}
			// Set the date in the DatePicker to the date of birth OR to the
			// current date
			dateDialog.updateDate(iYear, iMonth, iDay);
			return;
		case PASSWORD_DIALOG_ID:
			// Handle any Password Dialog initialization here
			// Since we don't want to show old password dialogs, just set new
			// ones, we need not do anything here
			// Because we are not "reusing" password dialogs once they have
			// finished, but removing them from
			// the Activity Dialog pool explicitly with removeDialog() and
			// recreating them as needed.
			return;
		}
	}
}