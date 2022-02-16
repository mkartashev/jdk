import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * This program helps to reproduce issue
 * JDK-8280468 Crashes in getConfigColormap, getConfigVisualId, XVisualIDFromVisual on Linux
 *
 * Steps to reproduce:
 * On a Linux box with exactly two monitors running X11 (no Wayland or XWayland),
 * - build this branch (JDK-8280468-reproducer),
 * - compile this file (javac Test.java),
 * - execute it using the frashly built java,
 * - when this gets printed to stdout
 *   makeConfigurations(): waiting for the signal to continue...
 *   unplug or disable the second monitor on the system,
 * - observe the crash.
 *
 * An example of the output can be found at the bottom of the file.
 */
class Test {
    static volatile JFrame frame;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait( () -> {
            frame = new JFrame();
            frame.setSize(300, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice devices[] = ge.getScreenDevices();
        for (int i = 0; i < devices.length; i++) {
            final GraphicsDevice gd = devices[i];
            System.out.println("graphics device: " + gd);
            GraphicsConfiguration gc = gd.getConfigurations()[0];
	    System.out.println("graphics configuration: " + gc);
        }
    }
}


/* 
Output example:

$ ./build/linux-x86_64-server-fastdebug/images/jdk/bin/java Test
X11GraphicsEnvironment.initNativeData(): allocated new x11Screens[2] at 0x8377430
rebuildDevices()
ensureConfigsInited() - re-creating all configs for screen 0, x11Screens=0x8377430
graphics device: X11GraphicsDevice[screen=0]
graphics configuration: X11GraphicsConfig[dev=X11GraphicsDevice[screen=0],vis=0x21]
graphics device: X11GraphicsDevice[screen=1]
ensureConfigsInited() - re-creating all configs for screen 1, x11Screens=0x8377430
makeConfigurations(): waiting for the signal to continue...
X11GraphicsEnvironment.initNativeData(): free()d x11Screens at 0x8377430
X11GraphicsEnvironment.initNativeData(): allocated new x11Screens[1] at 0x7800c590
initDevices(): Notified X11GraphicsDevice that it can continue; waiting for 500ms
makeConfigurations(): about to call getConfigVisualId() for screen 1
initDevices(): Done waiting...
GraphicsDevice: changed screen from 1 -> 0
ensureConfigsInited() - re-creating all configs for screen 1, x11Screens=0x7800c590
rebuildDevices()
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  SIGSEGV (0xb) at pc=0x00007f85dc17a194, pid=958606, tid=958607
#
# JRE version: OpenJDK Runtime Environment (19.0) (fastdebug build 19-internal+0-adhoc.work.jdk)
# Java VM: OpenJDK 64-Bit Server VM (fastdebug 19-internal+0-adhoc.work.jdk, mixed mode, sharing, tiered, compressed oops, compressed class ptrs, g1 gc, linux-amd64)
# Problematic frame:
# C  [libX11.so.6+0x2d194]  XVisualIDFromVisual+0x4
#
# Core dump will be written. Default location: Core dumps may be processed with "/usr/share/apport/apport %p %s %c %d %P %E" (or dumping to /home/work/work/OpenJDK/jdk/core.958606)
#
# An error report file with more information is saved as:
# /home/work/work/OpenJDK/jdk/hs_err_pid958606.log
#
# If you would like to submit a bug report, please visit:
#   https://bugreport.java.com/bugreport/crash.jsp
# The crash happened outside the Java Virtual Machine in native code.
# See problematic frame for where to report the bug.
#
[1]    958606 abort (core dumped)  ./build/linux-x86_64-server-fastdebug/images/jdk/bin/java Test


Excerpt from hs_err_pid958606.log:

Current thread (0x00007f8608027d00):  JavaThread "main" [_thread_in_native, id=958607, stack(0x00007f860eb65000,0x00007f860ec66000)]

Stack: [0x00007f860eb65000,0x00007f860ec66000],  sp=0x00007f860ec64548,  free space=1021k
Native frames: (J=compiled Java code, j=interpreted, Vv=VM code, C=native code)
C  [libX11.so.6+0x2d194]  XVisualIDFromVisual+0x4
C  [libawt_xawt.so+0x34da5]  getAllConfigs+0xb65
C  [libawt_xawt.so+0x3639b]  Java_sun_awt_X11GraphicsDevice_getConfigVisualId+0x5b
j  sun.awt.X11GraphicsDevice.getConfigVisualId(II)I+0 java.desktop@19-internal
j  sun.awt.X11GraphicsDevice.makeConfigurations()V+152 java.desktop@19-internal
j  sun.awt.X11GraphicsDevice.getConfigurations()[Ljava/awt/GraphicsConfiguration;+15 java.desktop@19-internal
j  Test.main([Ljava/lang/String;)V+48
v  ~StubRoutines::call_stub
V  [libjvm.so+0xf047a4]  JavaCalls::call_helper(JavaValue*, methodHandle const&, JavaCallArguments*, JavaThread*)+0x504
V  [libjvm.so+0x102a011]  jni_invoke_static(JNIEnv_*, JavaValue*, _jobject*, JNICallType, _jmethodID*, JNI_ArgumentPusher*, JavaThread*) [clone .isra.0] [clone .constprop.1]+0x331
V  [libjvm.so+0x102d6e9]  jni_CallStaticVoidMethod+0x1c9
C  [libjli.so+0x528e]  JavaMain+0xc2e
C  [libjli.so+0x7f3d]  ThreadJavaMain+0xd

Java frames: (J=compiled Java code, j=interpreted, Vv=VM code)
j  sun.awt.X11GraphicsDevice.getConfigVisualId(II)I+0 java.desktop@19-internal
j  sun.awt.X11GraphicsDevice.makeConfigurations()V+152 java.desktop@19-internal
j  sun.awt.X11GraphicsDevice.getConfigurations()[Ljava/awt/GraphicsConfiguration;+15 java.desktop@19-internal
j  Test.main([Ljava/lang/String;)V+48
v  ~StubRoutines::call_stub

*/
