/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

// Test deflate() with large buffers and dynamic change of compression level
class test_large_deflate_inflate{

  static final byte[] hello="123456789012345678901234567890, 123456789012345678901234567890! ".getBytes();
  static{
    hello[hello.length-1]=0;
  }

  public static void main(String[] arg){
	  
	  byte[] input = null;
	  byte[] large_input = getInput("../Shonot/largeText.txt");
	  
    int err;
    int comprLen=400000;
    int uncomprLen=comprLen;
    byte[] compr=new byte[comprLen];
    byte[] uncompr=new byte[uncomprLen];

    ZStream c_stream=new ZStream();

    err=c_stream.deflateInit(JZlib.Z_BEST_SPEED);
    CHECK_ERR(c_stream, err, "deflateInit");

    c_stream.next_out=compr;
    c_stream.next_out_index=0;
    c_stream.avail_out=comprLen;

    // At this point, uncompr is still mostly zeroes, so it should compress
    // very well:
    //-- my changes:
    //input = hello;
    input = large_input;
    c_stream.next_in=input;//hello;//uncompr;
    c_stream.avail_in=input.length;// hello.length; //uncomprLen;
    
    //-- till here
    err=c_stream.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(c_stream, err, "deflate");
    if(c_stream.avail_in!=0){
      System.out.println("deflate not greedy");
      System.exit(1);
    }
    /*
    // Feed in already compressed data and switch to no compression:
    c_stream.deflateParams(JZlib.Z_NO_COMPRESSION, JZlib.Z_DEFAULT_STRATEGY);
    c_stream.next_in=compr;
    c_stream.next_in_index=0;
    c_stream.avail_in=comprLen/2;
    err=c_stream.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(c_stream, err, "deflate");

    // Switch back to compressing mode:
    c_stream.deflateParams(JZlib.Z_BEST_COMPRESSION, JZlib.Z_FILTERED);
    c_stream.next_in=uncompr;
    c_stream.next_in_index=0;
    c_stream.avail_in=uncomprLen;
    err=c_stream.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(c_stream, err, "deflate");
     */
    err=c_stream.deflate(JZlib.Z_FINISH);
    if(err!=JZlib.Z_STREAM_END){
      System.out.println("deflate should report Z_STREAM_END");
      System.exit(1);
    }
    err=c_stream.deflateEnd();
    CHECK_ERR(c_stream, err, "deflateEnd");

    ZStream d_stream=new ZStream();

    d_stream.next_in=compr;
    d_stream.next_in_index=0;
    d_stream.avail_in=comprLen;

    err=d_stream.inflateInit();
    CHECK_ERR(d_stream, err, "inflateInit");

    while(true){
      d_stream.next_out=uncompr;
      d_stream.next_out_index=0;
      d_stream.avail_out=uncomprLen;
      err=d_stream.inflate(JZlib.Z_NO_FLUSH);
      if(err==JZlib.Z_STREAM_END) break;
      CHECK_ERR(d_stream, err, "inflate large");
    }

    err=d_stream.inflateEnd();
    CHECK_ERR(d_stream, err, "inflateEnd");

    //-- my changes
    int i=0;
    for(;i<input.length; i++) if(input[i]==0) break;
    int j=0;
    for(;j<uncompr.length; j++) if(uncompr[j]==0) break;

    if(i==j){
      for(i=0; i<j; i++) if(input[i]!=uncompr[i]) break;
      if(i==j){
	System.out.println("inflate(): "+new String(uncompr, 0, j));
	return;
      }
    }
    else{
      System.out.println("bad inflate");
    }
    
    
    /*
    if (d_stream.total_out!= 2*uncomprLen + comprLen/2) {
       System.out.println("bad large inflate: "+d_stream.total_out);
       System.exit(1);
    }
    else {
      System.out.println("large_inflate(): OK");
    }
    */
  //-- till here
  }

  static void CHECK_ERR(ZStream z, int err, String msg) {
    if(err!=JZlib.Z_OK){
      if(z.msg!=null) System.out.print(z.msg+" "); 
      System.out.println(msg+" error: "+err); 

      System.exit(1);
    }
  }
  
  static byte[] getInput(String fileName) {
	  byte[] buffer = null;
	  File file = new File(fileName);
	  buffer = new byte[(int)file.length()];
	  FileInputStream in = null;
	  try {
		  in = new FileInputStream(fileName);
		  in.read(buffer);
		  in.close();
	  }
	  catch(Exception e) {
		  e.printStackTrace();
	  }
	  return buffer;
  }
}
