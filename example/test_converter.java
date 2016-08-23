package example;

import com.jcraft.jzlib.Converter;

/**
 * Created by izeidman on 23/08/2016.
 */
public class test_converter {
    public static void main(String[] arg){
        String gzipAscii = Converter.ToGzipAscii("hello, hello! ");
        if(gzipAscii.equals("C104;C101;C108;C108;C111;C44;C32;C104;L4;D7;C33;C32;"))
            System.out.println("gzip ascii is as expected");
        else{
            System.out.println("gzip ascii is as not expected");
            System.exit(1);
        }
    }
}
