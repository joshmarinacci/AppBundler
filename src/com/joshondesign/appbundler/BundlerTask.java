package com.joshondesign.appbundler;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class BundlerTask extends Task {
    String target = null;
    File bundlefile = null;
    File destdir = null;
    String libdir = null;
    List<String> jardirs = new ArrayList<String>();
    
    public void setBundle(File bundlefile) {
        this.bundlefile = bundlefile;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }
    public void setLibdir(String libdir) {
        String[] dirs = libdir.split(";");
        for(String d : dirs) {
            this.jardirs.add(d);
        }
    }
    
    public void execute() {
        if(bundlefile == null) {
            throw new BuildException("bundle xml file not set");
        }
        
        String message = getProject().getProperty("ant.project.name");
        
        log("bundle file = " + bundlefile.getAbsolutePath());
        log("dest dir = " + destdir.getAbsolutePath());
        log("generating target " + target);
        
        try {
            Bundler.runit(bundlefile,jardirs,target, destdir.getAbsolutePath(),null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }
}
