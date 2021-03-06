package com.jogamp.common.os;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import jogamp.common.os.elf.ElfHeader;
import jogamp.common.os.elf.Section;
import jogamp.common.os.elf.SectionArmAttributes;
import jogamp.common.os.elf.SectionHeader;

import org.junit.Test;

import com.jogamp.common.os.Platform.OSType;
import com.jogamp.junit.util.JunitTracer;

public class TestElfReader01 extends JunitTracer {
    public static String GNU_LINUX_SELF_EXE = "/proc/self/exe";
    public static String ARM_HF_EXE = "tst-exe-armhf";
    public static String ARM_SF_EXE = "tst-exe-arm";
            
    private static boolean checkFileReadAccess(File file) {
        try {
            return file.isFile() && file.canRead();
        } catch (Throwable t) { }
        return false;
    }    
    static File findJVMLib(String libName) {
        ClassLoader cl = TestElfReader01.class.getClassLoader();
        final List<String> possibleLibPaths = NativeLibrary.enumerateLibraryPaths(libName, libName, libName, true, cl);
        for(int i=0; i<possibleLibPaths.size(); i++) {
            final String libPath = possibleLibPaths.get(i);
            final File lib = new File(libPath);
            System.err.println("XXX2 #"+i+": test "+lib);
            if( checkFileReadAccess(lib) ) {
                return lib;
            }
            System.err.println("XXX2 #"+i+": "+lib+" not readable");
        }
        return null;
    }
    
    @Test
    public void testGNULinuxSelfExe () throws IOException {
        if( OSType.LINUX == Platform.getOSType() ) {
            File f = new File(GNU_LINUX_SELF_EXE);
            if( checkFileReadAccess(f) ) {
                testElfHeaderImpl(f, false);
            }
        }
    }
    
    @Test
    public void testJavaLib () throws IOException {
        File jvmLib = findJVMLib("java");
        if( null == jvmLib ) {
            jvmLib = findJVMLib("jvm");
        }
        if( null != jvmLib ) {
            testElfHeaderImpl(jvmLib, false);
        }
    }
    
    void testElfHeaderImpl(File file, boolean fileOutSections) throws IOException {
        System.err.println("Test file "+file.getAbsolutePath());
        RandomAccessFile in = new RandomAccessFile(file, "r");
        try {
            final ElfHeader eh;
            try {
                eh = ElfHeader.read(in);
            } catch (Exception e) {
                System.err.println("Probably not an ELF file - or not in current format: (catched) "+e.getMessage());
                e.printStackTrace();
                return;
            }
            int i=0;
            System.err.println(eh);
            System.err.println("SH entsz     "+eh.d.getE_shentsize());
            System.err.println("SH off       "+toHexString(eh.d.getE_shoff()));
            System.err.println("SH strndx    "+eh.d.getE_shstrndx());
            System.err.println("SH num "+eh.sht.length);
            if( 0 < eh.sht.length ) {
                System.err.println("SH size "+eh.sht[0].d.getBuffer().limit());
            }
            {
                SectionHeader sh = eh.getSectionHeader(SectionHeader.SHT_ARM_ATTRIBUTES);
                boolean abiVFPArgsAcceptsVFPVariant = false;
                if( null != sh ) {
                    final SectionArmAttributes sArmAttrs = (SectionArmAttributes) sh.readSection(in);
                    final SectionArmAttributes.Attribute abiVFPArgsAttr = sArmAttrs.get(SectionArmAttributes.Tag.ABI_VFP_args);
                    if( null != abiVFPArgsAttr ) {
                        abiVFPArgsAcceptsVFPVariant = SectionArmAttributes.abiVFPArgsAcceptsVFPVariant(abiVFPArgsAttr.getULEB128());
                    }
                }
                System.err.println("abiVFPArgsAcceptsVFPVariant "+abiVFPArgsAcceptsVFPVariant);
            }
            for(i=0; i<eh.sht.length; i++) {
                final SectionHeader sh = eh.sht[i];
                System.err.println(sh);
                final int type = sh.getType();
                if( SectionHeader.SHT_STRTAB == type ) {
                    dumpSection(in, sh, "SHT_STRTAB", fileOutSections);
                } else if( SectionHeader.SHT_ARM_ATTRIBUTES == type ) {
                    dumpSection(in, sh, "SHT_ARM_ATTRIBUTES", fileOutSections);
                }
            }
        } finally {
            in.close();
        }
    }
    
    static void dumpSection(RandomAccessFile in, SectionHeader sh, String name, boolean fileOut) throws IllegalArgumentException, IOException {
        final Section s = sh.readSection(in);
        if(fileOut) {
            File outFile = new File("ElfSection-"+sh.getIndex()+"-"+name);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
            try {
                out.write(s.data, s.offset, s.length);
            } finally {
                out.close();
            }
        }
        System.err.println(name+": read "+s.length+", "+s);
    }
    
    public static void main(String args[]) throws IOException {
        String tstname = TestElfReader01.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }
    
    static String toHexString(int i) { return "0x"+Integer.toHexString(i); }
    static String toHexString(long i) { return "0x"+Long.toHexString(i); }
    
}
