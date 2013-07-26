
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PEMInputStream.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.common;

import java.io.*;

public final class PEMInputStream extends FilterInputStream {
  private static final char[] pem_array = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
    'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 
    'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 
    'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', 
    '8', '9', '+', '/'
  };

  private static final byte[] pem_convert_array = new byte[256];

  static final int EOF = -1;

  // populate the pem_convert_array
  static {
    for(int i = 0; i < 255; i++)
      pem_convert_array[i] = -1;

    for(int i = 0; i < pem_array.length; i++)
        pem_convert_array[pem_array[i]] = (byte)i;
  }

  public PEMInputStream( InputStream input ) throws IOException {
    super( null );

    if( 0 == input.available() ) {
      throw new IOException("PEMInputStream stream is empty.");
    }

    byte decode_buffer[] = new byte[45];
    int i;

    for(i = 0; i < decode_buffer.length; 
        i = decodeAtom(input, decode_buffer, i) != 3 ? 0 : i + 3);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(decode_buffer, 0, i);

    int j = 0;
    while(j <= 3) {

      int k = decodeAtom( input, decode_buffer, 0 );

      if(k > 0) {
        baos.write(decode_buffer, 0, k);

        if(k < 3)
          break;

        j = 0;
        continue;
      }

      if(k == -1)
        break;
      j++;
    }

    super.in = new ByteArrayInputStream(baos.toByteArray());
  }

  // decode one Base64 atom into 1, 2, or 3 bytes of data
  protected int decodeAtom(InputStream input, byte decode_buffer[], int rem)
                           throws IOException {
    int j = input.read();

    if(j < 0)
      return -1;

    j = pem_convert_array[j];

    if(j < 0)
      return 0;

    decode_buffer[rem] = (byte)(j << 2);

    j = input.read();

    if(j < 0)
      return -1;

    j = pem_convert_array[j];
    if(j < 0)
      return 0;

    decode_buffer[rem] |= j >> 4;
    decode_buffer[rem + 1] = (byte)(j << 4);

    j = input.read();

    if(j < 0 || j == 61)
      return 1;

    j = pem_convert_array[j];

    if(j < 0)
      return 0;

    decode_buffer[rem + 1] |= j >> 2;
    decode_buffer[rem + 2] = (byte)(j << 6);

    j = input.read();

    if(j < 0 || j == 61)
      return 2;

    j = pem_convert_array[j];

    if(j < 0)
      return 0;
    else {
      decode_buffer[rem + 2] |= j;
      return 3;
    }
  }
}
