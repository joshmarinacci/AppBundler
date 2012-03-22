/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joshondesign.appbundler;

import com.joshondesign.appbundler.onejar.OneJarBundler;
import com.joshondesign.appbundler.jnlp.JNLPBundler;
import com.joshondesign.appbundler.mac.MacBundler;
import com.joshondesign.appbundler.webos.WebosBundler;
import com.joshondesign.appbundler.win.WindowsBundler;
import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import com.joshondesign.xml.XMLWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joshmarinacci
 */
public class Bundler {

    public static void main(String ... args) throws Exception {
        List<String> jardirs = new ArrayList<String>();
        String DESCRIPTOR = args[0];
        String target = args[1];
        String DEST_DIR = args[2];
        String codebase = null;
        for(String arg : args) {
            if(arg.startsWith("--")) {
                if(arg.startsWith("--file=")) {
                    DESCRIPTOR = arg.substring("--file=".length());
                }
                if(arg.startsWith("--target=")) {
                    target = arg.substring("--target=".length());
                }
                if(arg.startsWith("--outdir=")) {
                    DEST_DIR = arg.substring("--outdir=".length());
                }
                if(arg.startsWith("--jardir=")) {
                    jardirs.add(arg.substring("--jardir=".length()));
                }
                if(arg.startsWith("--codebase=")) {
                    codebase = arg.substring("--codebase=".length());
                }
                p("Matched: " + arg);
            }
        }


        p("using target " + target);
        p("using dest_dir = " + DEST_DIR);
        p("using descriptor = " + DESCRIPTOR);

        //File keystore = new File("testkeystore");
        //load xml
        AppDescription app = parseDescriptor(DESCRIPTOR);
        //check xml
        verifyApp(app);
        //check support dirs
        verifyJarDirs(jardirs);
        //search support dirs for required jars
        findJars(app,jardirs);

        verifyNativeLibs(app);

        if("mac".equals(target)) {
            MacBundler.start(app,DEST_DIR);
            return;
        }
        if("jnlp".equals(target)) {
            JNLPBundler.start(app,DEST_DIR, codebase);
            return;
        }
        if("onejar".equals(target)) {
            OneJarBundler.start(app,DEST_DIR);
            return;
        }
        if("win".equals(target)) {
            WindowsBundler.start(app,DEST_DIR);
            return;
        }
        if("webos".equals(target)) {
            WebosBundler.start(app,DEST_DIR);
            return;
        }
        if("all".equals(target)) {
            MacBundler.start(app,DEST_DIR);
            OneJarBundler.start(app,DEST_DIR);
            JNLPBundler.start(app,DEST_DIR, codebase);
            WindowsBundler.start(app,DEST_DIR);
            return;
        }
        
        p("ERROR: unrecognized target: " + target);

    }

    private static void p(String[] args) {
        for(String s : args) {
            p(s);
        }
    }

    private static AppDescription parseDescriptor(String DESCRIPTOR) throws Exception {
        File descriptor = new File(DESCRIPTOR);
        if(!descriptor.exists()) throw new Error("Descriptor file: " + DESCRIPTOR + " does not exist");

        AppDescription app = new AppDescription();
        Doc doc = XMLParser.parse(descriptor);
        app.setName(doc.xpathString("/app/@name"));
        for(Elem jarElem : doc.xpath("/app/jar")) {
            Jar jar = new Jar(jarElem.attr("name"));
            if(jarElem.hasAttr("main-class")) {
                jar.setMain(true);
                jar.setMainClass(jarElem.attr("main-class"));
            }
            if(jarElem.hasAttr("os")) {
                jar.setOS(jarElem.attr("os"));
            }
            app.addJar(jar);
        }
        for(Elem extElem : doc.xpath("/app/filetype")) {
            app.addExtension(extElem.attr("extension"),extElem.attr("mimetype"),extElem.attr("icon"));
        }
        for(Elem iconE : doc.xpath("/app/icon")) {
            System.out.println("got an icon: " + iconE.attr("name"));
            app.addIcon(iconE.attr("name"));
        }

        for(Elem nativeE : doc.xpath("/app/native")) {
            app.addNative(new NativeLib(nativeE.attr("name")));
        }

        for(Elem propE : doc.xpath("/app/property")) {
            System.out.println("adding property");
            app.addProp(new Prop(propE.attr("name"),propE.attr("value")));
        }

        return app;

    }

    private static void verifyApp(AppDescription app) throws Exception {
        int mainCount = 0;
        for(Jar jar : app.getJars()) {
            if(jar.isMain()) mainCount++;
        }
        if(mainCount == 0) {
            throw new Exception("The app must have at least one jar with a main class in it");
        }
        if(mainCount > 1) {
            throw new Exception("You cannot have more than one jar with a main class set");
        }
    }

