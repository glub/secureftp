
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: JarUtil.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.*;
import java.util.*;
import java.util.jar.*;

public class JarUtil {
  private static boolean verifiedJar = false;
  private static String framework;

/*
  private static final String glubCertName = 
    "CN=\"Glub Tech, Inc.\", OU=Secure Application Development, O=\"Glub Tech, Inc.\", L=La Jolla, ST=California, C=US";

  private static final String issuingCertName = 
    "EmailAddress=server-certs@thawte.com, CN=Thawte Server CA, OU=Certification Services Division, O=Thawte Consulting cc, L=Cape Town, ST=Western Cape, C=ZA";
*/
  private static final int glubHash = 7307316;
  private static final int issuingHash = 6286758;

  public static final boolean verifyJar( Class c, String fw ) {
    return _verifyJar(c, fw);
  }

  private static final boolean _verifyJar( Class c, String fw ) {

    if ( verifiedJar ) {
      return verifiedJar;
    }

    framework = fw;

    URL jarURL = null;

    try {
      final ClassLoader cl = c.getClassLoader();

      String classPath = c.toString();
      classPath = Util.searchAndReplace( classPath, "class ", "", false );
      classPath = Util.searchAndReplace( classPath, ".", "/", true );
      classPath += ".class";
    
      final String className = classPath;

      URL url = (URL)AccessController.doPrivileged(
        new PrivilegedAction() {
          public Object run() {
            return cl.getResource( className );
          }
        });

      if ( url == null ) {
        throw new SecurityException( "Cannot get the " + framework + 
                                     " framework URL." );
      }

      String urlStr = url.toString();

      int per = urlStr.lastIndexOf( ".jar!/" );
      if ( per < 0 ) {
        throw new SecurityException( "The " + framework + 
                                     " framework is invalid." );
      }

      int slash = per + 5;
   
      jarURL = new URL( urlStr.substring(0, slash + 1) );

      int beginIndex = 4;
      URL ju = new URL( urlStr.substring(beginIndex, slash - 1));
      if ( !ju.getProtocol().equalsIgnoreCase("file") ) {
        throw new SecurityException( framework + " should be deployed " + 
                                     " as an installed extension or on the " +
                                     " class path." );
      }
    }
    catch ( IOException ioe ) {
      throw new SecurityException( "The " + framework + " framework could " +
                                   "not be authenticated." );
    }

    JarFile jf;

    try {
      final URL url = jarURL;
      jf = (JarFile)AccessController.doPrivileged(
        new PrivilegedExceptionAction() {
          public Object run() throws Exception {
            return ((JarURLConnection)url.openConnection()).getJarFile();
          }
        } );
    }
    catch ( PrivilegedActionException pae ) {
      throw new SecurityException( "Cannot authenticate the " + framework + 
                                   " framework: " + pae.getMessage() );
    }

    try {
      verifySingleJarFile( jf );
    }
    catch ( Exception e ) {
      throw new SecurityException( "Cannot authenticate the " + framework + 
                                   " framework: " + e.getMessage() );
    }

    verifiedJar = true;

    return verifiedJar;
  }

  private static void verifySingleJarFile( JarFile jf ) throws IOException, 
                                                        CertificateException {
    Vector entriesVec = new Vector();

    Manifest man = jf.getManifest();
    if ( man == null ) {
      throw new SecurityException( "The jar file is not signed!" );
    }

    byte[] buffer = new byte[8192];
    Enumeration entries = jf.entries();

    while ( entries.hasMoreElements() ) {
      JarEntry je = (JarEntry)entries.nextElement();
      entriesVec.addElement(je);
      InputStream is = jf.getInputStream( je );
      while ( (is.read(buffer, 0, buffer.length)) != -1 ) {
      }
      is.close();
    }
    jf.close();

    Enumeration e = entriesVec.elements();
    while ( e.hasMoreElements() ) {
      JarEntry je = (JarEntry)e.nextElement();

      if ( je.isDirectory() ) {
        continue;
      }

      Certificate[] certs = je.getCertificates();
      if ((certs == null) || (certs.length == 0)) {
        if ( !je.getName().startsWith("META-INF")) {
          throw new SecurityException( "The " + framework + " framework has " +
                                       "unsigned class files. ");
        }
      }
      else {
        Certificate[] chainRoots = getChainRoots( certs );

        X509Certificate issuingCert = (X509Certificate)chainRoots[0];
        //Principal issuingCertPrincipal = issuingCert.getSubjectDN();

        X509Certificate signingCert = (X509Certificate)certs[0];
        //Principal signingCertPrincipal = signingCert.getSubjectDN();

        boolean signedAsExpected = false;

        if ( issuingCert.hashCode() == issuingHash &&
             signingCert.hashCode() == glubHash ) {
          signedAsExpected = true;
        }

        if ( !signedAsExpected ) {
          throw new SecurityException( "The " + framework + " framework is " +
                                       "not signed by Glub Tech, Inc." );
        }
      }
    }
  }

  private static Certificate[] getChainRoots( Certificate[] certs ) {
    Vector result = new Vector( 3 );
    for ( int i = 0; i < certs.length - 1; i++ ) {
      if ( !((X509Certificate)certs[i+1]).getSubjectDN().equals(
        ((X509Certificate)certs[i]).getIssuerDN())) {
          result.addElement(certs[i]);
      }
    }

    result.addElement( certs[certs.length - 1] );
    Certificate[] ret = new Certificate[ result.size() ];
    result.copyInto( ret );

    return ret;
  }
}
