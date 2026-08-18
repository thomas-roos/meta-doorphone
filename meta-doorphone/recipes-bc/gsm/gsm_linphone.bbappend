# meta-bc sets SRCREV unconditionally to AUTOREV in this recipe:
#
#     python () { d.setVar('SRCREV', "${AUTOREV}") }
#
# so every parse runs a live "git ls-remote" against gitlab.linphone.org. That
# host is only intermittently reachable, and one failed lookup aborts the entire
# parse - it also means the built revision is whatever happened to be HEAD that
# day. Every sibling recipe in meta-bc guards AUTOREV behind LATEST_REVISIONS
# and ships a fixed SRCREV; gsm and libyuv are the only two that were missed.
#
# Pinned below to the branch head resolved from upstream on 2026-08-18.
#
# This has to be an anonymous python block, not a plain SRCREV assignment: the
# recipe's own anonymous function runs at finalise and would overwrite a normal
# assignment. Appends are parsed after the recipe they extend, so this block
# runs last and wins.
# Branch: linphone
python () {
    d.setVar('SRCREV', 'ff67768db9e49fc749920422ea4061b7f8f01b3f')
}
