/**
 * Created by izeidman on 23/08/2016.
 */
package com.jcraft.jzlib;

import com.jcraft.jzlib.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Converter {
    public static String ToGzipAscii(String input)
    {
        int err;
        int comprLen=40000;
        int uncomprLen=comprLen;
        byte[] compr=new byte[comprLen];
        byte[] uncompr=new byte[uncomprLen];

        ZStream c_stream=new ZStream();

        byte[] inpuBytes = input.getBytes();

        err=c_stream.deflateInit(JZlib.Z_DEFAULT_COMPRESSION);
        CHECK_ERR(c_stream, err, "deflateInit");

        c_stream.next_in=inpuBytes;  //what to compress
        c_stream.next_in_index=0;

        c_stream.next_out=compr;  //compressed data
        c_stream.next_out_index=0;

        while(c_stream.total_in!=inpuBytes.length &&   //there is more input to compress
                c_stream.total_out<comprLen){ //there is free space in the out buffer
            c_stream.avail_in=c_stream.avail_out=1; // force small buffers
            err=c_stream.deflate(JZlib.Z_NO_FLUSH);
            CHECK_ERR(c_stream, err, "deflate");
        }

        while(true){
            c_stream.avail_out=1;
            err=c_stream.deflate(JZlib.Z_FINISH);
            if(err==JZlib.Z_STREAM_END)break;
            CHECK_ERR(c_stream, err, "deflate");
        }

        err=c_stream.deflateEnd();
        CHECK_ERR(c_stream, err, "deflateEnd");
        // end of compression, compressed data in compr


        ZStream d_stream=new ZStream();

        d_stream.next_in=compr;
        d_stream.next_in_index=0;
        d_stream.next_out=uncompr;
        d_stream.next_out_index=0;

        err=d_stream.inflateInit();
        CHECK_ERR(d_stream, err, "inflateInit");

        while(d_stream.total_out<uncomprLen &&
                d_stream.total_in<comprLen) {
            d_stream.avail_in=d_stream.avail_out=1; /* force small buffers */
            err=d_stream.inflate(JZlib.Z_NO_FLUSH);
            if(err==JZlib.Z_STREAM_END) break;
            CHECK_ERR(d_stream, err, "inflate");
        }

        err=d_stream.inflateEnd();
        CHECK_ERR(d_stream, err, "inflateEnd");

        try {
            return processSnortHex(input,readFile("my_output_file.txt",StandardCharsets.US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String processSnortHex(String input,String gzipAscii){
        return gzipAscii;
    }

    static void CHECK_ERR(ZStream z, int err, String msg) {
        if(err!=JZlib.Z_OK){
            if(z.msg!=null) System.out.print(z.msg+" ");
            System.out.println(msg+" error: "+err);
            System.exit(1);
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
