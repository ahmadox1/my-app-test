#include <jni.h>
#include <string>

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
