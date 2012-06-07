/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joshondesign.appbundler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author joshmarinacci
 */
public class AppDescription {
    private List<Jar> jars;
    private String name;
    private Map<String,String> extensions;
    private Map<String,String> icons;
    private final ArrayList<String> appIcons;
    private List<NativeLib> natives;
    private List<Prop> props;
    private String splashImage;

    public AppDescription() {
        jars = new ArrayList<Jar>();
        extensions = new HashMap<String, String>();
        icons = new HashMap<String, String>();
        appIcons = new ArrayList<String>();
        natives = new ArrayList<NativeLib>();
        props = new ArrayList<Prop>();
    }

    void addJar(Jar jar) {
        this.jars.add(jar);
    }

    public Iterable<Jar> getJars() {
        return this.jars;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMainClass() throws Exception {
        for(Jar jar : jars) {
            if(jar.isMain()) return jar.getMainClass();
        }
        throw new Exception("Error! Couldn't find a main class for the app");
    }

    void addExtension(String fileExtension, String mimeType, String icon) {
        extensions.put(fileExtension,mimeType);
        if(icon != null) {
            icons.put(fileExtension,icon);
        }
    }
    public Collection<String> getExtensions() {
        return extensions.keySet();
    }
    public String getExtensionMimetype(String ext) {
        return extensions.get(ext);
    }

    public String getExtensionIcon(String ext) {
        return icons.get(ext);
    }

    public Collection<String> getAppIcons() {
        return appIcons;
    }

    void addIcon(String name) {
        appIcons.add(name);
    }

    void addNative(NativeLib nativeLib) {
        natives.add(nativeLib);
    }

    public Iterable<NativeLib> getNativeLibs() {
        return natives;
    }

    void addProp(Prop prop) {
        props.add(prop);
    }

    public Iterable<Prop> getProps() {
        return props;
    }

    public String getSplashImage() {
        return splashImage;
    }

    void setSplashImage(String img) {
        this.splashImage = img;
    }

}
