/* 

Get real-time contact quality readings

  */

#include <jni.h>
#include "emokit.h"
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include "headsets_epoc_EpocConnector.h"

struct emokit_device* d;
struct emokit_frame c;
int r, count;


JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_getContactQualityReadings
(JNIEnv *env, jobject obj) {
  int err = emokit_read_data_timeout(d, 1000);
  if(err > 0) {
    c = emokit_get_next_frame(d);			
    fprintf(stderr,"\033[H\033[2JPress CTRL+C to exit\n\nContact quality:\nF3  %4d\nFC6 %4d\nP7  %4d\nT8  %4d\nF7  %4d\nF8  %4d\nT7  %4d\nP8  %4d\nAF4 %4d\nF4  %4d\nAF3 %4d\nO2  %4d\nO1  %4d\nFC5 %4d",c.cq.F3, c.cq.FC6, c.cq.P7, c.cq.T8,c.cq.F7, c.cq.F8, c.cq.T7, c.cq.P8, c.cq.AF4, c.cq.F4, c.cq.AF3, c.cq.O2, c.cq.O1, c.cq.FC5);
    fflush(stdout);
    /* add client code here */
  } else if(err == 0) {
    fprintf(stderr, "Headset Timeout...\n");
  }
}

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_disconnectHeadsets
(JNIEnv *env, jobject obj) {
  emokit_close(d);
  emokit_delete(d);
}

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_connectToHeadsets
(JNIEnv *env, jobject obj) {
  d = emokit_create();
  count = emokit_get_count(d, EMOKIT_VID, EMOKIT_PID);  
  fprintf(stderr, "Current epoc devices connected: %d\n", count );
  int r = emokit_open(d, EMOKIT_VID, EMOKIT_PID, 1);
  if(r != 0)
    {
      emokit_close(d);
      emokit_delete(d);
      d = emokit_create();
      r = emokit_open(d, EMOKIT_VID, EMOKIT_PID, 0);
      if (r!=0) {
	fprintf(stderr, "CANNOT CONNECT: %d\n", r);
      }
    }
  fprintf(stderr, "Connected to headset.\n");
  
  r = emokit_read_data_timeout(d,1000);
  if (r<=0) {
    if(r<0)
      fprintf(stderr, "Error reading from headset\n");
    else
      fprintf(stderr, "Headset Timeout...\n");
    emokit_close(d);
    emokit_delete(d);
  }
}
