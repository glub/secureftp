
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CertInfo.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.common;

public class CertInfo {

  private String commonName       = null;
  private String organization     = null;
  private String organizationUnit = null;
  private String city             = null;
  private String state            = null;
  private String country          = null;
 
  public CertInfo() {
    this( "localhost", "?", "?", "?", "?", "?" );
  }

  public CertInfo( String cn, String o, String ou, String city, String state,
                   String country ) {
    setCommonName( cn );
    setOrganization( o );
    setOrganizationUnit( ou );
    setCity( city );
    setState( state );
    setCountry( country );
  }

  public String getCommonName() { return commonName; } 
  public void   setCommonName( String cn ) { this.commonName = cn; } 

  public String getOrganization() { return organization; } 
  public void   setOrganization( String o ) { this.organization = o; } 

  public String getOrganizationUnit() { return organizationUnit; } 
  public void   setOrganizationUnit( String ou ) { this.organizationUnit = ou; } 
  public String getCity() { return city; } 
  public void   setCity( String city ) { this.city = city; } 

  public String getState() { return state; } 
  public void   setState( String state ) { this.state = state; } 

  public String getCountry() { return country; } 
  public void   setCountry( String country ) { this.country = country; } 

}
