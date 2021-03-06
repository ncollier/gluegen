/**
 * Copyright 2011 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package com.jogamp.common.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import com.jogamp.junit.util.JunitTracer;

public class TestRunnableTask01 extends JunitTracer {

    @Test
    public void testInvokeAndWait00() throws IOException, InterruptedException, InvocationTargetException {
        final Object syncObject = new Object();
        final boolean[] done = {false};
        final Runnable clientAction = new Runnable() {
          public void run() {
            synchronized(syncObject) {
                System.err.println("CA.1: "+syncObject);
                done[ 0 ] = true;
                System.err.println("CA.X");
                syncObject.notifyAll();
            }
          }
        };
        
        System.err.println("BB.0: "+syncObject);
        synchronized (syncObject) {
            System.err.println("BB.1: "+syncObject);
            new Thread(clientAction, Thread.currentThread().getName()+"-clientAction").start();
            try {
                System.err.println("BB.2");                
                syncObject.wait();
                System.err.println("BB.3");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }                    
            Assert.assertTrue(done[0]);
            System.err.println("BB.X");            
        }
    }
    
    @Test
    public void testInvokeAndWait01() throws IOException, InterruptedException, InvocationTargetException {
        final boolean[] done = {false};
        final Runnable clientAction = new Runnable() {
          public void run() {
            System.err.println("CA.1");
            done[ 0 ] = true;
            System.err.println("CA.X");
          }
        };
        
        final RunnableTask rTask = new RunnableTask(clientAction, new Object(), false, null);                    
        System.err.println("BB.0: "+rTask.getSyncObject());
        synchronized (rTask.getSyncObject()) {
            System.err.println("BB.1: "+rTask.getSyncObject());
            new Thread(rTask, Thread.currentThread().getName()+"-clientAction").start();                    
            try {
                System.err.println("BB.2");
                rTask.getSyncObject().wait();
                System.err.println("BB.3");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }                    
            Assert.assertTrue(done[0]);
            System.err.println("BB.X");
        }        
    }

    public static void main(String args[]) throws IOException {
        String tstname = TestRunnableTask01.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }

}
