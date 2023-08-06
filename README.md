# SignGUI [![Maven Central](https://img.shields.io/maven-central/v/io.github.rapha149.signgui/signgui?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rapha149.signgui/signgui) [![Javadoc](https://javadoc.io/badge2/io.github.rapha149.signgui/signgui/Javadoc.svg)](https://javadoc.io/doc/io.github.rapha149.signgui/signgui) 
An api to get input text via a sign in Minecraft.  
The api supports the Minecraft versions from `1.8` to `1.20.1`.

## Integration

Maven dependency:
```xml
<dependency>
    <groupId>io.github.rapha149.signgui</groupId>
    <artifactId>signgui</artifactId>
    <version>2.2</version>
</dependency>
```

## Usage
To open a sign editor gui for a player, do the following:
```java
SignGUI gui = SignGUI.builder()
    // set lines
    .setLines("§6Line 1", null, "§6Line 3")

    // set specific line, starting index is 0
    .setLine(3, "Line 4")

    // set the sign type
    .setType(Material.DARK_OAK_SIGN)

    // set the sign color
    .setColor(DyeColor.YELLOW)

    // set the handler/listener (called when the player finishes editing)
    .setHandler((p, result) -> {
        // get a speficic line, starting index is 0
        String line0 = result.getLine(0);

        // get a specific line without color codes
        String line1 = result.getLineWithoutColor(1);

        // get all lines
        String[] lines = result.getLines();

        // get all lines without color codes
        String[] linesWithoutColor = result.getLinesWithoutColor();

        if (line1.isEmpty()) {
            // The user has not entered anything on line 2, so we open the sign again
            return List.of(SignGUIAction.displayNewLines("§6Line 1", null, "§6Line 3", "Line 4"));
        }

        if (line1.equals("inv")) {
            // close the sign and open an inventory
            return List.of(
                // "this" = your JavaPlugin instance
                SignGUIAction.openInventory(this, Bukkit.createInventory(player, 27)),
                SignGUIAction.run(() -> player.sendMessage("Inventory opened!"))
            );
        }

        // Just close the sign by not returning any actions
        return Collections.emptyList();
    })

    // build the SignGUI
    .build();

// open the sign
gui.open(player);

// you can also open the sign for multiple players
gui.open(player2);
```

You don't have to call all methods. Only `setHandler` is mandatory.

By default, the handler is called by an asynchronous thread. You can change that behaviour by calling the method `callHandlerSynchronously` of the builder.
An explanation for the different methods can be found on the [Javadoc](https://javadoc.io/doc/io.github.rapha149.signgui/signgui).

## Limitations

### The location of the sign
Especially in newer versions, I encountered the problem that the sign had to be near the player to edit it.  
In older versions it worked by placing the sign at the bottom of the world but that does not seem to be the case anymore, in my tests anyway.

Because of that the default location is three blocks behind the player (three blocks in the opposite view direction).
The only problem with this is that if you tell the api to redisplay the sign if the player e.g. typed something wrong, the player could slowly turn around and then see the sign.
You are, naturally, free to experiment with the location of the sign yourself.

Of course the sign is not really placed, it's just send to the player, so other players won't see it.

### Opening a sign after a player joins
Since the sign is not actually placed on the server, it can get overwritten when the chunks are sent to the player, which is the case when the player joins.

Because of that you may encounter problems when you send a sign to the player directly when he joins.
But even with 20 ticks (1 second) delay from the PlayerJoinEvent, the sign was empty sometimes.  
I would recommend waiting at least a few more seconds before opening the gui. 

## Credits
This project's structure was inspired by WesJD's [AnvilGUI](https://github.com/WesJD/AnvilGUI) and I used some code from Cleymax's [SignGUI](https://github.com/Cleymax/SignGUI).
