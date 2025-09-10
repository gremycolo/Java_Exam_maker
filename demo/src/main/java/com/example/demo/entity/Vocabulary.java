package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.example.demo.converter.EmbeddingConverter;

import java.util.List;

@Entity
@Table(name = "vocabulary") // Updated to lowercase to match your DB
@Data
public class Vocabulary {
    @Id
    @Column(name = "InsertOrder")  // Matches the exact column name in the database
    private int insertOrder;

    @Column(name = "JP_Writing", nullable = false)
    private String jpWriting;

    @Column(name = "JP_Reading", nullable = false)
    private String jpReading;

    @Column(name = "Meaning", nullable = false)
    private String meaning;

    @Column(name = "Category", nullable = false)
    private String category;

    @Column(name = "level", nullable = false)
    private String jlptLevel;

    @Column(name = "IsEntirelyKatakana", nullable = false)
    private boolean isEntirelyKatakana;

    @Convert(converter = EmbeddingConverter.class)
    @Column(name = "MeaningEmbedding", columnDefinition = "nvarchar(max)")
    private List<List<Float>> meaningEmbedding;

    public String getFurigana() {
        if (this.jpWriting == null) {
            return null;
        }
        if (this.jpWriting.contains("(") && this.jpWriting.contains(")")) {
            return this.jpWriting.replaceAll(".*?\\((.*?)\\).*", "$1").trim();
        }
        return this.jpWriting;
    }

    public String getKanjiWriting(){
        if (this.jpWriting == null) {
            return null;
        }
        return this.jpWriting.replaceAll("\\(.*?\\)", "").trim();
    }

