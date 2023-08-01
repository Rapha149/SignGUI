# SignGUI [![Build](https://github.com/Rapha149/SignGUI/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/Rapha149/SignGUI/actions/workflows/maven-publish.yml) [![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://rapha149.github.io/SignGUI/javadoc/)
An api to get input text via a sign in Minecraft.  
The api supports the Minecraft versions from `1.8` to `1.20.1`.

## Integration

Put the following in your `pom.xml`:
```xml
<repository>
    <id>rapha149-repo</id>
    <url>https://rapha149-robot:&#103;&#104;&#112;&#95;&#53;&#68;&#122;&#76;&#52;&#107;&#103;&#107;&#98;&#52;&#117;&#81;&#57;&#70;&#109;&#117;&#75;&#49;&#84;&#114;&#71;&#56;&#57;&#103;&#102;&#114;&#51;&#85;&#84;&#89;&#49;&#113;&#113;&#104;&#54;&#104;@maven.pkg.github.com/Rapha149/*</url>
</repository>
```
```xml
<dependency>
    <groupId>de.rapha149.signgui</groupId>
    <artifactId>signgui</artifactId>
    <version>1.9.3</version>
</dependency>
```

### Length of the repo url

I'm aware of the fact that the repository url is very long. That is because Github doesn't allow public access to packages and users have to authenticate themselves.
Therefore I included a personal access token in the url so you don't have to deal with that. And because Github automatically revokes all personal access tokens found in commits, it's encoded and even longer.

<details>
<summary>
    <h4>Alternative</h4>
</summary>

As an alternative, you can also define your personal access token in your `settings.xml` file.

**settings.xml**
```xml
<servers>
    <server>
        <id>rapha149-repo</id>
        <username>USERNAME</username>
        <!-- Public token with `read:packages` scope -->
        <password>TOKEN</password>
    </server>
</servers>
```
**pom.xml**
```xml
<repository>
    <id>rapha149-repo</id>
    <url>https://maven.pkg.github.com/Rapha149/*</url>
</repository>
```
(The server id and the repository id have to be the same)
</details>

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
