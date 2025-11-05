package org.igorgames;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class console {
    public static void main() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("stop")) {
                    System.out.println("["+infoname.NameCmd+"]: Shutting down...");
                    Main.ifisexit = true;
                    new Thread(() -> Main.exit(), "ConsoleThread").start();
                    break;
                }

                String[] parts = input.split(" ", 2);
                if (parts.length >= 2) {
                    String arg0 = parts[0];
                    String arg1 = parts[1];
                    boolean sendcommandtoserver = Main.runcommand(arg0,arg1);
                    if (!sendcommandtoserver) {
                        if (arg0.equalsIgnoreCase("makefolder")) {
                            makefolder.makefolder("servers/",arg1);
                        } else if (arg0.equalsIgnoreCase("start")) {
                            boolean startserver = Main.startserver(arg1);
                        } else {
                            System.out.println("["+infoname.NameCmd+"] usege <server name> <cmd> (or stop or makefolder <name>) ");
                        }
                    }
                } else {
                    System.out.println("["+infoname.NameCmd+"] usege <server name> <cmd> (or stop or makefolder <name>) ");
                }
            }
        }
    }
}
