#include <jni.h>
#include <string>

extern "C" JNIEXPORT jboolean JNICALL
Java_ai_screentalk_ml_llm_LlamaCppEngine_nativeInit(
        JNIEnv *env,
        jobject /* this */,
        jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    // Placeholder for loading llama.cpp model. Always succeeds for stub implementation.
    (void) path;
    env->ReleaseStringUTFChars(modelPath, path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_ai_screentalk_ml_llm_LlamaCppEngine_nativeRelease(
        JNIEnv * /* env */, jobject /* this */) {
    // Placeholder: nothing to release in stub implementation.
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_ai_screentalk_ml_llm_LlamaCppEngine_nativeGenerate(
        JNIEnv *env,
        jobject /* this */,
        jstring prompt,
        jfloat /* temperature */,
        jfloat /* topP */,
        jint /* maxTokens */) {
    const char *rawPrompt = env->GetStringUTFChars(prompt, nullptr);
    std::string promptStr = rawPrompt != nullptr ? rawPrompt : "";
    env->ReleaseStringUTFChars(prompt, rawPrompt);

    std::string response = std::string("[LLAMA STUB] ") + promptStr;

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray tokens = env->NewObjectArray(1, stringClass, nullptr);
    env->SetObjectArrayElement(tokens, 0, env->NewStringUTF(response.c_str()));
    return tokens;
}
