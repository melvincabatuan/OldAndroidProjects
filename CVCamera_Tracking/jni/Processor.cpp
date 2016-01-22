/*
 * Processor.cpp
 */

#include "Processor.h"
#include <sys/stat.h>
using namespace cv;

Processor::Processor()
{
	m_rifTrack = NULL;
	m_frame = NULL;
	m_frameWidth = 0;
	m_frameHeight = 0;
	m_frameCount = 0;
	m_trackingValid = false;
	m_downsampleFactor = 2;
}

Processor::~Processor()
{
	delete m_frame;
	delete m_rifTrack;
}

void Processor::processImage(int input_idx, image_pool* pool, int feature_type)
{
	// Get image from pool
	Mat greyimage = pool->getGrey(input_idx); // grayscale
	Mat img = pool->getImage(input_idx); // BGR

	// Return if no image is found
	if (img.empty() || greyimage.empty())
    	return;
    
    // Perform tracking
    if (feature_type == PERFORM_TRACKING) {
		// Downsample image
		resize(greyimage, m_greyimagesmall, Size(greyimage.cols/m_downsampleFactor, greyimage.rows/m_downsampleFactor));
		
		// Initialize tracker
		if (m_frameCount == 0) {
			initializeTracker(m_greyimagesmall.cols, m_greyimagesmall.rows);
		}
		
		// Track frame
		trackFrame(m_greyimagesmall);
		
		// Draw tracking visualization
		visualizeTracking(img);
	}
	m_frameCount++;
}

void Processor::initializeTracker(unsigned int width, unsigned int height)
{
	m_rifTrack = new RifTrack();
	m_frame = new Image<Byte>(width, height, 1);
	m_frameWidth = width;
	m_frameHeight = height;
}

unsigned int Processor::trackFrame(Mat greyimage)
{
	// Convert from CV image format to RIFF image format
	uchar* greyimagedata = greyimage.data;
	size_t greyimagestep = greyimage.step;
	Byte* frameptr = m_frame->PixelPointer(0,0);
	for (int row = 0; row < greyimage.rows; row++) {
		for (int col = 0; col < greyimage.cols; col++) {
			frameptr[col] = greyimagedata[col];
		}
		greyimagedata += greyimagestep;
		frameptr += greyimage.cols;
	} // row
	
	// Track frame
	m_trackingValid = m_rifTrack->TrackFrame(*m_frame, m_matchedPoints);
	
	// Return number of feature matches
	return m_rifTrack->iMatches.size();
}

void Processor::visualizeTracking(Mat& colorimage)
{
	// Print number of tracked features
	unsigned int numFeatureMatches = m_rifTrack->iMatches.size();
	sprintf(m_textBuffer, "%02d features tracked", numFeatureMatches);
	int fontFace = FONT_HERSHEY_COMPLEX;
	double fontScale = 1;
	Point textOrigin(10,25);
	rectangle(colorimage, Point(0,0), Point(400,45), Scalar(0,0,0), CV_FILLED);
	putText(colorimage, string(m_textBuffer), textOrigin, fontFace, fontScale, Scalar(255,255,0));

	// Draw motion vectors
	Point offset(-2,-2);
	int radius = 5;
	float motionVectorExpansion = 2;
	for (int n = 0; n < m_matchedPoints.size(); n++) {
		vector<float>& matchedPoint = m_matchedPoints[n];
		Point prevPt(matchedPoint[0]*m_downsampleFactor, matchedPoint[1]*m_downsampleFactor);
		Point currPt(matchedPoint[2]*m_downsampleFactor, matchedPoint[3]*m_downsampleFactor);
		Point deltaPt = motionVectorExpansion*(prevPt - currPt);
				
		circle(colorimage, currPt + offset, radius, Scalar(0,0,0), 3);
		circle(colorimage, currPt - offset, radius, Scalar(0,0,0), 3);
		circle(colorimage, currPt, radius, Scalar(255,255,0), 3);
		
		line(colorimage, currPt + offset, currPt + deltaPt + offset, Scalar(0,0,0), 3);
		line(colorimage, currPt - offset, currPt + deltaPt - offset, Scalar(0,0,0), 3);
		line(colorimage, currPt, currPt + deltaPt, Scalar(255,255,0), 3);
	} // n
}