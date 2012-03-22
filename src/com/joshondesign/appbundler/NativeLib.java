/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joshondesign.appbundler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joshmarinacci
 */
public class NativeLib {
    private final String name;
    private File baseDir;

    public NativeLib(String name) {
        p("parsing a native lib: " + name);
        this.name = name;
    }


    private static void p(String string) {
        System.out.println(string);
    }

    public String getName() {
        return name;
    }

    void setBaseDir(File file) {
        baseDir = file;
    }
    
    File getBaseDir() {
        return baseDir;
    }

    void verify() throws Exception {
        p("base dir = " + baseDir);
        if(baseDir == null) 
            throw new Exception("No basedir specified for native extension");
        if(!baseDir.exists())
            throw new Exception("The basedir for native lib '" + getName() + "' doesn't exist! : " + baseDir.getAbsolutePath());
        File natives = new File(baseDir,"native");
        if(!natives.exists())
            throw new Exception("the 'native' dir for native lib " + getName() + " doesn't exist!: " + natives.getAbsolutePath());
    }

    public File[] getOSDirs() {
        return new File(baseDir,"native").listFiles();
    }

    public Iterable<File> getJars() {
        List<File> jars = new ArrayList<File>();
        for(File f : baseDir.listFiles()) {
            if(!f.isDirectory() && f.exists() && f.getName().endsWith(".jar")) {
                jars.add(f);
            }
        }
        return jars;
    }
}
