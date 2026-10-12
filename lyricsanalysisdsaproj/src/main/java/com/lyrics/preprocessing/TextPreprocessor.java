package com.lyrics.preprocessing;

import com.lyrics.model.AnalysisResult.PreprocessingStats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Robust NLP pipeline for cleaning, normalizing, tokenizing,
 * filtering stop words, and stemming lyrics.
 */
public class TextPreprocessor {
    private static final Pattern METADATA_TAGS = Pattern.compile("\\[[^\\]]*\\]|\\([^\\)]*\\)");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final Set<String> stopWords;
    private final PorterStemmer stemmer;

    private static TextPreprocessor instance;

    public static synchronized TextPreprocessor getInstance() {
        if (instance == null) {
            instance = new TextPreprocessor();
        }
        return instance;
    }

    public TextPreprocessor() {
        this.stopWords = new HashSet<>();
        this.stemmer = new PorterStemmer();
        loadStopWords();
    }

    private void loadStopWords() {
        try (InputStream is = getClass().getResourceAsStream("/stopwords.txt")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim().toLowerCase();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            stopWords.add(line);
                        }
                    }
                }
            } else {
                loadDefaultFallbackStopWords();
            }
        } catch (Exception e) {
            loadDefaultFallbackStopWords();
        }
    }

    private void loadDefaultFallbackStopWords() {
        String[] defaults = {
                "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
                "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
                "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't",
                "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't",
                "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
                "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll",
                "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's",
                "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
                "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own",
                "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some",
                "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then",
                "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this",
                "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we",
                "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's",
                "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with",
                "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
                "yourself", "yourselves", "oh", "yeah", "la", "ooh", "ah", "hey", "baby", "gonna", "wanna",
                "gotta", "na", "chorus", "verse", "intro", "outro", "bridge", "hook"
        };
        Collections.addAll(stopWords, defaults);
    }

    /**
     * Preprocesses the raw lyrics into a normalized token string.
     */
    public String preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        List<String> tokens = extractProcessedTokens(text);
        return String.join(" ", tokens);
    }

    /**
     * Extracts cleaned, filtered, and stemmed tokens from raw text.
     */
    public List<String> extractProcessedTokens(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Remove bracketed lyric tags e.g. [Chorus], [Verse 1]
        String cleaned = METADATA_TAGS.matcher(text).replaceAll(" ");

        // 2. Lowercase
        cleaned = cleaned.toLowerCase(Locale.ROOT);

        // 3. Normalize punctuation & special characters
        cleaned = NON_ALPHANUMERIC.matcher(cleaned).replaceAll(" ");

        // 4. Tokenize by whitespace
        String[] rawTokens = WHITESPACE.split(cleaned.trim());

        List<String> processedTokens = new ArrayList<>();
        for (String token : rawTokens) {
            token = token.trim();
            if (token.length() >= 2 && !stopWords.contains(token)) {
                // Apply stemming
                String stemmed = stemmer.stem(token);
                if (stemmed != null && stemmed.length() >= 2 && !stopWords.contains(stemmed)) {
                    processedTokens.add(stemmed);
                }
            }
        }
        return processedTokens;
    }

    /**
     * Produces comprehensive preprocessing statistics for UI inspection.
     */
    public PreprocessingStats analyzePreprocessing(String text) {
        if (text == null) text = "";

        int rawCharCount = text.length();
        int rawWordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;

        // Clean text
        String cleaned = METADATA_TAGS.matcher(text).replaceAll(" ");
        cleaned = cleaned.toLowerCase(Locale.ROOT);
        cleaned = NON_ALPHANUMERIC.matcher(cleaned).replaceAll(" ");
        String[] initialTokens = cleaned.trim().isEmpty() ? new String[0] : WHITESPACE.split(cleaned.trim());

        int tokensAfterCleaning = 0;
        int stopWordsRemoved = 0;
        List<String> finalTokens = new ArrayList<>();
        Set<String> uniqueTokens = new HashSet<>();

        for (String rawTok : initialTokens) {
            String tok = rawTok.trim();
            if (tok.isEmpty()) continue;
            tokensAfterCleaning++;

            if (tok.length() < 2 || stopWords.contains(tok)) {
                stopWordsRemoved++;
            } else {
                String stemmed = stemmer.stem(tok);
                if (stemmed != null && stemmed.length() >= 2 && !stopWords.contains(stemmed)) {
                    finalTokens.add(stemmed);
                    uniqueTokens.add(stemmed);
                } else {
                    stopWordsRemoved++;
                }
            }
        }

        String processedText = String.join(" ", finalTokens);

        return new PreprocessingStats(
                text,
                processedText,
                rawWordCount,
                rawCharCount,
                tokensAfterCleaning,
                stopWordsRemoved,
                finalTokens.size(),
                uniqueTokens.size(),
                finalTokens
        );
    }

    public Set<String> getStopWords() {
        return Collections.unmodifiableSet(stopWords);
    }
}
