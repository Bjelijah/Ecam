
#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include "SDL.h"

#include "hwplay/stream_type.h"
#include "hwplay/play_def.h"
//#include <fcntl.h>
//#include <unistd.h>
//#include "com_howell_invite_Client.h"

#define AUDIO_BUFFER_SIZE (2048 * 42)

struct SDLResource
{
	SDL_Overlay* pOverlay ;
	SDL_Surface* surface ;

	pthread_mutex_t mutex;
	pthread_cond_t cond;

	pthread_mutex_t audio_mutex;
	//pthread_cond_t audio_cond;
	int flag;
	int phoneWidth,phoneHeight;
	int pictureWidth,pictureHeight;

	int audio_buffer_len;
	int audio_write_len;
	int audio_read_len;

	int write_round;
	int read_round;

	Uint8 audio_buffer[AUDIO_BUFFER_SIZE];
	//Uint8 audio_temp_buffer[2048];
	Uint8 *paudio_buf_read;
	Uint8 *paudio_buf_write;
	//Uint8 *paudio_temp_buffer;
	int audioFlag;

	SDL_AudioSpec wanted;
	int has_data;

	unsigned long long frameTime;

	int picture_flag;
	//char *path;
	char path[50];

	int get_time_flag;

	//JNIEnv* env;
	//jclass cls;

	//int method_count;
	//JavaVM *g_jvm;
	//jobject g_obj;
	//int fd;
};

static struct SDLResource *sdl_resource = NULL;
static int has_sdl_resource = 0;

sdl_display_input_data(unsigned char * y_buf,unsigned char * u_buf,unsigned char * v_buf,int width,int height,unsigned long long time)
{
	__android_log_print(ANDROID_LOG_INFO, "jni", "width,height %d %d",width,height);
	if(has_sdl_resource == 0 || sdl_resource->flag == -1){
		__android_log_print(ANDROID_LOG_INFO, "jni", "return");
		return;
	}

	if(sdl_resource->get_time_flag == -1){
		sdl_resource->frameTime = 0;
		return ;
	}
	sdl_resource->frameTime = time;

	__android_log_print(ANDROID_LOG_INFO, "getTime", "time %llu",time);

	pthread_mutex_lock(&sdl_resource->mutex);
	if (sdl_resource->pOverlay == NULL) {
	//	quit(1);
		sdl_resource->pictureWidth = width;
		sdl_resource->pictureHeight = height;
		sdl_resource->pOverlay = SDL_CreateYUVOverlay(width, height, SDL_YV12_OVERLAY, sdl_resource->surface);
		__android_log_print(ANDROID_LOG_INFO, "jni", "SDL_CreateYUVOverlay success!\n");
	}
	//SDL_LockSurface(sdl_resource->surface);
	if(sdl_resource->pictureWidth != width || sdl_resource->pictureHeight != height){
		__android_log_print(ANDROID_LOG_INFO, "Width Height", "here 1");
		SDL_FreeYUVOverlay(sdl_resource->pOverlay);
		__android_log_print(ANDROID_LOG_INFO, "Width Height", "here 2");
		sdl_resource->pictureWidth = width;
		__android_log_print(ANDROID_LOG_INFO, "Width Height", "here 3");
		sdl_resource->pictureHeight = height;
		__android_log_print(ANDROID_LOG_INFO, "Width Height", "here 4");
		sdl_resource->pOverlay = SDL_CreateYUVOverlay(width, height, SDL_YV12_OVERLAY, sdl_resource->surface);
		__android_log_print(ANDROID_LOG_INFO, "Width Height", "here 5");
	}
	SDL_LockYUVOverlay(sdl_resource->pOverlay);
	
	__android_log_print(ANDROID_LOG_INFO, "JNI", "---start memcpy---");
	memcpy(sdl_resource->pOverlay->pixels[0], y_buf, width * height);
	memcpy(sdl_resource->pOverlay->pixels[1], v_buf, width * height / 4);
	memcpy(sdl_resource->pOverlay->pixels[2], u_buf, width * height / 4);
	SDL_UnlockYUVOverlay(sdl_resource->pOverlay);
	//SDL_UnlockSurface(sdl_resource->surface);
	sdl_resource->has_data++;
	pthread_cond_signal(&sdl_resource->cond);
	pthread_mutex_unlock(&sdl_resource->mutex);
	
}

