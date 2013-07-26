#!/bin/bash

lang=$1

if [ x${lang} = x ]; then
	lang="en_US"
fi

java -Dfile.encoding=UTF8 -Dglub.debug=true -Dglub.language=$lang -Dglub.localdir="$HOME/Desktop" -classpath cls:lib/jakarta-regexp-1.5.jar:lib/jaxen-full.jar:lib/jdom.jar:lib/saxpath.jar:lib/jh.jar com.glub.secureftp.client.gui.SecureFTP
