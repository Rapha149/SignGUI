# SignGUI [![Build](https://github.com/Rapha149/SignGUI/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/Rapha149/SignGUI/actions/workflows/maven-publish.yml) [![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://rapha149.github.io/SignGUI/javadoc/)
An api to get input text via a sign in Minecraft.  
The api supports the Minecraft versions from `1.8` to `1.20.1`.

## Integration

Put the following in your `pom.xml`:
```xml
<repository>
    <id>rapha149-repo</id>
    <url>https://rapha149-robot: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@maven.pkg.github.com/Rapha149/*</url>
</repository>
```
```xml
<dependency>
    <groupId>de.rapha149.signgui</groupId>
    <artifactId>signgui</artifactId>
    <version>1.9.3</version>
</dependency>
```

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
An explanation for the different methods can be found on the [Javadoc](https://rapha149.github.io/SignGUI/javadoc/)

## Credits
This project structure was inspired by WesJD's [AnvilGUI](https://github.com/WesJD/AnvilGUI) and I used some code from Cleymax's [SignGUI](https://github.com/Cleymax/SignGUI).
