
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkVersion1.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.util;

public class BookmarkVersion1 {
  private String name;
  private String hostname;
  private String port;
  private String username;
  private String password;
  private int security;
  private boolean pasv;
  private boolean anon;
  private boolean proxy;
  private boolean ssldata;
  private String localdir;
  private String remotedir;

  public BookmarkVersion1(String t_name, String t_hostname, String t_port,
                  String t_username, String t_password, int t_security,
                  boolean t_pasv, boolean t_anon, boolean t_proxy,
                  boolean t_ssldata, String t_localdir, String t_remotedir) {
    name      = t_name;
    hostname  = t_hostname;
    port      = t_port;
    username  = t_username;
    password  = t_password;
    security  = t_security;
    pasv      = t_pasv;
    anon      = t_anon;
    proxy     = t_proxy;
    ssldata   = t_ssldata;
    localdir  = t_localdir;
    remotedir = t_remotedir;
  }

  public String  getName()     { return name; }
  public String  getHostname() { return hostname; }
  public String  getPort()     { return port; }
  public String  getUsername() { return username; }
  public String  getPassword() { return password; }

  public void    setName(String t_name)         { name = t_name; }
  public void    setHostname(String t_hostname) { hostname = t_hostname; }
  public void    setPort(String t_port)         { port = t_port; }
  public void    setUsername(String t_username) { username = t_username; }

  public int     getSecurity() { return security; }
  public boolean getPasv()     { return pasv; }
  public boolean getAnon()     { return anon; }
  public boolean getProxy()    { return proxy; }
  public boolean getSSLData()  { return ssldata; }

  public void    setSecurity(int t_security)    { security = t_security; }
  public void    setPasv(boolean t_pasv)        { pasv = t_pasv; }
  public void    setAnon(boolean t_anon)        { anon = t_anon; }
  public void    setProxy(boolean t_proxy)      { proxy = t_proxy; }
  public void    setSSLData(boolean t_ssldata)  { ssldata = t_ssldata; }

  public String  getLocalDir()   { return localdir; }
  public String  getRemoteDir()  { return remotedir; }

  public void    setLocalDir(String t_localdir)   { localdir = t_localdir; }
  public void    setRemoteDir(String t_remotedir)  { remotedir = t_remotedir; }
}

