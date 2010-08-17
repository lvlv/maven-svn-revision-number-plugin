package oe.maven.plugins.revision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

public final class IntegrationTestHelper {

    public static boolean modifyFile(File basedir, String fileName) throws IOException {
        File file = new File(basedir, fileName);
        System.out.println("modifying file " + file);
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.append("modified");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }

    public static boolean addFile(File basedir, String fileName, boolean useVersionControl) throws IOException, SVNException {
        File file = new File(basedir, fileName);
        System.out.println("adding file " + file + (useVersionControl ? " with " : " without ") + "version control");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.append("modified");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (useVersionControl) {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            clientManager.getWCClient().doAdd(file, false, false, false, SVNDepth.INFINITY, false, false, false);
        }
        return true;
    }

    public static boolean addDirectory(File basedir, String fileName, boolean useVersionControl) throws SVNException {
        File file = new File(basedir, fileName);
        System.out.println("adding directory " + file + (useVersionControl ? " with " : " without ") + "version control");
        boolean result = file.mkdirs();
        if (result && useVersionControl) {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            clientManager.getWCClient().doAdd(file, false, false, false, SVNDepth.INFINITY, false, false, false);
        }
        return result;
    }

    public static boolean deleteFile(File basedir, String fileName, boolean useVersionControl) throws SVNException {
        File file = new File(basedir, fileName);
        System.out.println("deleting " + file + (useVersionControl ? " with " : " without ") + "version control");
        if (useVersionControl) {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            clientManager.getWCClient().doDelete(file, false, true, false);
            return true;
        } else {
            return file.delete();
        }
    }

    public static String getProperty(File basedir, String fileName, String name) throws SVNException {
        File file = new File(basedir, fileName);
        System.out.println("getting property " + name + " on " + file);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        return clientManager.getWCClient().doGetProperty(file, name, SVNRevision.WORKING, SVNRevision.WORKING).getValue().getString();
    }

    public static boolean setProperty(File basedir, String fileName, String name, String value) throws SVNException {
        File file = new File(basedir, fileName);
        System.out.println("setting property " + name + '=' + value + " on " + file);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        clientManager.getWCClient().doSetProperty(file, name, SVNPropertyValue.create(value), false, SVNDepth.IMMEDIATES, null, null);
        return true;
    }

    public static boolean removeSvnFolders(File basedir) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> svnFolders = FileUtils.getDirectoryNames(basedir, "**/.svn", null, false);
        for (String svnFolder : svnFolders) {
            File file = new File(basedir, svnFolder);
            System.out.println("deleting " + file);
            FileUtils.deleteDirectory(file);
        }
        return true;
    }

    public static boolean verifyResult(File basedir, String revisionPattern, String pathPattern) throws IOException {
        File file = new File(basedir, "target/classes/revision.txt");
        System.out.println("reading file " + file);
        Properties properties = new Properties();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            properties.load(reader);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        String revision = properties.getProperty("revision");
        boolean revisionResult = revision.matches(revisionPattern);
        System.out.println("revision \"" + revision + "\" matches \"" + revisionPattern + "\" = " + revisionResult);
        String path = properties.getProperty("path");
        boolean pathResult = path.matches(pathPattern);
        System.out.println("path \"" + path + "\" matches \"" + pathPattern + "\" = " + pathResult);
        return revisionResult && pathResult;
    }


    private IntegrationTestHelper() {
    }

}