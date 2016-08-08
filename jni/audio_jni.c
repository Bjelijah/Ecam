#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include <semaphore.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "audio_jni", __VA_ARGS__))   
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "audio_jni", __VA_ARGS__))   
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "audio_jni", __VA_ARGS__))  


struct AudioPlay
{
  /* multi thread */
  int method_ready;
  JavaVM * jvm;
  JNIEnv * env;
  jmethodID mid;
  jobject obj;
  jfieldID data_length_id;
  jbyteArray data_array;
  int data_array_len;

	int stop;
  sem_t over_audio_sem;
  sem_t over_audio_ret_sem;
};
static struct AudioPlay audioSelf;

void audio_stop()
{
	audioSelf.stop=1;
	//self.over = 1;   
	//sem_post(&self.over_audio_sem);  
	//sem_wait(&self.over_audio_ret_sem); 
}


void audio_play(const char* buf,int len,int au_sample,int au_channel,int au_bits)
{
	
	if (audioSelf.stop) return;

/*
  if (sem_trywait(&self.over_audio_sem)==0) {  
	  if (self.method_ready)
	  {  
		  
	  }
	  sem_post(&self.over_audio_ret_sem);  
	  self.stop=1;
	  return;
  }
  */

  if ((*audioSelf.jvm)->AttachCurrentThread(audioSelf.jvm, &audioSelf.env, NULL) != JNI_OK) {   
      LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);   
      return;
    }
  /* get JAVA method first */
  if (!audioSelf.method_ready) {
    

    jclass cls;
    cls = (*audioSelf.env)->GetObjectClass(audioSelf.env,audioSelf.obj);
    if (cls == NULL) {   
      LOGE("FindClass() Error.....");   
      goto error;   
    }
    //�ٻ�����еķ���   
    audioSelf.mid = (*audioSelf.env)->GetMethodID(audioSelf.env, cls, "audioWrite", "()V");
    if (audioSelf.mid == NULL) {   
      LOGE("GetMethodID() Error.....");   
      goto error;
    }

    audioSelf.method_ready=1;
  }
   
  /* update length */
  (*audioSelf.env)->SetIntField(audioSelf.env,audioSelf.obj,audioSelf.data_length_id,len);
  /* update data */

  if (len<=audioSelf.data_array_len) {
	  //LOGI("audio_play");

    (*audioSelf.env)->SetByteArrayRegion(audioSelf.env,audioSelf.data_array,0,len,buf);

    /* notify the JAVA */
    (*audioSelf.env)->CallVoidMethod(audioSelf.env, audioSelf.obj, audioSelf.mid, NULL);


  }

   if ((*audioSelf.jvm)->DetachCurrentThread(audioSelf.jvm) != JNI_OK) {   
				LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);   
	}   
  /* char* data = (*self.env)->GetByteArrayElements(self.env,self.data_array,0); */
  /* memcpy(data,buf,len); */
 
  return;

 error:
  if ((*audioSelf.jvm)->DetachCurrentThread(audioSelf.jvm) != JNI_OK) {   
    LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);   
  }   
}

JNIEXPORT void JNICALL Java_com_howell_activity_PlayerActivity_nativeAudioInit
(JNIEnv *env, jobject obj)
{
  (*env)->GetJavaVM(env,&audioSelf.jvm);   

  //����ֱ�Ӹ�ֵ(g_obj = obj)   
  audioSelf.obj = (*env)->NewGlobalRef(env,obj);
  jclass clz = (*env)->GetObjectClass(env, obj);
  audioSelf.data_length_id = (*env)->GetFieldID(env,clz, "mAudioDataLength", "I");

  jfieldID id = (*env)->GetFieldID(env,clz,"mAudioData","[B");

  jbyteArray data = (*env)->GetObjectField(env,obj,id);
  audioSelf.data_array = (*env)->NewGlobalRef(env,data);
  (*env)->DeleteLocalRef(env, data);
  audioSelf.data_array_len =(*env)->GetArrayLength(env,audioSelf.data_array);

  sem_init(&audioSelf.over_audio_sem,0,0);
  sem_init(&audioSelf.over_audio_ret_sem,0,0);

  audioSelf.method_ready = 0;
  audioSelf.stop = 0;
}

JNIEXPORT void JNICALL Java_com_howell_activity_PlayerActivity_nativeAudioStop
(JNIEnv *env, jclass cls)
{
  audio_stop();
}

JNIEXPORT void JNICALL Java_com_howell_activity_PlayerActivity_nativeAudioDeinit
(JNIEnv *env, jobject obj)
{
  /* TODO */
  
}
