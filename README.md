# Minecraft Chat Log Filter
A Java tool to filter the chat messages logged in all Minecraft log files available using a specialized regex syntax and the simple press of a button

## How to use
1. Download the [latest release](https://github.com/doej1367/MinecraftChatLogFilter/releases) and execute the jar file
2. Enter a valid query string into the first box (more about that in the syntax section)
3. \[Optional\] Add custom minecraft directories with the right `Add custom .minecraft folder locations` button
4. Click the left `Start analyzing currently known .minecraft folders` button and wait for your results

## Syntax
The syntax is a simply RegEx syntax with a three additional rules so that it all works together with the concept of multiple chat log lines. Here are the main features:
- `(regex)` - filters all lines that match the provided regex
- `(regex1)|(regex2)` - filters all lines that match either one of the two provided regex
- `(regex1)->(regex2)` - filters all lines that match the first regex and the second regex matches one of the followup lines

These three can be combined to one big regex. One example for this would be a regex to filter all lines that either indicate the gaining of a 'Kismet Feather' or the successful S+ completion of a Floor 7 run (Hypixel SkyBlock example):

`(You bought Kismet Feather!.*)|(You claimed Kismet Feather from .* auction!)|(The Catacombs - Floor VII)->(Team Score: [0-9]+ \(S\+\).*)`

More examples can be found and posted in the [Discussions section](https://github.com/doej1367/MinecraftChatLogFilter/discussions/categories/regex-examples)

## Screenshot
![MinecraftChatSearch](screenshots/screenshot01.png)
