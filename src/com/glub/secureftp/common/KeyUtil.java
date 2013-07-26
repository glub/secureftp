
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: KeyUtil.java 115 2009-11-26 07:42:13Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.common;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;

//import com.sun.net.ssl.*;
import javax.net.ssl.*;
//import sun.security.x509.*;

import com.glub.util.*;

public class KeyUtil {

  // Class constants
  private static final String CERTIFICATE_TYPE      = "X.509";
  //private static final String KMFACTORY_ALGORITHM   = "SunX509";
  private static final String KEY_ALGORITHM         = "RSA";
  private static final String KEY_STORE_TYPE        = "JKS";
  private static final String SIG_ALGORITHM         = "MD5WithRSA";
  private static final int    KEY_SIZE              = 1024;

/*
  static {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
  }
*/

  public static CertificateFactory getCertificateFactory() {

    CertificateFactory certFactory = null;

    try {
      certFactory = CertificateFactory.getInstance( CERTIFICATE_TYPE );
    }
    catch (CertificateException ce) {
      // Leave certFactory as null
    }

    return certFactory;

  }

  public static X509Certificate[] getCertificateList( CertificateFactory f,
                                                      String[]           path )
         throws FileNotFoundException, CertificateException {
    return getCertificateList( f, path, "" );
  }

  public static X509Certificate[] getCertificateList( CertificateFactory f,
                                                      String[] path,
                                                      String password )
         throws FileNotFoundException, CertificateException {

    X509Certificate[] cert     = null;
    ArrayList         certList = new ArrayList();
    FileInputStream   fis      = null;

    try {
      for ( int i = 0; i < path.length; i++ ) {
        // Read all certificates from each file path
        fis = new FileInputStream( path[ i ] );

        if ( path[i].toLowerCase().endsWith(".pfx") ||
             path[i].toLowerCase().endsWith(".p12") ) {
          KeyStore ks = KeyStore.getInstance("PKCS12");
          ks.load( fis, password.toCharArray() ); 
          Enumeration e = ks.aliases();
          while ( e.hasMoreElements() ) {
            String alias = (String)e.nextElement();
            certList.add((X509Certificate)ks.getCertificate(alias));
          }
        }
        else if ( path[i].toLowerCase().endsWith(".pem") ) {
          PEMInputStream pis = new PEMInputStream( fis );
          certList.addAll( f.generateCertificates( pis ) );
          pis.close();
        }
        else {
          certList.addAll( f.generateCertificates( fis ) );
        }
        Util.close( fis );
        fis = null;
      }

      // Convert certificate list to an array
      cert = (X509Certificate[])
               certList.toArray( new X509Certificate[certList.size()] );
    }
    catch ( Exception e ) {
      //System.out.println( e.getMessage() );
    }
    finally {
      Util.close( fis );
    }

    return cert;
  }

  public static KeyFactory getKeyFactory() {

    KeyFactory keyFactory = null;

    try {
      keyFactory = KeyFactory.getInstance( KEY_ALGORITHM );
    }
    catch (NoSuchAlgorithmException nsae) {
      // Leave keyFactory as null
    }

    return keyFactory;

  }


  public static KeyManagerFactory getKeyManagerFactory( KeyStore store,
                                                        char[]   password )
         throws UnrecoverableKeyException, KeyStoreException {

    KeyManagerFactory kmFactory = null;

    try {
      //kmFactory = KeyManagerFactory.getInstance( KMFACTORY_ALGORITHM );
      kmFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
      kmFactory.init( store, password );
    }
    catch (NoSuchAlgorithmException nsae) {
      // Leave kmFactory as null
    }

    return kmFactory;

  }

  public static KeyStore getKeyStore( char[] password )
         throws KeyStoreException, CertificateException {
    KeyStore ks = null;

    try {
      ks = getKeyStore( null, password, null, null, null );
    }
    catch (IOException ioe) {
      // Should not be thrown since no file is being loaded
    }

    return ks;
  }

  public static KeyStore getKeyStore( File keyFile, char[] password )
         throws KeyStoreException, CertificateException, IOException {
    return getKeyStore( keyFile, password, null, null, null );
  }

