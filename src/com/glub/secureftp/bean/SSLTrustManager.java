
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLTrustManager.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import com.glub.secureftp.common.*;
import com.glub.util.*;

import java.io.*;
import java.net.*;

import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

/**
 * This handles the trust issues for the SSL certs.
 */
public class SSLTrustManager implements X509TrustManager {
  private TrustManager defaultTM = null;
  private SSLKeyStore sslKeyStore = null;
  private SSLSessionManager sslSessionManager = null;
  private InetAddress serverAddress = null;
  private OutputStreamWriter replyStreamWriter = null;
  private KeyStore tempKeyStore = null;
  private boolean certTrusted = false;

  /** Debug output */
  private boolean debug = GTOverride.getBoolean("glub.debug");

  public SSLTrustManager( SSLKeyStore sslKeyStore, 
                          SSLSessionManager sslSessionManager,
                          InetAddress serverAddress, 
                          OutputStream replyStream ) 
                          throws IllegalArgumentException {
    if ( sslKeyStore == null ) {
      throw new IllegalArgumentException("ssl key store cannot be null");
    }
    else if ( sslSessionManager == null ) {
      throw new IllegalArgumentException("ssl session mgr cannot be null");
    }

    this.sslKeyStore = sslKeyStore;
    this.sslSessionManager = sslSessionManager;
    this.serverAddress = serverAddress;

    try {
      TrustManagerFactory defaultTMF = 
      //  TrustManagerFactory.getInstance("SunX509", "SunJSSE");
        TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
      defaultTMF.init((java.security.KeyStore)null);
      defaultTM = defaultTMF.getTrustManagers()[0];
    }
    catch ( Exception e ) {}

    if ( replyStream != null ) {
      replyStreamWriter = new OutputStreamWriter(replyStream);
    }

    try {
      char[] tempPass = (new String("temP")).toCharArray();
      tempKeyStore = KeyUtil.getKeyStore(tempPass);
    }
    catch ( Exception e ) {}
  }

  public X509Certificate[] getAcceptedIssuers() {
    // not yet implemented
    return null;
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType) 
                                 throws CertificateException {
    boolean checkIt = true;

    try {
      if ( null != defaultTM ) {
        ((X509TrustManager)defaultTM).checkServerTrusted(chain, authType);
        X509Certificate cert = chain[0];
        SSLCertificate  sslCert = new SSLCertificate( cert );
        sslSessionManager.setCurrentCertificate( sslCert );
        checkIt = false;
      }
    }
    catch ( Exception e ) {}

    boolean throwIt = checkIt;

    if ( checkIt ) {
      throwIt = !isServerTrusted(chain);
    }

    if ( throwIt ) {
      throw new CertificateException("Untrusted Server Certificate Chain");
    }
  }

  public boolean isServerTrusted(X509Certificate[] chain) {
    boolean trustIt = false;

    if ( debug )
      System.out.println( "Checking if server is trusted" );

    if ( certTrusted ) {
      return true;
    }

    try {
      if ( chain == null || chain.length < 1 || chain[0] == null ) {
        certTrusted = sslSessionManager.continueWithoutServerCertificate();
        return certTrusted;
      }

      X509Certificate cert = chain[0];
      SSLCertificate  sslCert = new SSLCertificate( cert );
      sslSessionManager.setCurrentCertificate( sslCert );
      
      try {
        cert.checkValidity();
      }
      catch ( CertificateExpiredException cee ) {
        certTrusted = sslSessionManager.continueWithExpiredCertificate( sslCert );
        return certTrusted;
      }

      // if we have a server's address make sure it matches the certificates
      if ( serverAddress != null ) {
        String hostName = serverAddress.getHostName();
        String hostAddr = serverAddress.getHostAddress();
        if ( hostName == null ) {
          hostName = "";
        }

        if ( !sslCert.getCN().equalsIgnoreCase(hostName) &&
             !sslCert.getCN().equals(hostAddr) ) {
          certTrusted = 
            sslSessionManager.continueWithCertificateHostMismatch(sslCert,
                                                               hostName,
                                                               sslCert.getCN());
          return certTrusted;
        } 
      }

      // check the keystore for the certificate
      if ( KeyUtil.certificateExists(sslKeyStore.getKeyStore(), cert) ) {
        trustIt = true;
      }
      else if ( KeyUtil.certificateExists(tempKeyStore, cert) ) {
        trustIt = true;
      }
      else {
        short whatToDo = SSLSessionManager.DENY_CERTIFICATE;
        X509Certificate oldCert = null;

        if ( KeyUtil.certificateAliasExists(sslKeyStore.getKeyStore(), cert) ) {
          String alias = cert.getSubjectDN().toString();
          oldCert = 
            (X509Certificate)sslKeyStore.getKeyStore().getCertificate(alias);
          SSLCertificate oldSslCert = new SSLCertificate(oldCert);
          whatToDo = sslSessionManager.replaceCertificate(oldSslCert, sslCert);
        }
        else {
          whatToDo = sslSessionManager.newCertificateEncountered(sslCert);
        }

        switch ( whatToDo ) {
          case SSLSessionManager.ALLOW_CERTIFICATE:
            trustIt = true;
            KeyUtil.addCertificate( tempKeyStore, cert );
            break;

          case SSLSessionManager.SAVE_CERTIFICATE:
            trustIt = true;
            try {
              if ( oldCert != null ) {
                KeyUtil.removeCertificate(sslKeyStore.getKeyStore(), oldCert);
              }

              KeyUtil.addCertificate( sslKeyStore.getKeyStore(), cert );
              if ( null != sslKeyStore.getKeyStoreFile() ) {
                KeyUtil.writeKeyStore( sslKeyStore.getKeyStore(),
                                       sslKeyStore.getKeyStoreFile(),
                                       sslKeyStore.getKeyStorePass() );
              }
              else if ( null != replyStreamWriter ) {
                replyStreamWriter.write("The key store file is not valid.");
                replyStreamWriter.write(System.getProperty("line.separator"));
                replyStreamWriter.flush();
              }
            }
            catch ( Exception e ) {
              if ( null != replyStreamWriter ) {
                replyStreamWriter.write(e.getMessage());
                replyStreamWriter.write(System.getProperty("line.separator"));
                replyStreamWriter.flush();
              }
            }
            break;

          case SSLSessionManager.DENY_CERTIFICATE:
          default:
            trustIt = false;
            break;
        }
      }
    }
    catch ( Exception e ) {
      trustIt = false;
    }

    if ( debug )
      System.out.println( "Trust server: " + trustIt );

    certTrusted = trustIt;
    return certTrusted;
  }

  public void checkClientTrusted(X509Certificate[] chain, String authType) 
                                 throws CertificateException {
    if ( !isClientTrusted(chain) ) {
      throw new CertificateException("Untrusted Client Certificate Chain");
    }
  }

  public boolean isClientTrusted(X509Certificate[] chain) {
    // not used in client
    return true;
  }
}

