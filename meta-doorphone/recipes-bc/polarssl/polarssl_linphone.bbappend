# polarssl's CMake builds its demo/test programs by default (ENABLE_PROGRAMS and
# ENABLE_TESTING are ON upstream). One of them, programs/test/o_p_test, does a
# find_package for OpenSSL and resolves it to recipe-sysroot-NATIVE - so a
# cross build for aarch64 tries to link the x86_64 host libssl.so/libcrypto.so
# and dies with "file in wrong format" (the mangled "-Wl,-rpath,-native/usr/lib:"
# in the link line is the same leak showing through).
#
# Nothing here needs those binaries: bctoolbox and the rest of the linphone
# stack link libpolarssl only. Skip them instead of trying to teach a 2014-era
# CMakeLists about cross sysroots.
EXTRA_OECMAKE += "-DENABLE_PROGRAMS=OFF -DENABLE_TESTING=OFF"
