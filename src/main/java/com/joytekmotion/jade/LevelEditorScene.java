package com.joytekmotion.jade;

import com.joytekmotion.renderer.Shader;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {
    private String vertexShaderSrc =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1)  in vec4 aColor;\n" +
                    "out vec4 fColor;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    fColor = aColor;\n" +
                    "    gl_Position = vec4(aPos, 1.0);\n" +
                    "}\n";
    private String fragmentShaderSrc =
            "#version 330 core\n" +
                    "\n" +
                    "in vec4 fColor;\n" +
                    "out vec4 color;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    color = fColor;\n" +
                    "}";

    private float[] vertexArray = {
//      position                    color
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, //Bottom left - 0
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, //Top left - 1
            0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, //Top right - 2
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //Bottom left - 3
    };
    //    IMPORTANT: MUST BE IN COUNTER-CLOCKWISE ORDER
    private int[] elementArray = {
            /*
            *    x     x
            *
            *    x     x
            */
            2, 1, 0, // Top right triangle
            0, 1, 3 // Bottom left triangle
    };
    private int vertexID, fragmentID, shaderProgram;

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {
        Shader testShader = new Shader("assets/shaders/default.glsl");
    }

    @Override
    public void init() {
//        Compile and link the shaders
//        1: Load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);

//        2: Pass the shader source to the GPU
        glShaderSource(vertexID, vertexShaderSrc);
        glCompileShader(vertexID);

//        3: Check for errors in compilation process
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int length = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + "LevelEditorScene" + "'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, length));
            assert false : "";
        }


//        Compile and link the fragment shaders
//        1: Load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

//        2: Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentShaderSrc);
        glCompileShader(fragmentID);

//        3: Check for errors in compilation process
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int length = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + "LevelEditorScene" + "'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, length));
            assert false : "";
        }

//        Link shaders and track for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

//        Check for linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int length = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + "LevelEditorScene" + "'\n\tLinking shaders error.");
            System.out.println(glGetProgramInfoLog(shaderProgram, length));
            assert false : "";
        }

//      Generate VAO, VBO, and EBO buffer objects, and send to GPU
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

//      Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

//      Create VBO upload the vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

//      Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

//      Add the vertex attribute pointers
        int positionsSize = 3;
        int colorSize = 4;
        int floatBytes = 4;
        int vertexSizeBytes = (positionsSize + colorSize) * floatBytes;

//        Position
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

//        Color
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
//        Bind shader program
        glUseProgram(shaderProgram);
//      Bind the VAO that we're using
        glBindVertexArray(vaoID);

//        Enable the vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

//        Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);
        glUseProgram(0);
    }
}
