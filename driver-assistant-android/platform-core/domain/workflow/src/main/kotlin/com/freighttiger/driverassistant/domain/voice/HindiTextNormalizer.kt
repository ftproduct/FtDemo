package com.freighttiger.driverassistant.domain.voice

import java.util.Locale

/**
 * Normalises Hindi speech-recogniser output (Devanagari or romanised/Hinglish) into a canonical
 * lower-case romanised token stream so that rules can be written once.
 *
 * Normalisation only collapses spelling/script/gender variants ("नहीं", "nahin", "nhi" → "nahi";
 * "gayi"/"gaye" → "gaya"). It never changes meaning: negations, numbers and qualifiers are kept.
 */
object HindiTextNormalizer {

    private val devanagari: Map<String, String> = mapOf(
        "हाँ" to "haan", "हां" to "haan", "हा" to "haan", "हाँजी" to "haan ji", "हांजी" to "haan ji",
        "जी" to "ji", "नहीं" to "nahi", "नही" to "nahi", "ना" to "na", "मत" to "mat", "मना" to "mana",
        "अभी" to "abhi", "बाद" to "baad", "में" to "mein", "मैं" to "main", "हूं" to "hoon", "हूँ" to "hoon",
        "है" to "hai", "हैं" to "hain", "हो" to "ho", "गया" to "gaya", "गई" to "gaya", "गयी" to "gaya",
        "गए" to "gaya", "गये" to "gaya", "चुका" to "chuka", "चुकी" to "chuka", "चुके" to "chuka",
        "पहुंच" to "pahunch", "पहुँच" to "pahunch", "पहुंचा" to "pahuncha", "पहुँचा" to "pahuncha",
        "पहुंची" to "pahuncha", "पहुँची" to "pahuncha", "पहुंचे" to "pahuncha", "पहुँचे" to "pahuncha",
        "पहुंचने" to "pahunchne", "पहुँचने" to "pahunchne", "पहुंचूंगा" to "pahunchunga", "पहुँचूँगा" to "pahunchunga",
        "रहा" to "raha", "रही" to "raha", "रहे" to "raha", "वाला" to "wala", "वाली" to "wala", "वाले" to "wala",
        "रास्ते" to "raste", "रास्ता" to "raste", "आ" to "aa", "आया" to "aaya", "चल" to "chal",
        "दोबारा" to "dobara", "फिर" to "phir", "से" to "se", "बताओ" to "batao", "बताइए" to "batao",
        "बोलो" to "bolo", "बोलिए" to "bolo", "समझ" to "samajh", "सुना" to "suna", "सुनाई" to "sunai",
        "मदद" to "madad", "सहायता" to "sahayata", "सपोर्ट" to "support", "बात" to "baat",
        "करनी" to "karni", "करना" to "karna", "करो" to "karo", "कराओ" to "karao", "मुझे" to "mujhe",
        "कॉल" to "call", "फोन" to "phone", "फ़ोन" to "phone",
        "सहमत" to "sahmat", "बिल्कुल" to "bilkul", "बिलकुल" to "bilkul", "ज़रूर" to "zaroor", "जरूर" to "zaroor",
        "मंजूर" to "manzoor", "मंज़ूर" to "manzoor", "ठीक" to "theek", "चलेगा" to "chalega", "अनुमति" to "anumati",
        "शायद" to "shayad", "पता" to "pata", "मालूम" to "maloom", "सोच" to "soch", "सोचना" to "sochna",
        "लोडिंग" to "loading", "लोड" to "load", "शुरू" to "shuru", "खत्म" to "khatam", "ख़त्म" to "khatam",
        "पूरी" to "poori", "पूरा" to "poori", "इंतज़ार" to "intezaar", "इंतजार" to "intezaar", "रुका" to "ruka",
        "समस्या" to "samasya", "दिक्कत" to "dikkat", "परेशानी" to "pareshani", "देर" to "der", "देरी" to "deri",
        "लगेगा" to "lagega", "लगेंगे" to "lagega", "लगेगी" to "lagega", "और" to "aur",
        "एक" to "ek", "दो" to "do", "तीन" to "teen", "चार" to "char", "पांच" to "paanch", "पाँच" to "paanch",
        "छह" to "chhe", "छः" to "chhe", "छे" to "chhe", "सात" to "saat", "आठ" to "aath", "नौ" to "nau",
        "दस" to "das", "ग्यारह" to "gyarah", "बारह" to "barah", "पंद्रह" to "pandrah", "बीस" to "bees",
        "पच्चीस" to "pachchis", "तीस" to "tees", "पैंतीस" to "paintees", "चालीस" to "chalis",
        "पैंतालीस" to "paintalis", "पचास" to "pachas",
        "आधा" to "aadha", "आधे" to "aadha", "डेढ़" to "dedh", "डेढ" to "dedh", "ढाई" to "dhai",
        "सवा" to "sava", "पौने" to "paune", "साढ़े" to "saadhe", "साढे" to "saadhe",
        "घंटा" to "ghanta", "घंटे" to "ghanta", "घंटों" to "ghanta", "घण्टा" to "ghanta", "घण्टे" to "ghanta",
        "मिनट" to "minute", "कल" to "kal", "थोड़ी" to "thodi", "थोड़ा" to "thodi",
    )

