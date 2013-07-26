
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLUtil.java 109 2009-11-25 06:27:28Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.common;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;

import javax.net.ssl.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
//import com.sun.net.ssl.*;

public class SSLUtil
{
  // Class constants
  private static final String SSL_PROTOCOL          = "TLS";
  //private static final String SSL_PROVIDER          = "SunJSSE";

  private static String[] enabledCipherSuites = null;

/*
  static {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
  }
*/

  private static String[] getEnabledCipherSuites( String[] oldCipherOrder ) {
    if ( enabledCipherSuites == null ) {
      enabledCipherSuites = getSupportedCiphers( oldCipherOrder );
    }

    return enabledCipherSuites;
  }

  public static SSLServerSocket createServerSocket( int    port,
                                                    int    backlog,
                                                    String host,
                                                    SSLServerSocketFactory f )
         throws IOException, UnknownHostException
  {
    ServerSocket ss = null;

    if (f != null) {
      ss = f.createServerSocket( port,
                                 backlog,
                                 InetAddress.getByName( host ) );
    }
    else {
      return null;
    }

    String oldCipherOrder[] = ((SSLServerSocket)ss).getSupportedCipherSuites();
    //String oldCipherOrder[] = ((SSLServerSocket)ss).getEnabledCipherSuites();
    ((SSLServerSocket)ss).setEnabledCipherSuites( getEnabledCipherSuites(oldCipherOrder) );

    return (SSLServerSocket)ss;
  }

  // autoClose set to false will leave the oldSock open even when the new
  // one is closed (useful for CCC)
  public static SSLSocket createSocket( Socket oldSock, String addr, int port,
                                        SSLSocketFactory f, boolean autoClose )
         throws IOException
  {
    Socket s = null;

    if (f != null) {
      s = f.createSocket( oldSock, addr, port, autoClose );
    }
 
    String oldCipherOrder[] = ((SSLSocket)s).getSupportedCipherSuites();
    //String oldCipherOrder[] = ((SSLSocket)s).getEnabledCipherSuites();
    ((SSLSocket)s).setEnabledCipherSuites( getEnabledCipherSuites(oldCipherOrder) );

    return (SSLSocket)s;
  }

  public static SSLSocket createSocket( Socket oldSock, String addr, int port,
                                        SSLSocketFactory f )
         throws IOException
  {
    return createSocket( oldSock, addr, port, f, true );
  }
                                        
  public static SSLSocket createSocket( String addr, int port, 
                                        SSLSocketFactory f )
         throws IOException
  {
    return createSocket( new Socket(addr, port), addr, port, f );
  }

  public static SSLContext getContext( KeyManager[]   km,
                                       TrustManager[] tm,
                                       SecureRandom   random )
         throws KeyManagementException
  {
    SSLContext context = null;

    try {
      //context = SSLContext.getInstance( SSL_PROTOCOL, SSL_PROVIDER );
      context = SSLContext.getInstance( SSL_PROTOCOL );
      context.init( km, tm, random );
    }
    catch (NoSuchAlgorithmException nsae) {
      // Leave context as null
    }
/*
    catch (NoSuchProviderException nspe) {
      // Leave context as null
    }
*/

    return context;
  }

  public static SSLSocketFactory getSocketFactory( SSLContext c )
  {
    SSLSocketFactory factory = null;

    if (c != null) {
      factory = (SSLSocketFactory)c.getSocketFactory();
    }

    return factory;
  }

  public static SSLServerSocketFactory getServerSocketFactory( SSLContext c )
  {
    SSLServerSocketFactory factory = null;

    if (c != null) {
      factory = (SSLServerSocketFactory)c.getServerSocketFactory();
    }

    return factory;
  }

  private static String[] getSupportedCiphers( String ciphers[] ) {
    Pattern pattern256AES = Pattern.compile( "AES.256" );
    Pattern pattern128AES = Pattern.compile( "AES.128" );
    Pattern pattern3DES = Pattern.compile( "3DES" );

    Matcher matcher;
    boolean matchFound;

    int len = ciphers.length;

    ArrayList list256AES = new ArrayList();
    ArrayList list128AES = new ArrayList();
    ArrayList list3DES = new ArrayList();
    ArrayList listOthers = new ArrayList();

    for (int i = 0; i < len; i++) {
      // we won't accept anything using md5 (FIPS compliancy)
      if ( ciphers[i].toLowerCase().endsWith("md5") )
        continue;

      // determine if pattern exists in input
      matcher = pattern256AES.matcher( ciphers[i] );
      matchFound = matcher.find();

      if ( matchFound ) {
        list256AES.add( ciphers[i] );
      }
      else {
        matcher = pattern128AES.matcher( ciphers[i] );
        matchFound = matcher.find();

        if ( matchFound ) {
          list128AES.add( ciphers[i] );
        }
        else {
          matcher = pattern3DES.matcher( ciphers[i] );
          matchFound = matcher.find();

          if ( matchFound ) {
            list3DES.add( ciphers[i] );
          }
          else {
            //listOthers.add( ciphers[i] );
          }
        }
      }
    }

    String set[] = new String[list256AES.size() + list128AES.size() + list3DES.size() + listOthers.size()];

    int j = 0;

    for ( int k = 0; k < list256AES.size(); k++ ) {
      set[j++] = (String)list256AES.get( k );
    }

    for ( int l = 0; l < list128AES.size(); l++ ) {
      set[j++] = (String)list128AES.get( l );
    }

    for ( int m = 0; m < list3DES.size(); m++ ) {
      set[j++] = (String)list3DES.get( m );
    }

    for ( int n = 0; n < listOthers.size(); n++ ) {
      set[j++] = (String)listOthers.get( n );
    }

    return set;
  }
}

