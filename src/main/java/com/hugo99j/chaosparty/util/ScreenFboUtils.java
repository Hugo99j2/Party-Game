package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ScreenFboUtils {
    private static final IntBuffer INT_BUFF = ByteBuffer
        .allocateDirect(16 * Integer.BYTES).order(ByteOrder.nativeOrder())
        .asIntBuffer();

    public static int[] retrieveFboStatus() {
        int previousFBOHandle = getBoundFboHandle();
        int[] previousViewport = getViewport();

        return new int[] {previousFBOHandle, previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]};
    }

    public static void restoreFboStatus(int[] status) {
        if(status.length != 5) throw new IllegalArgumentException("Invalid argument");
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, status[0]);
        Gdx.gl20.glViewport(status[1], status[2], status[3], status[4]);
    }

    public static synchronized int getBoundFboHandle() {
        IntBuffer intBuf = INT_BUFF;
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, intBuf);
        return intBuf.get(0);
    }

    public static synchronized int[] getViewport() {
        IntBuffer intBuf = INT_BUFF;
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, intBuf);

        return new int[] { intBuf.get(0), intBuf.get(1), intBuf.get(2),
            intBuf.get(3) };
    }
}