  public static KeyStore getKeyStore( char[]            password,
                                      String            keyAlias,
                                      PrivateKey        privateKey,
                                      X509Certificate[] certList )
         throws KeyStoreException, CertificateException {
    KeyStore ks = null;

    try {
      ks = getKeyStore( null, password, keyAlias, privateKey, certList );
    }
    catch (IOException ioe) {
      // Should not be thrown since no file is being loaded
    }

    return ks;
  }

  public static KeyStore getKeyStore( File              keyFile,
                                      char[]            password,
                                      String            keyAlias,
                                      PrivateKey        privateKey,
                                      X509Certificate[] certList )
         throws KeyStoreException, CertificateException, IOException {

    KeyStore store = null;

    InputStream is = null;
    try {
      if ( keyFile != null ) {
        try {
          is = new FileInputStream( keyFile );
        }
        catch ( FileNotFoundException fnfe ) {
          // it's okay, just use null
        }
      }

      store = KeyStore.getInstance( KEY_STORE_TYPE );
      store.load( is, password );

      if (keyAlias != null && privateKey != null) {
        store.setKeyEntry( keyAlias, privateKey, password, certList );
      }
    }
    catch (NoSuchAlgorithmException nsae) {
      // Leave store as null
    }
    finally {
      if ( is != null ) {
          is.close();
        }
    }

    return store;

  }

  public static void writeKeyStore( KeyStore keyStore, 
                                    File     keyFile, 
                                    char[]   password ) 
                                    throws IOException, KeyStoreException,
                                           NoSuchAlgorithmException,
                                           CertificateException {
    OutputStream os = null;

    try {
      os = new FileOutputStream( keyFile );
    }
    catch ( FileNotFoundException fnfe ) {
      // it's okay, just use null
    }

    keyStore.store( os, password );
    os.flush(); 
    os.close(); 
  }

  public static void addCertificate( KeyStore keyStore, X509Certificate cert ) 
                                   throws KeyStoreException {
    keyStore.setCertificateEntry( cert.getSubjectDN().toString(), cert );
  }

  public static boolean certificateExists( KeyStore keyStore, 
                                           X509Certificate cert ) {
    boolean exists = false;

    try {
      exists = keyStore != null && keyStore.getCertificateAlias(cert) != null;
    }
    catch ( Exception e ) { exists = false; }

    return exists;
  }

  public static boolean certificateAliasExists( KeyStore keyStore, 
                                                X509Certificate cert ) {
    return certificateAliasExists( keyStore, cert.getSubjectDN().toString() );
  }

  public static boolean certificateAliasExists( KeyStore keyStore, 
                                                String alias ) {
    boolean exists = false;

    try {
      exists = keyStore != null && keyStore.getCertificate(alias) != null;
    }
    catch ( Exception e ) { exists = false; }

    return exists;
  }

  public static void removeCertificate( KeyStore keyStore, 
                                        X509Certificate cert ) 
                                       throws KeyStoreException {
    removeCertificate( keyStore, cert.getSubjectDN().toString() );
  }

  public static void removeCertificate( KeyStore keyStore, String alias ) 
                                       throws KeyStoreException {
    keyStore.deleteEntry( alias );
  }

  public static PrivateKey getPrivateKey( KeyFactory keyFactory,
                                          String     keyPath )
         throws FileNotFoundException, IOException, InvalidKeySpecException {
    return getPrivateKey( keyFactory, keyPath, "" );
  }

  public static PrivateKey getPrivateKey( KeyFactory keyFactory,
                                          String     keyPath,
                                          String     password )
         throws FileNotFoundException, IOException, InvalidKeySpecException {

    PrivateKey      key = null;
    FileInputStream fis = null;

    if (keyPath != null) {
      fis = new FileInputStream( keyPath );

      try {
        if ( keyPath.toLowerCase().endsWith(".pfx") ||
             keyPath.toLowerCase().endsWith(".p12") ) {
          KeyStore ks = KeyStore.getInstance("PKCS12");
          ks.load( fis, password.toCharArray() ); 
          Enumeration e = ks.aliases();
          while ( e.hasMoreElements() ) {
            String alias = (String)e.nextElement();
            if ( ks.isKeyEntry(alias) ) {
              key = (PrivateKey)ks.getKey( alias, password.toCharArray() );
              break;
            }
          }
        }
        else if ( keyPath.toLowerCase().endsWith(".pem") ) {
          PEMInputStream pis = new PEMInputStream( fis );
          
          byte[] b  = new byte[ pis.available() ];
          pis.read( b );
          pis.close();
          
          key = keyFactory.generatePrivate( new X509EncodedKeySpec( b ) );
        }
        else if ( keyPath.toLowerCase().endsWith(".pk8") ) {
          byte[] b  = new byte[ fis.available() ];
          fis.read( b );

          key = keyFactory.generatePrivate( new PKCS8EncodedKeySpec( b ) );
        }
        else {
          byte[] b  = new byte[ fis.available() ];
          fis.read( b );

          key = keyFactory.generatePrivate( new X509EncodedKeySpec( b ) );
        }
      }
      catch ( Exception e ) {
        //System.out.println(e.getMessage());
      }
      finally {
        Util.close( fis );
      }
    }

    return key;

  }

