package com.joshondesign.appbundler.mac;

import com.joshondesign.appbundler.AppDescription;
import com.joshondesign.appbundler.Bundler;
import com.joshondesign.appbundler.Jar;
import com.joshondesign.appbundler.NativeLib;
import com.joshondesign.appbundler.Prop;
import com.joshondesign.appbundler.Util;
import com.joshondesign.xml.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 29, 2010
 * Time: 4:54:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacBundler {
    
    private static final String OSNAME = "mac";

// http://developer.apple.com/library/mac/#documentation/Java/Reference/Java_InfoplistRef/Articles/JavaDictionaryInfo.plistKeys.html

    public static void start(AppDescription app, String dest_dir) throws Exception {
        //create the dir structure
        File destDir = new File(dest_dir+"/"+OSNAME+"/");
        File appDir = new File(destDir,app.getName()+".app");
        appDir.mkdirs();
        p("app dir exists = " + appDir.exists());
        File contentsDir = new File(appDir,"Contents");
        contentsDir.mkdir();
        new File(contentsDir,"MacOS").mkdir();
        File resourcesDir = new File(contentsDir, "Resources");
        resourcesDir.mkdir();
        File javadir = new File(resourcesDir,"Java");
        File depdir = new File(javadir,"lib");
        javadir.mkdir();
        depdir.mkdir();

        for(Jar jar : app.getJars()) {
            //p("processing jar = " + jar.getName() + " os = "+jar.getOS());
            if(jar.isOSSpecified()) {
                if(!jar.matchesOS(OSNAME)) {
                    p("   skipping jar " + jar.getName());
                    continue;
                }
            }

            File jarFile;
            if(jar.isMain()){
              jarFile = new File(javadir,jar.getName());
            } else {
              jarFile = new File(depdir,jar.getName());
            }
            Util.copyToFile(jar.getFile(), jarFile);
        }

        processNatives(javadir, app);

        // copy the icon
        for(String iconS : app.getAppIcons()) {
            if(iconS.toLowerCase().endsWith(".icns")) {
                p("Using icon: " + iconS);
                File icon = new File(iconS);
                File outIcon = new File(resourcesDir,"icon.icns");
                p("out icon = " + outIcon.getAbsolutePath());
                p("t = " + resourcesDir.getAbsolutePath());
                p("t = " + resourcesDir.exists());
                Bundler.copyStream(new FileInputStream(icon),new FileOutputStream(outIcon));
            }
        }

        // copy splash image
        String splashImage = app.getSplashImage();
        if (null != splashImage) {
            p("Using splash image: " + splashImage);
            File splash = new File(splashImage);
            File outSplash = new File(javadir,splash.getName());
            p("out splash = " + outSplash.getAbsolutePath());
            p("t = " + resourcesDir.getAbsolutePath());
            p("t = " + resourcesDir.exists());
            Bundler.copyStream(new FileInputStream(splash),new FileOutputStream(outSplash));
        }



        for(String ext : app.getExtensions()) {
            String exticon = app.getExtensionIcon(ext);
            if(exticon != null) {
                File ifile = new File(exticon);
                System.out.println("copying over icon " + ifile.getAbsolutePath());
                if(ifile.exists()) {
                    File outIcon = new File(resourcesDir,ifile.getName());
                    Bundler.copyStream(new FileInputStream(ifile), new FileOutputStream(outIcon));
                    p("copied: " + ifile.getAbsolutePath());
                    p("   to:  " + outIcon.getAbsolutePath());
                }

            }
        }

        //build the info plist
        processInfoPlist(app,contentsDir);

        //copy the pkginfo
        Bundler.copyStream(
                MacBundler.class.getResourceAsStream("PkgInfo.txt"),
                new FileOutputStream(new File(contentsDir,"PkgInfo")));

        // copy the java stub
        InputStream stub_path = MacBundler.class.getResourceAsStream("JavaApplicationStub");
        File stub_dest = new File(contentsDir,"MacOS/JavaApplicationStub");
        Bundler.copyStream(stub_path,new FileOutputStream(stub_dest));
        // make the stub executable
        String[] command = new String[3];
        command[0] = "chmod";
        command[1] = "755";
        command[2] = stub_dest.getAbsolutePath();
        p("calling: ");
        for(String s : command) {
            System.out.print(s + " ");
        }
        p("");
        Runtime.getRuntime().exec(command);


        // set the bundle bit
        try {
            //Runtime.getRuntime().exec("/Developer/Tools/SetFile -a B "+appDir);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void processInfoPlist(AppDescription app, File contentsDir) throws Exception {
        p("Processing the info plist");

        XMLWriter out = new XMLWriter(new File(contentsDir,"Info.plist"));
        out.header();
        out.start("plist","version","1.0");
        out.start("dict");

        for(String ext : app.getExtensions()) {
            //file extensions
            out.start("key").text("CFBundleDocumentTypes").end();
            out.start("array").start("dict");
                out.start("key").text("CFBundleTypeExtensions").end();
                out.start("array").start("string").text(ext).end().end();
                out.start("key").text("CFBundleTypeName").end();
                out.start("string").text(ext).end();
                out.start("key").text("CFBundleTypeMIMETypes").end();
                out.start("array").start("string").text(app.getExtensionMimetype(ext)).end().end();
                out.start("key").text("CFBundleTypeRole").end();
                out.start("string").text("Editor").end();
                String icon = app.getExtensionIcon(ext);
                if(icon != null) {
                    out.start("key").text("CFBundleTypeIconFile").end();
                    File ifile = new File(icon);
                    System.out.println("doing icon: " + ifile.getAbsolutePath());
                    out.start("string").text(ifile.getName()).end();
                    //copy over the icon
                }
            out.end().end();
        }

        out.start("key").text("CFBundleName").end().start("string").text(app.getName()).end();
        out.start("key").text("CFBundleDisplayName").end().start("string").text(app.getName()).end();
        out.start("key").text("CFBundleExecutable").end().start("string").text(app.getName()).end();
        out.start("key").text("CFBundleGetInfoString").end().start("string").text(app.getName()).end();

        out.start("key").text("CFBundleVersion").end().start("string").text("10.2").end();
        out.start("key").text("CFBundleAllowMixedLocalizations").end().start("string").text("true").end();
        out.start("key").text("CFBundleExecutable").end().start("string").text("JavaApplicationStub").end();
        out.start("key").text("CFBundleDevelopmentRegion").end().start("string").text("English").end();
        out.start("key").text("CFBundlePackageType").end().start("string").text("APPL").end();
        out.start("key").text("CFBundleSignature").end().start("string").text("????").end();
        out.start("key").text("CFBundleInfoDictionaryVersion").end().start("string").text("6.0").end();
        out.start("key").text("CFBundleIconFile").end().start("string").text("icon.icns").end();

        out.start("key").text("CFBundleInfoDictionaryVersion").end().start("string").text("6.0").end();
        out.start("key").text("CFBundleInfoDictionaryVersion").end().start("string").text("6.0").end();
        //LSMinimumSystemVersion
        //LSMultipleInstancesProhibited

        out.start("key").text("Java").end();
        out.start("dict");
        key(out,"MainClass",app.getMainClass());
        key(out,"JVMVersion","1.6+");

        String splashImage = app.getSplashImage();
        if (null != splashImage) {
            File splash = new File(splashImage);
            // See: http://lists.apple.com/archives/java-dev/2008/Jun/msg00012.html
            key(out,"SplashFile","$JAVAROOT/"+splash.getName());
        }

        out.start("key").text("ClassPath").end();
        out.start("array");
        for(Jar jar : app.getJars()) {
            if(jar.isMain()) {
                out.start("string").text("$JAVAROOT/"+jar.getName()).end();
            } else {
                out.start("string").text("$JAVAROOT/lib/"+jar.getName()).end();
            }
        }
        for(NativeLib lib : app.getNativeLibs()) {
            p("native lib: " + lib.getName());
            p("getting the common jars");
            for(File jar : lib.getCommonJars()) {
                p("adding native common jar to plist: " + jar.getName());
                out.start("string").text("$JAVAROOT/"+jar.getName()).end();
            }
            p("getting the platform jars");
            for(File jar : lib.getPlatformJars(OSNAME)) {
                p("adding native only jar to plist: " + jar.getName());
                out.start("string").text("$JAVAROOT/"+jar.getName()).end();
            }
        }
        out.end();//array

        out.start("key").text("Properties").end();
        out.start("dict");
        key(out,"apple.laf.useScreenMenuBar","true");
        for(Prop prop : app.getProps()) {
            key(out,prop.getName(),prop.getValue());
        }
        out.end();//dict

        out.end();//dict

        out.end().end(); //dict, plist
        out.close();
    }

    private static void key(XMLWriter out, String key, String value) {
        out.start("key").text(key).end().start("string").text(value).end();
    }

    private static void p(String s) {
        System.out.println(s);
    }

    private static void processNatives(File javaDir, AppDescription app) throws IOException {
        //track the list of files in the appbundler_tasks.xml
        for(NativeLib lib : app.getNativeLibs()) {
            p("=== sucking in native lib: " + lib.getName());
            for(File os : lib.getOSDirs()) {
                //p("os = " + os.getName());
                if(OSNAME.equals(os.getName())) {
                    for(File file : os.listFiles()) {
                        //p("   file = " + file.getName());
                        File destFile = new File(javaDir, file.getName());
                        //p("copying to file: " + destFile);
                        Util.copyToFile(file, destFile);
                    }
                }
            }
            for(File jar : lib.getCommonJars()) {
                p("copying over native common jar: " + jar.getName());
                Util.copyToFile(jar, new File(javaDir, jar.getName()));
            }
            for(File jar : lib.getPlatformJars(OSNAME)) {
                p("copying over native only jar: " + jar.getName());
                Util.copyToFile(jar, new File(javaDir, jar.getName()));
            }
        }
    }
}
