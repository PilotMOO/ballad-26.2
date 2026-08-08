package mod.pilot.birch_n_bees.data;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class InputReader {
    public static boolean leftShift(){
        return keyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
    }
    public static boolean leftControl(){
        return keyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
    }

    public static boolean keyDown(int glfw){
        return  InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), glfw);
    }
}