
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLSessionManager.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * The <code>SSLSessionManager</code> interface is responsible for dealing
 * with SSL related events such as certificates being sent from the FTP
 * server and randomization information.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public interface SSLSessionManager {
  /** Used to signal certificate acceptance. */ 
  public static final short ALLOW_CERTIFICATE = 0;

  /** 
   * Used to signal certificate acceptance and that it should be saved in 
   * the certificate key store. 
   */ 
  public static final short SAVE_CERTIFICATE  = 1;

  /** Used to signal certificate rejection. */
  public static final short DENY_CERTIFICATE  = 2;

  /** 
   * When a certificate is encountered from the FTP server, this method
   * will be called.
   *
   * @param cert   the SSL (X509) certificate. 
   */
  public void    setCurrentCertificate( SSLCertificate cert );

  /**
   * If the certificate sent by the FTP server is not found in the key store, 
   * this method will be called.
   *
   * @param cert   the SSL (X509) certificate. 
   *
   * @return       a certificate acception/rejection code.
   *
   * @see #ALLOW_CERTIFICATE
   * @see #SAVE_CERTIFICATE
   * @see #DENY_CERTIFICATE
   */
  public short   newCertificateEncountered( SSLCertificate cert );

  /**
   * If a certificate is found in the key store and a new one that matches the
   * same name is sent from the FTP server, this method will be called.
   *
   * @param oldCert   the known SSL (X509) certificate. 
   * @param newCert   the new SSL (X509) certificate. 
   *
   * @return       a certificate acception/rejection code.
   *
   * @see #ALLOW_CERTIFICATE
   * @see #SAVE_CERTIFICATE
   * @see #DENY_CERTIFICATE
   */
  public short   replaceCertificate( SSLCertificate oldCert, 
                                     SSLCertificate newCert );

  /**
   * If the server doesn't send a certificate, this method will be called.
   *
   * @return true to continue the connection.
   */
  public boolean continueWithoutServerCertificate();

  /**
   * If the server sends an expired certificate, this method will be called.
   *
   * @param cert  the certificate sent from the FTP server.
   *
   * @return true to continue the connection.
   */
  public boolean continueWithExpiredCertificate( SSLCertificate cert );

  /**
   * If the server sends an invalid certificate, this method will be called.
   * An invalid certificate can include a cert that is not yet vaild.
   *
   * @param cert  the certificate sent from the FTP server.
   *
   * @return true to continue the connection.
   */
  public boolean continueWithInvalidCertificate( SSLCertificate cert );

  /**
   * If the server sends a certificate which doesn't match the hostname, 
   * this method will be called.
   *
   * @param cert        the certificate sent from the FTP server.
   * @param actualHost  the hostname of the server.
   * @param certHost    the hostname as specified in the certificate.
   *
   * @return true to continue the connection.
   */
  public boolean continueWithCertificateHostMismatch( SSLCertificate cert,
                                                      String actualHost, 
                                                      String certHost );

  /**
   * If the random number generator is seeding and is not ready for setting
   * up SSL sockets, this method will be called.
   */
  public void randomSeedIsGenerating();

  /**
   * When the random number generator is finished seeding, this method will
   * be called.
   */
  public void randomSeedGenerated();
}