/*JNIEXPORT void Java_org_libsdl_app_SDLActivity_setJNIEnv( JNIEnv* env, jobject obj)
{
	//保存全局JVM以便在子线程中使用
    sdl_resource->g_jvm = (*env)->GetJavaVM(env,&sdl_resource->g_jvm);
    //不能直接赋值(g_obj = obj)
    sdl_resource->g_obj = (*env)->NewGlobalRef(env,obj);
}*/

JNIEXPORT unsigned long long JNICALL Java_org_libsdl_app_SDLActivity_getTime(JNIEnv *env, jclass cls , jint get_time_flag)
{
	if(has_sdl_resource == 0){
		return 0;
	}
	if(get_time_flag == -1){
		sdl_resource->get_time_flag = -1;
	//	return sdl_resource->frameTime;
	}else if(get_time_flag == 0){
		sdl_resource->get_time_flag = 0;
	}
	return sdl_resource->frameTime/1000;
}

void fill_audio(void *udata, Uint8 *stream, int len)
{
	//return;
	if( sdl_resource->flag == -1){
		return;
	}
	//__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----len %d-----|",len);
	//pthread_mutex_lock(&sdl_resource->audio_mutex);
	//pthread_cond_wait(&sdl_resource->audio_cond,&sdl_resource->audio_mutex);
	
	if ( sdl_resource->audio_read_len >= sdl_resource->audio_buffer_len )
	{
		sdl_resource->read_round++;
		sdl_resource->audio_read_len = 0;
		sdl_resource->paudio_buf_read = sdl_resource->audio_buffer;	
	} 
	#if 1 
	if((sdl_resource->audio_read_len >= sdl_resource->audio_write_len && sdl_resource->read_round == sdl_resource->write_round) || sdl_resource->read_round > sdl_resource->write_round)
	{
		//Uint8 temp = 0;
		//Uint8 *ptemp = &temp;
		//memcpy(stream ,ptemp,sizeof(Uint8));
		return;
	}                            
	//len = ( len > sdl_resource->audio_read_len ? sdl_resource->audio_read_len : len );
	#endif
	__android_log_print(ANDROID_LOG_INFO, "JNI", "start read buf");       
	
	memcpy(stream ,sdl_resource->paudio_buf_read,len);

	//SDL_LockAudio();
	//__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----paudio_buf_read %p-----|",sdl_resource->paudio_buf_read);
	//SDL_MixAudio(stream, sdl_resource->paudio_temp_buffer, len, SDL_MIX_MAXVOLUME);   // void SDL_MixAudio(Uint8 *dst, Uint8 *src, Uint32 len, int volume);
	//SDL_UnlockAudio();

	sdl_resource->paudio_buf_read += len;
	sdl_resource->audio_read_len += len;
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----audio_read_len %d-----|",sdl_resource->audio_read_len);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----audio_write_len %d-----|",sdl_resource->audio_write_len);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----read_round %d-----|",sdl_resource->read_round);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----write_round %d-----|",sdl_resource->write_round);
	//pthread_mutex_unlock(&sdl_resource->audio_mutex);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "finish read buf");
	
}

sdl_audio_play(const char* buf,int len,int au_sample,int au_channel,int au_bits)
{
	if(has_sdl_resource==0 || sdl_resource->flag == -1 || sdl_resource->audioFlag == 1){
		__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----sdl_resource->audioFlag %d-----|",sdl_resource->audioFlag);
		return;
	}
	//int ret = write(sdl_resource->fd,buf,len);
	//__android_log_print(ANDROID_LOG_INFO, "JNI", "len:%d",len);
	//return;
	#if 1
	__android_log_print(ANDROID_LOG_INFO, "JNI", "len:%d au_sample:%d au_bits:%d au_channel:%d",len,au_sample,au_bits,au_channel);
	//pthread_mutex_lock(&sdl_resource->audio_mutex);
	if(sdl_resource->audio_write_len >= sdl_resource->audio_buffer_len){
		//memset(&sdl_resource->audio_buf_arr,0,sizeof(sdl_resource->audio_buf_arr));
		sdl_resource->write_round++;
		sdl_resource->paudio_buf_write = sdl_resource->audio_buffer;
		sdl_resource->audio_write_len = 0;
	}
	__android_log_print(ANDROID_LOG_INFO, "JNI", "start write buf");
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----paudio_buf_write %p-----|",sdl_resource->paudio_buf_write);

	memcpy(sdl_resource->paudio_buf_write, buf,len);

	sdl_resource->paudio_buf_write += len;
	sdl_resource->audio_write_len += len;

	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----audio_read_len %d-----|",sdl_resource->audio_read_len);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----audio_write_len %d-----|",sdl_resource->audio_write_len);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----read_round %d-----|",sdl_resource->read_round);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----write_round %d-----|",sdl_resource->write_round);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "finish write buf");
	//pthread_cond_signal(&sdl_resource->audio_cond);
	//pthread_mutex_unlock(&sdl_resource->audio_mutex);
	#endif
}

