#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "cubex.h"
#include <string>

#define MIN(a,b) ((a)>(b)?(b):(a))
#define MAX(a,b) ((a)<(b)?(b):(a))

#define  LOG_TAG    "libcolordecoder"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
	JNIEXPORT jstring JNICALL Java_com_droidtools_rubiksolver_RubikCube_nativeSolve(JNIEnv * env, jobject obj, jintArray data)
	{

		Cubex thecube;
		jstring jstrOutput;
		//jboolean blnIsCopy;
		//char* strCOut;
		//const char* strCIn = (env)->GetStringUTFChars(data , &blnIsCopy);
		/*char dat[55];
		jint *elements=(env)->GetIntArrayElements(data, 0);
		for (int i=0; i<54; i++)
		{
			dat[i] = elements[i]+48;
		}
		dat[54] = 0;
		(env)->ReleaseIntArrayElements(data, elements, 0);*/
		
		jint *dat=(env)->GetIntArrayElements(data, 0);
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				*thecube.face(j, 2, -i) = dat[i*3+j+4];
				*thecube.face(-2, -i, -j) = dat[i*3+j+13];
				*thecube.face(j, -i, -2) = dat[i*3+j+22];
				*thecube.face(2, -i, j) = dat[i*3+j+31];
				*thecube.face(-j, -i, 2) = dat[i*3+j+40];
				*thecube.face(j, -2, i) = dat[i*3+j+49];
			}
		}
		(env)->ReleaseIntArrayElements(data, dat, 0);
		//LOGE("JELLO", 0);
		int x = thecube.SolveCube();
		string thesolution;
		if (x != 0) {
			thesolution = "Error: Invalid Cube"; 
		} else {
			thesolution = thecube.solution;
		}
	
		//(env)->ReleaseStringUTFChars(data , strCIn); // release jstring
		jstrOutput = (env)->NewStringUTF(thesolution.c_str()); // convert char array to jstring
		return jstrOutput;


	}

}