    public String toRomaji() {
        String input = this.getFurigana();

        StringBuilder romaji = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if ((current == 'っ' || current == 'ッ') && i + 1 < input.length()) {
                // Small tsu: double the next consonant
                String nextSyllable = (i + 2 < input.length() && isSmallKana(input.charAt(i + 2)))
                        ? input.substring(i + 1, i + 3) : String.valueOf(input.charAt(i + 1));
                String nextRomaji = syllableToRomaji(nextSyllable);
                if (!nextRomaji.isEmpty()) {
                    romaji.append(nextRomaji.charAt(0)); // Double the first consonant
                }
            } else if (current == 'ー' && i > 0) {
                // Hyphen: elongate the previous vowel
                String prevRomaji = syllableToRomaji(String.valueOf(input.charAt(i - 1)));
                if (!prevRomaji.isEmpty()) {
                    char lastVowel = prevRomaji.charAt(prevRomaji.length() - 1);
                    if ("aeiou".indexOf(lastVowel) != -1) {
                        romaji.append(lastVowel); // Repeat the vowel
                    }
                }
            } else {
                // Check for combination syllables (e.g., "きゃ")
                String syllable = (i + 1 < input.length() && isSmallKana(input.charAt(i + 1)))
                        ? input.substring(i, i + 2) : String.valueOf(current);
                romaji.append(syllableToRomaji(syllable));
                if (syllable.length() > 1) i++; // Skip the next character if it’s part of a combination
            }
        }
        return romaji.toString();
    }

    private static boolean isSmallKana(char c) {
        return c == 'ゃ' || c == 'ゅ' || c == 'ょ' || c == 'ャ' || c == 'ュ' || c == 'ョ';
    }

    private static String syllableToRomaji(String syllable) {
        return switch (syllable) {
            // Hiragana - Basic
            case "あ" -> "a"; case "い" -> "i"; case "う" -> "u"; case "え" -> "e"; case "お" -> "o";
            case "か" -> "ka"; case "き" -> "ki"; case "く" -> "ku"; case "け" -> "ke"; case "こ" -> "ko";
            case "さ" -> "sa"; case "し" -> "shi"; case "す" -> "su"; case "せ" -> "se"; case "そ" -> "so";
            case "た" -> "ta"; case "ち" -> "chi"; case "つ" -> "tsu"; case "て" -> "te"; case "と" -> "to";
            case "な" -> "na"; case "に" -> "ni"; case "ぬ" -> "nu"; case "ね" -> "ne"; case "の" -> "no";
            case "は" -> "ha"; case "ひ" -> "hi"; case "ふ" -> "fu"; case "へ" -> "he"; case "ほ" -> "ho";
            case "ま" -> "ma"; case "み" -> "mi"; case "む" -> "mu"; case "め" -> "me"; case "も" -> "mo";
            case "や" -> "ya"; case "ゆ" -> "yu"; case "よ" -> "yo";
            case "ら" -> "ra"; case "り" -> "ri"; case "る" -> "ru"; case "れ" -> "re"; case "ろ" -> "ro";
            case "わ" -> "wa"; case "ゐ" -> "wi"; case "ゑ" -> "we"; case "を" -> "wo"; case "ん" -> "n";
            // Hiragana - Voiced (dakuten ゛)
            case "が" -> "ga"; case "ぎ" -> "gi"; case "ぐ" -> "gu"; case "げ" -> "ge"; case "ご" -> "go";
            case "ざ" -> "za"; case "じ" -> "ji"; case "ず" -> "zu"; case "ぜ" -> "ze"; case "ぞ" -> "zo";
            case "だ" -> "da"; case "ぢ" -> "ji"; case "づ" -> "zu"; case "で" -> "de"; case "ど" -> "do";
            case "ば" -> "ba"; case "び" -> "bi"; case "ぶ" -> "bu"; case "べ" -> "be"; case "ぼ" -> "bo";
            // Hiragana - Semi-voiced (handakuten ゜)
            case "ぱ" -> "pa"; case "ぴ" -> "pi"; case "ぷ" -> "pu"; case "ぺ" -> "pe"; case "ぽ" -> "po";
            // Hiragana - Combinations
            case "きゃ" -> "kya"; case "きゅ" -> "kyu"; case "きょ" -> "kyo";
            case "しゃ" -> "sha"; case "しゅ" -> "shu"; case "しょ" -> "sho";
            case "ちゃ" -> "cha"; case "ちゅ" -> "chu"; case "ちょ" -> "cho";
            case "にゃ" -> "nya"; case "にゅ" -> "nyu"; case "にょ" -> "nyo";
            case "ひゃ" -> "hya"; case "ひゅ" -> "hyu"; case "ひょ" -> "hyo";
            case "みゃ" -> "mya"; case "みゅ" -> "myu"; case "みょ" -> "myo";
            case "りゃ" -> "rya"; case "りゅ" -> "ryu"; case "りょ" -> "ryo";
            case "ぎゃ" -> "gya"; case "ぎゅ" -> "gyu"; case "ぎょ" -> "gyo";
            case "じゃ" -> "ja"; case "じゅ" -> "ju"; case "じょ" -> "jo";
            case "びゃ" -> "bya"; case "びゅ" -> "byu"; case "びょ" -> "byo";
            case "ぴゃ" -> "pya"; case "ぴゅ" -> "pyu"; case "ぴょ" -> "pyo";
            // Katakana - Basic
            case "ア" -> "a"; case "イ" -> "i"; case "ウ" -> "u"; case "エ" -> "e"; case "オ" -> "o";
            case "カ" -> "ka"; case "キ" -> "ki"; case "ク" -> "ku"; case "ケ" -> "ke"; case "コ" -> "ko";
            case "サ" -> "sa"; case "シ" -> "shi"; case "ス" -> "su"; case "セ" -> "se"; case "ソ" -> "so";
            case "タ" -> "ta"; case "チ" -> "chi"; case "ツ" -> "tsu"; case "テ" -> "te"; case "ト" -> "to";
            case "ナ" -> "na"; case "ニ" -> "ni"; case "ヌ" -> "nu"; case "ネ" -> "ne"; case "ノ" -> "no";
            case "ハ" -> "ha"; case "ヒ" -> "hi"; case "フ" -> "fu"; case "ヘ" -> "he"; case "ホ" -> "ho";
            case "マ" -> "ma"; case "ミ" -> "mi"; case "ム" -> "mu"; case "メ" -> "me"; case "モ" -> "mo";
            case "ヤ" -> "ya"; case "ユ" -> "yu"; case "ヨ" -> "yo";
            case "ラ" -> "ra"; case "リ" -> "ri"; case "ル" -> "ru"; case "レ" -> "re"; case "ロ" -> "ro";
            case "ワ" -> "wa"; case "ヰ" -> "wi"; case "ヱ" -> "we"; case "ヲ" -> "wo"; case "ン" -> "n";
            // Katakana - Voiced (dakuten ゛)
            case "ガ" -> "ga"; case "ギ" -> "gi"; case "グ" -> "gu"; case "ゲ" -> "ge"; case "ゴ" -> "go";
            case "ザ" -> "za"; case "ジ" -> "ji"; case "ズ" -> "zu"; case "ゼ" -> "ze"; case "ゾ" -> "zo";
            case "ダ" -> "da"; case "ヂ" -> "ji"; case "ヅ" -> "zu"; case "デ" -> "de"; case "ド" -> "do";
            case "バ" -> "ba"; case "ビ" -> "bi"; case "ブ" -> "bu"; case "ベ" -> "be"; case "ボ" -> "bo";
            // Katakana - Semi-voiced (handakuten ゜)
            case "パ" -> "pa"; case "ピ" -> "pi"; case "プ" -> "pu"; case "ペ" -> "pe"; case "ポ" -> "po";
            // Katakana - Combinations
            case "キャ" -> "kya"; case "キュ" -> "kyu"; case "キョ" -> "kyo";
            case "シャ" -> "sha"; case "シュ" -> "shu"; case "ショ" -> "sho";
            case "チャ" -> "cha"; case "チュ" -> "chu"; case "チョ" -> "cho";
            case "ニャ" -> "nya"; case "ニュ" -> "nyu"; case "ニョ" -> "nyo";
            case "ヒャ" -> "hya"; case "ヒュ" -> "hyu"; case "ヒョ" -> "hyo";
            case "ミャ" -> "mya"; case "ミュ" -> "myu"; case "ミョ" -> "myo";
            case "リャ" -> "rya"; case "リュ" -> "ryu"; case "リョ" -> "ryo";
            case "ギャ" -> "gya"; case "ギュ" -> "gyu"; case "ギョ" -> "gyo";
            case "ジャ" -> "ja"; case "ジュ" -> "ju"; case "ジョ" -> "jo";
            case "ビャ" -> "bya"; case "ビュ" -> "byu"; case "ビョ" -> "byo";
            case "ピャ" -> "pya"; case "ピュ" -> "pyu"; case "ピョ" -> "pyo";
            // Small kana (used in combinations, no standalone romaji)
            case "ゃ", "ャ" -> ""; case "ゅ", "ュ" -> ""; case "ょ", "ョ" -> "";
            case "っ", "ッ" -> ""; // Handled in main loop
            default -> ""; // Ignore unmapped characters
        };
    }
}