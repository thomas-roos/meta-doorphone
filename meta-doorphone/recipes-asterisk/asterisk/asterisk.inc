DESCRIPTION = "Asterisk is an Open Source PBX and telephony toolkit."
LICENSE = "GPLv2"

DEPENDS += "util-linux libxml2-native ncurses-native"

inherit autotools-brokensep pkgconfig useradd systemd

# configure.ac has no m4_include and no AC_CONFIG_MACRO_DIR, so aclocal only
# finds asterisk's own AST_* macros (autoconf/ast_ext_lib.m4 and 18 others) if
# that directory is passed with -I - which is exactly what upstream's
# bootstrap.sh does ("aclocal -I autoconf"). Without it AST_EXT_LIB_CHECK is
# undefined, its invocation passes through unexpanded, and the standard macros
# after it end up verbatim in configure, where m4_pattern_forbid reports them as
# "undefined or overquoted macro: AC_MSG_WARN" - a confusing symptom of a
# missing include path rather than anything wrong with those macros.
#
# third-party/ needs its own -I as well as the three subdirectories: it holds
# configure.m4, which defines THIRD_PARTY_CONFIGURE. Left undefined, the literal
# text "THIRD_PARTY_CONFIGURE()" lands in configure, where the shell reads it as
# the start of a function definition and dies with "syntax error near unexpected
# token" on whatever follows.
EXTRA_AUTORECONF += "\
    -I ${S}/autoconf \
    -I ${S}/third-party \
    -I ${S}/third-party/libjwt \
    -I ${S}/third-party/jansson \
    -I ${S}/third-party/pjproject \
"

