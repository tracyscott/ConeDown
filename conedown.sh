#!/bin/bash
/home/pi/jdk1.8.0_211/bin/java -Djna.nosys=true -Djava.ext.dirs=/usr/local/lib/processing-3.4/java/lib/ext -Duser.dir=/home/pi/conedown -Djava.library.path=/usr/local/lib/processing-3.4/core/library:/usr/java/packages/lib/arm:/lib:/usr/lib -cp /usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-amd64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-aarch64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-i586.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-armv6hf.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-macosx-universal.jar:/usr/local/lib/processing-3.4/core/library/jogl-all.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-i586.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-windows-i586.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-windows-amd64.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-windows-i586.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-amd64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-armv6hf.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-macosx-universal.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-windows-amd64.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-aarch64.jar:/home/pi/conedown/library/core-3.4.1.jar:/usr/local/lib/processing-3.4/java/lib/rt.jar:/usr/local/lib/processing-3.4/lib/ant-launcher.jar:/usr/local/lib/processing-3.4/lib/ant.jar:/usr/local/lib/processing-3.4/lib/jna-platform.jar:/usr/local/lib/processing-3.4/lib/jna.jar:/usr/local/lib/processing-3.4/lib/pde.jar:/usr/local/lib/processing-3.4/core/library/core.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-aarch64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-amd64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-armv6hf.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-linux-i586.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-macosx-universal.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-windows-amd64.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt-natives-windows-i586.jar:/usr/local/lib/processing-3.4/core/library/gluegen-rt.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-aarch64.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-amd64.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-armv6hf.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-linux-i586.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-macosx-universal.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-windows-amd64.jar:/usr/local/lib/processing-3.4/core/library/jogl-all-natives-windows-i586.jar:/usr/local/lib/processing-3.4/core/library/jogl-all.jar:/usr/local/lib/processing-3.4/modes/java/mode/JavaMode.jar:/usr/local/lib/processing-3.4/modes/java/mode/antlr.jar:/usr/local/lib/processing-3.4/modes/java/mode/classpath-explorer-1.0.jar:/usr/local/lib/processing-3.4/modes/java/mode/com.ibm.icu.jar:/usr/local/lib/processing-3.4/modes/java/mode/jdi.jar:/usr/local/lib/processing-3.4/modes/java/mode/jdimodel.jar:/usr/local/lib/processing-3.4/modes/java/mode/jdtCompilerAdapter.jar:/usr/local/lib/processing-3.4/modes/java/mode/jsoup-1.7.1.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.core.contenttype.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.core.jobs.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.core.resources.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.core.runtime.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.equinox.common.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.equinox.preferences.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.jdt.core.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.osgi.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.eclipse.text.jar:/usr/local/lib/processing-3.4/modes/java/mode/org.netbeans.swing.outline.jar:conedown-1.0.0.jar:P3LX-0.1.3-SNAPSHOT.jar:LXStudio-0.1.3-SNAPSHOT.jar:guava-26.0-jre.jar:rtree-0.8.7.jar:sjmply-1.0.1-ALPHA.jar art.lookingup.ConeDown >& /tmp/cd.out
