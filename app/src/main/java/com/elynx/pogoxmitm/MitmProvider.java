package com.elynx.pogoxmitm;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class MitmProvider {
    // TODO make protected
    public static final ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETON);

    /**
     * Processes single package going from client to server
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public static ByteBuffer processOutboundPackage(ByteBuffer roData) {
        roData.rewind();

        if (BuildConfig.DEBUG) {
            XposedBridge.log("Processing outbound package of size " + Integer.toString(roData.remaining()));
        }

        try {
            //byte[] buffer = new byte[roData.remaining()];
            //roData.get(buffer);

            synchronized (scriptingContainer) {
                scriptingContainer.runScriptlet("puts 'Request'");
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        return null;
    }

    /**
     * Processes single package going from server to client
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public static ByteBuffer processInboundPackage(ByteBuffer roData) {
        roData.rewind();

        if (BuildConfig.DEBUG) {
            XposedBridge.log("Processing inbound package of size " + Integer.toString(roData.remaining()));
        }

        try {
            //byte[] buffer = new byte[roData.remaining()];
            //roData.get(buffer);

            synchronized (scriptingContainer) {
                scriptingContainer.runScriptlet("puts 'Response'");
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        return null;
    }
}
