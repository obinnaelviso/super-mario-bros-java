package com.joytekmotion.jade;

import com.joytekmotion.util.Time;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final int width, height;
    private long glfwWindow;
    private static Window window = null;
    public float r, g, b, a;

    private static Scene currentScene;

    private Window() {
        this.width = 1366;
        this.height = 768;
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.a = 1f;
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static void changeScene(int newSceneIndex) {
        switch (newSceneIndex) {
            case 0 -> {
                currentScene = new LevelEditorScene();
                currentScene.init();
            }
            case 1 -> {
                currentScene = new LevelScene();
                currentScene.init();
            }
            default -> {
                assert false : "Unknown scene '" + newSceneIndex + "'";
            }
        }

    }

    public void run() {
        System.out.println("Running..." + Version.getVersion() + "!");

        init();
        loop();

//        Free the memory
        glfwFreeCallbacks(this.glfwWindow);
        glfwDestroyWindow(this.glfwWindow);

//        Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        // Set up an error callback
        GLFWErrorCallback.createPrint(System.err).set();

//       Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }


        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
//        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

//        Create the window
        String title = "Super Mario";
        this.glfwWindow = glfwCreateWindow(this.width, this.height, title, NULL, NULL);
        if (this.glfwWindow == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

//        All windows functions here

        glfwSetCursorPosCallback(this.glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(this.glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(this.glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(this.glfwWindow, KeyListener::keyCallback);

//        Make the OpenGL context current
        glfwMakeContextCurrent(this.glfwWindow);

//        Enable v-sync
        glfwSwapInterval(1);

//        Make the window visible
        glfwShowWindow(this.glfwWindow);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        Window.changeScene(0);
    }

    private void loop() {
        float beginTime = Time.getTime();
        float endTime;
        float dt = -1.0f;
        while (!glfwWindowShouldClose(this.glfwWindow)) {
            glfwPollEvents();
            glClearColor(this.r, this.g, this.b, this.a);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                currentScene.update(dt);
            }

            glfwSwapBuffers(this.glfwWindow);

            endTime = Time.getTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

}
