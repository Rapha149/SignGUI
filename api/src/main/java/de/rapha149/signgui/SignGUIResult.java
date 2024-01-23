package de.rapha149.signgui;

import org.bukkit.ChatColor;

/**
 * The result of the sign editing.
 */
public class SignGUIResult {

    private final String[] lines;

    SignGUIResult(String[] lines) {
        this.lines = lines;
    }

    /**
     * @return The lines of the sign when the player finished editing.
     */
    public String[] getLines() {
        return lines;
    }

    /**
     * Used for getting a specific line of the sign.
     * @param index The index of the desired line.
     * @return The line at the given index.
     */
    public String getLine(int index) {
        return lines[index];
    }

    /**
     * Used for getting the lines of the sign without color codes.
     * Calls {@link org.bukkit.ChatColor#stripColor(String)} on each line.
     * This method only strips color codes with § (not with {@literal &} for example).
     *
     * @return The lines of the sign without color codes.
     */
    public String[] getLinesWithoutColor() {
        String[] linesWithoutColor = new String[lines.length];
        for (int i = 0; i < lines.length; i++)
            linesWithoutColor[i] = ChatColor.stripColor(lines[i]);
        return linesWithoutColor;
    }

    /**
     * Used for getting a specific line of the sign without color codes.
     * Calls {@link org.bukkit.ChatColor#stripColor(String)} on the line.
     * This method only strips color codes with § (not with {@literal &} for example).
     *
     * @param index The index of the desired line.
     * @return The line at the given index without color codes.
     */
    public String getLineWithoutColor(int index) {
        return ChatColor.stripColor(lines[index]);
    }
}