    private static void verifyNativeLibs(AppDescription app) throws Exception {
        for(NativeLib nlib : app.getNativeLibs()) {
            nlib.verify();
        }
    }
    private static void verifyJarDirs(List<String> jardirs) throws Exception {
        for(String dir : jardirs) {
            if(!new File(dir).exists()) {
                throw new Exception("directory: " + dir + " does not exist");
            }
        }
    }

    private static void findJars(AppDescription app, List<String> jardirs) throws Exception {
        for(Jar jar : app.getJars()) {
            for(String sdir : jardirs) {
                File dir = new File(sdir);
                for(File file : dir.listFiles()){
                    if(file.getName().equals(jar.getName())) {
                        jar.setFile(file);
                        break;
                    }
                }
                if(jar.getFile() != null) break;
            }
            if(jar.getFile() == null) {
                throw new Exception("jar " + jar.getName() + " not found");
            }
            p("matched jar with file: " + jar.getFile().getName() + " " + jar.getFile().length() + " bytes");
        }

        for(NativeLib nlib : app.getNativeLibs()) {
            p("looking for native lib: " + nlib.getName());
            for(String sdir : jardirs) {
                File dir = new File(sdir);
                for(File file : dir.listFiles()) {
                    //p("looking at: " + file.getName() + " is dir = " + file.isDirectory());
                    if(file.getName().equals(nlib.getName()) && file.isDirectory()) {
                        p("found native lib: " + file.getAbsolutePath());
                        nlib.setBaseDir(file);
                    }
                }
            }
            if(nlib.getBaseDir() == null) {
                p("WARNING: no basedir found for : " + nlib.getName());
            }
        }
    }

    private static void p(String string) {
        System.out.println(string);
    }

    private static File prepDestDir(String DEST_DIR, AppDescription app) {
        File destDir = new File(DEST_DIR,app.getName());
        destDir.mkdirs();
        return destDir;
    }

    private static void processJars(AppDescription app,
                                    File outDir, File keystore,
                                    String alias, String password) throws FileNotFoundException, IOException {
        File libdir = new File(outDir,"lib");
        libdir.mkdir();
        for(Jar jar : app.getJars()) {
            File jarFile = new File(libdir,jar.getName());
            byte[] buf = new byte[1024*16];
            FileInputStream fin = new FileInputStream(jar.getFile());
            FileOutputStream fout = new FileOutputStream(jarFile);
            while(true) {
                int n = fin.read(buf);
                if(n < 0) break;
                fout.write(buf,0,n);
            }
            fin.close();
            fout.close();
            p("copied jar: " + jar.getName());
        }

        File antFile = File.createTempFile("sign", ".xml");
        p("created ant file: " + antFile);
        XMLWriter out = new XMLWriter(antFile);
        out.header();
        out.start("project", "name","Signer","default","default","basedir",outDir.getAbsolutePath())
                .start("target", "name","sign")
                .start("signjar")
                    .attr("alias",alias)
                    .attr("storepass",password)
                    .attr("keystore",keystore.getAbsolutePath())
                    .attr("verbose", "true")
                    .attr("lazy", "false")
                        .start("fileset", "dir",libdir.getAbsolutePath())
                            .start("include","name","*.jar").end()
                        .end()
                    .end()
                .end()
            .end();
        out.close();
        String[] cmd = new String[] { "ant","-f",antFile.getAbsolutePath(),"sign"};
        Process proc = Runtime.getRuntime().exec(cmd);
        //copyStream(proc.getInputStream(),System.out);
    }

    private static void generateJNLP(AppDescription app, File outDir) throws FileNotFoundException, UnsupportedEncodingException, Exception {
        XMLWriter out = new XMLWriter(new File(outDir,"launch.jnlp"));
//        XMLWriter out = new XMLWriter(new PrintWriter(System.out),null);
        out.header();
        out.start("jnlp")
                .attr("codebase", "file:"+outDir.getAbsolutePath())
                .attr("href", "launch.jnlp")
                ;
        out.start("information")
                .start("title").text(app.getName()).end()
                .start("description").text("dummy description").end()
                .start("vendor").text("vendor").end()
                .end();

        out.start("security").start("all-permissions").end().end();

        out.start("resources")
                .start("j2se", "version","1.6+").end();
        for(Jar jar : app.getJars()) {
            out.start("jar","href","lib/"+jar.getName());
            if(jar.isMain()) out.attr("main", "true");
            out.end();
        }
        out.end();

        out.start("application-desc")
                .attr("main-class", app.getMainClass())
                .end();
        out.end();
        out.close();
    }

    public static void copyStream(InputStream fin, OutputStream fout) throws IOException {
        byte[] buf = new byte[1024*16];
        while(true) {
            int n = fin.read(buf);
            if(n < 0) break;
            fout.write(buf,0,n);
        }
        fin.close();
        fout.close();
    }
}
