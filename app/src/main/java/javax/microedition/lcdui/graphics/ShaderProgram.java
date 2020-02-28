/*
 *  Copyright 2020 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.microedition.lcdui.graphics;

import android.opengl.GLU;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.util.ContextHolder;

import static android.opengl.GLES20.*;

public class ShaderProgram {
	private static final String TAG = ShaderProgram.class.getName();
	private static final String VERTEX = "shaders/simple.vsh";
	private static final String FRAGMENT = "shaders/simple.fsh";

	public int aTexCoord;
	public int uTextureUnit;
	public int aPosition;

	public ShaderProgram() {
		String vertexCode = ContextHolder.getAssetAsString(VERTEX);
		String fragmentCode = ContextHolder.getAssetAsString(FRAGMENT);
		int program = createProgram(vertexCode, fragmentCode);
		glReleaseShaderCompiler();
		if (program == -1) {
			throw new RuntimeException("Init shader program error: see log for detail");
		}
	}

	public int createProgram(String vertex, String fragment) {
		int vertexId = loadShader(GL_VERTEX_SHADER, vertex);
		int fragmentId = loadShader(GL_FRAGMENT_SHADER, fragment);

		int program = glCreateProgram();     // create empty OpenGL ShaderProgram
		glAttachShader(program, vertexId);   // add the vertex shader to program
		glAttachShader(program, fragmentId); // add the fragment shader to program

		glLinkProgram(program);              // create OpenGL program executables
		String s = glGetProgramInfoLog(program);
		if (s.length() > 0) {
			Log.e(TAG, "createProgram: " + s);
		}
		int error = glGetError();
		if (error != GL_NO_ERROR) {
			String errorString = GLU.gluErrorString(error);
			Log.e(TAG, "init program: glError " + errorString);
		}
		aPosition = glGetAttribLocation(program, "a_position");
		aTexCoord = glGetAttribLocation(program, "a_texcoord0");
		uTextureUnit = glGetUniformLocation(program, "sampler0");
		glUseProgram(program);
		int error1 = glGetError();
		if (error1 != GL_NO_ERROR) {
			String s1 = GLU.gluErrorString(error1);
			Log.e(TAG, "init program: glError " + s1);
			glDeleteShader(vertexId);
			glDeleteShader(fragmentId);
			glDeleteProgram(program);
			return -1;
		}
		return program;
	}

	private int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = glCreateShader(type);

		// add the source code to the shader and compile it
		glShaderSource(shader, shaderCode);
		glCompileShader(shader);
		String s = glGetShaderInfoLog(shader);
		if (s.length() > 0) {
			Log.e(TAG, "loadShader: " + s);
			glDeleteShader(shader);
			return -1;
		}
		return shader;
	}

	public void loadVbo(FloatBuffer vbo) {
		vbo.rewind();
		glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, 4 * 4, vbo);
		glEnableVertexAttribArray(aPosition);

		// координаты текстур
		vbo.position(2);
		glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, false, 4 * 4, vbo);
		glEnableVertexAttribArray(aTexCoord);
	}
}