void pauseAudio(){
	
	if ( SDL_OpenAudio(&sdl_resource->wanted, NULL) < 0 ) 
	{
		fprintf(stderr, "Couldn't open audio: %s\n", SDL_GetError());
		return;
	}
	SDL_PauseAudio(0);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "freq:%d format:%d channels:%d samples:%d",sdl_resource->wanted.freq,sdl_resource->wanted.format,sdl_resource->wanted.channels,sdl_resource->wanted.samples);
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_destorySDL(JNIEnv *env, jclass cls)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI", "SDL start destory");
	has_sdl_resource = 0;
	//close(sdl_resource->fd);
	
	SDL_FreeYUVOverlay(sdl_resource->pOverlay);
	SDL_FreeSurface(sdl_resource->surface);
	SDL_Quit();	

	free(sdl_resource);
	
	__android_log_print(ANDROID_LOG_INFO, "JNI", "SDL end destory");
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_closeAudio(JNIEnv *env, jclass cls)
{
	SDL_CloseAudio();
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_setAudio(JNIEnv *env, jclass cls, jint audioFlag)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-------------SDL_PauseAudio :%d-------------|",audioFlag);
	sdl_resource->audioFlag = audioFlag;
	SDL_PauseAudio(audioFlag);
}

void initSDLOverlay() {
	
	/* Set default options and check command-line */
	//Uint32 overlay_format = SDL_YV12_OVERLAY; //yes
	//resource->overlay_format = SDL_IYUV_OVERLAY; //yes
	//resource->overlay_format = SDL_YUY2_OVERLAY; //no
	//resource->overlay_format = SDL_UYVY_OVERLAY; //no
	//resource->overlay_format = SDL_YVYU_OVERLAY; //no
	//int desired_bpp = 24;
	if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO) < 0) {
		//quit(1);
		__android_log_print(ANDROID_LOG_INFO, "JNI", "SDL_INIT fail");
	}
	sdl_resource->surface = SDL_SetVideoMode(sdl_resource->phoneWidth, sdl_resource->phoneWidth*9/16, 24, SDL_HWSURFACE | SDL_DOUBLEBUF);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "create surface success");
	if (sdl_resource->surface == NULL) {
	//	quit(1);
		__android_log_print(ANDROID_LOG_INFO, "jni", "SDL_CreateYUVOverlay fial!\n");
	}

	//----audio	
	//extern void fill_audio(void *udata, Uint8 *stream, int len);
	//memset(sdl_resource->audio_buffer,0,2048*42);
	sdl_resource->paudio_buf_read = sdl_resource->audio_buffer;
	sdl_resource->paudio_buf_write = sdl_resource->audio_buffer;
	//sdl_resource->paudio_temp_buffer = sdl_resource->audio_temp_buffer;
	sdl_resource->audio_buffer_len = AUDIO_BUFFER_SIZE;
	sdl_resource->audio_write_len = sdl_resource->audio_read_len = 0;
	sdl_resource->write_round = sdl_resource->read_round = 0;
	__android_log_print(ANDROID_LOG_INFO, "JNI", "|-----paudio_buf_read %p,paudio_buf_write %p,sdl_resource->audio_buffer %p-----|",sdl_resource->paudio_buf_read
								,sdl_resource->paudio_buf_write,&sdl_resource->audio_buffer);
	
	sdl_resource->wanted.freq = 8000;
	sdl_resource->wanted.format = AUDIO_S16;
	sdl_resource->wanted.channels = 1;//au_channel;
	sdl_resource->wanted.samples = 1024 ;   //Audio buffer size in samples
	sdl_resource->wanted.silence = 0;
	sdl_resource->wanted.callback = fill_audio;
	sdl_resource->wanted.userdata = NULL;
	
	pauseAudio();
	//sdl_resource->fd = open("/sdcard/test.pcm",O_RDWR|O_CREAT|O_APPEND,0640);
	//memset(&sdl_resource->audio_buf_arr,0,sizeof(sdl_resource->audio_buf_arr));
	
}

