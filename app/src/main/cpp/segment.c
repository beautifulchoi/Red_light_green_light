#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>

int fd =0;

JNIEXPORT jint JNICALL
Java_com_example_test_SEGevent_openDriver(JNIEnv *env, jclass clazz, jstring path) {
    // TODO: implement openDriver()
    jboolean iscopy;
    const char *path_utf= (*env)->GetStringUTFChars(env,path,&iscopy);
    fd =open(path_utf, O_WRONLY);
    (*env)->ReleaseStringUTFChars(env,path,path_utf);

    if(fd<0) return -1;
    else return 1;
}

JNIEXPORT void JNICALL
Java_com_example_test_SEGevent_closeDriver(JNIEnv *env, jclass clazz) {
// TODO: implement closeDriver()
if(fd>0) close(fd);
}

JNIEXPORT void JNICALL
Java_com_example_test_SEGevent_writeDriver(JNIEnv *env, jclass clazz, jbyteArray data,
jint length) {
// TODO: implement writeDriver()
jbyte* chars =(*env)->GetBooleanArrayElements(env, data, 0);
if(fd>0) write(fd, (unsigned char*)chars, length);
(*env)->ReleaseByteArrayElements(env, data, chars, 0);
}