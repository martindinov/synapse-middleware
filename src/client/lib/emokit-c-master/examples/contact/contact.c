/* 

Get real-time contact quality readings

  */

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include "emokit/emokit.h"
#include "headsets_epoc_EpocConnector.h"

static struct emokit_device* d;

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_helloTest
(JNIEnv *env, jobject obj) {
  fprintf(stderr, "\nBlabla, test, blabla\n");
}

void connectHeadsets() {
  d = emokit_create();
  int count=emokit_get_count(d, EMOKIT_VID, EMOKIT_PID);
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
}

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_connectHeadsets
(JNIEnv *env, jobject obj) {
  connectHeadsets();
}

void readData() {
  struct emokit_frame c;
  int err = emokit_read_data_timeout(d, 1000);
  if(err > 0) {
    c = emokit_get_next_frame(d);			
    fprintf(stderr,"\nData:\nF3:%4d\nFC6:%4d\nP7:%4d\nT8:%4d\nF7:%4d\nF8:%4d\nT7:%4d\nP8:%4d\nAF4:%4d\nF4:%4d\nAF3:%4d\nO2:%4d\nO1:%4d\nFC5:%4d",c.F3, c.FC6, c.P7, c.T8,c.F7, c.F8, c.T7, c.P8, c.AF4, c.F4, c.AF3, c.O2, c.O1, c.FC5);
    
  } else if(err == 0) {
    fprintf(stderr, "Headset Timeout...\n");
  }
}

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_readData
(JNIEnv *env, jobject obj) {
  readData();
}

void disconnectHeadsets() {
  emokit_close(d);
  emokit_delete(d);
}

JNIEXPORT void JNICALL Java_headsets_epoc_EpocConnector_disconnectHeadsets
(JNIEnv *env, jobject obj) {
  disconnectHeadsets();
}





/*
//Uncomment the below to test as a standalone test app, otherwise we're compiling as a  JNI lib
int main(int argc, char **argv)
{
  int count = 0;
  d = emokit_create();
  connectHeadsets();
  while(count < 10000) {
    count++;
    readData();
  }
}
*/




