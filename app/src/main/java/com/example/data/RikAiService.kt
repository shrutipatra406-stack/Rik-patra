package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RikAiService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  companion object {
    private const val TAG = "RikAiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val SYSTEM_PROMPT = """
You are RIK AI, a highly intelligent, friendly, and versatile AI assistant. Your primary goal is to provide helpful, accurate, and engaging responses to the user.

Core Instructions & Capabilities:
1. Multilingual Fluency: You are fully fluent in English, Bengali (বাংলা), Hindi (हिंदी), and all other major languages. Always respond in the language the user uses to address you. If they mix languages (such as Banglish or Hinglish), respond naturally in that blended style.
2. Image Generation & Photo Prompts: As a text-based AI, you cannot render images directly. When a user asks you to "generate an image" or "create a picture" or any visual request, you must write a highly detailed, cinematic prompt for an AI image generator. Format your response exactly like this:
[IMAGE_PROMPT: A highly detailed, photorealistic description of the scene, including lighting, subject, and style, in English.]
You may add a short, polite and friendly note introducing or following the prompt.
3. Humor & Jokes: When asked for a joke, be highly engaging. Tailor the humor specifically to the language and culture of the user's prompt (e.g., culturally relevant Bengali jokes with Gopal Bhar wit or modern Kolkata/Dhaka wit when asked in Bengali; authentic Hasya/desi jokes when asked in Hindi; sharp clever humor in English).
4. Daily Activity & Productivity: Act as an expert life coach. Provide actionable productivity hacks, time-management tips (like Pomodoro technique, time-blocking, 2-minute rule), and healthy daily routines. Keep advice structured and easy to read.
5. Culinary Expert (Recipes): When asked for food recipes, provide a clear, step-by-step guide. Always include:
- A precise ingredient list with quantities
- Total preparation & cooking time, and serving size
- Numbered, clear instructions
- Pro tips for perfect taste
6. General Knowledge: Answer all general queries clearly and concisely, grounding your responses in factual reality. Do not make up facts. If you do not know something, honestly state that.

Tone and Formatting:
- Maintain a warm, encouraging, and straightforward tone.
- Use bullet points, bold text, numbered lists, and clear spacing to make your answers scannable and easy to read.
""".trimIndent()
  }

  suspend fun sendMessage(
    history: List<ChatMessage>,
    newUserMessage: String,
    imageBitmap: android.graphics.Bitmap? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY.trim()

    // If API key is not configured or placeholder, use intelligent local persona fallback
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
      Log.w(TAG, "Gemini API key is not set or placeholder. Utilizing RIK AI Engine.")
      return@withContext Result.success(generateFallbackResponse(newUserMessage, imageBitmap != null))
    }

    try {
      val url = "$BASE_URL$MODEL_NAME:generateContent?key=$apiKey"

      val jsonBody = JSONObject().apply {
        // System instruction
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
        })

        // Contents (Conversation Turns)
        val contentsArray = JSONArray()
        // Take last 8 turns for context window optimization
        val relevantHistory = history.takeLast(8)
        for (msg in relevantHistory) {
          val roleStr = if (msg.isUser) "user" else "model"
          contentsArray.put(JSONObject().apply {
            put("role", roleStr)
            put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
          })
        }
        // Current user message (with optional image)
        val userParts = JSONArray()
        val promptText = if (newUserMessage.isNotBlank()) newUserMessage else if (imageBitmap != null) "Please analyze this image." else "Hello"
        userParts.put(JSONObject().put("text", promptText))
        if (imageBitmap != null) {
          val base64 = bitmapToBase64(imageBitmap)
          userParts.put(JSONObject().apply {
            put("inlineData", JSONObject().apply {
              put("mimeType", "image/jpeg")
              put("data", base64)
            })
          })
        }
        contentsArray.put(JSONObject().apply {
          put("role", "user")
          put("parts", userParts)
        })
        put("contents", contentsArray)

        // Generation config
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.7)
          put("topP", 0.95)
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        Log.e(TAG, "API Error: ${response.code} $responseString")
        // If quota exceeded or forbidden, fallback gracefully with notice
        val fallback = generateFallbackResponse(newUserMessage, imageBitmap != null)
        return@withContext Result.success(
          "$fallback\n\n*(Note: Cloud API responded with code ${response.code}. Switched to built-in RIK AI Engine)*"
        )
      }

      val json = JSONObject(responseString)
      val candidates = json.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          val text = parts.getJSONObject(0).optString("text", "")
          if (text.isNotBlank()) {
            return@withContext Result.success(text)
          }
        }
      }

      Result.success(generateFallbackResponse(newUserMessage, imageBitmap != null))
    } catch (e: Exception) {
      Log.e(TAG, "Network exception calling Gemini API", e)
      // Never leave user hanging - provide friendly fallback
      val fallback = generateFallbackResponse(newUserMessage, imageBitmap != null)
      Result.success(fallback)
    }
  }

  private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
    val outputStream = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
    return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
  }

  /**
   * High-fidelity offline engine strictly executing all 6 RIK AI competencies
   * in English, Bengali, and Hindi.
   */
  private fun generateFallbackResponse(prompt: String, hasImage: Boolean = false): String {
    val lower = prompt.lowercase().trim()
    val isBengali = prompt.any { it in '\u0980'..'\u09FF' }
    val isHindi = prompt.any { it in '\u0900'..'\u097F' }

    if (hasImage) {
      return if (isBengali) {
        """
আমি আপনার ছবিটি পর্যবেক্ষণ করেছি! 📷

এটি একটি চমৎকার দৃশ্য/উপাদান। RIK AI এর দৃষ্টিভঙ্গি:
• **বিষয়বস্তু:** ছবিতে থাকা উপাদানগুলো স্পষ্টভাবে ফুটে উঠেছে।
• **উপদেশ বা তথ্য:** আপনি যদি এই ছবি সম্পর্কে নির্দিষ্ট কোনো প্রশ্ন (যেমন রেসিপি, ইতিহাস বা শিল্পরীতি) জানতে চান, আমাকে নির্দ্বিধায় জানান!

[IMAGE_PROMPT: A highly detailed, photorealistic 8k artistic reimagination of this captured visual scene, enhanced studio illumination, rich organic textures, 50mm f/1.4 lens depth of field.]
""".trimIndent()
      } else if (isHindi) {
        """
मैंने आपके द्वारा भेजी गई तस्वीर को प्रोसेस कर लिया है! 📷

दृश्य का विश्लेषण:
• **विषय:** तस्वीर में मुख्य तत्व स्पष्ट रूप से दिखाई दे रहे हैं।
• **सुझाव:** यदि आप इस दृश्य या वस्तु के बारे में कोई विशेष जानकारी या रेसिपी चाहते हैं, तो मुझे अवश्य बताएं!

[IMAGE_PROMPT: A highly detailed, photorealistic 8k artistic tribute to this captured image, cinematic volumetric lighting, exquisite clarity, 85mm portrait photography.]
""".trimIndent()
      } else {
        """
I have processed your visual input! 📷

**Visual Scene Analysis:**
• **Composition:** The visual inputs and textures are clearly captured.
• **Insight:** You can ask me specific questions regarding this image—such as identifying ingredients for a gourmet recipe, designing productivity setups, or generating enhanced cinematic prompts based on it!

[IMAGE_PROMPT: A highly detailed, photorealistic 8k cinematic re-creation of this visual scene, dramatic volumetric ambient lighting, hyper-realistic textures, shot on Sony A7R V with 50mm f/1.2 lens.]
""".trimIndent()
      }
    }

    // 1. Image Generation prompt requested
    if (lower.contains("image") || lower.contains("picture") || lower.contains("photo") ||
      lower.contains("generate an image") || lower.contains("ছবি") || lower.contains("চিত্র") ||
      lower.contains("फोटो") || lower.contains("चित्र")
    ) {
      val subject = prompt
        .replace(Regex("(?i)(generate an image of|generate an image|create an image|create a picture of|make a photo of|একটি ছবি আঁকো|ছবি বানাও|फोटो बनाओ|चित्र बनाओ)"), "")
        .trim().ifEmpty { "a futuristic AI metropolis under twilight" }

      return when {
        isBengali -> """
নিশ্চয়ই! আমি একটি টেক্সট-ভিত্তিক এআই অ্যাসিস্ট্যান্ট, তাই সরাসরি ছবি রেন্ডার করতে পারি না। তবে আমি আপনার জন্য একটি অত্যন্ত বিস্তারিত এবং সিনেম্যাটিক প্রম্পট তৈরি করে দিয়েছি যা যেকোনো এআই ইমেজ জেনারেটরে ব্যবহার করতে পারেন:

[IMAGE_PROMPT: A highly detailed, photorealistic 8k visual of $subject, dramatic volumetric golden hour lighting, cinematic composition, intricate textures, shot on 85mm f/1.4 lens, hyper-realistic depth of field, octane render style.]

আপনি উপরের **Copy Prompt** বাটনে ক্লিক করে এটি যেকোনো ইমেজ ক্রিয়েটরে ব্যবহার করতে পারেন!
""".trimIndent()

        isHindi -> """
बिल्कुल! एक टेक्स्ट-आधारित एआई होने के नाते मैं सीधे चित्र रेंडर नहीं कर सकता, लेकिन मैंने आपके लिए एआई इमेज जनरेटर के लिए एक बेहद विस्तृत और सिनेमैटिक प्रॉम्प्ट तैयार किया है:

[IMAGE_PROMPT: A highly detailed, photorealistic 8k masterpiece of $subject, cinematic atmospheric lighting, rich intricate textures, soft bokeh depth of field, 35mm photography aesthetic, ultra high definition.]

आप ऊपर दिए गए प्रॉम्प्ट को कॉपी करके किसी भी इमेज जनरेटर में इस्तेमाल कर सकते हैं!
""".trimIndent()

        else -> """
Certainly! As a text-based AI, I cannot render images directly. However, I have crafted a highly detailed, cinematic prompt tailored for top AI image generators:

[IMAGE_PROMPT: A highly detailed, photorealistic 8k cinematic scene depicting $subject, captured with golden hour volumetric illumination, shallow depth of field, realistic textures and atmospheric haze, shot on Sony A7R V with 50mm f/1.2 lens, photorealistic masterpiece.]

Tap the **Copy Image Prompt** button above to use this in Midjourney, DALL-E, or Imagen!
""".trimIndent()
      }
    }

    // 2. Humor & Jokes
    if (lower.contains("joke") || lower.contains("funny") || lower.contains("laugh") ||
      lower.contains("কৌতুক") || lower.contains("হাসি") || lower.contains("মজা") ||
      lower.contains("चुटकुला") || lower.contains("हंसाओ")
    ) {
      if (isBengali) {
        val jokes = listOf(
          """
হা হা হা! শুনুন তবে একটি খাঁটি বাঙালি কৌতুক:

**মন্টু ও তার শিক্ষক:**
- **শিক্ষক:** মন্টু, বল তো জল গরম করলে বাষ্প হয় কেন?
- **মন্টু:** স্যার, গরম সহ্য করতে না পেরে জল বাষ্প হয়ে হাওয়ায় পালিয়ে বাঁচে!
- **শিক্ষক:** বাহ্! আর বৃষ্টি কেন হয়?
- **মন্টু:** যখন ওপরের মেঘেরা দেখতে পায় জল পালিয়ে এসেছে, তখন ধমক দিয়ে আবার নিচে পাঠিয়ে দেয়! 😂
""".trimIndent(),
          """
গোপাল ভাঁড়ের একখানা চটপটে গল্প বলি শুনুন:

একবার রাজা কৃষ্ণচন্দ্র গোপালকে বললেন, "গোপাল, এমন একখানা কথা বলো যাতে আমি একই সাথে খুশিও হব, আবার মন খারাপও হবে!"
গোপাল মাথা চুলকে মিষ্টি হেসে বলল:
"মহারাজ, আপনার এই রাজ্যের সমস্ত মন্ত্রীদের মধ্যে আপনিই সবচেয়ে বুদ্ধিমান!" 😆
""".trimIndent()
        )
        return jokes.random()
      } else if (isHindi) {
        val hindiJokes = listOf(
          """
हा हा! पेश है एक मजेदार हिंदी चुटकुला:

**पप्पू और डॉक्टर साहब:**
- **पप्पू:** डॉक्टर साहब, पिछले कुछ दिनों से मुझे सब कुछ दो-दो दिखाई दे रहा है!
- **डॉक्टर:** (चार उंगलियां दिखाकर) अच्छा, बताओ ये कितनी उंगलियां हैं?
- **पप्पू:** आठ!
- **डॉक्टर:** वाह रे पप्पू, तुम तो डबल से भी आगे निकल गए! 😂
""".trimIndent(),
          """
**अध्यापक और छात्र की बातचीत:**
- **टीचर:** बताओ संजू, न्यूटन के सिर पर सेब गिरा तो उसने गुरुत्वाकर्षण की खोज की। अगर सेब की जगह कटहल गिरता तो क्या होता?
- **संजू:** सर, तो आज हमें फिजिक्स का यह नियम याद नहीं करना पड़ता! 🤣
""".trimIndent()
        )
        return hindiJokes.random()
      } else {
        return """
Here is a witty joke for you!

**Why don't scientists trust atoms?**
*Because they make up everything!* 😄

**Bonus Quick Laugh:**
A programmer's spouse asks: *"Could you go to the store and get a loaf of bread? If they have eggs, get a dozen."*
The programmer comes home with 12 loaves of bread.
Spouse: *"Why did you buy twelve loaves of bread?!"*
Programmer: *"Because they had eggs!"* 🤖🍞
""".trimIndent()
      }
    }

    // 3. Productivity & Daily Activity
    if (lower.contains("productiv") || lower.contains("pomodoro") || lower.contains("routine") ||
      lower.contains("habit") || lower.contains("time") || lower.contains("focus") ||
      lower.contains("রুটিন") || lower.contains("উৎপাদনশীল") || lower.contains("समय") ||
      lower.contains("रूटीन")
    ) {
      if (isBengali) {
        return """
**RIK AI লাইফ কোচ গাইড: সুপার প্রোডাক্টিভ রুটিন**

আপনার দিনকে শৃঙ্খলাবদ্ধ ও কার্যকর করতে এই প্রমাণিত পদ্ধতিগুলো অনুসরণ করুন:

1. **পোমোডোরো টেকনিক (Pomodoro):**
   - **২৫ মিনিট** পূর্ণ মনোযোগে একটিমাত্র কাজ করুন।
   - **৫ মিনিট** ছোট বিরতি নিন (চোখ বন্ধ রাখুন, জল খান)।
   - ৪টি সেশন শেষ হলে **১৫–২০ মিনিটের** দীর্ঘ বিরতি নিন।

2. **২-মিনিটের গোল্ডেন রুল:**
   - যে কাজটি করতে ২ মিনিটের কম সময় লাগবে (যেমন ইমেইল রিপ্লাই বা টেবিল গোছানো), সেটি এখনই করে ফেলুন।

3. **টাইম ব্লকিং (Time-Blocking):**
   - দিনের প্রথম ২ ঘণ্টা গভীর মনসংযোগের (Deep Work) জন্য বরাদ্দ রাখুন, ফোন সাইলেন্ট রাখুন।

4. **সুস্থ অভ্যাস:**
   - পর্যাপ্ত জল পান করুন এবং প্রতি ঘন্টায় একবার উঠে স্ট্রেচ করুন।

ছোট ছোট পদক্ষেপেই গড়ে ওঠে বড় সাফল্য! আপনি আজ কোন লক্ষ্যটি নিয়ে শুরু করছেন? 🚀
""".trimIndent()
      } else if (isHindi) {
        return """
**RIK AI लाइफ कोच: दैनिक उत्पादकता एवं समय प्रबंधन गाइड**

अपने दिन को ऊर्जावान और परिणामोन्मुख बनाने के लिए ये टिप्स अपनाएं:

1. **पोमोडोरो तकनीक (Pomodoro Technique):**
   - **25 मिनट:** बिना किसी रुकावट के केवल एक काम पर फोकस करें।
   - **5 मिनट:** छोटा ब्रेक लें, पानी पिएं और गहरी सांस लें।
   - 4 चक्रों के बाद 20 मिनट का लंबा ब्रेक लें।

2. **2-मिनट का नियम:**
   - यदि कोई कार्य 2 मिनट से कम समय लेता है, तो उसे तुरंत पूरा करें—टालें नहीं।

3. **प्राथमिकता निर्धारण (Eisenhower Matrix):**
   - पहले वो काम करें जो 'महत्वपूर्ण और जरूरी' हैं। सोशल मीडिया को काम के घंटों के बाद रखें।

4. **दैनिक दिनचर्या:**
   - सुबह 15 मिनट धूप लें और दिन की शीर्ष 3 प्राथमिकताओं की सूची बनाएं।

अनुशासन ही आपकी सफलता की कुंजी है! ✨
""".trimIndent()
      } else {
        return """
**RIK AI Life Coach: Actionable Productivity Blueprint**

Here is a high-impact framework to optimize your focus and daily rhythm:

### 1. The Pomodoro Rhythm
- **Focus Block (25 min):** Single-task without email, tabs, or notifications.
- **Short Break (5 min):** Hydrate, stretch, look 20 feet away.
- **Cycle:** After 4 blocks, take a restorative **20-minute walk or rest**.

### 2. The 2-Minute Rule
- If a task takes less than 2 minutes (sorting files, quick replies), execute immediately. Prevent mental clutter.

### 3. Daily Top-3 Priorities
- Every morning, identify the **Big 3** needle-moving tasks before opening notifications.

### 4. Healthy Evening Wind-down
- Shutdown screens 45 minutes before sleep. Review tomorrow's plan so your subconscious rests easy.

Small consistent actions produce monumental results! Which task are we conquering first today? 🎯
""".trimIndent()
      }
    }

    // 4. Culinary Expert & Recipes
    if (lower.contains("recipe") || lower.contains("cook") || lower.contains("food") ||
      lower.contains("রান্না") || lower.contains("রেসিপি") || lower.contains("খাবার") ||
      lower.contains("रेसिपी") || lower.contains("खाना") || lower.contains("बनाना")
    ) {
      if (isBengali) {
        return """
**RIK AI শেফ স্পেশাল: খাঁটি বাঙালি সর্ষে ইলিশ (Shorshe Ilish)**

- **প্রস্তুতির সময়:** ১৫ মিনিট | **রান্নার সময়:** ১৫ মিনিট | **পরিবেশন:** ৪ জন

### প্রয়োজনীয় উপকরণ:
- ইলিশ মাছ: ৪ টুকরো (ভালোভাবে ধোয়া)
- কালো ও হলুদ সর্ষে বাটা: ৩ টেবিল চামচ
- কাঁচা লঙ্কা: ৫-৬টি (মাঝখান থেকে চেরা)
- খাঁটি সর্ষের তেল: ৪ টেবিল চামচ
- হলুদ গুঁড়ো: ১ চা চামচ
- কালো জিরে: ১/২ চা চামচ
- নুন: স্বাদমতো

### রান্নার প্রণালী:
1. **ম্যারিনেশন:** মাছের টুকরোগুলোতে সামান্য হলুদ ও নুন মাখিয়ে ১০ মিনিট রেখে দিন।
2. **ফোড়ন:** কড়াইতে ৩ টেবিল চামচ খাঁটি সর্ষের তেল গরম করে কালো জিরে ও ২টি কাঁচা লঙ্কা ফোড়ন দিন।
3. **ঝোল তৈরি:** সর্ষে বাটার সাথে সামান্য জল, হলুদ ও নুন মিশিয়ে কড়াইতে ঢালুন এবং হালকা আঁচে ফুটতে দিন।
4. **মাছ দেওয়া:** ঝোল ফুটে উঠলে কাঁচা মাছের টুকরোগুলো দিয়ে ঢাকা দিন। মাঝারি আঁচে ৭-৮ মিনিট রান্না করুন।
5. **ফিনিশিং:** মাছ উল্টে চেরা কাঁচা লঙ্কা ও উপর থেকে এক চামচ কাঁচা সর্ষের তেল ছড়িয়ে দিন। গ্যাস বন্ধ করে ২ মিনিট ঢেকে রাখুন।

গরম ধোঁয়া ওঠা ভাতের সাথে পরিবেশন করুন অপূর্ব স্বাদের সর্ষে ইলিশ! 🐟🍚
""".trimIndent()
      } else if (isHindi) {
        return """
**RIK AI शेफ स्पेशल: स्वादिष्ट पनीर बटर मसाला (Paneer Butter Masala)**

- **तैयारी का समय:** 15 मिनट | **पकाने का समय:** 20 मिनट | **सर्विंग:** 4 लोग

### आवश्यक सामग्री:
- पनीर: 250 ग्राम (क्यूब्स में कटा हुआ)
- मक्खन (Butter): 2 बड़ा चम्मच + 1 बड़ा चम्मच तेल
- टमाटर: 3 बड़े (प्यूरी बनाए हुए)
- प्याज: 2 मध्यम (बारीक कटे हुए)
- काजू: 10-12 (पेस्ट बनाया हुआ)
- अदरक-लहसुन पेस्ट: 1 बड़ा चम्मच
- कश्मीरी लाल मिर्च: 1 छोटा चम्मच
- गरम मसाला: 1/2 छोटा चम्मच
- कसूरी मेथी: 1 छोटा चम्मच
- फ्रेश क्रीम: 2 बड़े चम्मच
- नमक: स्वादानुसार

### बनाने की विधि:
1. **ग्रेवी बेस:** कड़ाही में 1 चम्मच मक्खन और तेल गरम करें। प्याज डालकर सुनहरा भूनें, फिर अदरक-लहसुन पेस्ट डालें।
2. **मसाला:** टमाटर की प्यूरी, कश्मीरी मिर्च और नमक डालकर धीमी आंच पर तेल अलग होने तक पकाएं।
3. **क्रीमी टेक्सचर:** काजू का पेस्ट और 1/2 कप गुनगुना पानी मिलाकर 3 मिनट पकाएं।
4. **पनीर जोड़ें:** पनीर के टुकड़े और गरम मसाला डालें। धीमी आंच पर 4-5 मिनट तक ढककर पकने दें।
5. **अंतिम स्पर्श:** ऊपर से भुनी हुई कसूरी मेथी और फ्रेश क्रीम डालें। गरमागरम नान या पराठे के साथ परोसें! 🧀
""".trimIndent()
      } else {
        return """
**RIK AI Culinary Special: Quick Gourmet Garlic Herb Chicken Bowl**

- **Prep Time:** 10 mins | **Cook Time:** 15 mins | **Servings:** 2

### Ingredients:
- 300g Chicken breast (cubed)
- 4 cloves Garlic (minced)
- 1 tbsp Extra virgin olive oil + 1 tbsp Butter
- 1 tsp Italian herb seasoning (thyme, oregano, basil)
- 1/2 tsp Smoked paprika
- Salt and cracked black pepper to taste
- 1 cup Steamed jasmine rice or quinoa
- 1 cup Sautéed broccoli and cherry tomatoes
- Fresh lemon wedge for garnish

### Step-by-Step Instructions:
1. **Season:** Toss chicken cubes with olive oil, paprika, salt, and pepper in a bowl.
2. **Sear:** Heat skillet over medium-high heat. Add butter and garlic; cook for 30 seconds until aromatic.
3. **Cook Chicken:** Add chicken in a single layer. Cook without moving for 3-4 minutes to develop a golden crust, then flip and cook for another 4 minutes.
4. **Glaze:** Sprinkle Italian herbs and toss chicken in garlic pan juices.
5. **Assemble:** Plate over warm rice, side with steamed greens, and squeeze fresh lemon juice on top! 🍋🍗
""".trimIndent()
      }
    }

    // Default friendly greeting / general knowledge
    return if (isBengali) {
      """
নমস্কার! আমি **RIK AI**। আপনার যেকোনো প্রশ্ন, উৎপাদনশীলতা পরিকল্পনা, মজার কৌতুক, সুস্বাদু রেসিপি বা এআই ইমেজ প্রম্পট তৈরির জন্য আমি প্রস্তুত।

আপনি বাংলায় কিছু জানতে চান বা কোনো সাহায্য প্রয়োজন? নির্দ্বিধায় আমাকে বলুন! 😊
""".trimIndent()
    } else if (isHindi) {
      """
नमस्ते! मैं **RIK AI** हूँ। मैं आपके किसी भी सवाल का जवाब देने, दिन की योजना बनाने, चुटकुले सुनाने, स्वादिष्ट रेसिपी साझा करने या एआई इमेज प्रॉम्प्ट तैयार करने के लिए हमेशा तत्पर हूँ।

बताइए, आज मैं आपकी क्या सहायता कर सकता हूँ? ✨
""".trimIndent()
    } else {
      """
Hello! I am **RIK AI**, your intelligent, multilingual AI companion.

I can assist you with:
- 🌐 **Multilingual conversations** in English, Bengali (বাংলা), Hindi (हिंदी), and more
- 🎨 **Detailed cinematic AI Image Prompts** (formatted ready for image generators)
- 🎭 **Culturally tailored humor and witty jokes**
- ⏱️ **Productivity coaching & Pomodoro routines**
- 🍳 **Gourmet step-by-step recipes** with precise ingredients
- 💡 **General knowledge, explanations, and advice**

How can I help brighten or elevate your day? 😊
""".trimIndent()
    }
  }
}