JNIEXPORT jstring JNICALL Java_org_libsdl_app_SDLActivity_sdlInit(JNIEnv* env, jclass cls)
{
	
	sdl_resource = (struct SDLResource *)malloc(sizeof(*sdl_resource));
	memset(sdl_resource,0,sizeof(struct SDLResource));
	sdl_resource->get_time_flag = 0;
	sdl_resource->flag = -1;
	//设置手机屏幕大小
	jmethodID getPhoneWidth = (*env)->GetStaticMethodID(env,cls,"getPhoneWidth","()I");
	jmethodID getPhoneHeight = (*env)->GetStaticMethodID(env,cls,"getPhoneHeight","()I");

	sdl_resource->phoneWidth = (*env)->CallStaticIntMethod(env, cls,getPhoneWidth);
	sdl_resource->phoneHeight = (*env)->CallStaticIntMethod(env, cls,getPhoneHeight);
	
	SDL_Android_Init(env, cls);
	initSDLOverlay();
	
	pthread_mutex_init(&sdl_resource->mutex,NULL);
	pthread_cond_init(&sdl_resource->cond,NULL);

	//pthread_mutex_init(&sdl_resource->audio_mutex,NULL);
	//pthread_cond_init(&sdl_resource->audio_cond,NULL);

	sdl_resource->has_data=0;
	sdl_resource->flag = 0;
	has_sdl_resource = 1;
	__android_log_print(ANDROID_LOG_INFO, "JNI", "init finish");

}


JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_setFlag(JNIEnv *env, jclass cls, jint jflag)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI", "setflag");
	sdl_resource->flag = jflag;
	pthread_cond_signal(&sdl_resource->cond);
	__android_log_print(ANDROID_LOG_INFO, "JNI", "setflag over");
	
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_display(JNIEnv *env, jclass cls)
{
	__android_log_print(ANDROID_LOG_INFO, "jni", "start call display");
	while(sdl_resource->flag == 0){
		//__android_log_print(ANDROID_LOG_INFO, "JNI", "------------start display");
		SDL_Rect rect;

		rect.w = sdl_resource->phoneWidth;
		rect.h = sdl_resource->phoneWidth*9/16;
		rect.x = 0;
		rect.y = 0;

		//__android_log_print(ANDROID_LOG_INFO, "JNI", "lock!");
		pthread_mutex_lock(&sdl_resource->mutex);
		while (sdl_resource->has_data==0 && sdl_resource->flag==0)
		{
			pthread_cond_wait(&sdl_resource->cond,&sdl_resource->mutex);
		}
		
		if (sdl_resource->flag == -1)
		{
			return;
		}
		sdl_resource->has_data--;
		__android_log_print(ANDROID_LOG_INFO, "JNI", "--start display--");
		SDL_DisplayYUVOverlay(sdl_resource->pOverlay, &rect);
		__android_log_print(ANDROID_LOG_INFO, "JNI", "--end display--");
		pthread_mutex_unlock(&sdl_resource->mutex);
	}
}

void native_catch_picture(PLAY_HANDLE handle){
	if(sdl_resource->picture_flag == 0){
		return;
	}
__android_log_print(ANDROID_LOG_INFO, ">>>", "sdl_resource->picture_flag %d",sdl_resource->picture_flag);

	//hwplay_save_to_bmp(handle,sdl_resource->path);
	hwplay_save_to_jpg(handle,sdl_resource->path,70);
	sdl_resource->picture_flag = 0;
	__android_log_print(ANDROID_LOG_INFO, ">>>", "finish fill buf");
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLActivity_setCatchPictureFlag(JNIEnv *env, jclass cls,jstring jpath,jint jlength)
{
	__android_log_print(ANDROID_LOG_INFO, "--->", "setflag");
	sdl_resource->picture_flag = 1;
	char* temp = (*env)-> GetStringUTFChars(env,jpath,NULL);
	//__android_log_print(ANDROID_LOG_INFO, "--->", "temp %s",temp);
	memcpy(sdl_resource->path, temp, jlength);
	__android_log_print(ANDROID_LOG_INFO, "--->", "sdl_resource->path %s",sdl_resource->path);
	(*env)->ReleaseStringUTFChars(env,jpath,temp);
	__android_log_print(ANDROID_LOG_INFO, "--->", "setflag over");
	
}
