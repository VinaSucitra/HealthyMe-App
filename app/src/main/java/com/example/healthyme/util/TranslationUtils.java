package com.example.healthyme.util;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.HashMap;
import java.util.Map;

public class TranslationUtils {

    private static final Map<String, String> translations = new HashMap<>();

    static {
        // Muscles
        translations.put("abdominals", "Perut");
        translations.put("abductors", "Abduktor");
        translations.put("adductors", "Aduktor");
        translations.put("biceps", "Bisep");
        translations.put("calves", "Betis");
        translations.put("chest", "Dada");
        translations.put("forearms", "Lengan Bawah");
        translations.put("glutes", "Bokong");
        translations.put("hamstrings", "Paha Belakang");
        translations.put("lats", "Punggung Samping");
        translations.put("lower_back", "Punggung Bawah");
        translations.put("middle_back", "Punggung Tengah");
        translations.put("traps", "Trapezius");
        translations.put("triceps", "Trisep");

        // Types
        translations.put("cardio", "Kardio");
        translations.put("olympic_weightlifting", "Angkat Besi");
        translations.put("plyometrics", "Pliometrik");
        translations.put("powerlifting", "Powerlifting");
        translations.put("strength", "Kekuatan");
        translations.put("stretching", "Peregangan");
        translations.put("strongman", "Strongman");

        // Difficulty
        translations.put("beginner", "Pemula");
        translations.put("intermediate", "Menengah");
        translations.put("expert", "Ahli");
    }

    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return "n/a";
        }
        
        String key = text.toLowerCase().replace(" ", "_");
        if (translations.containsKey(key)) {
            return translations.get(key);
        }
        
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Merapikan teks instruksi dengan membaginya menjadi poin-poin (bullet points).
     */
    public static String formatInstructions(String instructions) {
        if (instructions == null || instructions.isEmpty()) return "";
        
        // Menghapus spasi berlebih
        instructions = instructions.trim();
        
        // Membagi teks berdasarkan titik yang diikuti spasi (akhir kalimat)
        String[] steps = instructions.split("\\.\\s+");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].trim();
            if (step.isEmpty()) continue;
            
            // Tambahkan bullet point
            formatted.append("• ").append(step);
            
            // Pastikan setiap kalimat diakhiri titik jika belum ada
            if (!step.endsWith(".")) {
                formatted.append(".");
            }
            
            // Tambahkan baris baru antar poin, kecuali di akhir
            if (i < steps.length - 1) {
                formatted.append("\n\n");
            }
        }
        
        return formatted.toString();
    }

    public interface TranslationCallback {
        void onTranslationComplete(String translatedText);
    }

    /**
     * Menerjemahkan teks menggunakan ML Kit Google (Inggris -> Indonesia)
     */
    public static void translateWithMLKit(String text, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            callback.onTranslationComplete("");
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.INDONESIAN)
                .build();
        
        final Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    translator.translate(text)
                            .addOnSuccessListener(translatedText -> {
                                callback.onTranslationComplete(formatInstructions(translatedText));
                                translator.close();
                            })
                            .addOnFailureListener(e -> {
                                callback.onTranslationComplete(formatInstructions(text));
                                translator.close();
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onTranslationComplete(formatInstructions(text));
                    translator.close();
                });
    }
}
