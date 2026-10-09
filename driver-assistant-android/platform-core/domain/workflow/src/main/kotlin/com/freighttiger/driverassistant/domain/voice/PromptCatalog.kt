package com.freighttiger.driverassistant.domain.voice

import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.Trip

/**
 * All spoken assistant text, centralised per language so more Indian languages can be added by
 * providing another implementation. Spoken text avoids personal or sensitive data (no phone
 * numbers, consignor names or addresses beyond the loading point name and city).
 */
interface PromptCatalog {
    val languageTag: String

    fun tripBriefing(trip: Trip, nextAction: NextAction): String
    fun consentQuestion(): String
    fun consentClarify(): String
    fun consentCapturedYes(): String
    fun consentCapturedNo(): String
    fun consentExpired(): String
    fun trackingActivated(): String
    fun trackingFailed(): String
    fun trackingStopped(): String
    fun etaQuestion(): String
    fun etaClarify(): String
    fun etaCaptured(minutes: Int, approximate: Boolean): String
    fun arrivalQuestion(): String
    fun arrivalClarify(): String
    fun arrivalCapturedYes(): String
    fun arrivalCapturedNo(etaMinutes: Int?): String
    fun loadingQuestion(): String
    fun loadingClarify(): String
    fun loadingCaptured(status: LoadingStatus): String
    fun didNotHear(): String
    fun deferAcknowledged(): String
    fun escalate(): String
    fun micUnavailable(): String
    fun supportRequestQueued(): String
    fun tripCancelled(): String
    fun tripCompleted(): String
    fun testPrompt(): String
    fun commandHelp(): String
    fun commandNotUnderstood(): String
    fun formatDuration(minutes: Int): String
}

enum class NextAction { GIVE_TRACKING_CONSENT, SHARE_ETA, REACH_LOADING_POINT, UPDATE_LOADING_STATUS, NONE }

/** Hindi (Devanagari) catalogue. Devanagari gives much better pronunciation on hi-IN TTS voices. */
class HindiPromptCatalog : PromptCatalog {
    override val languageTag = "hi-IN"

    override fun tripBriefing(trip: Trip, nextAction: NextAction): String {
        val next = when (nextAction) {
            NextAction.GIVE_TRACKING_CONSENT -> "अगला काम: लोकेशन ट्रैकिंग की अनुमति के बारे में जवाब देना।"
            NextAction.SHARE_ETA -> "अगला काम: लोडिंग पॉइंट पर पहुँचने का समय बताना।"
            NextAction.REACH_LOADING_POINT -> "अगला काम: लोडिंग पॉइंट पर पहुँचना।"
            NextAction.UPDATE_LOADING_STATUS -> "अगला काम: लोडिंग की स्थिति बताना।"
            NextAction.NONE -> ""
        }
        return "नया ट्रिप मिला है। ${trip.origin.city} से ${trip.destination.city}। " +
            "लोडिंग पॉइंट: ${trip.loadingPoint.name}, ${trip.loadingPoint.city}। $next".trim()
    }

    override fun consentQuestion() =
        "नमस्ते। फ्रेट टाइगर को इस ट्रिप के दौरान आपकी गाड़ी की लोकेशन ट्रैक करने की अनुमति चाहिए। " +
            "क्या आप इसके लिए सहमत हैं? आप हाँ या नहीं कह सकते हैं।"

    override fun consentClarify() =
        "माफ़ कीजिए, मैं साफ़ समझ नहीं पाया। क्या आप इस ट्रिप के दौरान लोकेशन ट्रैकिंग के लिए सहमत हैं? " +
            "कृपया सिर्फ़ हाँ या नहीं कहिए।"

    override fun consentCapturedYes() =
        "धन्यवाद, आपका जवाब दर्ज कर लिया गया है। फ्रेट टाइगर से पुष्टि होने के बाद ही ट्रैकिंग शुरू होगी, और तब मैं आपको बताऊँगा।"

    override fun consentCapturedNo() = "ठीक है, आपने मना किया है। ट्रैकिंग शुरू नहीं की जाएगी।"

    override fun consentExpired() = "यह अनुमति अनुरोध समाप्त हो चुका है। नया अनुरोध आने पर मैं फिर पूछूँगा।"

    override fun trackingActivated() = "फ्रेट टाइगर ने पुष्टि कर दी है। इस ट्रिप के लिए लोकेशन ट्रैकिंग अब चालू है।"

    override fun trackingFailed() =
        "ट्रैकिंग शुरू नहीं हो पाई। ज़रूरत हो तो ऐप में सहायता का विकल्प चुनिए।"

    override fun trackingStopped() = "इस ट्रिप के लिए लोकेशन ट्रैकिंग बंद कर दी गई है।"

