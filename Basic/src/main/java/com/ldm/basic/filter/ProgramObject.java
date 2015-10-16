package com.ldm.basic.filter;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.utils.Log;

import android.annotation.SuppressLint;
import android.opengl.GLES20;

/**
 * Created by wangyang on 15/7/27.
 */


public class ProgramObject {

    private int mProgramID;
    private ShaderObject mVertexShader, mFragmentShader;

    //单独初始化之后可以进行一些 attribute location 的绑定操作
    //之后再进行init
    public ProgramObject() {
        mProgramID = GLES20.glCreateProgram();
    }

    public ProgramObject(final String vsh, final String fsh) {
        init(vsh, fsh);
    }

    public int programID() {
        return mProgramID;
    }

    public final void release() {
        if(mProgramID != 0)
        {
            GLES20.glDeleteProgram(mProgramID);
            mProgramID = 0;
        }
    }

    public boolean init(final String vsh, final String fsh) {
        return init(vsh, fsh, 0);
    }

    @SuppressLint("Assert")
	public boolean init(final String vsh, final String fsh, int programID) {
        if(programID == 0)
            programID = GLES20.glCreateProgram();

        if (BasicApplication.IS_DEBUG) {
        	assert programID != 0 : "glCreateProgram failed!";
		}

        if(mVertexShader != null)
            mVertexShader.release();
        if(mFragmentShader != null)
            mFragmentShader.release();

        mVertexShader = new ShaderObject(vsh, GLES20.GL_VERTEX_SHADER);
        mFragmentShader = new ShaderObject(fsh, GLES20.GL_FRAGMENT_SHADER);

        GLES20.glAttachShader(programID, mVertexShader.shaderID());
        GLES20.glAttachShader(programID, mFragmentShader.shaderID());
        GLES20.glLinkProgram(programID);

        int[] programStatus = {0};
        GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, programStatus, 0);

        //link 完毕之后即可释放 shader object
        mVertexShader.release();
        mFragmentShader.release();
        mVertexShader = null;
        mFragmentShader = null;

        if(programStatus[0] != GLES20.GL_TRUE) {
            String msg = GLES20.glGetProgramInfoLog(programID);
            Log.e(msg);
            return false;
        }

        mProgramID = programID;
        return true;
    }

    public void bind() {
        GLES20.glUseProgram(mProgramID);
    }

    public int getUniformLoc(final String name) {
        int uniform = GLES20.glGetUniformLocation(mProgramID, name);
        if(BasicApplication.IS_DEBUG) {
            if(uniform < 0)
                Log.e(String.format("uniform name %s does not exist", name));
        }
        return uniform;
    }

    public void sendUniformf(final String name, float x) {
        GLES20.glUniform1f(getUniformLoc(name), x);
    }

    public void sendUniformf(final String name, float x, float y) {
        GLES20.glUniform2f(getUniformLoc(name), x, y);
    }

    public void sendUniformf(final String name, float x, float y, float z) {
        GLES20.glUniform3f(getUniformLoc(name), x, y, z);
    }

    public void sendUniformf(final String name, float x, float y, float z, float w) {
        GLES20.glUniform4f(getUniformLoc(name), x, y, z, w);
    }

    public void sendUniformi(final String name, int x) {
        GLES20.glUniform1i(getUniformLoc(name), x);
    }

    public void sendUniformi(final String name, int x, int y) {
        GLES20.glUniform2i(getUniformLoc(name), x, y);
    }

    public void sendUniformi(final String name, int x, int y, int z) {
        GLES20.glUniform3i(getUniformLoc(name), x, y, z);
    }

    public void sendUniformi(final String name, int x, int y, int z, int w) {
        GLES20.glUniform4i(getUniformLoc(name), x, y, z, w);
    }

    public void sendUniformMat2(final String name, int count, boolean transpose, float[] matrix) {
        GLES20.glUniformMatrix2fv(getUniformLoc(name), count, transpose, matrix, 0);
    }

    public void sendUniformMat3(final String name, int count, boolean transpose, float[] matrix) {
        GLES20.glUniformMatrix3fv(getUniformLoc(name), count, transpose, matrix, 0);
    }

    public void sendUniformMat4(final String name, int count, boolean transpose, float[] matrix) {
        GLES20.glUniformMatrix4fv(getUniformLoc(name), count, transpose, matrix, 0);
    }

    public int attributeLocation(final String name) {
        return GLES20.glGetAttribLocation(mProgramID, name);
    }

    public void bindAttribLocation(final String name, int index) {
        GLES20.glBindAttribLocation(mProgramID, index, name);
    }

    /**
     * Created by wangyang on 15/7/18.
     */
    public static class ShaderObject {

        private int mShaderID;


        public int shaderID() {
            return mShaderID;
        }

        public ShaderObject() {
            mShaderID = 0;
        }

        public ShaderObject(final String shaderCode, final int shaderType) {
            init(shaderCode, shaderType);
        }

        @SuppressLint("Assert")
		public boolean init(final String shaderCode, final int shaderType) {
            mShaderID = loadShader(shaderType, shaderCode);

            if (BasicApplication.IS_DEBUG) {
            	assert mShaderID != 0 : "Shader Create Failed!";
			}

            if(mShaderID == 0) {
                Log.e("glCreateShader Failed!...");
                return false;
            }

            return true;
        }

        public final void release() {
            if(mShaderID == 0)
                return;
            GLES20.glDeleteShader(mShaderID);
            mShaderID = 0;
        }

        public static int loadShader(int type, final String code) {
            int shaderID = GLES20.glCreateShader(type);

            if(shaderID != 0) {
                GLES20.glShaderSource(shaderID, code);
                GLES20.glCompileShader(shaderID);
                int[] compiled = {0};
                GLES20.glGetShaderiv(shaderID, GLES20.GL_COMPILE_STATUS, compiled, 0);
                if(compiled[0] != GLES20.GL_TRUE)
                {
                    String errMsg = GLES20.glGetShaderInfoLog(shaderID);
                    Log.e(errMsg);
                    GLES20.glDeleteShader(shaderID);
                    return 0;
                }
            }
            return shaderID;
        }

    }
}
