# SignGUI [![](https://jitpack.io/v/Rapha149/SignGUI.svg)](https://jitpack.io/#Rapha149/SignGUI)
An api to get input text via a sign in Minecraft.  
The api supports the Minecraft versions from `1.8` to `1.18.2`.

## Integration

Put the following in your `pom.xml`:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.Rapha149.SignGUI</groupId>
    <artifactId>signgui</artifactId>
    <version>v1.7.1</version>
</dependency>
```

### [Javadoc](https://javadoc.jitpack.io/com/github/Rapha149/SignGUI/signgui/v1.7/javadoc/de/rapha149/signgui/package-summary.html)  

## Usage
To open a sign editor gui for a player, do the following:
```java
new SignGUI()
    .lines("§6Line 1", null, "§6Line 3")
    .line(3, "Line 4")
    .type(Material.DARK_OAK_SIGN)
    .color(DyeColor.YELLOW)
    .stripColor()
    .onFinish((p, lines) -> {
        if (!lines[1].isEmpty() && !lines[3].isEmpty()) {
            player.sendMessage("Line 2: " + lines[1] + "\nLine 4:" + lines[3]);
            return null;
        } else
            // Due to stripColor the sign won't display line 1 and 3 in orange after it has been closed once.
            return lines;
    }).open(player);
```
You don't have to call all methods. Only `onFinish` and `open` are mandatory.  
Here is the explanation of the different methods:

#### `lines(String... lines)`
Sets the lines to show when the sign is opened. You don't have to pass 4 strings. The default is 4 empty lines. You can pass `null` for an empty line.

#### `line(int index, String line)`
Sets the line at the specific index. The index has to be between 0 and 3. You can pass `null` for an empty line.

#### `type(Material type)`
Sets the type of the sign. The default is OAK_SIGN.

#### `color(DyeColor color)`
Sets the color of the text. The default is BLACK. You can also use color codes to color your text. The cursor will be always in the given color, however. The returned lines will not be colored in this color.

#### `stripColor()`
Executes `stripColor(true)`

#### `stripColor(true)`
If enabled, the returned lines will not have any colors. Colors stated by the plugin and by players will be stripped (Players can use color codes by pasting a `§`)

#### `signLocation(Location)`
Sets the location of the sign. Default is the player's coordinates, but `y = 1`.

#### `onFinish(Function<String[], String[]>`
Same as `onFinish(BiFunction<Player, String[], String[]>` but without the player.  
Overrides `onFinish(BiFunction<Player, String[], String[]>`

#### `onFinish(BiFunction<Player, String[], String[]>`
Sets the function which will be executed when the player finishes editing. You can return `null` or new lines. If you return new lines, the sign editor will be opened with these lines again.  
Overrides `onFinish(Function<String[], String[]>`  
Please note that due to packet listening the function will be executed asynchronously. If you want to execute synchronous actions such as inventory handling or block placing, you have to do that in a Bukkit task.

#### `open(Player player)`
Opens the sign editor for the player. You can call this method multiple times.

## Credits
This project structure was inspired by WesJD's [AnvilGUI](https://github.com/WesJD/AnvilGUI) and I used some code from Cleymax's [SignGUI](https://github.com/Cleymax/SignGUI).
