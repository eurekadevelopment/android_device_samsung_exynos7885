# Exynos7885 Camera

## Patches needed

- sed -i 's/_ZN7android8hardware6camera6device4V3_214implementation12CameraDevice24getCameraCharacteristicsENSt3__18functionIFvNS1_6common4V1_06StatusERKNS0_8hidl_vecIhEEEEE/_ZN7android8hardware6camera6device4V3_214implementation12CameraDevice24getEurekaCharacteristicsENSt3__18functionIFvNS1_6common4V1_06StatusERKNS0_8hidl_vecIhEEEEE/g' camera.device@3.2-impl.so 
- sed -i 's/_ZN7android8hardware6camera6device4V3_214implementation12CameraDevice4openERKNS_2spINS3_21ICameraDeviceCallbackEEENSt3__18functionIFvNS1_6common4V1_06StatusERKNS6_INS3_20ICameraDeviceSessionEEEEEE/_ZN7android8hardware6camera6device4V3_214implementation12CameraDevice4nukeERKNS_2spINS3_21ICameraDeviceCallbackEEENSt3__18functionIFvNS1_6common4V1_06StatusERKNS6_INS3_20ICameraDeviceSessionEEEEEE/g' camera.device@3.2-impl.so 
- patchelf --add-needed libcorrectcamera.so camera.device@3.2-impl.so 

## Camera IDs reported by Exynos camera module

- Camera ID 0 = Back primary camera, this camera has correct resolution and direction, no need to touch.
- Camera ID 1 = Mixed cam which has back secondary cam's resolution and front cam's direction. ignored by us
- Camera ID 2 = Real front camera, which has correct values, we map this camera's info to cam id 1
- Canera ID 50 = Back secondary camera, which is not shown with AOSP original camera provider HAL. Need to manually query and add it with custom provider HAL.

## Hacks

- In [libcorrectcamera](universal7885-common/libshims/camera/CorrectCameraID.cpp) shim, if the Android OS request info for cam id 1, it will ask the Exynos camera module with cam id 2, and get the correct resolution for front cam.
- In [custom camera provider](universal7885-common/hidl/camera/provider/SamsungCameraProvider.cpp), We ask for additional camera info, rear back cam, id 50, and add it to camera device list. Also, gets rid of useless camera id 2, which info is mapped to cam id 1.
