#include <stdlib.h>
#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>

#define MIN(a,b) ((a)>(b)?(b):(a))
#define MAX(a,b) ((a)<(b)?(b):(a))

#define  LOG_TAG    "libcolordecoder"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// com.droidtools.rubiksolver.ColorDecoder
JNIEXPORT jobjectArray JNICALL Java_com_droidtools_rubiksolver_ColorDecoder_nativeSobelData(JNIEnv * env, jobject  obj, jobject bitmap)
{
	AndroidBitmapInfo info;
    uint32_t *pixels;
	uint32_t pixel;
    int ret,i,j,x,y,r,g,b,horizSobel,vertSobel,imageIndex;
	jobjectArray result;
	int sob[3][3];
	jint* data;
	jintArray iarr;

	jclass intArrCls = (*env)->FindClass(env, "[I");
     if (intArrCls == NULL) {
		 LOGE("FindClass intArrCld failed ! error=%d", 0);
         return NULL; /* exception thrown */
     }

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 && info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Bitmap format is not RGBA_8888 ! format=%d", info.format);
        return;
    }

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **)(&pixels))) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

	result = (*env)->NewObjectArray(env, info.width, intArrCls,
                                     NULL);

	/*for (i = 0; i < info.width; i++) {
		jint* tmp = malloc(info.height * sizeof(jint));
		jintArray iarr = (*env)->NewIntArray(env, info.height);
		if (tmp == NULL || iarr == NULL) {
			LOGE("Out of memory ! error=%d", 0);
            return NULL; /* out of memory error thrown /
        }

		for (j = 0; j < info.height; j++) {
            tmp[j] = 0;
        }

		(*env)->SetIntArrayRegion(env, iarr, 0, info.height, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
		free(tmp);
	}*/
	imageIndex = 0;
	data = malloc(info.height * sizeof(jint));
	if (data == NULL)
	{
		LOGE("Memory allocation failed - A");
		return NULL;
	}
	if (data == NULL) { LOGE("Out of memory ! error=%d", 0); return NULL; }
	for (j = 0; j < info.height; j++) {
		data[j] = 0;
    }
	iarr = (*env)->NewIntArray(env, info.height);
	if (iarr == NULL) { LOGE("Out of memory ! error=%d", 0); return NULL; }
	(*env)->SetIntArrayRegion(env, iarr, 0, info.height, data);
	(*env)->SetObjectArrayElement(env, result, imageIndex, iarr);
	imageIndex++;
	(*env)->DeleteLocalRef(env, iarr);
	free(data);

	int imW = info.width - 1;
	int imH = info.height - 1;
	//LOGE("STARTING COPY");
	for (x=1; x<imW; x++) {
		data = malloc(info.height * sizeof(jint));
		if (data == NULL)
		{
			LOGE("Memory allocation failed - B");
			return NULL;
		}
		data[0] = 0;
		//LOGE("STARTING ITER");
		for (y=1; y<imH; y++) {
			for (i=-1; i<=1; i++) {
				//LOGE("READING X1");
				for (j=-1; j<=1; j++) {
					int xx = x+i;
					int yy = y+j;
					if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
						//LOGE("READING X2");
						//pixels = (char*)pixels + info->stride;
						//pixel = pixels[( ((char*)pixels + info.stride*yy) +(xx % info.width) )];//image.getPixel(x+i,y+j);
						pixel = ((char*)pixels + info.stride*yy)[xx % info.width]; //pixels[( ((char*)pixels + info.stride*yy) +(xx % info.width) )];
						r = (int) ((pixel & 0x0000f800) >> 11);
						g = (int)((pixel& 0x000007e0) >> 5);
						b = (int) (pixel & 0x000001F );
					} else {
						//pixel = pixels[( ((char*)pixels + info.stride*yy) + (xx % info.width) )];//image.getPixel(x+i,y+j);
						pixel = ((char*)pixels + info.stride*yy)[xx % info.width]; 
						r = (int) ((pixel & 0x00FF0000) >> 16);
						g = (int)((pixel& 0x0000FF00) >> 8);
						b = (int) (pixel & 0x00000FF );
					}
					sob[i+1][j+1] = (int) (r * 299.0/1000 + g * 587.0/1000 + b * 114.0/1000);
				}
			}
			horizSobel = -(sob[1-1][1-1]) + 
			    (sob[1+1][1-1]) - 
			    (sob[1-1][1]) - (sob[1-1][1]) +
			    (sob[1+1][1]) + (sob[1+1][1]) -
			    (sob[1-1][1+1]) + 
			    (sob[1+1][1+1]);
	        vertSobel =  -(sob[1-1][1-1]) - 
	        (sob[1][1-1]) - sob[1][1-1] - 
	        (sob[1+1][1-1]) +
	        (sob[1-1][1+1]) + 
	        (sob[1][1+1]) + (sob[1][1+1]) + 
	        (sob[1+1][1+1]);
	        data[y] = MIN(255, MAX(0, (horizSobel+vertSobel)/2));
			//(*env)->SetObjectArrayElement(env, ((*env)->GetObjectArrayElement(env, result, x)), y, val);
		}
		//LOGE("FINISHED ITER");
		data[imH+1] = 0;
		iarr = (*env)->NewIntArray(env, info.height);
		if (iarr == NULL) { LOGE("Out of memory ! error=%d", 0); return NULL; }
		(*env)->SetIntArrayRegion(env, iarr, 0, info.height, data);
		(*env)->SetObjectArrayElement(env, result, imageIndex, iarr);
		imageIndex++;
		(*env)->DeleteLocalRef(env, iarr);
		free(data);
	}

	data = malloc(info.height * sizeof(jint));
	if (data == NULL)
	{
		LOGE("Memory allocation failed - C");
		return NULL;
	}
	if (data == NULL) { LOGE("Out of memory ! error=%d", 0); return NULL; }
	for (j = 0; j < info.height; j++) {
		data[j] = 0;
    }
	iarr = (*env)->NewIntArray(env, info.height);
	if (iarr == NULL) { LOGE("Out of memory ! error=%d", 0); return NULL; }
	(*env)->SetIntArrayRegion(env, iarr, 0, info.height, data);
	(*env)->SetObjectArrayElement(env, result, imageIndex, iarr);
	imageIndex++;
	(*env)->DeleteLocalRef(env, iarr);
	free(data);

	AndroidBitmap_unlockPixels(env, bitmap);
	return result;
}