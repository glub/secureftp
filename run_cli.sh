#/bin/sh

java -Dglub.debug=false -Dfile.encoding=UTF8 -Dglub.language=en_US -Dglub.default.prompt.no=false -Dglub.dataencrypt.override=false -Dglub.security.override=false -Dglub.ssl.explicit.only="false" -classpath cls:lib/jakarta-regexp-1.5.jar:lib/jsse.jar:lib/jcert.jar:lib/jnet.jar:lib/sunrsasign.jar com.glub.secureftp.client.cli.SecureFTP $*
