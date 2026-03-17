package com.megrez.utils;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SensitiveWordFilter {

    // Trie节点
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    private final TrieNode root = new TrieNode();

    // 最大敏感词长度
    private int maxWordLength = 0;

    // 添加敏感词
    public void addWord(String word) {
        TrieNode node = root;

        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }

        node.isEnd = true;

        if (word.length() > maxWordLength) {
            maxWordLength = word.length();
        }
    }

    // 批量加载
    public void loadWords(List<String> words) {
        for (String word : words) {
            addWord(word);
        }
    }

    // 是否符号（可跳过）
    private boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c) &&
                (c < 0x2E80 || c > 0x9FFF);
    }

    // 判断是否命中敏感词
    public boolean containsSensitiveWord(String text) {

        int length = text.length();

        for (int i = 0; i < length; i++) {

            TrieNode node = root.children.get(text.charAt(i));

            // 首字符不命中直接跳过
            if (node == null) {
                continue;
            }

            if (node.isEnd) {
                return true;
            }

            int step = 1;

            for (int j = i + 1; j < length && step < maxWordLength; j++, step++) {

                char c = text.charAt(j);

                // 跳过特殊符号
                if (isSymbol(c)) {
                    continue;
                }

                node = node.children.get(c);

                if (node == null) {
                    break;
                }

                if (node.isEnd) {
                    return true;
                }
            }
        }

        return false;
    }


}