  public static boolean writeCertAndKey( CertInfo info, int days, 
                                         File certFile, File keyFile ) {

    boolean bRet = false;
    FileOutputStream fileOut = null;

    try {
/*
      CertAndKeyGen keypair = new CertAndKeyGen( KEY_ALGORITHM, SIG_ALGORITHM );
      X500Name x500Name = new X500Name( info.getCommonName(), 
                                        info.getOrganizationUnit(), 
                                        info.getOrganization(),
                                        info.getCity(),
                                        info.getState(), 
                                        info.getCountry() );

      keypair.generate( KEY_SIZE );
      PrivateKey privKey = keypair.getPrivateKey();
      X509Certificate cert = keypair.getSelfCertificate( x500Name, 
                                                         days * 86400 );
*/
      String osName = System.getProperty("os.name");

      String certAndKeyGenClass = "sun.security.x509.CertAndKeyGen";
      if ( osName.equals("AIX") || GTOverride.getBoolean("security.ibm") ) {
        certAndKeyGenClass = "com.ibm.security.x509.CertAndKeyGen";
      }

      Class certAndKey = Class.forName( certAndKeyGenClass );
      Class[] certAndKeyArgs = { String.class, String.class };
      Constructor certAndKeyConstructor =
        certAndKey.getConstructor(certAndKeyArgs);
      Object[] certAndKeyConstArgs = { KEY_ALGORITHM, SIG_ALGORITHM };
      Object cakInstance =
        certAndKeyConstructor.newInstance( certAndKeyConstArgs );

      String x500NameClass = "sun.security.x509.X500Name";
      if ( osName.equals("AIX") || GTOverride.getBoolean("security.ibm") ) {
        x500NameClass = "com.ibm.security.x509.X500Name";
      }

      Class x500Name = Class.forName( x500NameClass );
      Class[] x500NameArgs = { String.class, String.class, String.class,
                               String.class, String.class, String.class };
      Constructor x500NameConstructor = x500Name.getConstructor(x500NameArgs);
      Object[] x500NameConstArgs = {
                                     info.getCommonName(),
                                     info.getOrganizationUnit(),
                                     info.getOrganization(),
                                     info.getCity(),
                                     info.getState(),
                                     info.getCountry()
      };
      Object x500Instance =
        x500NameConstructor.newInstance( x500NameConstArgs );

      Method generate = certAndKey.getMethod( "generate",
                                              new Class[] { int.class } );
      generate.invoke( cakInstance, new Integer[] { new Integer(KEY_SIZE) } );

      Method getPrivateKey = certAndKey.getMethod( "getPrivateKey", null );
      PrivateKey privKey = (PrivateKey)getPrivateKey.invoke(cakInstance, null);

      Method getSelfCert =
        certAndKey.getMethod( "getSelfCertificate",
                              new Class[] { x500Name, long.class} );
      X509Certificate cert = (X509Certificate)
        getSelfCert.invoke( cakInstance,
                            new Object[] {
                              x500Instance, new Long(days * 86400)
                            } );

      fileOut = new FileOutputStream( keyFile );
      fileOut.write( privKey.getEncoded() );
      fileOut.close();

      fileOut = new FileOutputStream( certFile );
      fileOut.write( cert.getEncoded() );
      fileOut.close();

      bRet = true;
    } 
    catch ( Exception e ) {
      if (GTOverride.getBoolean("glub.debug"))
    	  e.printStackTrace();
    }
    finally {
      if ( null != fileOut ) {
        try {
          fileOut.close();
        }
        catch ( Exception e ) {}
      }
    }

    return bRet;

  }

}

