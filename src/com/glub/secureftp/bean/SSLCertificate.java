
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLCertificate.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.lang.String;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * The <code>SSLCertificate</code> class contains useful information about
 * an SSL (X509) certificate.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.1.3
 */

public class SSLCertificate {
  X509Certificate cert;
  Hashtable subject, issuer;

  /**
   * Create a <code>SSLCertificate</code> object.
   *
   * @param cert  the X509 certificate.
   */
  public SSLCertificate(X509Certificate cert) {
    this.cert = cert;

    if ( null != cert ) {
      subject = parsePrincipalString(cert.getSubjectDN().toString());
      issuer = parsePrincipalString(cert.getIssuerDN().toString());
    }
    else {
      subject = new Hashtable();
      subject.put( "CN", "Unknown (peer not authenticated)" );
      issuer = new Hashtable();
      issuer.put( "CN", "Unknown (peer not authenticated)" );
    }
  }

  /**
   * Returns the X.509 certificate.
   * 
   * @return the X.509 certificate
   */
  public X509Certificate getX509Certificate() {
    return cert;
  }

  /**
   * Returns the crypto fingerprint of the certificate.
   *
   * @return the crypto fingerprint of the certificate.
   */
  public String getFingerprint() {
    byte[] b;

    if ( null == cert ) {
      return "";
    }

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      b = md.digest(cert.getEncoded());
    }
    catch (Exception e) {
      return "<error occurred while calculating fingerprint>";
    }

    char[] c = new char[(b.length * 3) - 1];
    int j = 0;

    char hexdigits[] = { '0','1','2','3','4','5','6','7',
                         '8','9','A','B','C','D','E','F' };

    if (b == null || b.length <= 0)
      return null;

    c[j++] = hexdigits[(b[0] & 0xff) >> 4];
    c[j++] = hexdigits[(b[0] & 0xf)];

    for (int i = 1; i < b.length; i++) {
      c[j++] = ':';

      c[j++] = hexdigits[(b[i] & 0xff) >> 4];
      c[j++] = hexdigits[(b[i] & 0xf)];
    }

    return new String(c);
  }

  /**
   * Returns the serial number of the certificate.
   *
   * @return the serial number of the certificate.
   */
  public String getSerialNumber() { 
    if ( null == cert ) return "";
    return cert.getSerialNumber().toString(); 
  }

  /**
   * Returns the starting date of the certificate.
   *
   * @return the starting date of the certificate.
   */
  public Date getStartDate() { 
    if ( null == cert ) return new Date();
    return cert.getNotBefore(); 
  }

  /**
   * Returns the ending date of the certificate.
   *
   * @return the ending date of the certificate.
   */
  public Date getEndDate() { 
    if ( null == cert ) return new Date();
    return cert.getNotAfter(); 
  }

  /**
   * Returns the common name of the certificate.
   * <p>
   * Usually this is the hostname of the server.
   *
   * @return the common name of the certificate.
   */
  public String getCN() { return (String) subject.get("CN"); }

  /**
   * Returns the e-mail address associated with the certificate.
   *
   * @return the e-mail address associated with the certificate.
   */
  public String getEmail() { return (String) subject.get("EMail"); }

  /**
   * Returns the organizational unit associated with the certificate.
   *
   * @return the organizational unit associated with the certificate.
   */
  public String getOU() { return (String) subject.get("OU"); }

  /**
   * Returns the organization associated with the certificate.
   *
   * @return the organization associated with the certificate.
   */
  public String getOrg() { return (String) subject.get("O"); }

  /**
   * Returns the locality (or city) associated with the certificate.
   *
   * @return the locality (or city) associated with the certificate.
   */
  public String getLocality() { return (String) subject.get("L"); }

  /**
   * Returns the state associated with the certificate.
   *
   * @return the state associated with the certificate.
   */
  public String getState() { return (String) subject.get("ST"); }

  /**
   * Returns the country associated with the certificate.
   *
   * @return the country associated with the certificate.
   */
  public String getCountry() { return (String) subject.get("C"); }

  /**
   * Returns the common name of the certificate issuer.
   *
   * @return the common name of the certificate issuer.
   */
  public String getIssuerCN() { return (String) issuer.get("CN"); }

  /**
   * Returns the e-mail address associated with the certificate issuer.
   *
   * @return the e-mail address associated with the certificate issuer.
   */
  public String getIssuerEmail() { return (String) issuer.get("EMail"); }

  /**
   * Returns the organizational unit associated with the certificate issuer.
   *
   * @return the organizational unit associated with the certificate issuer.
   */
  public String getIssuerOU() { return (String) issuer.get("OU"); }

  /**
   * Returns the organization associated with the certificate issuer.
   *
   * @return the organization associated with the certificate issuer.
   */
  public String getIssuerOrg() { return (String) issuer.get("O"); }

  /**
   * Returns the locality (or city) associated with the certificate issuer.
   *
   * @return the locality (or city) associated with the certificate issuer.
   */
  public String getIssuerLocality() { return (String) issuer.get("L"); }

  /**
   * Returns the state associated with the certificate issuer.
   *
   * @return the state associated with the certificate issuer.
   */
  public String getIssuerState() { return (String) issuer.get("ST"); }

  /**
   * Returns the country associated with the certificate issuer.
   *
   * @return the country associated with the certificate issuer.
   */
  public String getIssuerCountry() { return (String) issuer.get("C"); }

  /**
   * Return the bit strength of the RSA certificate
   *
   * @return the bit strength of the RSA certificate (-1 if unknown).
   */
  public int getBitStrength() {
    int result = -1;

    PublicKey publicKey = null;

    if ( null != cert && 
         (publicKey = cert.getPublicKey()) instanceof RSAPublicKey ) {
      result = ((RSAPublicKey)publicKey).getModulus().bitLength();
    }

    return result;
  }

  private Hashtable parsePrincipalString(String p) {
    Hashtable table = new Hashtable();
    StringTokenizer tok = new StringTokenizer(p, ",");

    while (tok.hasMoreTokens()) {
      String temp = tok.nextToken().trim();

      int index = temp.indexOf('=');

      if (index <= 0)
        continue;

      String key = temp.substring(0, index);
      String value = temp.substring(index + 1);

      if ( value.startsWith("\"") && tok.hasMoreTokens() ) {
        while ( !value.endsWith("\"") && tok.hasMoreTokens() ) {
          value += ", " + tok.nextToken().trim();
        }

        value = value.substring(1, value.length()-1);
      }

      table.put(key, value);
    }

    return table;
  }
}

