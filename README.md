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
    <version>2.0</version>
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
        <username>rapha149-repo</username>
        <!-- Public token with `read:packages` scope -->
        <password>&#103;&#104;&#112;&#95;&#53;&#68;&#122;&#76;&#52;&#107;&#103;&#107;&#98;&#52;&#117;&#81;&#57;&#70;&#109;&#117;&#75;&#49;&#84;&#114;&#71;&#56;&#57;&#103;&#102;&#114;&#51;&#85;&#84;&#89;&#49;&#113;&#113;&#104;&#54;&#104;</password>
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
SignGUI.builder()
    .setLines("§6Line 1", null, "§6Line 3") // set lines
    .setLine(3, "Line 4") // set specific line, starting index is 0
    .setType(Material.DARK_OAK_SIGN) // set the sign type
    .setColor(DyeColor.YELLOW) // set the sign color
    .setHandler((p, result) -> { // set the handler/listener (called when the player finishes editing)
        String line0 = result.getLine(0); // get a speficic line, starting index is 0
        String line1 = result.getLineWithoutColor1); // get a specific line without color codes
        String[] lines = result.getLines(); // get all lines
        String[] linesWithoutColor = result.getLinesWithoutColor(); // get all lines without color codes

        if (line1.isEmpty() {
            // The user has not entered anything on line 2, so we open the sign again
            return List.of(SignGUIAction.displayNewLines("§6Line 1", null, "§6Line 3", "Line 4"));
        }

        if (line1.equals("inv")) {
            // close the sign and open an inventory
            return List.of(
                SignGUIAction.openInventory(Bukkit.createInventory(player, 27)),
                SignGUIAction.run(() -> player.sendMessage("Inventory opened!"))
            );
        }

        // Just close the sign by not returning any actions
        return Collections.emptylist();
    }).build().open(player);
```

You don't have to call all methods. Only `setHandler` is mandatory.  
By default, the handler is called by an asynchronous thread. You can change that behaviour by calling the method `callHandlerSynchronously` of the builder.
An explanation for the different methods can be found on the [Javadoc](https://rapha149.github.io/SignGUI/javadoc/)

## Credits
This project structure was inspired by WesJD's [AnvilGUI](https://github.com/WesJD/AnvilGUI) and I used some code from Cleymax's [SignGUI](https://github.com/Cleymax/SignGUI).
