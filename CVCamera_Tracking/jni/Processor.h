/*
 * Processor.h
 */

#ifndef PROCESSOR_H_
#define PROCESSOR_H_

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>
#include "image_pool.h"
#include "cbir/RifTrack.h"

#define PERFORM_TRACKING 0

class Processor
{
public:

  Processor();
  virtual ~Processor();

  void processImage(int idx, image_pool* pool, int feature_type);
  
private:

	void initializeTracker(unsigned int width, unsigned int height);
	
	unsigned int trackFrame(cv::Mat greyimage);
	
	void visualizeTracking(cv::Mat& colorimage);

	RifTrack* m_rifTrack;
	Image<Byte>* m_frame;
	unsigned int m_frameWidth;
	unsigned int m_frameHeight;
  	unsigned int m_frameCount;
  	unsigned int m_downsampleFactor;
  	bool m_trackingValid;
  	char m_textBuffer[1024];
  	cv::Mat m_greyimagesmall;
  	vector< vector<float> > m_matchedPoints;
};

#endif /* PROCESSOR_H_ */
