package org.opencv.samples.tutorial2;

import java.io.File;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import org.opencv.samples.tutorial2.BaseAlbumDirFactory;
import org.opencv.samples.tutorial2.FroyoAlbumDirFactory;
import org.opencv.utils.Converters;

//import com.example.android.photobyintent.AlbumStorageDirFactory;
//import com.example.android.photobyintent.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Auto_grader extends Activity {

	private static final int ACTION_TAKE_PHOTO = 1;
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	public static File sdCardPath = Environment.getExternalStorageDirectory();

	// private Bitmap mImageBitmap;

	private String mRefPhotoPath;
	private String mComputeAnswerPhotoPath;
	public static String matchingImagePath = null;

	private int photo_indication = 0; // 0 : reference 1: student photo

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private TextView tvProcessingMsg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_omr_reader);

		tvProcessingMsg = (TextView) findViewById(R.id.textViewProcessingMsg);
		tvProcessingMsg.setTextColor(Color.parseColor("#000000"));
		
		// 1. button : taking picture of reference answer sheet
		Button btnReference = (Button) findViewById(R.id.btnReference);
		setBtnListenerOrDisable(btnReference, mTakeRefOnClickListener, MediaStore.ACTION_IMAGE_CAPTURE);

		// 2. button : extract answers and make answer table.
		Button btnComputeAnswer = (Button) findViewById(R.id.btnComputeAnswer);
		btnComputeAnswer.setOnClickListener(mComputeAnswerOnClickListener);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		// 3. button : view answers
		Button btnViewAnswers = (Button) findViewById(R.id.btnViewAnswers);
		btnViewAnswers.setOnClickListener(mViewAnswersOnClickListener);

	}

	Button.OnClickListener mTakeRefOnClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			File f = null;
			// Define Intent to take a picture
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			photo_indication = 0;
			try {
				// Create a temporary file to save an image.
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String imageFileName = "reference_answer_sheet" + timeStamp;
				// String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

				// Create a album directory
				f = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumDir());
				mRefPhotoPath = f.getAbsolutePath();
				takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mRefPhotoPath = null;
			}

			startActivityForResult(takePicture, ACTION_TAKE_PHOTO);

		}
	};

	Button.OnClickListener mComputeAnswerOnClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Log.v("Vaibhav", "ee368 function called");

			File f = null;
			// Define Intent to take a picture
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			photo_indication = 1;

			try {
				// Create a temporary file to save an image.
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String imageFileName = "student_answer_sheet" + timeStamp;
				// String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

				// Create a album directory
				f = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumDir());
				mRefPhotoPath = f.getAbsolutePath();
				takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mRefPhotoPath = null;
			}

			startActivityForResult(takePicture, ACTION_TAKE_PHOTO);

		}
	};

	Button.OnClickListener mViewAnswersOnClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), ViewAnswerActivity.class));
		}
	};

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mRefPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void handleBigCameraPhoto() {

		if (mRefPhotoPath != null) {
			galleryAddPic();
		}

	}

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
			Log.d("getAlbum", "Making EE368 directory: " + storageDir);

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("getAlbum", "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO: {
			if (resultCode == RESULT_OK) {
				handleBigCameraPhoto();
				
				new processImage().execute();

			}
			break;
		} // ACTION_TAKE_PHOTO
		} // switch
	}

	private class processImage extends AsyncTask<Void, Void, ArrayList<ListItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//tvProcessingMsg.setVisibility(View.VISIBLE);
			Log.v("Vaibhav","In onPreExecute");
			tvProcessingMsg.setTextColor(Color.parseColor("#000000"));

		}
		
		@Override
		protected ArrayList<ListItem> doInBackground(Void... params) {
			Log.v("Vaibhav","In doInBackground");
			File f1 = new File(sdCardPath, "AnswerTemplateNewVGA.jpg");
			return ee368(f1.getAbsolutePath(), mRefPhotoPath);
		}

		@Override
		protected void onPostExecute(ArrayList<ListItem> result) {
			super.onPostExecute(result);
			Log.v("Vaibhav","In doPostExecute");

			//ArrayList<ListItem>
			if (photo_indication == 0) // reference photo
				ViewAnswerActivity.referenceAnswerData = result ;
			else
				ViewAnswerActivity.studentAnswerData = result;

			ViewImageActivity.viewImagePath = mComputeAnswerPhotoPath;
			tvProcessingMsg.setTextColor(Color.parseColor("#000000"));
			startActivity(new Intent(getApplicationContext(), ViewImageActivity.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.omr_reader, menu);

		return true;
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This method queries the package manager for installed packages that can respond to an intent with the specified action. If no suitable package is found, this method returns false. http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 * 
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable(Button btn, Button.OnClickListener onClickListener, String intentName) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);
		} else {
			btn.setText(getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}

	public ArrayList<ListItem> ee368(String refer_path, String solu_path) {

		System.out.println("\nRunning EE368 project");

		Mat image_refer = Highgui.imread(refer_path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat image_solu = Highgui.imread(solu_path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		Log.v("Vaibhav", "Files are read");

		// 1st step: Detect key points using SURF detector
		FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
		// FeatureDetector surfDetector =FeatureDetector.create(FeatureDetector.FAST);
		// FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.ORB); //Worst results. Does not detect many key points

		List<KeyPoint> keyPoints_ref = new ArrayList<KeyPoint>();
		List<KeyPoint> keyPoints_solu = new ArrayList<KeyPoint>();
		surfDetector.detect(image_refer, keyPoints_ref);
		Log.v("Vaibhav", "Surf detection 1 done1");
		surfDetector.detect(image_solu, keyPoints_solu);
		Log.v("Vaibhav", "Surf detection 2 done1");

		// Feature Detectors / Descriptor Extractors / Matchers types

		// FAST / ORB / BRUTEFORCE_HAMMING : Worst performance
		// SURF / SURF / FlannBased : Our original combination - works good but takes lot of time
		// ORB / ORB / BRUTEFORCE_HAMMING : Crashes
		// FAST / SIFT / FLANNBASED : Crashes - Memory issues
		// FAST / SURF / FLANNBASED : Comparatively faster and more robust

		// 2nd step: Calculate descriptors
		DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		Mat descriptors_ref = new Mat();
		Mat descriptors_solu = new Mat();
		surfExtractor.compute(image_refer, keyPoints_ref, descriptors_ref);
		Log.v("Vaibhav", "Extraction 1 done");
		surfExtractor.compute(image_solu, keyPoints_solu, descriptors_solu);
		Log.v("Vaibhav", "Extraction 2 done");

		// 3rd step: Matching descriptor vectors using FLANN matcher
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		// DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);//Required for ORB

		List<DMatch> matches = new ArrayList<DMatch>();
		matcher.match(descriptors_ref, descriptors_solu, matches);
		Log.v("Vaibhav", "Matching using matcher.match done");
		double max_dist = 0;
		double min_dist = 100;

		for (int i = 0; i < descriptors_ref.rows(); i++) {
			double dist = matches.get(i).distance;
			if (dist < min_dist)
				min_dist = dist;
			if (dist > max_dist)
				max_dist = dist;
		}

		List<DMatch> good_matches = new ArrayList<DMatch>();
		for (int i = 0; i < descriptors_ref.rows(); i++) {
			// need to add small number to min_dist, in case of min_dist = 0
			if (matches.get(i).distance < 3 * (min_dist + 0.01)) {
				good_matches.add((matches.get(i)));
			}
		}

		// draw matches of two images
		Mat img_matches = new Mat();
		// img_matches : Output image.
		Features2d.drawMatches(image_refer, keyPoints_ref, image_solu, keyPoints_solu, good_matches, img_matches, Scalar.all(-1), Scalar.all(-1), null, Features2d.NOT_DRAW_SINGLE_POINTS);

		// get the transformation matrix
		List<Point> refer = new ArrayList<Point>();
		List<Point> solu = new ArrayList<Point>();
		for (int i = 0; i < good_matches.size(); i++) {
			refer.add(keyPoints_ref.get(good_matches.get(i).queryIdx).pt);
			solu.add(keyPoints_solu.get(good_matches.get(i).trainIdx).pt);
		}

		Mat H = Calib3d.findHomography(refer, solu, Calib3d.RANSAC);

		// Get corners from template

		List<Point> corners_template = new ArrayList<Point>();
		List<Point> corners_solu = new ArrayList<Point>();

		corners_template.add(new Point(0, 0));
		corners_template.add(new Point(image_refer.cols(), 0));
		corners_template.add(new Point(image_refer.cols(), image_refer.rows()));
		corners_template.add(new Point(0, image_refer.rows()));

		Mat corners_tem_mat = Converters.vector_Point2d_to_Mat(corners_template);
		Mat corners_sol_mat = new Mat();
		Core.perspectiveTransform(corners_tem_mat, corners_sol_mat, H);

		Converters.Mat_to_vector_Point2d(corners_sol_mat, corners_solu);

		// Draw lines between the corners on the solution image.
		// Since we have two images side by side in our output image (img_matches) i.e. first we have template & then
		// the solution image. To get the coordinates of points on solution image we need to offset those by first image size.

		Core.line(img_matches, new Point(corners_solu.get(0).x + corners_template.get(1).x, corners_solu.get(0).y), new Point(corners_solu.get(1).x + corners_template.get(1).x, corners_solu.get(1).y), new Scalar(0, 255, 0), 4);
		Core.line(img_matches, new Point(corners_solu.get(1).x + corners_template.get(1).x, corners_solu.get(1).y), new Point(corners_solu.get(2).x + corners_template.get(1).x, corners_solu.get(2).y), new Scalar(0, 255, 0), 4);
		Core.line(img_matches, new Point(corners_solu.get(2).x + corners_template.get(1).x, corners_solu.get(2).y), new Point(corners_solu.get(3).x + corners_template.get(1).x, corners_solu.get(3).y), new Scalar(0, 255, 0), 4);
		Core.line(img_matches, new Point(corners_solu.get(3).x + corners_template.get(1).x, corners_solu.get(3).y), new Point(corners_solu.get(0).x + corners_template.get(1).x, corners_solu.get(0).y), new Scalar(0, 255, 0), 4);

		List<Point> center_locations = new ArrayList<Point>();
		List<Integer> y_cor = new ArrayList<Integer>(Arrays.asList(168, 196, 223, 251, 278, 305, 333, 360, 388, 416, 443, 471, 498, 526, 554));

		// Add centers of circles in the first column
		for (int i = 0; i < 15; i++) {
			center_locations.add(new Point(108, y_cor.get(i)));
			center_locations.add(new Point(138, y_cor.get(i)));
			center_locations.add(new Point(168, y_cor.get(i)));
			center_locations.add(new Point(198, y_cor.get(i)));
		}

		// Add centers of circles in the second column
		for (int i = 0; i < 15; i++) {
			center_locations.add(new Point(300, y_cor.get(i)));
			center_locations.add(new Point(330, y_cor.get(i)));
			center_locations.add(new Point(360, y_cor.get(i)));
			center_locations.add(new Point(390, y_cor.get(i)));
		}

		Mat center_locations_mat = Converters.vector_Point2d_to_Mat(center_locations);
		Mat center_locations_transfered = new Mat();
		Core.perspectiveTransform(center_locations_mat, center_locations_transfered, H);

		List<Point> center_locations_t = new ArrayList<Point>();
		Converters.Mat_to_vector_Point2d(center_locations_transfered, center_locations_t);

		for (int i = 0; i < center_locations_t.size(); i++) {
			Core.circle(img_matches, new Point(center_locations_t.get(i).x + corners_template.get(1).x, center_locations_t.get(i).y), 3, new Scalar(0, 0, 255), -1);
		}

		mComputeAnswerPhotoPath = fileWriteToSdCard("ProcessedImage3.png", img_matches, sdCardPath);
		Auto_grader.matchingImagePath = mComputeAnswerPhotoPath;

		Mat image_solu_bw = fileConvertImgToBW(image_solu, "solu_bw.png", sdCardPath, false);

		// Diameter of circles in VGA template with 2 pixel margin is 24 pixels
		return getAnswers(image_solu_bw, center_locations_t, 12);

	}

	// Writes the file to the SD card and returns the path
	private String fileWriteToSdCard(String filename, Mat img, File sdCardPath) {
		File file1 = new File(sdCardPath, filename);
		filename = file1.toString();
		Highgui.imwrite(filename, img);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));

		return filename;
	}

	// Converts the gray scale image to black and white and write the resulting
	// image to SD card
	// img should be Mat for gray scale image
	private Mat fileConvertImgToBW(Mat img, String filename, File sdCardPath, boolean applyGaussBlurr) {
		Mat img_gray = img.clone();
		if (applyGaussBlurr) {
			Imgproc.GaussianBlur(img_gray, img_gray, new Size(3, 3), 0, 0);
		}

		// Imgproc.adaptiveThreshold(img_gray, img_gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);

		// compute optimal Otsu's threshold
		double thresh = Imgproc.threshold(img_gray, img_gray, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

		// apply threshold
		Imgproc.threshold(img_gray, img_gray, thresh, 255, Imgproc.THRESH_BINARY_INV);

		Log.v("Vaibhav", "conveted bw img matrix:" + img_gray.toString());
		fileWriteToSdCard(filename, img_gray, sdCardPath);
		return img_gray;
	}

	private ArrayList<ListItem> getAnswers(Mat imgToProcess, List<Point> ptList, int radius) {
		ArrayList<ListItem> tempAnswerData = new ArrayList<ListItem>();
		// ViewAnswerActivity.answerData = new ArrayList<ListItem>();
		int thresh = 150;

		for (int i = 0; i < (ptList.size() / 4); i++) {
			StringBuffer buf = new StringBuffer();
			int[] pixelCnt = new int[4];

			for (int j = 0; j < 4; j++) {
				int position = i * 4 + j;
				Point pt = ptList.get(position);
				pixelCnt[j] = getWhitePixelsInBlob(imgToProcess, pt, radius);
				buf.append(pixelCnt[j] + " ");
			}

			Log.v("Vaibhav", "White pixel cnt for question no " + i + ":" + buf.toString());
			// Question number starts with 1 but index starts with 0 so add one while creating list item.
			tempAnswerData.add(new ListItem(i + 1, pixelCnt[0] > thresh, pixelCnt[1] > thresh, pixelCnt[2] > thresh, pixelCnt[3] > thresh));
		}
		return tempAnswerData;
	}

	// Checks the pixel value for all the pixels in the square with center at given pt
	// Basically circle is approximated to square for simplicity
	private int getWhitePixelsInBlob(Mat imgToProcess, Point pt, int radius) {
		int centerX = (int) pt.x;
		int centerY = (int) pt.y;
		int cntOfWhite = 0;
		for (int i = (centerX - radius); i < (centerX + radius); i++) {
			for (int j = (centerY - radius); j < (centerY + radius); j++) {
				if (j < imgToProcess.height() && i < imgToProcess.width()) {
					double val = imgToProcess.get(j, i)[0];
					if (val == 255)
						cntOfWhite++;
				}
			}
		}

		return cntOfWhite;
	}
}