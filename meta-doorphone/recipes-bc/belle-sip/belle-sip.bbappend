# belle-sip files ${datadir}/belr into its -dev package, but the belr grammar
# files there are runtime data, not development files: belle-sip loads
# sdp_grammar to parse SDP bodies. Without it linphonec dies with
#
#   belr-error- Could not load grammar sdp_grammar ...
#   belle-sip-fatal- Unable to load SDP grammar
#
# the instant a call is ANSWERED - SDP only appears in the 200 OK, so ringing
# looks perfectly fine and the process aborts on pickup.
#
# meta-bc builds this stack statically, so belle-sip installs no .so.* files and
# its main package comes out empty and is never written. Moving the grammars into
# it gives it content, so belle-sip_*.ipk exists and doorphone can RDEPEND on it.
FILES:${PN}-dev:remove = "${datadir}/belr"
FILES:${PN} += "${datadir}/belr"
