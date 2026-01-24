#include <jni.h>
#include "NitroStrokeTextOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::stroketext::initialize(vm);
}