PACKAGECONFIG ?= "\
    ${@bb.utils.contains('DISTRO_FEATURES', 'largefile', 'largefile', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd',   'systemd', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'largefile', 'largefile', '', d)} \
    libxml2 \
    jansson \
    sqlite3 \
    libedit \
    libjwt \
    pjproject \
\
    timerfd \
    zlib \
    ncurses \
    asteriskssl openssl crypto \
    gsm \
"
# Optional features
PACKAGECONFIG[libjwt] = "--with-libjwt,--without-libjwt,libjwt"
PACKAGECONFIG[jansson] = "--with-jansson,--without-jansson,jansson"
PACKAGECONFIG[sqlite3] = "--with-sqlite3,--without-sqlite3,sqlite3"
PACKAGECONFIG[libedit] = "--with-libedit,--without-libedit,libedit"
PACKAGECONFIG[dev-mode] = "--enable-dev-mode,--disable-dev-mode"
PACKAGECONFIG[coverage] = "--enable-coverage,--disable-coverage"
PACKAGECONFIG[xmldoc] = "--enable-xmldoc,--disable-xmldoc"
PACKAGECONFIG[largefile] = "--enable-largefile,--disable-largefile"
PACKAGECONFIG[internal-poll] = "--enable-internal-poll,--disable-internal-poll"
PACKAGECONFIG[asteriskssl] = "--enable-asteriskssl,--disable-asteriskssl,openssl"
PACKAGECONFIG[rpath] = "--enable-rpath,--disable-rpath"
# Optional packages
PACKAGECONFIG[bfd] = "--with-bfd,--without-bfd"
PACKAGECONFIG[bluetooth] = "--with-bluetooth,--without-bluetooth,bluez5"
PACKAGECONFIG[backtrace] = "--with-execinfo,--without-execinfo"
PACKAGECONFIG[cap] = "--with-cap,--without-cap,libcap"
PACKAGECONFIG[corosync] = "--with-cpg,--without-cpg,corosync"
PACKAGECONFIG[curses] = "--with-curses,--without-curses,ncurses"
PACKAGECONFIG[crypt] = "--with-crypt,--without-crypt"
PACKAGECONFIG[crypto] = "--with-crypto,--without-crypto"
PACKAGECONFIG[dahdi] = "--with-dahdi,--without-dahdi,dahdi-tools"
# To enable FFMPEG add LICENSE_FLAGS_WHITELIST = "commercial" to your local.conf
PACKAGECONFIG[ffmpeg] = "--with-avcodec,--without-avcodec,libav"
PACKAGECONFIG[gsm] = "--with-gsm,--without-gsm,libgsm"
PACKAGECONFIG[ilbc] = "--with-ilbc,--without-ilbc"
PACKAGECONFIG[gtk2] = "--with-gtk2,--without-gtk2,gtk+"
PACKAGECONFIG[gmime] = "--with-gmime,--without-gmime"
PACKAGECONFIG[openh323] = "--with-h323,--without-h323"
PACKAGECONFIG[hoard] = "--with-hoard,--without-hoard"
PACKAGECONFIG[ical] = "--with-ical,--without-ical"
PACKAGECONFIG[iconv] = "--with-iconv,--without-iconv,libiconv"
PACKAGECONFIG[iksemel] = "--with-iksemel,--without-iksemel,iksemel"
PACKAGECONFIG[imap_tk] = "--with-imap,--without-imap,uw-imap"
PACKAGECONFIG[inotify] = "--with-inotify,--without-inotify"
PACKAGECONFIG[iodbc] = "--with-iodbc,--without-iodbc"
PACKAGECONFIG[isdnnet] = "--with-isdnnet,--without-isdnnet"
# Jack depends on kqeueu
PACKAGECONFIG[jack] = "--with-jack,--without-jack,jack"
PACKAGECONFIG[uriparser] = "--with-uriparser,--without-uriparser"
PACKAGECONFIG[kqueue] = "--with-kqueue,--without-kqueue"
PACKAGECONFIG[ldap] = "--with-ldap,--without-ldap,openldap"
PACKAGECONFIG[libcurl] = "--with-libcurl,--without-libcurl,curl"
PACKAGECONFIG[libxml2] = "--with-libxml2,--without-libxml2,libxml2"
PACKAGECONFIG[libxslt] = "--with-libxslt,--without-libxslt,libxslt"
PACKAGECONFIG[ltdl] = "--with-ltdl,--without-ltdl,libtool"
PACKAGECONFIG[lua] = "--with-lua,--without-lua,lua"
PACKAGECONFIG[misdn] = "--with-misdn,--without-misdn,misdn-utils"
PACKAGECONFIG[mysqlclient] = "--with-mysqlclient,--without-mysqlclient,mariadb"
PACKAGECONFIG[nbs] = "--with-nbs,--without-nbs"
PACKAGECONFIG[ncurses] = "--with-curses,--without-curses,ncurses"
PACKAGECONFIG[neon] = "--with-neon,--without-neon,neon"
PACKAGECONFIG[netsnmp] = "--with-netsnmp,--without-netsnmp,net-snmp"
PACKAGECONFIG[newt] = "--with-newt,--without-newt,libnewt"
PACKAGECONFIG[ogg] = "--with-ogg,--without-ogg,libogg"
PACKAGECONFIG[openr2] = "--with-openr2,--without-openr2"
PACKAGECONFIG[opus] = "--with-opus,--without-opus,libopus"
# Needed even though we never enable it. With opus off, OPUS_INCLUDE is empty and
# asterisk's configure.ac falls back to a hardcoded host path:
#
#   __opus_include=${OPUS_INCLUDE}
#   if test -z "$__opus_include" ... ; then __opus_include=-I/usr/include/opus; fi
#   AST_EXT_LIB_CHECK([OPUSFILE], ..., [$__opus_include])
#
# gcc then rejects it (-Werror=poison-system-directories, "include location
# /usr/include/opus is unsafe for cross-compilation") and oe-core's
# [configure-unsafe] QA check fails the task for touching host paths. Declaring
# the PACKAGECONFIG makes "--without-opusfile" get passed, so the probe - and the
# hardcoded path with it - never runs.
PACKAGECONFIG[opusfile] = "--with-opusfile,--without-opusfile,opusfile"
PACKAGECONFIG[osptk] = "--with-osptk,--without-osptk"
PACKAGECONFIG[oss] = "--with-oss,--without-oss,alsa-oss"
PACKAGECONFIG[pgsql] = "--with-postgres,--without-postgres,postgresql"
PACKAGECONFIG[pjproject] = "--with-pjproject --with-pjproject-bundled=no,--without-pjproject,pjproject"
PACKAGECONFIG[popt] = "--with-popt,--without-popt,popt"
PACKAGECONFIG[portaudio] = "--with-portaudio,--without-portaudio,portaudio-v19"
PACKAGECONFIG[pri] = "--with-pri,--without-pri,misdn-utils"
PACKAGECONFIG[pwlib] = "--with-pwlib,--without-pwlib,pwlib"
PACKAGECONFIG[radius] = "--with-radius,--without-radius,radiusclient-ng"
PACKAGECONFIG[resample] = "--with-resample,--without-resample,ncurses"
# Need to enable sdl_images to use sdl
PACKAGECONFIG[sdl] = "--with-sdl,--without-sdl,libsdl"
PACKAGECONFIG[sdl_image] = "--with-SDL_image,--without-SDL_image,libsdl-image"
PACKAGECONFIG[sounds-cache] = "--with-sounds-cache,--without-sounds-cache"
PACKAGECONFIG[spandsp] = "--with-spandsp,--without-spandsp"
PACKAGECONFIG[ss7] = "--with-ss7,--without-ss7"
PACKAGECONFIG[speex] = "--with-speex,--without-speex,speex"
PACKAGECONFIG[speexdsp] = "--with-speexdsp,--without-speexdsp,speexdsp"
PACKAGECONFIG[srtp] = "--with-srtp,--without-srtp"
PACKAGECONFIG[openssl] = "--with-ssl,--without-ssl,openssl"
PACKAGECONFIG[suppserv] = "--with-suppserv,--without-suppserv,misdn-utils"
PACKAGECONFIG[freetds] = "--with-tds,--without-tds"
PACKAGECONFIG[termcap] = "--with-termcap,--without-termcap,libcap"
PACKAGECONFIG[timerfd] = "--with-timerfd,--without-timerfd"
PACKAGECONFIG[tinfo] = "--with-tinfo,--without-tinfo,ncurses"
PACKAGECONFIG[tonezone] = "--with-tonezone,--without-tonezone"
PACKAGECONFIG[unixodbc] = "--with-unixodbc,--without-unixodbc,libodbc"
PACKAGECONFIG[vorbis] = "--with-vorbis,--without-vorbis,libvorbis"
PACKAGECONFIG[vpb] = "--with-vpb,--without-vpb"
PACKAGECONFIG[x11] = "--with-x11,--without-x11,libx11"
PACKAGECONFIG[zlib] = "--with-z,--without-z,zlib"
PACKAGECONFIG[systemd] = "--with-systemd,--without-systemd,systemd"

