// Problem:    Design In-Memory File System
// Link:       https://leetcode.com/problems/design-in-memory-file-system/
// Difficulty: Hard
// Tags:       design, trie, tree-map, string
// Time:       O(p log c) path walk for p path parts, c children per dir; ls adds O(k) for k entries; add/read add O(content)
// Space:      O(total path parts + total content)

import java.util.*;

class FileSystem {
    private static class Node {
        final String name;
        boolean isFile;
        final StringBuilder content = new StringBuilder();
        final TreeMap<String, Node> children = new TreeMap<>();
        Node(String name) { this.name = name; }
    }

    private final Node root = new Node("");

    public List<String> ls(String path) {
        Node node = walk(path, false);
        return node.isFile ? List.of(node.name) : new ArrayList<>(node.children.keySet());
    }

    public void mkdir(String path) { walk(path, true); }

    public void addContentToFile(String filePath, String content) {
        Node node = walk(filePath, true);
        node.isFile = true;
        node.content.append(content);
    }

    public String readContentFromFile(String filePath) {
        return walk(filePath, false).content.toString();
    }

    // Inputs are guaranteed valid, so a non-creating walk never hits a missing node.
    private Node walk(String path, boolean create) {
        Node node = root;
        for (String part : path.split("/")) {
            if (part.isEmpty()) continue;          // leading "" from "/a/b", and all of "/"
            node = create ? node.children.computeIfAbsent(part, Node::new)
                          : node.children.get(part);
        }
        return node;
    }
}

class Solution {

    // Local driver: LeetCode example, plus listing a file path and appending to a file.
    // Expected: [] [a] hello | [d] [c, x] | helloworld
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        System.out.print(fs.ls("/") + " ");
        fs.mkdir("/a/b/c");
        fs.addContentToFile("/a/b/c/d", "hello");
        System.out.print(fs.ls("/") + " ");
        System.out.print(fs.readContentFromFile("/a/b/c/d") + " | ");

        fs.mkdir("/a/b/x");
        System.out.print(fs.ls("/a/b/c/d") + " ");
        System.out.print(fs.ls("/a/b") + " | ");

        fs.addContentToFile("/a/b/c/d", "world");
        System.out.println(fs.readContentFromFile("/a/b/c/d"));
    }
}