    private val variants: Map<String, String> = mapOf(
        "han" to "haan", "haa" to "haan", "ha" to "haan", "haan" to "haan", "haanji" to "haan ji",
        "hanji" to "haan ji", "hnji" to "haan ji", "yes" to "haan", "yeah" to "haan", "yup" to "haan",
        "nahin" to "nahi", "nahi" to "nahi", "nai" to "nahi", "nahee" to "nahi", "nhi" to "nahi",
        "nahii" to "nahi", "no" to "nahi", "nope" to "nahi",
        "abi" to "abhi", "abhee" to "abhi",
        "mai" to "main", "me" to "mein", "mei" to "mein", "hu" to "hoon", "hun" to "hoon", "hoo" to "hoon",
        "gya" to "gaya", "gayi" to "gaya", "gai" to "gaya", "gaye" to "gaya", "gye" to "gaya", "gyi" to "gaya",
        "chuki" to "chuka", "chuke" to "chuka",
        "pahuch" to "pahunch", "pohoch" to "pahunch", "pahoch" to "pahunch", "pohanch" to "pahunch",
        "pauch" to "pahunch", "pahunchi" to "pahuncha", "pahunche" to "pahuncha", "pohocha" to "pahuncha",
        "pahucha" to "pahuncha", "rahi" to "raha", "rahe" to "raha", "wali" to "wala", "wale" to "wala",
        "rashte" to "raste", "raaste" to "raste", "rastey" to "raste", "rasta" to "raste",
        "dubara" to "dobara", "dobaara" to "dobara", "fir" to "phir", "firse" to "phir se", "phirse" to "phir se",
        "thik" to "theek", "theekh" to "theek", "okay" to "ok", "okk" to "ok",
        "sahamat" to "sahmat", "manjoor" to "manzoor", "jaroor" to "zaroor", "zarur" to "zaroor",
        "suport" to "support", "sapot" to "support", "sapport" to "support", "help" to "madad",
        "intzar" to "intezaar", "intejar" to "intezaar", "intezar" to "intezaar", "intjar" to "intezaar",
        "khatm" to "khatam", "complete" to "poori", "completed" to "poori", "puri" to "poori", "pura" to "poori",
        "chaar" to "char", "panch" to "paanch", "chah" to "chhe", "che" to "chhe", "chhah" to "chhe",
        "adha" to "aadha", "aadhe" to "aadha", "half" to "aadha", "derh" to "dedh", "deddh" to "dedh",
        "dhaai" to "dhai", "dhaee" to "dhai", "sawa" to "sava", "pone" to "paune", "poune" to "paune",
        "sadhe" to "saadhe", "saade" to "saadhe",
        "ghante" to "ghanta", "ghanton" to "ghanta", "ghantey" to "ghanta", "hour" to "ghanta",
        "hours" to "ghanta", "hr" to "ghanta", "hrs" to "ghanta",
        "minat" to "minute", "mint" to "minute", "min" to "minute", "mins" to "minute", "minutes" to "minute",
        "lagenge" to "lagega", "lagegi" to "lagega", "lgega" to "lagega",
    )

    private val devanagariDigits = mapOf(
        '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4',
        '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9',
    )

    /** Returns canonical tokens joined by single spaces. */
    fun normalize(raw: String): String = tokens(raw).joinToString(" ")

    fun tokens(raw: String): List<String> {
        val lowered = raw.lowercase(Locale.ROOT)
            .map { devanagariDigits[it] ?: it }
            .joinToString("")
            // Keep letters, combining marks (matras/nukta) and digits. Everything else separates tokens.
            .replace(Regex("[^\\p{L}\\p{M}\\p{N}]+"), " ")
            .trim()
        if (lowered.isEmpty()) return emptyList()
        return lowered.split(' ')
            .filter { it.isNotBlank() }
            .flatMap { token ->
                val mapped = devanagari[token] ?: variants[token] ?: token
                mapped.split(' ')
            }
    }
}
