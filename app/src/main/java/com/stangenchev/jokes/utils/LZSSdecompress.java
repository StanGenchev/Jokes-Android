package com.stangenchev.jokes.utils;

import java.io.ByteArrayOutputStream;

public class LZSSdecompress {
    static final int BUFFER_SIZE = 1024;
    static final int N_CODED_BYTES_NUMBER_BITS = 4;
    static final int N_CODED_BYTES_OFFSET_BITS = 10;
    byte[] Buffer = new byte[BUFFER_SIZE];
    int CurrentByte = 0;
    byte[] InputFileBuffer = null;
    int InputFileBufferLength = 0;
    byte[] OutBuffer = new byte[18];
    int ReadBits = 0;
    ByteArrayOutputStream btarray = null;
    int iBuffer = 0;
    int iInputFile = 0;
    int iOutBuffer = 0;
    int nCodedBytes = 0;
    int nOffset = 0;
    int nRemainingBits = 0;

    public byte[] decompress(byte[] in, int len) {
        this.btarray = new ByteArrayOutputStream();
        this.InputFileBuffer = in;
        this.InputFileBufferLength = len;
        this.iInputFile = 0;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            this.Buffer[i] = 0;
        }
        this.iBuffer = 0;
        this.nRemainingBits = 0;
        this.nOffset = 0;
        this.nCodedBytes = 0;
        while (GetBits(1)) {
            if (this.ReadBits == 0) {
                if (!GetBits(8)) {
                    break;
                }
                byte[] bArr = this.Buffer;
                int i2 = this.iBuffer;
                this.iBuffer = i2 + 1;
                bArr[i2] = (byte) this.ReadBits;
                if (this.iBuffer == BUFFER_SIZE) {
                    this.iBuffer = 0;
                }
                this.btarray.write(this.ReadBits);
            } else if (!GetBits(N_CODED_BYTES_OFFSET_BITS)) {
                break;
            } else {
                this.nOffset = this.ReadBits;
                if (!GetBits(4)) {
                    break;
                }
                this.nCodedBytes = this.ReadBits + 2;
                int iStart = this.nOffset;
                this.iOutBuffer = 0;
                while (this.iOutBuffer < this.nCodedBytes) {
                    int iStart2 = iStart + 1;
                    this.OutBuffer[this.iOutBuffer] = this.Buffer[iStart];
                    if (iStart2 == BUFFER_SIZE) {
                        iStart = 0;
                    } else {
                        iStart = iStart2;
                    }
                    this.iOutBuffer++;
                }
                this.iOutBuffer = 0;
                while (this.iOutBuffer < this.nCodedBytes) {
                    byte[] bArr2 = this.Buffer;
                    int i3 = this.iBuffer;
                    this.iBuffer = i3 + 1;
                    bArr2[i3] = this.OutBuffer[this.iOutBuffer];
                    if (this.iBuffer == BUFFER_SIZE) {
                        this.iBuffer = 0;
                    }
                    this.btarray.write(this.OutBuffer[this.iOutBuffer]);
                    this.iOutBuffer++;
                }
            }
        }
        return this.btarray.toByteArray();
    }

    private boolean GetBits(int nBits) {
        this.ReadBits = 0;
        while (true) {
            int nBits2 = nBits;
            nBits = nBits2 - 1;
            if (nBits2 == 0) {
                return true;
            }
            if (this.nRemainingBits == 0) {
                this.nRemainingBits = 8;
                if (this.iInputFile >= this.InputFileBufferLength) {
                    return false;
                }
                byte[] bArr = this.InputFileBuffer;
                int i = this.iInputFile;
                this.iInputFile = i + 1;
                this.CurrentByte = bArr[i];
            }
            this.ReadBits <<= 1;
            if ((this.CurrentByte & 128) != 0) {
                this.ReadBits |= 1;
            }
            this.CurrentByte <<= 1;
            this.nRemainingBits--;
        }
    }
}
