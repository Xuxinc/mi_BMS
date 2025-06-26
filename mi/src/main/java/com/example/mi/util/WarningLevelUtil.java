package com.example.mi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarningLevelUtil {

    // 缓存结构：规则字符串 -> 已解析规则列表
    private static final Map<String, List<ParsedRule>> ruleCache = new ConcurrentHashMap<>();

    /**
     * 解析规则字符串并计算报警等级
     *
     * @param signal     JSON字符串，如 {"Mx": 3.5, "Mi": 2.1, "Ix": 4.2, "Ii": 1.2}
     * @param ruleString 规则字符串，从数据库中读取
     * @return 报警等级，-1 表示不报警
     */
    public static Integer calculateWarningLevel(String signal, String ruleString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode signalJson = objectMapper.readTree(signal);

        float Mx = signalJson.has("Mx") ? signalJson.get("Mx").floatValue() : 0.0f;
        float Mi = signalJson.has("Mi") ? signalJson.get("Mi").floatValue() : 0.0f;
        float Ix = signalJson.has("Ix") ? signalJson.get("Ix").floatValue() : 0.0f;
        float Ii = signalJson.has("Ii") ? signalJson.get("Ii").floatValue() : 0.0f;

        float voltageDiff = Mx - Mi;
        float currentDiff = Ix - Ii;

        // 替换中文括号
        ruleString = ruleString.replaceAll("（", "(").replaceAll("）", ")");

        // 获取缓存或解析规则
        List<ParsedRule> rules = ruleCache.computeIfAbsent(ruleString, WarningLevelUtil::parseRules);

        for (ParsedRule rule : rules) {
            boolean isMatch = evaluateCondition(rule.condition, voltageDiff, currentDiff);
            if (isMatch) {
                return rule.level;
            }
        }

        return -1; // 所有规则不匹配
    }

    /**
     * 解析规则字符串为结构化规则对象
     */
    private static List<ParsedRule> parseRules(String ruleString) {
        List<ParsedRule> parsedRules = new ArrayList<>();
        String[] rules = ruleString.split(";");

        for (String rule : rules) {
            rule = rule.trim();
            if (rule.isEmpty()) continue;

            String[] parts = rule.split(",");
            if (parts.length != 2) continue;

            String condition = parts[0].trim();
            String levelStr = parts[1].trim();

            int level = -1;
            if (!levelStr.contains("不报警")) {
                Matcher matcher = Pattern.compile("报警等级[:：]\\s*(\\d+)").matcher(levelStr);
                if (matcher.find()) {
                    level = Integer.parseInt(matcher.group(1));
                }
            }

            parsedRules.add(new ParsedRule(condition, level));
        }

        return parsedRules;
    }

    /**
     * 动态解析表达式并计算是否匹配
     */
    private static boolean evaluateCondition(String expression, float voltageDiff, float currentDiff) {
        float value;

        if (expression.contains("Mx-Mi")) {
            value = voltageDiff;
            expression = expression.replace("(Mx-Mi)", "x");
        } else if (expression.contains("Ix-Ii")) {
            value = currentDiff;
            expression = expression.replace("(Ix-Ii)", "x");
        } else {
            return false;
        }

        // 替换中文符号
        expression = expression.replace("≤", "<=").replace("＜", "<").replace("＞", ">");

        try {
            Pattern pattern = Pattern.compile("([0-9.]+)?\\s*(<=|>=|<|>)\\s*x\\s*(<=|>=|<|>)?\\s*([0-9.]+)?");
            Matcher matcher = pattern.matcher(expression);

            if (matcher.matches()) {
                boolean result = true;

                String leftValue = matcher.group(1);
                String leftOp = matcher.group(2);
                if (leftValue != null && leftOp != null) {
                    float bound = Float.parseFloat(leftValue);
                    result = result && compare(value, flipOperator(leftOp), bound);
                }

                String rightOp = matcher.group(3);
                String rightValue = matcher.group(4);
                if (rightOp != null && rightValue != null) {
                    float bound = Float.parseFloat(rightValue);
                    result = result && compare(value, rightOp, bound);
                }

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean compare(float val, String operator, float bound) {
        return switch (operator) {
            case "<" -> val < bound;
            case "<=" -> val <= bound;
            case ">" -> val > bound;
            case ">=" -> val >= bound;
            case "==" -> val == bound;
            default -> false;
        };
    }

    private static String flipOperator(String op) {
        return switch (op) {
            case "<" -> ">";
            case "<=" -> ">=";
            case ">" -> "<";
            case ">=" -> "<=";
            default -> op;
        };
    }

    // 内部类，用于缓存规则的结构化表达
    private static class ParsedRule {
        String condition;
        int level;

        ParsedRule(String condition, int level) {
            this.condition = condition;
            this.level = level;
        }
    }
}
