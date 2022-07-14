/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include "./include/user_processing_jni.h"

class UserInfoHandler
{
private:
  std::string _usernameString;
  char *_passwordChar; //Use char * so we can secure wipe memory

public:
  UserInfoHandler();
  ~UserInfoHandler();
}

void UserInfoHandler::UserInfoHandler()
{
  memset(&_passwordChar, 0, sizeof(char));
  _usernameString = "Anonymous";
}

void UserInfoHandler::~UserInfoHandler()
{
  const int c = 0;
  size_t n = strlen(_passwordChar)
  volatile char *p = (volatile char *)_passwordChar; //tell compiler the value can change
  while (n--)
  {
    *p++ = (char)c;
  }
}

static UserInfoHandler s_userInfoHandler;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = nullptr;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        return result;
    }

    assert(env != nullptr);
    result = JNI_VERSION_1_6;
    return result;
}

JNIEXPORT void JNICALL Java_com_raywenderlich_android_petsave_PetSaveApplication_doRegisterProcessing
(JNIEnv *env, jobject obj)
{
	if (s_userInfoHandler)
  {
    s_userInfoHandler->setupUser();
  }
}

#ifdef __cplusplus
}
#endif
