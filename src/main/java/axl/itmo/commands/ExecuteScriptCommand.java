package axl.itmo.commands;

import axl.itmo.utils.CommandManager;
import axl.itmo.utils.Console;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Executes another script from a file. Includes a pre-execution recursive dependency scan
 * that builds a call graph of referenced scripts and detects cycles before any execution.
 */
public class ExecuteScriptCommand extends Command {
    private final CommandManager commandManager;
    private final Console console;

    public ExecuteScriptCommand(CommandManager commandManager, Console console) {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.commandManager = commandManager;
        this.console = console;
    }

    @Override
    /** {@inheritDoc} */
    public boolean execute(String argument) {
        if (argument.isEmpty()) {
            console.printError("Command 'execute_script' requires a file name argument.");
            return false;
        }

        File file = new File(argument);
        if (!file.exists()) {
            console.printError("File not found: " + argument);
            return false;
        }

        if (detectPreExecutionRecursion(file)) {
            console.printError("Recursion detected before executing script: " + argument);
            return false;
        }

        if (commandManager.getScriptStack().contains(file.getAbsolutePath())) {
            console.printError("Recursion detected: " + argument);
            return false;
        }

        commandManager.getScriptStack().add(file.getAbsolutePath());
        try (Scanner fileScanner = new Scanner(file)) {
            Console fileConsole = new Console(fileScanner, true);
            CommandManager fileCommandManager = new CommandManager(commandManager.getCollectionManager(), fileConsole);
            fileCommandManager.getScriptStack().addAll(commandManager.getScriptStack());

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.trim().isEmpty()) continue;
                fileCommandManager.execute(line);
            }
            return true;
        } catch (FileNotFoundException e) {
            console.printError("Error reading script file: " + e.getMessage());
        } finally {
            commandManager.getScriptStack().remove(file.getAbsolutePath());
        }
        return false;
    }

    /**
     * Performs a recursive pre-scan of the target script and its transitive dependencies.
     * The method builds a call graph of execute_script inclusions and detects a cycle
     * before any execution happens. It also treats any reference to a script that is
     * already on the current execution stack as a recursion.
     *
     * @param entry the script file to analyze
     * @return true if a recursion is detected in the call graph or with the current stack
     */
    private boolean detectPreExecutionRecursion(File entry) {
        java.util.Map<String, java.util.Set<String>> graph = new java.util.HashMap<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Deque<String> stack = new java.util.ArrayDeque<>();

        String entryPath = entry.getAbsolutePath();

        if (buildGraphDFS(entryPath, visited, graph)) {
            return true;
        }

        java.util.Set<String> globalVisited = new java.util.HashSet<>();
        java.util.Set<String> recStack = new java.util.HashSet<>();

        return hasCycleDFS(entryPath, graph, globalVisited, recStack);
    }

    /**
     * Recursively collects execute_script dependencies into a directed graph and
     * immediately returns true if a dependency points to a script already present
     * on the current execution stack.
     *
     * @param nodePath absolute path of the current script
     * @param visited global set of already expanded nodes
     * @param graph adjacency list to fill
     * @return true if an immediate recursion with the current stack is detected
     */
    private boolean buildGraphDFS(String nodePath, java.util.Set<String> visited,
                                  java.util.Map<String, java.util.Set<String>> graph) {
        if (!visited.add(nodePath)) {
            return false;
        }
        java.util.Set<String> deps = parseScriptDependencies(new File(nodePath));
        graph.put(nodePath, deps);
        for (String dep : deps) {
            if (commandManager.getScriptStack().contains(dep)) {
                return true;
            }
            if (buildGraphDFS(dep, visited, graph)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects a cycle in the directed dependency graph using DFS with a recursion stack.
     *
     * @param node current node
     * @param graph adjacency list
     * @param visited global visited set
     * @param recStack nodes in the current DFS path
     * @return true if a cycle is found
     */
    private boolean hasCycleDFS(String node,
                                java.util.Map<String, java.util.Set<String>> graph,
                                java.util.Set<String> visited,
                                java.util.Set<String> recStack) {
        if (!visited.add(node)) {
            return false;
        }
        recStack.add(node);
        for (String next : graph.getOrDefault(node, java.util.Collections.emptySet())) {
            if (!visited.contains(next)) {
                if (hasCycleDFS(next, graph, visited, recStack)) {
                    return true;
                }
            } else if (recStack.contains(next)) {
                return true;
            }
        }
        recStack.remove(node);
        return false;
    }

    /**
     * Parses a script file and extracts absolute paths from lines that start with
     * the command "execute_script" followed by a path.
     *
     * @param script the script file
     * @return a set of absolute dependent script paths
     */
    private java.util.Set<String> parseScriptDependencies(File script) {
        java.util.Set<String> deps = new java.util.HashSet<>();
        try (Scanner preScan = new Scanner(script)) {
            while (preScan.hasNextLine()) {
                String line = preScan.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.toLowerCase().startsWith("execute_script")) {
                    String[] parts = line.split("\\s+", 2);
                    String arg = parts.length > 1 ? parts[1].trim() : "";
                    if (!arg.isEmpty()) {
                        File f = new File(arg);
                        String path = f.getAbsolutePath();
                        if (new File(path).exists()) {
                            deps.add(path);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
        }
        return deps;
    }
}
