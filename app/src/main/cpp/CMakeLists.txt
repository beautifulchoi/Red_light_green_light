# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

set(pathPROJECT C:/Users/yjchoi/AndroidStudioProjects/test) # 수정필요
set(pathOPENCV ${pathPROJECT}/sdk) # 수정 필요
set(pathLIBOPENCV_JAVA ${pathOPENCV}/native/libs/${ANDROID_ABI}/libopencv_java3.so)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathOPENCV}/native/jni/include)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
add_library(GLES_mali STATIC IMPORTED)
set_target_properties( # Specifies the target library.
        GLES_mali

        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION

        # Provides the path to the library you want to import.
        C:/Users/yjchoi/Desktop/OpenCL_lib_and_include/lib/libGLES_mali.so )

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp )

add_library( # Sets the name of the library.
        led

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        led.c)

add_library( # Sets the name of the library.
        segment

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        segment.c)

add_library( # Sets the name of the library.
        button

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        button.c)
add_library( # Sets the name of the library.
        picture_change

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        picture_change.c)

add_library( lib_opencv SHARED IMPORTED )

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        jnigraphics-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        led

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_link_libraries( # Specifies the target library.
        segment

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_link_libraries( # Specifies the target library.
        button

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_link_libraries( # Specifies the target library.
        picture_change

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_link_libraries( # Specifies the target library.
        native-lib

        #lib_opencv

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

target_link_libraries( # Specifies the target library.
        picture_change
        # Links the target library to the log library
        # included in the NDK.
        GLES_mali ${log-lib} ${jnigraphics-lib})

target_include_directories(picture_change PRIVATE src/main/cpp)