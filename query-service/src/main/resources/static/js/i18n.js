/**
 * Kisan AI — Comprehensive Website Multilingual Support (i18n)
 * Supports: English ('en'), Tamil ('ta', தமிழ்), Hindi ('hi', हिंदी)
 */

(function () {
    'use strict';

    const TRANSLATIONS = {
        en: {
            // Brand & Navigation
            brand_title: "KISAN AI",
            brand_subtitle: "Multilingual Agricultural Chatbot • ICAR & TNAU Grounded",
            nav_home: "🌾 Agricultural Assistant",
            nav_dashboard: "🌾 Farm Memory",
            nav_history: "📜 Query History",
            nav_login: "Sign In",
            nav_register: "Register",
            nav_logout: "Sign Out",
            nav_clear: "Clear",
            user_farmer: "Farmer",

            // AI Assistant Page (index.html)
            chat_welcome_title: "Welcome to Kisan AI Assistant",
            chat_welcome_desc: "Your trusted decision support partner for science-backed agronomic guidance, pesticide safety, and cost optimization.",
            chat_engine_label: "Engine:",
            chat_engine_general: "1. Risk-Aware Decision Assistant",
            chat_engine_weather: "2. Weather & Sowing Risk Advisory",
            chat_engine_budget: "3. Budget-Aware Crop Planner",
            chat_engine_low_cost: "4. Low-Cost Farming Planner",
            chat_engine_scheme: "5. Govt Scheme Navigator",
            chat_engine_pesticide: "6. Safe Pesticide Guidance (CIBRC)",
            chat_engine_triage: "7. Crop Problem Triage",
            chat_acres_label: "Acres:",
            chat_language_label: "Language:",
            chat_lang_auto: "Auto-detect",
            chat_lang_en: "English (English)",
            chat_lang_ta: "Tamil (தமிழ்)",
            chat_lang_hi: "Hindi (हिंदी)",
            chat_location_label: "Location:",
            chat_location_placeholder: "e.g. Thanjavur",
            chat_season_label: "Season:",
            chat_season_auto: "Auto",
            chat_season_kharif: "Kharif",
            chat_season_rabi: "Rabi",
            chat_season_summer: "Summer",
            chat_season_zaid: "Zaid",
            parcel_bar_title: "Farm Parcels (Farm Memory):",
            parcel_select_default: "-- Choose a Parcel to Auto-fill Location & Crop --",
            btn_clear_plot: "Clear",
            quick_parcel_inquiries: "Quick Parcel Inquiries:",
            chip_dosage: "🧪 Safe Pesticide & Fertilizer",
            chip_sowing: "🌦️ Weather & Sowing Risk",
            chip_budget: "💰 Budget Crop Plan",
            chip_lowcost: "🌿 Low-Cost Farming",
            tip_no_parcels: "Tip: You haven't recorded any land parcels yet. Add your parcels in Farm Memory to auto-load location, crop stage, and soil pH into the Assistant.",
            chat_input_placeholder: "Ask your agricultural question... (e.g. How to prevent pest damage in rice?) — Press Enter to send",
            chat_send_btn: "Send",
            disclaimer_grounded: "Grounded with ICAR/TNAU science • Real-time weather integration",
            input_hint_enter: "Enter ↵ to send • Shift + Enter for newline",
            btn_copy: "Copy",
            btn_copied: "Copied!",
            btn_listen: "Listen",
            verified_sources: "Verified Research & Data Sources",
            view_reference: "View Reference",

            // Dashboard Page (dashboard.html)
            dash_title: "Kisan Farm Memory",
            dash_subtitle: "Personalized Field Records & Agronomic Task Calendar",
            dash_profile_heading: "Agronomic Profile",
            dash_state_label: "State",
            dash_district_label: "District",
            dash_primary_crop_label: "Primary Cultivated Crop",
            dash_land_acres_label: "Total Land Holding (Acres)",
            dash_soil_type_label: "Predominant Soil Type",
            dash_irrigation_label: "Primary Irrigation Source",
            dash_budget_label: "Seasonal Working Budget (₹ INR)",
            dash_language_label: "Preferred Advisory Language",
            dash_btn_save_profile: "Save Agronomic Profile Changes",
            dash_plots_title: "Land Parcels & Plots",
            dash_new_parcel_heading: "Record New Field Parcel",
            dash_plot_name_label: "Parcel Identifier / Name",
            dash_plot_area_label: "Parcel Acreage (Acres)",
            dash_current_crop_label: "Current Crop Variety",
            dash_crop_stage_label: "Crop Growth Stage",
            dash_plot_location_label: "Plot Location / Village / District",
            dash_soil_ph_label: "Soil pH (from soil test)",
            dash_soil_oc_label: "Organic Carbon (%)",
            dash_btn_save_parcel: "Save Parcel to Farm Memory",
            dash_empty_plots: "No land parcels recorded in memory. Record your first field parcel above with soil pH, location, and current crop stage to receive parcel-specific risk alerts.",
            dash_btn_ask_assistant: "🌾 Ask Assistant",
            dash_btn_remove: "Remove",
            dash_reminders_title: "Farm Reminders & Tasks",
            dash_empty_reminders: "No active farm reminders scheduled. Add a seasonal task above (such as basal fertilizer split, irrigation cycle, or pest scouting) to track your field calendar.",
            dash_saved_plans_title: "Saved Crop Plans & Feasibility",
            dash_empty_plans: "No crop plans saved to memory. Generate budget-aware plans or low-cost farming alternatives in the Agricultural Assistant to compare seasonal financial feasibility here.",

            // History Page (history.html)
            hist_title: "KISAN AI",
            hist_subtitle: "Archived Farmer Queries & Grounded Recommendations",
            hist_total_queries: "Total Queries",
            hist_storage_backend: "Storage Backend",
            hist_supported_langs: "Supported Languages",
            hist_farm_dashboard: "🌾 Farm Dashboard",
            hist_back_assistant: "Back to Assistant",
            hist_empty_title: "No Queries Recorded Yet",
            hist_empty_desc: "Submitted agricultural questions, weather context, and grounded recommendations will appear here automatically.",
            hist_ask_first: "Ask Your First Question",
            hist_search_placeholder: "Search queries, crops, locations...",
            hist_filter_all: "All",

            // Auth Pages
            auth_login_title: "Farmer Sign In",
            auth_login_subtitle: "Access your personalized plots, reminders, and farm plans",
            auth_email_label: "Registered Email",
            auth_password_label: "Password",
            auth_forgot_link: "Forgot password?",
            auth_guest_chat: "Return to Guest Chat",
            auth_btn_login: "Sign In to Farm Dashboard",
            auth_no_account: "New farmer?",
            auth_register_link: "Create an account",

            auth_register_title: "Create Farmer Profile",
            auth_register_subtitle: "Store your land parcels, crop history, and personalized advisories",
            auth_fullname_label: "Full Name",
            auth_phone_label: "Phone Number (Optional)",
            auth_consent: "I consent to Kisan AI storing my farm parcel data and query history for personalized agronomical follow-ups.",
            auth_btn_register: "Register Account",
            auth_has_account: "Already registered?",
            auth_guest_mode: "Guest Mode",

            auth_forgot_title: "Password Recovery",
            auth_forgot_subtitle: "Enter your registered email to request password reset token",
            auth_btn_send_reset: "Generate Reset Instructions",
            auth_remember_creds: "Remember your credentials?",
            auth_back_login: "Back to Sign In",

            auth_reset_title: "Set New Password",
            auth_reset_subtitle: "Create a secure new password for your farmer account",
            auth_new_password_label: "New Password (Minimum 8 Characters)",
            auth_btn_reset_submit: "Update Password"
        },

        ta: {
            // Brand & Navigation
            brand_title: "கிசான் AI",
            brand_subtitle: "பல்மொழி வேளாண் சாட்பாட் • ICAR & TNAU வழிகாட்டுதல்",
            nav_home: "🌾 வேளாண் உதவியாளர்",
            nav_dashboard: "🌾 பண்ணை நினைவகம்",
            nav_history: "📜 கேள்வி வரலாறு",
            nav_login: "உள்நுழைய",
            nav_register: "பதிவு செய்ய",
            nav_logout: "வெளியேறு",
            nav_clear: "அழி",
            user_farmer: "விவசாயி",

            // AI Assistant Page (index.html)
            chat_welcome_title: "கிசான் AI உதவியாளருக்கு நல்வரவு",
            chat_welcome_desc: "விஞ்ஞானபூர்வ வேளாண் வழிகாட்டுதல், பூச்சிக்கொல்லி பாதுகாப்பு மற்றும் செலவு குறைப்புக்கான உங்களின் நம்பகமான துணைவன்.",
            chat_engine_label: "இயந்திரம்:",
            chat_engine_general: "1. இடர்-அறிந்த முடிவு உதவியாளர்",
            chat_engine_weather: "2. வானிலை & விதைப்பு இடர் ஆலோசனை",
            chat_engine_budget: "3. பட்ஜெட் பயிர் திட்டமிடல்",
            chat_engine_low_cost: "4. குறைந்த செலவு சாகுபடி திட்டம்",
            chat_engine_scheme: "5. அரசு திட்ட வழிகாட்டி",
            chat_engine_pesticide: "6. பூச்சிக்கொல்லி பாதுகாப்பு (CIBRC)",
            chat_engine_triage: "7. பயிர் நோய் & பூச்சி அறிதல்",
            chat_acres_label: "ஏக்கர்:",
            chat_language_label: "மொழி:",
            chat_lang_auto: "தானாக கண்டறி (Auto)",
            chat_lang_en: "ஆங்கிலம் (English)",
            chat_lang_ta: "தமிழ் (Tamil)",
            chat_lang_hi: "இந்தி (Hindi)",
            chat_location_label: "இடம்:",
            chat_location_placeholder: "எ.கா. தஞ்சாவூர், மதுரை",
            chat_season_label: "பருவம்:",
            chat_season_auto: "தானியங்கி (Auto)",
            chat_season_kharif: "காரிப் (Kharif)",
            chat_season_rabi: "ரபி (Rabi)",
            chat_season_summer: "கோடை (Summer)",
            chat_season_zaid: "ஜைத் (Zaid)",
            parcel_bar_title: "பண்ணை நிலப் பிரிவுகள் (பண்ணை நினைவகம்):",
            parcel_select_default: "-- இடம் மற்றும் பயிரை நிரப்ப நிலத்தைத் தேர்ந்தெடுக்கவும் --",
            btn_clear_plot: "அழி",
            quick_parcel_inquiries: "விரைவு நில வினவல்கள்:",
            chip_dosage: "🧪 பூச்சிக்கொல்லி & உர மருந்தளவு",
            chip_sowing: "🌦️ வானிலை & விதைப்பு இடர்",
            chip_budget: "💰 பட்ஜெட் பயிர் திட்டம்",
            chip_lowcost: "🌿 குறைந்த செலவு விவசாயம்",
            tip_no_parcels: "குறிப்பு: நீங்கள் இதுவரை எந்த நிலப்பிரிவையும் பதிவு செய்யவில்லை. இடம், பயிர் நிலை மற்றும் மண் pH-ஐ உதவியாளரில் ஏற்ற பண்ணை நினைவகத்தில் உங்கள் நிலங்களைச் சேர்க்கவும்.",
            chat_input_placeholder: "வேளாண் கேள்வி அல்லது பயிர் பிரச்சனையை விவரிக்கவும் (எ.கா. நெல்லில் பூச்சி தாக்குதலை தடுப்பது எப்படி?)... — அனுப்ப Enter அழுத்தவும்",
            chat_send_btn: "அனுப்பு",
            disclaimer_grounded: "ICAR/TNAU அறிவியல் மற்றும் நிகழ்நேர வானிலை தரவுகளுடன் ஒருங்கிணைக்கப்பட்டது",
            input_hint_enter: "அனுப்ப Enter ↵ • புதிய வரிக்கு Shift + Enter",
            btn_copy: "நகலெடு",
            btn_copied: "நகலெடுக்கப்பட்டது!",
            btn_listen: "கேட்க",
            verified_sources: "சரிபார்க்கப்பட்ட ஆராய்ச்சி மற்றும் தரவு மூலங்கள்",
            view_reference: "குறிப்பைப் பார்க்கவும்",

            // Dashboard Page (dashboard.html)
            dash_title: "கிசான் பண்ணை நினைவகம்",
            dash_subtitle: "தனிப்பயனாக்கப்பட்ட வயல்வெளி பதிவுகள் & பண்ணை காலண்டர்",
            dash_profile_heading: "விவசாய சுயவிவரம்",
            dash_state_label: "மாநிலம்",
            dash_district_label: "மாவட்டம்",
            dash_primary_crop_label: "முதன்மை சாகுபடி பயிர்",
            dash_land_acres_label: "மொத்த நிலப்பரப்பு (ஏக்கர்)",
            dash_soil_type_label: "முதன்மை மண் வகை",
            dash_irrigation_label: "முதன்மை பாசன ஆதாரம்",
            dash_budget_label: "பருவக்கால சாகுபடி பட்ஜெட் (₹ INR)",
            dash_language_label: "விருப்பமான ஆலோசனை மொழி",
            dash_btn_save_profile: "சுயவிவர மாற்றங்களைச் சேமிக்கவும்",
            dash_plots_title: "நிலப் பிரிவுகள் & வயல்வெளிகள்",
            dash_new_parcel_heading: "புதிய நிலப் பிரிவு பதிவு செய்க",
            dash_plot_name_label: "நிலம் / வயல் அடையாளம்",
            dash_plot_area_label: "பரப்பளவு (ஏக்கர்)",
            dash_current_crop_label: "தற்போதைய பயிர் ரகம்",
            dash_crop_stage_label: "பயிர் வளர்ச்சி நிலை",
            dash_plot_location_label: "நிலத்தின் இடம் / கிராமம் / மாவட்டம்",
            dash_soil_ph_label: "மண் pH அளவு (மண் பரிசோதனை)",
            dash_soil_oc_label: "கரிம கார்பன் (%)",
            dash_btn_save_parcel: "நிலத்தை பண்ணை நினைவகத்தில் சேமிக்கவும்",
            dash_empty_plots: "நினைவகத்தில் நிலங்கள் எதுவும் பதிவு செய்யப்படவில்லை. நிலம் சார்ந்த எச்சரிக்கைகளைப் பெற மண் pH, இடம் மற்றும் பயிர் நிலையுடன் உங்கள் முதல் நிலப் பிரிவை பதிவு செய்யவும்.",
            dash_btn_ask_assistant: "🌾 உதவியாளரிடம் கேள்",
            dash_btn_remove: "நீக்கு",
            dash_reminders_title: "பண்ணை நினைவூட்டல்கள் & பணிகள்",
            dash_empty_reminders: "செயலில் உள்ள பண்ணை நினைவூட்டல்கள் எதுவும் இல்லை. உங்கள் வயல் காலண்டரைக் கண்காணிக்க புதிய பணியைச் சேர்க்கவும்.",
            dash_saved_plans_title: "சேமிக்கப்பட்ட பயிர் திட்டங்கள் & சாத்தியக்கூறு",
            dash_empty_plans: "நினைவகத்தில் பயிர் திட்டங்கள் எதுவும் சேமிக்கப்படவில்லை. நிதி சாத்தியத்தை ஒப்பிட வேளாண் உதவியாளரில் பட்ஜெட் திட்டங்களை உருவாக்கவும்.",

            // History Page (history.html)
            hist_title: "கிசான் AI",
            hist_subtitle: "பதிவு செய்யப்பட்ட விவசாய வினவல்கள் & பரிந்துரைகள்",
            hist_total_queries: "மொத்த கேள்விகள்",
            hist_storage_backend: "தரவுத்தள சேமிப்பகம்",
            hist_supported_langs: "ஆதரிக்கப்படும் மொழிகள்",
            hist_farm_dashboard: "🌾 பண்ணை டாஷ்போர்டு",
            hist_back_assistant: "உதவியாளருக்கு திரும்பு",
            hist_empty_title: "இன்னும் கேள்விகள் பதிவு செய்யப்படவில்லை",
            hist_empty_desc: "நீங்கள் கேட்கும் வேளாண் கேள்விகள், வானிலை மற்றும் பரிந்துரைகள் இங்கே தானாக தோன்றும்.",
            hist_ask_first: "உங்கள் முதல் கேள்வியைக் கேளுங்கள்",
            hist_search_placeholder: "கேள்விகள், பயிர்கள், இடங்களைத் தேடுங்கள்...",
            hist_filter_all: "அனைத்தும்",

            // Auth Pages
            auth_login_title: "விவசாயி உள்நுழைவு",
            auth_login_subtitle: "உங்கள் நில விவரங்கள், நினைவூட்டல்கள் மற்றும் திட்டங்களை அணுக உள்நுழையவும்",
            auth_email_label: "பதிவுசெய்த மின்னஞ்சல்",
            auth_password_label: "கடவுச்சொல்",
            auth_forgot_link: "கடவுச்சொல் மறந்துவிட்டதா?",
            auth_guest_chat: "விருந்தினர் உரையாடலுக்கு திரும்பு",
            auth_btn_login: "பண்ணை டாஷ்போர்டில் உள்நுழையவும்",
            auth_no_account: "புதிய விவசாயியா?",
            auth_register_link: "புதிய கணக்கை உருவாக்கவும்",

            auth_register_title: "விவசாயி சுயவிவரம் உருவாக்கு",
            auth_register_subtitle: "உங்கள் நிலங்கள், பயிர் வரலாறு மற்றும் ஆலோசனைகளை சேமித்து வைக்கவும்",
            auth_fullname_label: "முழு பெயர்",
            auth_phone_label: "தொலைபேசி எண் (விருப்பத்தேர்வு)",
            auth_consent: "தனிப்பயனாக்கப்பட்ட வேளாண்மை வழிகாட்டலுக்காக எனது பண்ணை நில விவரங்களை கிசான் AI சேமிக்க ஒப்புக்கொள்கிறேன்.",
            auth_btn_register: "கணக்கை பதிவு செய்யவும்",
            auth_has_account: "ஏற்கனவே பதிவு செய்தவரா?",
            auth_guest_mode: "விருந்தினர் பயன்முறை",

            auth_forgot_title: "கடவுச்சொல் மீட்பு",
            auth_forgot_subtitle: "மீட்டமைப்பு டோக்கனைப் பெற உங்கள் பதிவுசெய்த மின்னஞ்சலை உள்ளிடவும்",
            auth_btn_send_reset: "மீட்டமைப்பு வழிமுறைகளை உருவாக்கவும்",
            auth_remember_creds: "கடவுச்சொல் நினைவிருக்கிறதா?",
            auth_back_login: "மீண்டும் உள்நுழைவுக்குச் செல்ல",

            auth_reset_title: "புதிய கடவுச்சொல்லை அமைக்கவும்",
            auth_reset_subtitle: "உங்கள் விவசாயி கணக்கிற்கு பாதுகாப்பான புதிய கடவுச்சொல்லை உருவாக்கவும்",
            auth_new_password_label: "புதிய கடவுச்சொல் (குறைந்தது 8 எழுத்துக்கள்)",
            auth_btn_reset_submit: "கடவுச்சொல்லை புதுப்பிக்கவும்"
        },

        hi: {
            // Brand & Navigation
            brand_title: "किसान AI",
            brand_subtitle: "बहुभाषी कृषि सहायक • ICAR एवं TNAU आधारित",
            nav_home: "🌾 कृषि सहायक",
            nav_dashboard: "🌾 फार्म मेमोरी",
            nav_history: "📜 प्रश्न इतिहास",
            nav_login: "साइन इन",
            nav_register: "पंजीकरण",
            nav_logout: "लॉग आउट",
            nav_clear: "साफ़ करें",
            user_farmer: "किसान",

            // AI Assistant Page (index.html)
            chat_welcome_title: "किसान AI सहायक में आपका स्वागत है",
            chat_welcome_desc: "वैज्ञानिक कृषि परामर्श, कीटनाशक सुरक्षा और लागत अनुकूलन हेतु आपका विश्वसनीय डिजिटल साथी।",
            chat_engine_label: "इंजन:",
            chat_engine_general: "1. जोखिम-जागरूक निर्णय सहायक",
            chat_engine_weather: "2. मौसम व बुवाई जोखिम परामर्श",
            chat_engine_budget: "3. बजट-आधारित फसल योजना",
            chat_engine_low_cost: "4. कम लागत वाली खेती योजना",
            chat_engine_scheme: "5. सरकारी योजना मार्गदर्शक",
            chat_engine_pesticide: "6. कीटनाशक सुरक्षा एवं लेबल जांच (CIBRC)",
            chat_engine_triage: "7. फसल रोग एवं कीट निदान",
            chat_acres_label: "एकड़:",
            chat_language_label: "भाषा:",
            chat_lang_auto: "स्वतः पहचान (Auto)",
            chat_lang_en: "अंग्रेजी (English)",
            chat_lang_ta: "तमिल (Tamil)",
            chat_lang_hi: "हिंदी (Hindi)",
            chat_location_label: "स्थान:",
            chat_location_placeholder: "उदा. पुणे, करनाल, इंदौर",
            chat_season_label: "मौसम:",
            chat_season_auto: "स्वतः (Auto)",
            chat_season_kharif: "खरीफ (Kharif)",
            chat_season_rabi: "रबी (Rabi)",
            chat_season_summer: "ग्रीष्म / जायद",
            chat_season_zaid: "जायद (Zaid)",
            parcel_bar_title: "खेत के भूखंड (फार्म मेमोरी):",
            parcel_select_default: "-- स्थान व फसल स्वतः भरने हेतु भूखंड चुनें --",
            btn_clear_plot: "साफ़ करें",
            quick_parcel_inquiries: "त्वरित पार्सल पूछताछ:",
            chip_dosage: "🧪 कीटनाशक और उर्वरक मात्रा",
            chip_sowing: "🌦️ मौसम और बुवाई जोखिम",
            chip_budget: "💰 बजट फसल योजना",
            chip_lowcost: "🌿 कम लागत वाली खेती",
            tip_no_parcels: "सुझाव: आपने अभी तक कोई भूमि पार्सल दर्ज नहीं किया है। स्थान, फसल चरण और मिट्टी का पीएच सहायक में लोड करने के लिए फार्म मेमोरी में अपने पार्सल जोड़ें।",
            chat_input_placeholder: "अपना कृषि प्रश्न पूछें... (उदा: धान में कीट नियंत्रण कैसे करें?) — भेजने के लिए Enter दबाएं",
            chat_send_btn: "भेजें",
            disclaimer_grounded: "ICAR/TNAU वैज्ञानिक डेटा और वास्तविक समय के मौसम से एकीकृत",
            input_hint_enter: "भेजने के लिए Enter ↵ • नई पंक्ति के लिए Shift + Enter",
            btn_copy: "कॉपी करें",
            btn_copied: "कॉपी हो गया!",
            btn_listen: "सुनें",
            verified_sources: "सत्यापित अनुसंधान एवं डेटा स्रोत",
            view_reference: "संदर्भ देखें",

            // Dashboard Page (dashboard.html)
            dash_title: "किसान फार्म मेमोरी",
            dash_subtitle: "व्यक्तिगत खेत रिकॉर्ड एवं कृषि कार्य कैलेंडर",
            dash_profile_heading: "कृषि प्रोफाइल",
            dash_state_label: "राज्य",
            dash_district_label: "जिला",
            dash_primary_crop_label: "प्रमुख उगाई जाने वाली फसल",
            dash_land_acres_label: "कुल भूमि जोत (एकड़)",
            dash_soil_type_label: "प्रमुख मृदा प्रकार",
            dash_irrigation_label: "प्रमुख सिंचाई स्रोत",
            dash_budget_label: "मौसमी कार्यशील बजट (₹ INR)",
            dash_language_label: "पसंदीदा परामर्श भाषा",
            dash_btn_save_profile: "कृषि प्रोफाइल परिवर्तन सहेजें",
            dash_plots_title: "भूमि पार्सल एवं भूखंड",
            dash_new_parcel_heading: "नया खेत पार्सल दर्ज करें",
            dash_plot_name_label: "पार्सल पहचान / नाम",
            dash_plot_area_label: "पार्सल रकबा (एकड़)",
            dash_current_crop_label: "वर्तमान फसल किस्म",
            dash_crop_stage_label: "फसल वृद्धि चरण",
            dash_plot_location_label: "भूखंड स्थान / गांव / जिला",
            dash_soil_ph_label: "मृदा pH (परीक्षण से)",
            dash_soil_oc_label: "कार्बनिक कार्बन (%)",
            dash_btn_save_parcel: "पार्सल को फार्म मेमोरी में सहेजें",
            dash_empty_plots: "मेमोरी में कोई भूमि पार्सल दर्ज नहीं है। पार्सल-विशिष्ट जोखिम चेतावनियां पाने के लिए मृदा pH, स्थान और फसल चरण के साथ अपना पहला खेत दर्ज करें।",
            dash_btn_ask_assistant: "🌾 सहायक से पूछें",
            dash_btn_remove: "हटाएं",
            dash_reminders_title: "खेत अनुस्मारक एवं कार्य",
            dash_empty_reminders: "कोई सक्रिय अनुस्मारक निर्धारित नहीं है। अपने खेत कैलेंडर को ट्रैक करने के लिए ऊपर एक मौसमी कार्य जोड़ें।",
            dash_saved_plans_title: "सहेजी गई फसल योजनाएं एवं व्यवहार्यता",
            dash_empty_plans: "मेमोरी में कोई फसल योजना सहेजी नहीं गई है। मौसमी वित्तीय व्यवहार्यता की तुलना करने के लिए कृषि सहायक में बजट योजनाएं बनाएं।",

            // History Page (history.html)
            hist_title: "किसान AI",
            hist_subtitle: "संग्रहीत किसान प्रश्न एवं वैज्ञानिक सिफारिशें",
            hist_total_queries: "कुल प्रश्न",
            hist_storage_backend: "भंडारण बैकएंड",
            hist_supported_langs: "समर्थित भाषाएं",
            hist_farm_dashboard: "🌾 फार्म डैशबोर्ड",
            hist_back_assistant: "सहायक पर वापस जाएं",
            hist_empty_title: "अभी तक कोई प्रश्न दर्ज नहीं है",
            hist_empty_desc: "आपके पूछे गए कृषि प्रश्न, मौसम संदर्भ और सिफारिशें यहां स्वतः दिखाई देंगी।",
            hist_ask_first: "अपना पहला प्रश्न पूछें",
            hist_search_placeholder: "प्रश्न, फसल या स्थान खोजें...",
            hist_filter_all: "सभी",

            // Auth Pages
            auth_login_title: "किसान साइन इन",
            auth_login_subtitle: "अपने व्यक्तिगत भूखंड, अनुस्मारक और फसल योजनाएं देखने हेतु लॉगिन करें",
            auth_email_label: "पंजीकृत ईमेल",
            auth_password_label: "पासवर्ड",
            auth_forgot_link: "पासवर्ड भूल गए?",
            auth_guest_chat: "अतिथि चैट पर वापस जाएं",
            auth_btn_login: "फार्म डैशबोर्ड में साइन इन करें",
            auth_no_account: "नए किसान हैं?",
            auth_register_link: "खाता बनाएं",

            auth_register_title: "किसान प्रोफाइल बनाएं",
            auth_register_subtitle: "अपने भूमि पार्सल, फसल इतिहास और व्यक्तिगत परामर्श सहेजें",
            auth_fullname_label: "पूरा नाम",
            auth_phone_label: "फ़ोन नंबर (वैकल्पिक)",
            auth_consent: "मैं व्यक्तिगत कृषि सलाह हेतु किसान AI को अपनी भूमि और प्रश्न इतिहास सहेजने की सहमति देता हूँ।",
            auth_btn_register: "खाता पंजीकृत करें",
            auth_has_account: "पहले से पंजीकृत हैं?",
            auth_guest_mode: "अतिथि मोड",

            auth_forgot_title: "पासवर्ड पुनर्प्राप्ति",
            auth_forgot_subtitle: "पासवर्ड रीसेट टोकन हेतु अपना पंजीकृत ईमेल दर्ज करें",
            auth_btn_send_reset: "रीसेट निर्देश उत्पन्न करें",
            auth_remember_creds: "पासवर्ड याद है?",
            auth_back_login: "साइन इन पर वापस जाएं",

            auth_reset_title: "नया पासवर्ड सेट करें",
            auth_reset_subtitle: "अपने किसान खाते के लिए सुरक्षित नया पासवर्ड बनाएं",
            auth_new_password_label: "नया पासवर्ड (न्यूनतम 8 वर्ण)",
            auth_btn_reset_submit: "पासवर्ड अपडेट करें"
        }
    };

    const STORAGE_KEY = 'kisan_language';

    function getCurrentLanguage() {
        return localStorage.getItem(STORAGE_KEY) || 'en';
    }

    function setKisanLanguage(lang) {
        if (!TRANSLATIONS[lang]) {
            lang = 'en';
        }
        localStorage.setItem(STORAGE_KEY, lang);
        applyLanguage(lang);

        // Dispatch custom event for page scripts
        window.dispatchEvent(new CustomEvent('kisan-language-changed', {
            detail: { language: lang }
        }));
    }

    function applyLanguage(lang) {
        const dict = TRANSLATIONS[lang] || TRANSLATIONS.en;
        document.documentElement.lang = lang;

        // Apply Indic font helper class
        if (lang === 'ta') {
            document.body.classList.add('lang-tamil');
            document.body.classList.remove('lang-hindi');
        } else if (lang === 'hi') {
            document.body.classList.add('lang-hindi');
            document.body.classList.remove('lang-tamil');
        } else {
            document.body.classList.remove('lang-tamil', 'lang-hindi');
        }

        // 1. Text content replacement via data-i18n
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (dict[key]) {
                el.textContent = dict[key];
            }
        });

        // 2. Placeholder replacement via data-i18n-placeholder
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            if (dict[key]) {
                el.placeholder = dict[key];
            }
        });

        // 3. Title / tooltip replacement via data-i18n-title
        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const key = el.getAttribute('data-i18n-title');
            if (dict[key]) {
                el.title = dict[key];
            }
        });

        // 4. ARIA label replacement via data-i18n-aria
        document.querySelectorAll('[data-i18n-aria]').forEach(el => {
            const key = el.getAttribute('data-i18n-aria');
            if (dict[key]) {
                el.setAttribute('aria-label', dict[key]);
            }
        });

        // 5. Update language button active classes
        document.querySelectorAll('.kisan-lang-btn').forEach(btn => {
            const btnLang = btn.getAttribute('data-lang');
            if (btnLang === lang) {
                btn.classList.add('active');
                btn.setAttribute('aria-pressed', 'true');
            } else {
                btn.classList.remove('active');
                btn.setAttribute('aria-pressed', 'false');
            }
        });

        // 6. Synchronize chat dropdown on index.html if present
        const chatLangSelect = document.getElementById('chatLanguage');
        if (chatLangSelect) {
            chatLangSelect.value = lang;
        }
    }

    function renderLanguageSwitcher() {
        const containers = document.querySelectorAll('.kisan-lang-container');
        if (containers.length === 0) return;

        const currentLang = getCurrentLanguage();

        const html = `
            <div class="kisan-lang-switcher" role="region" aria-label="Website Language Selector">
                <span class="lang-globe-icon" aria-hidden="true">🌐</span>
                <button type="button" class="kisan-lang-btn ${currentLang === 'en' ? 'active' : ''}" 
                        data-lang="en" aria-pressed="${currentLang === 'en'}">English</button>
                <button type="button" class="kisan-lang-btn ${currentLang === 'ta' ? 'active' : ''}" 
                        data-lang="ta" aria-pressed="${currentLang === 'ta'}">தமிழ்</button>
                <button type="button" class="kisan-lang-btn ${currentLang === 'hi' ? 'active' : ''}" 
                        data-lang="hi" aria-pressed="${currentLang === 'hi'}">हिंदी</button>
            </div>
        `;

        containers.forEach(container => {
            container.innerHTML = html;
            container.querySelectorAll('.kisan-lang-btn').forEach(btn => {
                btn.addEventListener('click', function () {
                    const targetLang = this.getAttribute('data-lang');
                    setKisanLanguage(targetLang);
                });
            });
        });
    }

    // Expose global methods
    window.KisanI18n = {
        getLanguage: getCurrentLanguage,
        setLanguage: setKisanLanguage,
        applyLanguage: applyLanguage,
        t: function (key, lang) {
            const selectedLang = lang || getCurrentLanguage();
            const dict = TRANSLATIONS[selectedLang] || TRANSLATIONS.en;
            return dict[key] || key;
        }
    };

    // Auto-initialize on DOM ready
    document.addEventListener('DOMContentLoaded', function () {
        renderLanguageSwitcher();
        applyLanguage(getCurrentLanguage());
    });

})();
