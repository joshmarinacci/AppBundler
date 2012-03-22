/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joshondesign.appbundler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author joshmarinacci
 */
public class Util {

    public static void copyToFile(File srcFile, File dstFile) throws FileNotFoundException, IOException {
        byte[] buf = new byte[1024*16];
        FileInputStream fin = new FileInputStream(srcFile);
        FileOutputStream fout = new FileOutputStream(dstFile);
        while(true) {
            int n = fin.read(buf);
            if(n < 0) break;
            fout.write(buf,0,n);
        }
        fin.close();
        fout.close();
        p("m copied file: " + srcFile.getName());
    }

    private static void p(String s) {
        System.out.println(s);
    }

    public static void copyToFile(URL resource, File dstFile) throws FileNotFoundException, IOException {
        byte[] buf = new byte[1024*16];
        InputStream in = resource.openStream();
        FileOutputStream fout = new FileOutputStream(dstFile);
        while(true) {
            int n = in.read(buf);
            if(n < 0) break;
            fout.write(buf,0,n);
        }
        in.close();
        fout.close();
        p("m copied jar: " + resource.toString());
    }
}
