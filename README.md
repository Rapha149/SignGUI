# SignGUI
An api to get input text via a sign in Minecraft.  
Currently the api only supports 1.17, support for lower versions will come soon.

## Integration
Coming soon

## Usage
To open a sign editor gui for a player, do the following:
```java
new SignGUI()
    .lines("Line 1", null, "Line 3")
    .line(3, "Line 4")
    .type(Material.DARK_OAK_SIGN)
    .color(DyeColor.YELLOW)
    .onFinish((p, lines) -> {
        if (!lines[1].isEmpty() && !lines[3].isEmpty()) {
            player.sendMessage("Line 2: " + lines[1] + "\nLine 4:" + lines[3]);
            return null;
        } else
            return lines;
    }).open(player);
```
Here is the explanation of the different methods:

#### `lines(String... lines)`
Sets the lines to show when the sign is opened. You don't have to pass 4 strings. The default is 4 empty lines.

#### `line(int index, String line)`
Sets the line at the specific index. The index has to be between 0 and 3.

#### `type(Material type)`
Sets the type of the sign. The default is OAK_SIGN.

#### `color(DyeColor color`
Sets the color of the text. The default is BLACK.

#### `onFinish(BiFunction<Player, String[], String[]> function`
Sets the function which will be executed when the player finishes editing. You can return `null` or new lines. If you return new lines, the sign editor will be opened with these lines again.

#### `open(Player player)`
Opens the sign editor for the player. You can call this method multiple times.