    override fun etaQuestion() = "आप लोडिंग पॉइंट पर कितनी देर में पहुँच जाएँगे?"

    override fun etaClarify() = "माफ़ कीजिए, समय समझ नहीं आया। कृपया ऐसे बताइए — एक घंटा, या तीस मिनट।"

    override fun etaCaptured(minutes: Int, approximate: Boolean) =
        "ठीक है, आपने बताया ${if (approximate) "लगभग " else ""}${formatDuration(minutes)}। यह जानकारी भेजी जा रही है।"

    override fun arrivalQuestion() = "क्या आप लोडिंग पॉइंट पर पहुँच गए हैं?"

    override fun arrivalClarify() = "कृपया हाँ या नहीं में बताइए — क्या आप लोडिंग पॉइंट पर पहुँच गए हैं?"

    override fun arrivalCapturedYes() = "धन्यवाद। आपके पहुँचने की जानकारी भेजी जा रही है।"

    override fun arrivalCapturedNo(etaMinutes: Int?) =
        if (etaMinutes != null) {
            "ठीक है, आप लगभग ${formatDuration(etaMinutes)} में पहुँचेंगे। पहुँचने पर बता दीजिए।"
        } else {
            "ठीक है। पहुँचने पर बता दीजिए।"
        }

    override fun loadingQuestion() =
        "लोडिंग की क्या स्थिति है? जैसे — इंतज़ार कर रहा हूँ, लोडिंग शुरू हो गई, या लोडिंग पूरी हो गई।"

    override fun loadingClarify() =
        "माफ़ कीजिए, समझ नहीं आया। बताइए — इंतज़ार, लोडिंग शुरू, लोडिंग पूरी, या कोई समस्या।"

    override fun loadingCaptured(status: LoadingStatus) = "ठीक है, ${loadingLabel(status)}। यह जानकारी भेजी जा रही है।"

    override fun didNotHear() = "मुझे आपकी आवाज़ सुनाई नहीं दी।"

    override fun deferAcknowledged() = "ठीक है, मैं बाद में फिर पूछूँगा। सुरक्षित चलाइए।"

    override fun escalate() =
        "लगता है बात समझने में दिक्कत हो रही है। गाड़ी सुरक्षित रोकने के बाद आप स्क्रीन पर बटन दबाकर जवाब दे सकते हैं, या सहायता चुन सकते हैं।"

    override fun micUnavailable() = "माइक्रोफ़ोन उपलब्ध नहीं है। कृपया स्क्रीन पर बटन दबाकर जवाब दीजिए।"

    override fun supportRequestQueued() =
        "आपका सहायता अनुरोध दर्ज किया जा रहा है। फ्रेट टाइगर से पुष्टि मिलने पर ऐप में दिखेगा।"

    override fun tripCancelled() = "यह ट्रिप रद्द कर दिया गया है। इस ट्रिप के लिए अब कुछ करने की ज़रूरत नहीं है।"

    override fun tripCompleted() = "ट्रिप पूरा हो गया है। धन्यवाद।"

    override fun testPrompt() = "नमस्ते! मैं फ्रेट टाइगर ड्राइवर असिस्टेंट हूँ। क्या आप मेरी आवाज़ साफ़ सुन पा रहे हैं?"

    override fun commandHelp() =
        "बोलिए — जैसे “मैं पहुँच गया”, “एक घंटा लगेगा”, “लोडिंग शुरू हो गई”, या “सहायता चाहिए”।"

    override fun commandNotUnderstood() =
        "माफ़ कीजिए, समझ नहीं आया। आप स्क्रीन पर बटन से भी अपडेट दे सकते हैं।"

    override fun formatDuration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m मिनट"
            m == 0 -> if (h == 1) "1 घंटा" else "$h घंटे"
            else -> "${if (h == 1) "1 घंटा" else "$h घंटे"} $m मिनट"
        }
    }

    private fun loadingLabel(status: LoadingStatus) = when (status) {
        LoadingStatus.NOT_REACHED -> "आप अभी नहीं पहुँचे हैं"
        LoadingStatus.REACHED_LOADING_POINT -> "आप लोडिंग पॉइंट पर पहुँच गए हैं"
        LoadingStatus.WAITING_FOR_LOADING -> "आप लोडिंग का इंतज़ार कर रहे हैं"
        LoadingStatus.LOADING_STARTED -> "लोडिंग शुरू हो गई है"
        LoadingStatus.LOADING_COMPLETED -> "लोडिंग पूरी हो गई है"
        LoadingStatus.ISSUE_REPORTED -> "आपने समस्या या देरी बताई है"
    }
}
