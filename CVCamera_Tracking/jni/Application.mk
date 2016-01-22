# The ARMv7 is significanly faster due to the use of the hardware FPU
APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions
APP_ABI := $(ARM_TARGETS)
