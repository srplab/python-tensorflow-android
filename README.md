# python-tensorflow-android
tensorflow 1.0 python libs for android with a python console

Tensorflow uses python as the main language, although tensorflow provides an example of running on android, but using java. Based on the source code, using NDK, you can compile android version of tensorflow.
Compiler environment using ubuntu 16.04, NDK uses r13b version. Compiling with the latest r14 version of NDK has some problems.

#### 1. Clone tensorflow source code V1.0
#### 2. Compile and install tensorflow. If the network environment is not good, this sep may need to try many times.
#### 3. Modify WORDSPACE, uncomment and set the android sdk,ndk path, and version.
```Bash
android_sdk_repository(
    name = "androidsdk",
    api_level = 23,
    build_tools_version = "25.0.2",
    # Replace with path to Android SDK on your system
    path = "/home/xxx/Android/Sdk",
)
#
android_ndk_repository(
    name="androidndk",
    #path="/home/xxx/Android/Sdk/ndk-bundle",
    path="/home/xxx/Android/android-ndk-r13b",
    api_level=21)
```
#### 4. compile
```Bash
bazel build -c opt //tensorflow/tools/pip_package:build_pip_package \--crosstool_top=//external:android/crosstool \--host_crosstool_top=@bazel_tools//tools/cpp:toolchain \--cpu=armeabi-v7a --verbose_failures
```
The most error is related to STL. The general solution is to open the corresponding package BUILD file, find the wrong package name, add:
```Bash
copts = [“-std=c++11”]
```
#### 5. Screenshots

![](https://github.com/srplab/python-tensorflow-android/blob/master/screenshot/screenshot_320.png)

In the WIFI environment, you can telnet link to your phone from your computer and run the tensorflow code as follows:
![](https://github.com/srplab/python-tensorflow-android/blob/master/screenshot/screensho2_640.png)

Python console source code is developed with android studio

Unzip tensorflow_python_android.zip package to built-in sd card directory "/sdcard/tensorflow/python2.7/dist-packages"
