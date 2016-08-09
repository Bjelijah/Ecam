#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <jni.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <pthread.h>
#include <semaphore.h>
#include "ecamstreamreq.h"
#include "ice.h"
#include "hwplay/stream_type.h"
#include "hwplay/play_def.h"
#include <time.h>
#include <unistd.h>
#include "g711/g711.h"
#include <sys/timeb.h>
#include "hwplay/stream_type.h"
#include "hwplay/play_def.h"

#include "com_howell_jni_JniUtil.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "jni.cc", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "jni.cc", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "jni.cc", __VA_ARGS__))


struct YV12glDisplay
{
	char * y;
	char * u;
	char * v;
	unsigned long long time;
	int width;
	int height;
	//int inited;
	int enable;
	int is_catch_picture;
	char path[50];

	/* multi thread */
	int method_ready;
	JavaVM * jvm;
	JNIEnv * env;
	jmethodID mid,mSetTime;
	jobject obj;
	pthread_mutex_t lock;
	sem_t over_sem;
	sem_t over_ret_sem;
	int lock_ret;
};

static struct YV12glDisplay self;


void yuv12gl_set_enable(int enable)
{
	self.enable = enable;
	self.method_ready = 0;
}

void yv12gl_display(const unsigned char * y, const unsigned char *u,const unsigned char *v, int width, int height, unsigned long long time)
{
	//LOGE("display timestamp: %llu",time);

	if (!self.enable) return;
	self.time = time/1000;

	//LOGE("self.time :%llu %llu", self.time,time);
	if(self.jvm->AttachCurrentThread( &self.env, NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}

	/* get JAVA method first */
	if (!self.method_ready) {
		//LOGE("111111111");

		jclass cls = self.env->GetObjectClass(self.obj);
		//self.clz = (*self.env)->FindClass(self.env, "com/howell/webcam/player/YV12Renderer");
		if (cls == NULL) {
			LOGE("FindClass() Error.....");
			goto error;
		}
		//�ٻ�����еķ���
		self.mid = self.env->GetMethodID( cls, "requestRender", "()V");
		self.mSetTime = self.env->GetMethodID( cls, "setTime", "(J)V");
		if (self.mid == NULL || self.mSetTime == NULL) {
			LOGE("GetMethodID() Error.....");
			goto error;
		}
		self.method_ready=1;
	}
	//LOGE("22222222");
	self.env->CallVoidMethod(self.obj,self.mSetTime,self.time);
	/*
  if (sem_trywait(&self.over_sem)==0) {
	  if (self.method_ready)
	  {

	  }
	  sem_post(&self.over_ret_sem);
	  self.enable=0;
	  return;
  }
	 */
	//LOGE("33333333");
	pthread_mutex_lock(&self.lock);
	if (width!=self.width || height!=self.height) {
		self.y = (char *)realloc(self.y,width*height);
		self.u = (char *)realloc(self.u,width*height/4);
		self.v = (char *)realloc(self.v,width*height/4);
		self.width = width;
		self.height = height;
	}
	memcpy(self.y,y,width*height);
	memcpy(self.u,u,width*height/4);
	memcpy(self.v,v,width*height/4);
	pthread_mutex_unlock(&self.lock);

	//LOGE("4444444");
	/* notify the JAVA */
	self.env->CallVoidMethod( self.obj, self.mid, NULL);
	//LOGE("555555555");
	//getNowTime();
	if (self.jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return;

	error:
	if (self.jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeInit
(JNIEnv *env, jclass,jobject obj){
	env->GetJavaVM(&self.jvm);
	self.obj = env->NewGlobalRef(obj);
	pthread_mutex_init(&self.lock,NULL);
	self.width = 352;
	self.height = 288;
	self.y = (char *)malloc(self.width*self.height);
	self.u = (char *)malloc(self.width*self.height/4);
	self.v = (char *)malloc(self.width*self.height/4);
	memset(self.y,0,self.width*self.height);
	memset(self.u,128,self.width*self.height/4);
	memset(self.v,128,self.width*self.height/4);
	self.time = 0;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeOnSurfaceCreated
(JNIEnv *, jclass){
	self.enable=1;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeRenderY
(JNIEnv *, jclass){
	//	LOGE("nativeRenderY");
	self.lock_ret = pthread_mutex_trylock(&self.lock);
	if(self.lock_ret != 0){
		return;
	}
	if (self.y == NULL) {
		char value[4] = {0,0,0,0};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,2,2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		//LOGI("render y");
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,self.width,self.height,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,self.y);
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeRenderU
(JNIEnv *, jclass){
	if(self.lock_ret != 0){
		return;
	}
	if (self.u == NULL) {
		char value[] = {128};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,self.width/2,self.height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,self.u);
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeRenderV
(JNIEnv *, jclass){
	if(self.lock_ret != 0){
		return;
	}
	if (self.v==NULL) {
		char value[] = {128};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,self.width/2,self.height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,self.v);
	}
	pthread_mutex_unlock(&self.lock);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeDeinit
(JNIEnv *, jclass){
	self.method_ready = 0;
	free(self.y);
	free(self.u);
	free(self.v);
}



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
}

void audio_play(const char* buf,int len,int au_sample,int au_channel,int au_bits)
{

	if (audioSelf.stop) return;


	if (audioSelf.jvm->AttachCurrentThread( &audioSelf.env, NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	/* get JAVA method first */
	if (!audioSelf.method_ready) {


		jclass cls;
		cls = audioSelf.env->GetObjectClass(audioSelf.obj);
		if (cls == NULL) {
			LOGE("FindClass() Error.....");
			goto error;
		}
		//�ٻ�����еķ���
		audioSelf.mid = audioSelf.env->GetMethodID( cls, "audioWrite", "()V");
		if (audioSelf.mid == NULL) {
			LOGE("GetMethodID() Error.....");
			goto error;
		}

		audioSelf.method_ready=1;
	}

	/* update length */
	audioSelf.env->SetIntField(audioSelf.obj,audioSelf.data_length_id,len);
	/* update data */

	if (len<=audioSelf.data_array_len) {
		//LOGI("audio_play");

		audioSelf.env->SetByteArrayRegion(audioSelf.data_array,0,len,(const signed char *)buf);

		/* notify the JAVA */
		audioSelf.env->CallVoidMethod( audioSelf.obj, audioSelf.mid, NULL);


	}

	if (audioSelf.jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	/* char* data = (*self.env)->GetByteArrayElements(self.env,self.data_array,0); */
	/* memcpy(data,buf,len); */

	return;

	error:
	if (audioSelf.jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
}







JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioInit
(JNIEnv *env, jclass,jobject obj){
	env->GetJavaVM(&audioSelf.jvm);

	audioSelf.obj = env->NewGlobalRef(obj);
	jclass clz = env->GetObjectClass( obj);
	audioSelf.data_length_id = env->GetFieldID(clz, "mAudioDataLength", "I");

	jfieldID id = env->GetFieldID(clz,"mAudioData","[B");

	jbyteArray data = (jbyteArray)env->GetObjectField(obj,id);
	audioSelf.data_array = (jbyteArray)env->NewGlobalRef(data);
	env->DeleteLocalRef( data);
	audioSelf.data_array_len =env->GetArrayLength(audioSelf.data_array);

	sem_init(&audioSelf.over_audio_sem,0,0);
	sem_init(&audioSelf.over_audio_ret_sem,0,0);

	audioSelf.method_ready = 0;
	audioSelf.stop = 0;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioStop
(JNIEnv *, jclass){
	audio_stop();
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeAudioDeinit
(JNIEnv *, jclass){

}

#define RESOURCE_ARRAY_LENGHT 20
struct StreamResource
{
	ecam_stream_req_t * req;
	//struct ecam_stream_req_context * context;
	PLAY_HANDLE play_handle;
	int is_playback;
	int has_create_thread;
	int is_exit;
	int stream_count;
	pthread_t id;
	pthread_t audio_thread_id;

	JavaVM * jvm;
	JNIEnv * env;
	jmethodID mid;
	jobject obj;

	size_t stream_len;

	time_t beg_time,end_time;
	//unsigned long yuv_timestamp;
};
static pthread_once_t once_ctrl = PTHREAD_ONCE_INIT;
static struct StreamResource *res[RESOURCE_ARRAY_LENGHT] ;


static void global_init(void)
{
	ice_global_init();
	hwplay_init(1,0,0);
}

struct timeval last_tv;

static int num = 0;
static uint32_t last_time = 0;


static void on_yuv_callback_ex(PLAY_HANDLE handle,
		const unsigned char* y,
		const unsigned char* u,
		const unsigned char* v,
		int y_stride,
		int uv_stride,
		int width,
		int height,
		unsigned long long time,
		long user)
{
	if(res[user]->is_exit == 1) return;
	yv12gl_display(y,u,v,width,height,time);
}

static void on_source_callback(PLAY_HANDLE handle,
		int type,//3-音频,1-视频
		const char* buf,//数据缓存,如果是视频，则为YV12数据，如果是音频则为pcm数据
		int len,//数据长度,如果为视频则应该等于w * h * 3 / 2
		unsigned long timestamp,//时标,单位为毫秒
		long sys_tm,//osd 时间(1970到现在的UTC时间)
		int w,//视频宽,音频数据无效
		int h,//视频高,音频数据无效
		int framerate,//视频帧率,音频数据无效
		int au_sample,//音频采样率,视频数据无效
		int au_channel,//音频通道数,视频数据无效
		int au_bits,//音频位宽,视频数据无效
		long user)
{
	//LOGE("type=%d  len=%d  w=%d  h=%d  timestamp=%ld sys_tm=%ld  framerate=%d  au_sample=%d  au_channel=%d au_bits=%d",type,len,w,h,timestamp,sys_tm,framerate,au_sample,au_channel,au_bits);
	if(res[user]->is_exit == 1) return;
	if(type == 0){//音频
//		audio_play(buf,len,0,0,0);//add cbj
		audio_play(buf,len,au_sample,au_channel,au_bits);
	}else if(type == 1){//视频
		unsigned char* y = (unsigned char *)buf;
		unsigned char* u = y+w*h;
		unsigned char* v = u+w*h/4;
		yv12gl_display(y,u,v,w,h,timestamp);
	}
}

static void on_audio_callback(PLAY_HANDLE handle,
		const char* buf,//数据缓存,如果是视频，则为YV12数据，如果是音频则为pcm数据
		int len,//数据长度,如果为视频则应该等于w * h * 3 / 2
		unsigned long timestamp,//时标,单位为毫秒
		long user)
{
	if(res[user]->is_exit == 1) return;
	audio_play(buf,len,0,0,0);

}

void* timer_thread(void *arg){
	int arr_index = (int)arg;
	if(res[arr_index]->jvm->AttachCurrentThread(&res[arr_index]->env, NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return 0;
	}
	/* get JAVA method first */
	jclass cls = res[arr_index]->env->GetObjectClass(res[arr_index]->obj);
	if (cls == NULL) {
		LOGE("FindClass() Error.....");
		goto error;
	}
	res[arr_index]->mid = res[arr_index]->env->GetMethodID( cls, "getStreamLen", "(I)V");

	if (res[arr_index]->mid == NULL) {
		goto error;
	}
	while(!res[arr_index]->is_exit){
		usleep(200*1000);
		/* notify the JAVA */
		res[arr_index]->env->CallVoidMethod(res[arr_index]->obj,res[arr_index]->mid,res[arr_index]->stream_len);
		res[arr_index]->stream_len = 0;
	}
	if (res[arr_index]->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return 0;
	error:
	if (res[arr_index]->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return 0;
}

static uint32_t get_my_clock() {
	struct timeval time;
	gettimeofday(&time, NULL);
	uint64_t value = ((uint64_t)time.tv_sec) * 1000 + (time.tv_usec / 1000);
	return (uint32_t)(value & 0xfffffffful);
}

static uint32_t my_last_time = 0;
static int my_frame_num = 0;


static void OnStreamArrive(ecam_stream_req_t * req, ECAM_STREAM_REQ_FRAME_TYPE media_type, const char * data, size_t len, uint32_t timestamp) {
	/**
	 *帧数统计
	my_frame_num++;
	if(my_last_time==0){
		my_last_time = get_my_clock();
		LOGI("last_time=%d",my_last_time);
	}



	if((get_my_clock()-my_last_time)>5000){
		LOGI("num=%d",my_frame_num);
		my_frame_num=0;
		my_last_time=0;
	}

	return;
	 */
	//__android_log_print(ANDROID_LOG_INFO, "thread", "aaaaa");
	//return;
	//PLAY_HANDLE ph = ecam_stream_req_get_usr_data(req);
	//__android_log_print(ANDROID_LOG_INFO, "OnStreamArrive", "timestamp: %d",timestamp);
	//return;
	//if(media_type == 2){
	//return;
	//}
	//	LOGI("on stream come");
	int arr_index = (int)ecam_stream_req_get_usr_data(req);
	//__android_log_print(ANDROID_LOG_INFO, "OnStreamArrive", "len: %d, arr_idx: %d",len,arr_index);

	res[arr_index]->stream_len += len;
	if(media_type != 2){
		res[arr_index]->stream_count++;
	}
	if(res[arr_index]->has_create_thread == 0){
		int ret;
		ret = pthread_create(&res[arr_index]->id,NULL,timer_thread,(void *)arr_index);
		if(ret != 0){
			__android_log_print(ANDROID_LOG_INFO, "thread", "create thread fail");
			//return;
		}
		res[arr_index]->has_create_thread = 1;
		//pthread_join(res->id,NULL);
	}

	stream_head head ;
	head.len = len + sizeof(stream_head);
	head.sys_time = time(NULL);
	head.tag = 0x48574D49;
	head.time_stamp =  (unsigned long long)timestamp / 90 * 1000;
	if(media_type == kFrameTypeAudio){
		head.time_stamp =  (unsigned long long)timestamp / 8 * 1000;
	}
	head.type = media_type;
	//__android_log_print(ANDROID_LOG_INFO, "jni", "-------------media_type %d- timestamp: %llu",media_type,head.time_stamp);
	//getNowTime();

	if(res[arr_index]->is_playback == 0){
		hwplay_input_data(res[arr_index]->play_handle, (char*)&head ,sizeof(head));

		hwplay_input_data(res[arr_index]->play_handle, data ,len);

	}else if (res[arr_index]->is_playback == 1)
	{

		while(res[arr_index]->play_handle != -1 && !res[arr_index]->is_exit)
		{
			if(!hwplay_input_data(res[arr_index]->play_handle, (char*)&head ,sizeof(head)))
			{
				usleep(10000);
				continue;
			}
			if(!hwplay_input_data(res[arr_index]->play_handle, data ,len))
			{
				usleep(10000);
				continue;
			}
			break;
		}

	}

}

static PLAY_HANDLE init_play_handle(int is_playback,int arr_index){
	char *desc = (char *)malloc(100);
	memset(desc,0,100);
	int payload;
	int ret = -1;
	ret = ecam_stream_req_get_audio(res[arr_index]->req, desc, &payload);
	__android_log_print(ANDROID_LOG_INFO, "init_play_handle", "ecam_stream_req_get_audio ret:%d,desc:%s,payload:%d",ret,desc,payload);

	RECT area ;
	HW_MEDIAINFO media_head;
	memset(&media_head,0,sizeof(media_head));
	media_head.media_fourcc = HW_MEDIA_TAG;
	media_head.au_channel = 1;
	media_head.au_sample = 8;
	media_head.au_bits = 16;
	media_head.adec_code = ADEC_AAC;
	media_head.vdec_code = VDEC_H264;
	if(ret == 1){
		if(strstr(desc,"pcmu") != NULL || strstr(desc,"PCMU") != NULL){
			__android_log_print(ANDROID_LOG_INFO, "init_play_handle", "ecam_stream_req_get_audio g711");
			media_head.adec_code = ADEC_G711U;
		}
	}
	free(desc);
	__android_log_print(ANDROID_LOG_INFO, "init_play_handle", "ecam_stream_req_get_audio aac");
	PLAY_HANDLE  ph = hwplay_open_stream((char*)&media_head,sizeof(media_head),1024*1024,is_playback,area);
	ret = hwplay_open_sound(ph);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "hwplay_open_sound ret:%d",ret);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "is_playback is:%d",is_playback);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "ph is:%d",ph);
//		hwplay_register_yuv_callback_ex(ph,on_yuv_callback_ex,arr_index);//FIXME
	//	hwplay_register_audio_callback(ph,on_audio_callback,arr_index);

	hwplay_register_source_data_callback(ph,on_source_callback,arr_index);
	hwplay_play(ph);
	return ph;
}

static struct ecam_stream_req_context * fill_context(JNIEnv *env,jobject obj){
	__android_log_print(ANDROID_LOG_INFO, "jni", "start init context");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "1");
	if(env == NULL){
		__android_log_print(ANDROID_LOG_INFO, "fill_context", "env == null");
	}
	if(obj == NULL){
		__android_log_print(ANDROID_LOG_INFO, "fill_context", "obj == null");
	}
	jclass clazz = env->GetObjectClass(obj);
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.1");
	jfieldID playbackID = env->GetFieldID(clazz, "playback", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.2");
	jfieldID begID = env->GetFieldID(clazz,"beg", "J");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.3");
	jfieldID endID = env->GetFieldID(clazz,"end", "J");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.4");
	jfieldID re_inviteID = env->GetFieldID(clazz, "re_invite", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.5");
	jfieldID method_bitmapID = env->GetFieldID(clazz, "method_bitmap", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.6");
	jfieldID udp_addrID = env->GetFieldID(clazz, "udp_addr", "Ljava/lang/String;");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.7");
	jfieldID udp_portID = env->GetFieldID(clazz, "udp_port", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.8");
	jfieldID ice_optID = env->GetFieldID(clazz, "ice_opt", "Lcom/howell/entityclass/StreamReqIceOpt;");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "0.9");
	jfieldID cryptoID = env->GetFieldID(clazz, "crypto", "Lcom/howell/entityclass/Crypto;");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "1.0");
	jfieldID channelID = env->GetFieldID(clazz, "channel", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "1.1");
	jfieldID streamID = env->GetFieldID(clazz, "stream", "I");
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "1.2");
	jint jplayback = (jint)env->GetIntField(obj, playbackID);
	jlong jbeg = (jlong)env->GetLongField(obj, begID);
	jlong jend = (jlong)env->GetLongField(obj, endID);
	jint jre_invite = (jint)env->GetIntField(obj, re_inviteID);
	jint jmethod_bitmap = (jint)env->GetIntField(obj, method_bitmapID);
	jstring judp_addr = (jstring)env->GetObjectField(obj, udp_addrID);
	jint judp_port = (jint)env->GetIntField(obj, udp_portID);
	//jobject jice_opt = (jobject)(*env)->GetObjectField(env,obj, ice_optID);

	jint jchannel = (jint)env->GetIntField(obj, channelID);
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "channel:%d",jchannel);
	jint jstream = (jint)env->GetIntField(obj, streamID);
	__android_log_print(ANDROID_LOG_INFO, "fill_context", "stream:%d",jstream);

	const char* cudp_addr = env-> GetStringUTFChars(judp_addr,NULL);
	__android_log_print(ANDROID_LOG_INFO, "jni", "jplayback %d,jre_invite %d,jmethod_bitmap %d,judp_addr %s,judp_port %d",jplayback,jre_invite,jmethod_bitmap
			,cudp_addr,judp_port);

	jobject streamReqIceOpt = (jobject)env->GetObjectField(obj, ice_optID);

	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 0");

	jclass clazz2 = env->GetObjectClass( streamReqIceOpt);

	jfieldID comp_cntID = env->GetFieldID(clazz2, "comp_cnt", "I");
	jfieldID stun_addrID = env->GetFieldID(clazz2, "stun_addr", "Ljava/lang/String;");
	jfieldID stun_portID = env->GetFieldID(clazz2, "stun_port", "I");
	jfieldID turn_addrID = env->GetFieldID(clazz2, "turn_addr", "Ljava/lang/String;");
	jfieldID turn_portID = env->GetFieldID(clazz2, "turn_port", "I");
	jfieldID turn_tcpID = env->GetFieldID(clazz2, "turn_tcp", "I");
	jfieldID turn_usernameID = env->GetFieldID(clazz2, "turn_username", "Ljava/lang/String;");
	jfieldID turn_passwordID = env->GetFieldID(clazz2, "turn_password", "Ljava/lang/String;");

	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 10");

	jint jcomp_cnt = (jint)env->GetIntField(streamReqIceOpt, comp_cntID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 11");
	jstring jstun_addr = (jstring)env->GetObjectField(streamReqIceOpt, stun_addrID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 12");
	jint jstun_port = (jint)env->GetIntField(streamReqIceOpt, stun_portID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 13");
	jstring jturn_addr = (jstring)env->GetObjectField(streamReqIceOpt, turn_addrID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 14");
	jint jturn_port = (jint)env->GetIntField(streamReqIceOpt, turn_portID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 15");
	jint jturn_tcp = (jint)env->GetIntField(streamReqIceOpt, turn_tcpID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 16");
	jstring jturn_username = (jstring)env->GetObjectField(streamReqIceOpt, turn_usernameID);
	__android_log_print(ANDROID_LOG_INFO, "jni", ">>>>>>>>>>> here 17");
	jstring jturn_password = (jstring)env->GetObjectField(streamReqIceOpt, turn_passwordID);
	__android_log_print(ANDROID_LOG_INFO, "jni", "!!!!!----------turn_addr %p----------!!!!!!",jturn_addr);


	const char* cstun_addr = env-> GetStringUTFChars(jstun_addr,NULL);

	jobject crypto = (jobject)env->GetObjectField(obj, cryptoID);
	jclass clazz3 = env->GetObjectClass( crypto);

	jfieldID enableID = env->GetFieldID(clazz3, "enable", "I");
	jint jenable = (jint)env->GetIntField(crypto, enableID);
	__android_log_print(ANDROID_LOG_INFO, "jni", "jenable:%d",jenable);
	__android_log_print(ANDROID_LOG_INFO, "jni", "finish receive data");

	struct ecam_stream_req_context *c =(struct ecam_stream_req_context *) malloc(sizeof(*c));
	memset(c,0,sizeof(struct ecam_stream_req_context));
	__android_log_print(ANDROID_LOG_INFO, "jni", "start fill context");
	__android_log_print(ANDROID_LOG_INFO, "jni", "1");
	c->playback = jplayback;
	__android_log_print(ANDROID_LOG_INFO, "jni", "2");
	c->beg = jbeg;
	__android_log_print(ANDROID_LOG_INFO, "jni", "3");
	c->end = jend;
	__android_log_print(ANDROID_LOG_INFO, "jni", "4");
	c->re_invite = jre_invite;//false
	__android_log_print(ANDROID_LOG_INFO, "jni", "5");
	c->method_map = jmethod_bitmap;
	__android_log_print(ANDROID_LOG_INFO, "jni", "6");
	//if (udp_addr != NULL) {
	strncpy(c->udp_addr,cudp_addr,63);
	__android_log_print(ANDROID_LOG_INFO, "jni", "7");
	//}
	c->udp_port = judp_port;
	__android_log_print(ANDROID_LOG_INFO, "jni", "8");
	struct ICEOption *opt = (struct ICEOption *)malloc(sizeof(*opt));
	memset(opt,0,sizeof(struct ICEOption));
	__android_log_print(ANDROID_LOG_INFO, "jni", "9");
	opt->comp_cnt = jcomp_cnt;
	__android_log_print(ANDROID_LOG_INFO, "jni", "10");
	//stun server
	strcpy(opt->stun_addr,cstun_addr);
	opt->stun_port = jstun_port;
	__android_log_print(ANDROID_LOG_INFO, "jni", "11");
	//turn server
	if(jturn_addr != NULL){
		const char* cturn_addr = env-> GetStringUTFChars(jturn_addr,NULL);
		strcpy(opt->turn_addr,cturn_addr);
		env->ReleaseStringUTFChars(jturn_addr,cturn_addr);
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "12");
	if(jturn_port != -1){
		opt->turn_port = jturn_port;
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "13");
	if (jturn_tcp != -1)
	{
		opt->turn_tcp = jturn_tcp;
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "14");
	if(jturn_username != NULL){
		const char* cturn_username = env-> GetStringUTFChars(jturn_username,NULL);
		strcpy(opt->turn_username,cturn_username);
		env->ReleaseStringUTFChars(jturn_username,cturn_username);
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "15");
	if (jturn_password != NULL)
	{
		const char* cturn_password = env-> GetStringUTFChars(jturn_password,NULL);
		strcpy(opt->turn_password,cturn_password);
		env->ReleaseStringUTFChars(jturn_password,cturn_password);
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "16");
	//__android_log_print(ANDROID_LOG_INFO, "jni", "jturn_addr %s,jturn_port %d,jturn_tcp %d,jturn_username %s,jturn_password %s",jturn_addr,jturn_port,jturn_tcp,jturn_username,jturn_password);

	c->ice_opt = *opt;
	__android_log_print(ANDROID_LOG_INFO, "jni", "17");
	free(opt);
	//__android_log_print(ANDROID_LOG_INFO, "jni", "stun_server:<%s>",c->ice_opt.stun_addr);
	//__android_log_print(ANDROID_LOG_INFO, "jni", "createStreamReqContext success");
	env->ReleaseStringUTFChars(judp_addr,cudp_addr);
	env->ReleaseStringUTFChars(jstun_addr,cstun_addr);
	__android_log_print(ANDROID_LOG_INFO, "jni", "18");
	c -> crypto.enable = jenable;
	LOGI("crypto  enable = %d",c->crypto.enable);
	c->channel = jchannel;
	__android_log_print(ANDROID_LOG_INFO, "jni", "jni channel:%d",c->channel);
	c->stream = jstream;
	__android_log_print(ANDROID_LOG_INFO, "jni", "jni stream:%d",c->stream);
	__android_log_print(ANDROID_LOG_INFO, "jni", "19");
	return c;
}

static jlong new_resource(JNIEnv *env,jobject obj,const char * account,int is_playback)
{
	/* make sure init once */
	int arr_index = -1;
	pthread_once(&once_ctrl,global_init);
	__android_log_print(ANDROID_LOG_INFO, "res[handle_flag]", "11111111");
	int i;
	for(i = 0 ; i < RESOURCE_ARRAY_LENGHT ; i++){
		if(res[i] == NULL){
			arr_index = i;
			break;
		}
	}
	if (arr_index == -1) {
		__android_log_print(ANDROID_LOG_INFO, "new resource", "index out of bound");
		return -1;
	}
	res[arr_index] = (struct StreamResource *)calloc(1,sizeof(struct StreamResource));
	res[arr_index]->req = ecam_stream_req_new(account);
	res[arr_index]->is_playback = is_playback;
	res[arr_index]->is_exit = 0;
	res[arr_index]->stream_len = 0;
	res[arr_index]->has_create_thread = 0;
	res[arr_index]->stream_count = 0;
	res[arr_index]->beg_time = 0;
	res[arr_index]->end_time = 0;
	ecam_stream_req_set_usr_data(res[arr_index]->req,(void *)arr_index);
	ecam_stream_req_regist_stream_cb(res[arr_index]->req,OnStreamArrive);
	env->GetJavaVM(&res[arr_index]->jvm);
	res[arr_index]->obj = env->NewGlobalRef(obj);
	return arr_index;
}

static void free_resource(int handle)
{
	int arr_index = handle;
	if (res[arr_index] != NULL) {
		__android_log_print(ANDROID_LOG_INFO, "jni", "stop1111");

		__android_log_print(ANDROID_LOG_INFO, "jni", "stop22222");
		//pthread_join(res[flag]->id,NULL);
		__android_log_print(ANDROID_LOG_INFO, "jni", "start free ecam_stream_req stop");
		ecam_stream_req_free(res[arr_index]->req);
		__android_log_print(ANDROID_LOG_INFO, "jni", "finish free ecam_stream_req stop");
		__android_log_print(ANDROID_LOG_INFO, "jni", "start free hwplay stop");
		__android_log_print(ANDROID_LOG_INFO, "jni", "res[arr_index]->play_handle %d",res[arr_index]->play_handle);
		hwplay_stop(res[arr_index]->play_handle);
		__android_log_print(ANDROID_LOG_INFO, "jni", "stop333333333333");
		__android_log_print(ANDROID_LOG_INFO, "jni", "finish free hwplay stop");
		free(res[arr_index]);
		res[arr_index]=NULL;
	}else{
		__android_log_print(ANDROID_LOG_INFO, "jni", "stop handle is NULL!! ");
	}
}


JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeSetCatchPictureFlag
(JNIEnv *env, jclass, jlong index, jstring jpath, jint jlength){
	const char* temp = env-> GetStringUTFChars(jpath,NULL);
	int ret = hwplay_save_to_jpg(res[index]->play_handle,temp,70);
	env->ReleaseStringUTFChars(jpath,temp);
	return ret;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeJoinThread
(JNIEnv *, jclass, jlong handle){
	__android_log_print(ANDROID_LOG_INFO, "jni", "handle:%d",handle);
	__android_log_print(ANDROID_LOG_INFO, "jni", "start join thread ");
	int arr_index = handle;
	res[arr_index]->is_exit = 1;
	if(res[arr_index]->id != 0)
		pthread_join(res[arr_index]->id,NULL);
	__android_log_print(ANDROID_LOG_INFO, "jni", "finish join thread ");
}

JNIEXPORT jlong JNICALL Java_com_howell_jni_JniUtil_nativeCreateHandle
(JNIEnv *env, jclass, jobject obj, jstring str, jint is_palyback){
	const char * account = env->GetStringUTFChars(str,NULL);
	return new_resource(env,obj,account,is_palyback);
}

JNIEXPORT jstring JNICALL Java_com_howell_jni_JniUtil_nativePrepareSDP
(JNIEnv *env, jclass, jlong handle, jobject obj){
	__android_log_print(ANDROID_LOG_INFO, "jni", "start prepareSDP ");
	if(obj == NULL){
		__android_log_print(ANDROID_LOG_INFO, "jni", "obj is null ");
	}
	char * local_sdp;
	struct ecam_stream_req_context *c = fill_context(env,obj);
	int arr_index = handle;
	__android_log_print(ANDROID_LOG_INFO, "jni", "arr_index:%d handle:%ld",arr_index,handle);
	__android_log_print(ANDROID_LOG_INFO, "jni", "res:%p",res[arr_index]);
	__android_log_print(ANDROID_LOG_INFO, "jni", "req:%p",res[arr_index]->req);
	local_sdp = (char *)ecam_stream_req_prepare_sdp(res[arr_index]->req,c);
	__android_log_print(ANDROID_LOG_INFO, "jni", "sdp:<%s>",local_sdp);
	free(c);
	__android_log_print(ANDROID_LOG_INFO, "jni", "prepareSDP success");
	return env->NewStringUTF(local_sdp);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeHandleRemoteSDP
(JNIEnv *env, jclass, jlong handle, jobject obj, jstring dialog_id, jstring remote_sdp){
	struct ecam_stream_req_context *c = fill_context(env,obj);
	int arr_index = handle;
	if(res[arr_index] == NULL) return -1;//澶辫触
	const char *dialog_id_jni = env-> GetStringUTFChars(dialog_id,NULL);
	const char *remote_sdp_jni = env-> GetStringUTFChars(remote_sdp,NULL);
	ecam_stream_req_handle_remote_sdp(res[arr_index]->req,c,dialog_id_jni,remote_sdp_jni);
	//初始化解码器
	res[arr_index]->play_handle = init_play_handle(res[arr_index]->is_playback,arr_index);
	free(c);
	env->ReleaseStringUTFChars(remote_sdp,remote_sdp_jni);
	env->ReleaseStringUTFChars(remote_sdp,dialog_id_jni);
	__android_log_print(ANDROID_LOG_INFO, "jni", "handleRemoteSDP success");
	return 0;//鎴愬姛
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeStart
(JNIEnv *env, jclass, jlong handle, jobject obj, jint timeout_ms){
	__android_log_print(ANDROID_LOG_INFO, "jni", "!!!!!!-----start start----------!!!!");
	struct ecam_stream_req_context *c = fill_context(env,obj);
	__android_log_print(ANDROID_LOG_INFO, "jni", "stream:%d,channel:%d",c->stream,c->channel);
	int arr_index = handle;
	if(res[arr_index] == NULL){
		__android_log_print(ANDROID_LOG_INFO, "start func", "res[arr_index] == NULL");
		return -1;//澶辫触
	}
	__android_log_print(ANDROID_LOG_INFO, "jni", "ecam_stream_req_start");
	int ret = ecam_stream_req_start(res[arr_index]->req,c,timeout_ms);
	__android_log_print(ANDROID_LOG_INFO, "ret------------>", "ret %d",ret);
	free(c);
	__android_log_print(ANDROID_LOG_INFO, "jni", "!!!!!!-----finish start----------!!!!");
	return ret;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativeFreeHandle
(JNIEnv *, jclass, jlong handle){
	free_resource( handle);
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativePrepareReplay
(JNIEnv *, jclass, jint isPlayBack, jlong handle ){
	int arr_index = handle;
	ecam_stream_req_stop(res[arr_index]->req,3000);
	__android_log_print(ANDROID_LOG_INFO, ">>>>>>>>>", "ecam_stream_req_stop");
	hwplay_stop(res[arr_index]->play_handle);
	__android_log_print(ANDROID_LOG_INFO, ">>>>>>>>>", "hwplay_stop");
	yuv12gl_set_enable(1);
	res[arr_index]->play_handle = init_play_handle(isPlayBack,arr_index);
	__android_log_print(ANDROID_LOG_INFO, ">>>>>>>>>", "init_play_handle");
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeGetMethod
  (JNIEnv *, jclass, jlong handle){
	int arr_index = handle;
	int req_flag = ecam_stream_req_get_transfer_method(res[arr_index]->req);
	if(req_flag == 0){
		return 0;//OTHER
	}else if(req_flag == 1){
		return 3;//UPNP
	}else if(req_flag == 2){
		ICE_t *ice = ecam_stream_req_get_ice(res[arr_index]->req);
		int ice_flag = ice_get_type(ice);
		if(ice_flag == 0){
			return 0;//OTHER
		}else if(ice_flag == 1){
			return 2;//STUN
		}else if(ice_flag == 2){
			return 1;//TURN
		}else{
			return -1;//error
		}
	}else{
		return -1;//error
	}
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeGetStreamCount
  (JNIEnv *, jclass, jlong handle){
	__android_log_print(ANDROID_LOG_INFO, "getStreamCount", "000000000 ,handle:%d",handle);
	int arr_index = handle;
	__android_log_print(ANDROID_LOG_INFO, "getStreamCount", "1111111111");
	__android_log_print(ANDROID_LOG_INFO, "getStreamCount", "2222222222,res[arr_index]->stream_count: %d",res[arr_index]->stream_count);
	return res[arr_index]->stream_count;
}

JNIEXPORT void JNICALL Java_com_howell_jni_JniUtil_nativePlaybackPause
  (JNIEnv *, jclass, jlong handle, jboolean bPause){
	int arr_index = handle;
	hwplay_pause(res[arr_index]->play_handle,bPause);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeGetSdpTime
  (JNIEnv *, jclass, jlong handle){
	int arr_index = handle;
	ecam_stream_req_get_sdp_time(res[arr_index]->req,&res[arr_index]->beg_time, &res[arr_index]->end_time);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeGetBegSdpTime
  (JNIEnv *, jclass, jlong handle){
	int arr_index = handle;
	return res[arr_index]->beg_time;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeGetEndSdpTime
  (JNIEnv *, jclass, jlong handle){
	int arr_index = handle;
	return res[arr_index]->end_time;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_JniUtil_nativeSetAudioData
  (JNIEnv *env, jclass, jlong handle, jbyteArray bytes, jint len){
	int arr_index = handle;
	char *data = (char *)env->GetByteArrayElements(bytes,NULL);
	if(data == NULL){
		__android_log_print(ANDROID_LOG_INFO, "setAudioData", "data == NULL");
		return -1;
	}
	int dstlen = 0;
	char *encodeData = (char *)malloc(1024);
	memset(encodeData,0,1024);
	g711u_Encode((unsigned char *)data,(unsigned char *) encodeData, (unsigned int)len,(unsigned int*) &dstlen);
	int ret = ecam_stream_send_audio(res[arr_index]->req,0, encodeData, dstlen, 0);
	env->ReleaseByteArrayElements(bytes,(signed char *) data, 0);
	free(encodeData);
	return ret;
}