EXTRA_OECONF += "\
    ac_cv_file_bridges_bridge_softmix_include_hrirs_h=no \
    "

MENUSELECT_OECONF = "\
    CC='${BUILD_CC}' \
    CXX='${BUILD_CXX}' \
    CFLAGS='${BUILD_CFLAGS}' \
    CXXFLAGS='${BUILD_CXXFLAGS}' \
    LDFLAGS='${BUILD_LDFLAGS}' \
"

do_configure_menuselect[dirs] = "${B}/menuselect"
do_configure_menuselect() {
	./configure ${MENUSELECT_OECONF}
}
addtask do_configure_menuselect after do_configure before do_compile

do_install:append() {
	install -D -p -m 0644 ${UNPACKDIR}/asterisk.tmpfiles ${D}${libdir}/tmpfiles.d/asterisk.conf
	install -D -p -m 0644 ${UNPACKDIR}/asterisk.socket   ${D}${systemd_system_unitdir}/asterisk.socket
	install -D -p -m 0644 ${UNPACKDIR}/asterisk.service  ${D}${systemd_system_unitdir}/asterisk.service

	rm -rf ${D}/var/run
	rm -rf ${D}/var/log
	rm -rf ${D}/tmp
}

FILES:${PN} += "\
    ${systemd_system_unitdir}/asterisk*.* \
    ${libdir}/tmpfiles.d/asterisk.conf \
"
RDEPENDS:${PN} += "bash"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN}  = "--system --home-dir /var/lib/asterisk -g asterisk --shell /bin/false asterisk"
GROUPADD_PARAM:${PN} = "--system asterisk"

SYSTEMD_SERVICE:${PN} = "${PN}.service"

PACKAGES =+ "${PN}-sounds-en"
FILES:${PN}-sounds-en = "${datadir}/asterisk/sounds/en"
