package com.example.core.i18n

import com.example.domain.model.University

/**
 * Localizes Hungarian university names and academic degree programs / majors
 * into English and German while preserving authentic Hungarian names.
 */
object AcademicDataLocalizer {

    // =========================================================================
    // UNIVERSITIES
    // =========================================================================

    private data class UniTranslation(
        val en: String,
        val de: String
    )

    private val universityTranslations = mapOf(
        // BME
        "budapesti muszaki es gazdasagtudomanyi egyetem" to UniTranslation(
            en = "Budapest University of Technology and Economics",
            de = "Technische und Wirtschaftswissenschaftliche Universität Budapest"
        ),
        "bme" to UniTranslation(
            en = "Budapest University of Technology and Economics",
            de = "Technische und Wirtschaftswissenschaftliche Universität Budapest"
        ),

        // ELTE
        "eotvos lorand tudomanyegyetem" to UniTranslation(
            en = "Eötvös Loránd University",
            de = "Eötvös-Loránd-Universität"
        ),
        "elte" to UniTranslation(
            en = "Eötvös Loránd University",
            de = "Eötvös-Loránd-Universität"
        ),

        // Corvinus
        "budapesti corvinus egyetem" to UniTranslation(
            en = "Corvinus University of Budapest",
            de = "Corvinus-Universität Budapest"
        ),
        "corvinus" to UniTranslation(
            en = "Corvinus University of Budapest",
            de = "Corvinus-Universität Budapest"
        ),
        "bce" to UniTranslation(
            en = "Corvinus University of Budapest",
            de = "Corvinus-Universität Budapest"
        ),

        // BGE
        "budapesti gazdasagi egyetem" to UniTranslation(
            en = "Budapest Business University",
            de = "Wirtschaftsuniversität Budapest"
        ),
        "bge" to UniTranslation(
            en = "Budapest Business University",
            de = "Wirtschaftsuniversität Budapest"
        ),

        // METU
        "budapesti metropolitan egyetem" to UniTranslation(
            en = "Budapest Metropolitan University",
            de = "Metropolitan-Universität Budapest"
        ),
        "metu" to UniTranslation(
            en = "Budapest Metropolitan University",
            de = "Metropolitan-Universität Budapest"
        ),

        // Óbuda
        "obudai egyetem" to UniTranslation(
            en = "Óbuda University",
            de = "Óbuda-Universität"
        ),
        "oe" to UniTranslation(
            en = "Óbuda University",
            de = "Óbuda-Universität"
        ),

        // Semmelweis
        "semmelweis egyetem" to UniTranslation(
            en = "Semmelweis University",
            de = "Semmelweis-Universität"
        ),
        "sote" to UniTranslation(
            en = "Semmelweis University",
            de = "Semmelweis-Universität"
        ),
        "se" to UniTranslation(
            en = "Semmelweis University",
            de = "Semmelweis-Universität"
        ),

        // Debreceni Egyetem
        "debreceni egyetem" to UniTranslation(
            en = "University of Debrecen",
            de = "Universität Debrecen"
        ),
        "de" to UniTranslation(
            en = "University of Debrecen",
            de = "Universität Debrecen"
        ),

        // Szegedi Tudományegyetem
        "szegedi tudomanyegyetem" to UniTranslation(
            en = "University of Szeged",
            de = "Universität Szeged"
        ),
        "szte" to UniTranslation(
            en = "University of Szeged",
            de = "Universität Szeged"
        ),

        // Pécsi Tudományegyetem
        "pecsi tudomanyegyetem" to UniTranslation(
            en = "University of Pécs",
            de = "Universität Pécs"
        ),
        "pte" to UniTranslation(
            en = "University of Pécs",
            de = "Universität Pécs"
        ),

        // Pázmány
        "pazmany peter katolikus egyetem" to UniTranslation(
            en = "Pázmány Péter Catholic University",
            de = "Katholische Péter-Pázmány-Universität"
        ),
        "ppke" to UniTranslation(
            en = "Pázmány Péter Catholic University",
            de = "Katholische Péter-Pázmány-Universität"
        ),

        // Széchenyi István Egyetem
        "szechenyi istvan egyetem" to UniTranslation(
            en = "Széchenyi István University",
            de = "Széchenyi-István-Universität"
        ),
        "sze" to UniTranslation(
            en = "Széchenyi István University",
            de = "Széchenyi-István-Universität"
        ),

        // Miskolci Egyetem
        "miskolci egyetem" to UniTranslation(
            en = "University of Miskolc",
            de = "Universität Miskolc"
        ),
        "me" to UniTranslation(
            en = "University of Miskolc",
            de = "Universität Miskolc"
        ),

        // Pannon Egyetem
        "pannon egyetem" to UniTranslation(
            en = "University of Pannonia",
            de = "Pannonische Universität"
        ),
        "pe" to UniTranslation(
            en = "University of Pannonia",
            de = "Pannonische Universität"
        ),

        // NKE
        "nemzeti kozszolgalati egyetem" to UniTranslation(
            en = "Ludovika University of Public Service",
            de = "Ludovika-Universität für den öffentlichen Dienst"
        ),
        "nke" to UniTranslation(
            en = "Ludovika University of Public Service",
            de = "Ludovika-Universität für den öffentlichen Dienst"
        ),

        // Állatorvostudományi
        "allatorvostudomanyi egyetem" to UniTranslation(
            en = "University of Veterinary Medicine Budapest",
            de = "Veterinärmedizinische Universität Budapest"
        ),
        "ate" to UniTranslation(
            en = "University of Veterinary Medicine Budapest",
            de = "Veterinärmedizinische Universität Budapest"
        ),

        // MATE
        "magyar agrar- es elettudomanyi egyetem" to UniTranslation(
            en = "Hungarian University of Agriculture and Life Sciences",
            de = "Ungarische Universität für Landwirtschaft und Biowissenschaften"
        ),
        "mate" to UniTranslation(
            en = "Hungarian University of Agriculture and Life Sciences",
            de = "Ungarische Universität für Landwirtschaft und Biowissenschaften"
        ),

        // MKE
        "magyar kepzomuveszeti egyetem" to UniTranslation(
            en = "Hungarian University of Fine Arts",
            de = "Ungarische Akademie der Bildenden Künste"
        ),
        "mke" to UniTranslation(
            en = "Hungarian University of Fine Arts",
            de = "Ungarische Akademie der Bildenden Künste"
        ),

        // LFZE
        "liszt ferenc zenemuveszeti egyetem" to UniTranslation(
            en = "Liszt Ferenc Academy of Music",
            de = "Franz-Liszt-Musikakademie"
        ),
        "lfze" to UniTranslation(
            en = "Liszt Ferenc Academy of Music",
            de = "Franz-Liszt-Musikakademie"
        ),

        // SZFE
        "szinhaz- es filmmuveszeti egyetem" to UniTranslation(
            en = "University of Theatre and Film Arts",
            de = "Universität für Theater- und Filmkunst"
        ),
        "szfe" to UniTranslation(
            en = "University of Theatre and Film Arts",
            de = "Universität für Theater- und Filmkunst"
        ),

        // MOME
        "moholy-nagy muveszeti egyetem" to UniTranslation(
            en = "Moholy-Nagy University of Art and Design",
            de = "Moholy-Nagy-Universität für Kunsthandwerk und Gestaltung"
        ),
        "mome" to UniTranslation(
            en = "Moholy-Nagy University of Art and Design",
            de = "Moholy-Nagy-Universität für Kunsthandwerk und Gestaltung"
        ),

        // KRE
        "karoli gaspar reformatus egyetem" to UniTranslation(
            en = "Károli Gáspár University of the Reformed Church",
            de = "Károli-Gáspár-Universität der Reformierten Kirche"
        ),
        "kre" to UniTranslation(
            en = "Károli Gáspár University of the Reformed Church",
            de = "Károli-Gáspár-Universität der Reformierten Kirche"
        ),

        // DUE
        "dunaujvarosi egyetem" to UniTranslation(
            en = "University of Dunaújváros",
            de = "Universität Dunaújváros"
        ),
        "due" to UniTranslation(
            en = "University of Dunaújváros",
            de = "Universität Dunaújváros"
        ),

        // Milton Friedman
        "milton friedman egyetem" to UniTranslation(
            en = "Milton Friedman University",
            de = "Milton-Friedman-Universität"
        ),

        // Soproni Egyetem
        "soproni egyetem" to UniTranslation(
            en = "University of Sopron",
            de = "Universität Sopron"
        ),
        "soe" to UniTranslation(
            en = "University of Sopron",
            de = "Universität Sopron"
        ),

        // NJE
        "neumann janos egyetem" to UniTranslation(
            en = "John von Neumann University",
            de = "John-von-Neumann-Universität"
        ),
        "nje" to UniTranslation(
            en = "John von Neumann University",
            de = "John-von-Neumann-Universität"
        ),

        // TF
        "magyar testnevelesi es sporttudomanyi egyetem" to UniTranslation(
            en = "Hungarian University of Sports Science",
            de = "Ungarische Universität für Sportwissenschaften"
        ),
        "testnevelesi egyetem" to UniTranslation(
            en = "Hungarian University of Sports Science",
            de = "Ungarische Universität für Sportwissenschaften"
        ),
        "tf" to UniTranslation(
            en = "Hungarian University of Sports Science",
            de = "Ungarische Universität für Sportwissenschaften"
        ),

        // GFE
        "gal ferenc egyetem" to UniTranslation(
            en = "Gál Ferenc University",
            de = "Gál-Ferenc-Universität"
        ),
        "gfe" to UniTranslation(
            en = "Gál Ferenc University",
            de = "Gál-Ferenc-Universität"
        ),

        // NYE
        "nyiregyhazi egyetem" to UniTranslation(
            en = "University of Nyíregyháza",
            de = "Universität Nyíregyháza"
        ),
        "nye" to UniTranslation(
            en = "University of Nyíregyháza",
            de = "Universität Nyíregyháza"
        ),

        // EKKE
        "eszterhazy karoly katolikus egyetem" to UniTranslation(
            en = "Eszterházy Károly Catholic University",
            de = "Katholische Eszterházy-Károly-Universität"
        ),
        "eszterhazy karoly egyetem" to UniTranslation(
            en = "Eszterházy Károly Catholic University",
            de = "Katholische Eszterházy-Károly-Universität"
        ),
        "ekke" to UniTranslation(
            en = "Eszterházy Károly Catholic University",
            de = "Katholische Eszterházy-Károly-Universität"
        ),

        // Tokaj-Hegyalja
        "tokaj-hegyalja egyetem" to UniTranslation(
            en = "Tokaj-Hegyalja University",
            de = "Tokaj-Hegyalja-Universität"
        ),
        "the" to UniTranslation(
            en = "Tokaj-Hegyalja University",
            de = "Tokaj-Hegyalja-Universität"
        ),

        // Colleges & Other Institutions
        "a tan kapuja buddhista foiskola" to UniTranslation(
            en = "Dharma Gate Buddhist College",
            de = "Dharma-Tor Buddhistische Hochschule"
        ),
        "atkb" to UniTranslation(
            en = "Dharma Gate Buddhist College",
            de = "Dharma-Tor Buddhistische Hochschule"
        ),
        "apor vilmos katolikus foiskola" to UniTranslation(
            en = "Vilmos Apor Catholic College",
            de = "Katholische Hochschule Vilmos Apor"
        ),
        "avkf" to UniTranslation(
            en = "Vilmos Apor Catholic College",
            de = "Katholische Hochschule Vilmos Apor"
        ),
        "bhaktivedanta hittudomanyi foiskola" to UniTranslation(
            en = "Bhaktivedanta College",
            de = "Bhaktivedanta Theologische Hochschule"
        ),
        "brenner janos hittudomanyi foiskola" to UniTranslation(
            en = "János Brenner Theological College",
            de = "Theologische Hochschule János Brenner"
        ),
        "debreceni reformatus hittudomanyi egyetem" to UniTranslation(
            en = "Debrecen Reformed Theological University",
            de = "Reformierte Theologische Universität Debrecen"
        ),
        "edutus foiskola" to UniTranslation(
            en = "Edutus University",
            de = "Edutus Universität"
        ),
        "edutus egyetem" to UniTranslation(
            en = "Edutus University",
            de = "Edutus Universität"
        ),
        "evangelikus hittudomanyi egyetem" to UniTranslation(
            en = "Evangelical Lutheran Theological University",
            de = "Evangelisch-Lutherische Theologische Universität"
        ),
        "gabor denes egyetem" to UniTranslation(
            en = "Dennis Gabor University",
            de = "Dennis-Gabor-Universität"
        ),
        "gabor denes foiskola" to UniTranslation(
            en = "Dennis Gabor University",
            de = "Dennis-Gabor-Universität"
        ),
        "ibs nemzetkozi uzleti foiskola" to UniTranslation(
            en = "International Business School (IBS)",
            de = "Internationale Wirtschaftshochschule (IBS)"
        ),
        "orszagos rabbikepzo - zsido egyetem" to UniTranslation(
            en = "Jewish Theological Seminary – University of Jewish Studies",
            de = "Jüdisch-Theologisches Seminar – Universität für Jüdische Studien"
        ),
        "pecsi puspoki hittudomanyi foiskola" to UniTranslation(
            en = "Episcopal Theological College of Pécs",
            de = "Bischöfliche Theologische Hochschule Pécs"
        ),
        "punkosdi teologiai foiskola" to UniTranslation(
            en = "Pentecostal Theological College",
            de = "Pfingstlich-Theologische Hochschule"
        ),
        "sapientia szerzetesi hittudomanyi foiskola" to UniTranslation(
            en = "Sapientia College of Theology",
            de = "Ordenshochschule für Theologie Sapientia"
        ),
        "sarospataki reformatus teologiai akademia" to UniTranslation(
            en = "Sárospatak Reformed Theological Academy",
            de = "Reformierte Theologische Akademie Sárospatak"
        ),
        "selye janos egyetem" to UniTranslation(
            en = "J. Selye University",
            de = "J.-Selye-Universität"
        ),
        "szent atanaz gorogkatolikus hittudomanyi foiskola" to UniTranslation(
            en = "Saint Athanasius Greek Catholic Theological Institute",
            de = "Griechisch-Katholische Theologische Hochschule St. Athanasius"
        ),
        "tomori pal foiskola" to UniTranslation(
            en = "Tomori Pál College",
            de = "Tomori-Pál-Hochschule"
        ),
        "wekerle sandor uzleti foiskola" to UniTranslation(
            en = "Wekerle Business School",
            de = "Wekerle-Wirtschaftshochschule"
        ),
        "wesley janos lelkepzokepzo foiskola" to UniTranslation(
            en = "John Wesley Theological College",
            de = "Theologische Hochschule John Wesley"
        )
    )

    fun localizeUniversityName(rawName: String?, languageCode: String): String {
        if (rawName.isNullOrBlank()) return ""
        val trimmed = rawName.trim()
        if (languageCode == "hu") return trimmed

        val normalized = normalizeKey(trimmed)

        // 1. Direct match
        universityTranslations[normalized]?.let { translation ->
            return if (languageCode == "de") translation.de else translation.en
        }

        // 2. Substring match for known institution names
        for ((key, translation) in universityTranslations) {
            if (key.length >= 6 && (normalized.contains(key) || key.contains(normalized))) {
                return if (languageCode == "de") translation.de else translation.en
            }
        }

        // 3. Heuristic fallback for any unlisted institution
        return fallbackTranslateUniversity(trimmed, languageCode)
    }

    fun localizeUniversity(uni: University?, languageCode: String): String {
        if (uni == null) return ""
        val translated = localizeUniversityName(uni.name, languageCode)
        return if (translated.isNotBlank()) translated else uni.name
    }

    private fun fallbackTranslateUniversity(name: String, languageCode: String): String {
        if (languageCode == "hu") return name

        var result = name
        if (languageCode == "en") {
            result = result
                .replace("Tudományegyetem", "University")
                .replace("tudományegyetem", "University")
                .replace("Katolikus Egyetem", "Catholic University")
                .replace("Református Egyetem", "Reformed University")
                .replace("Gazdasági Egyetem", "Business University")
                .replace("Műszaki Egyetem", "Technical University")
                .replace("Művészeti Egyetem", "University of Arts")
                .replace("Egyetem", "University")
                .replace("egyetem", "University")
                .replace("Főiskola", "College")
                .replace("főiskola", "College")
                .replace("Akadémia", "Academy")
                .replace("akadémia", "Academy")
        } else if (languageCode == "de") {
            result = result
                .replace("Tudományegyetem", "Universität")
                .replace("tudományegyetem", "Universität")
                .replace("Katolikus Egyetem", "Katholische Universität")
                .replace("Református Egyetem", "Reformierte Universität")
                .replace("Gazdasági Egyetem", "Wirtschaftsuniversität")
                .replace("Műszaki Egyetem", "Technische Universität")
                .replace("Művészeti Egyetem", "Kunstuniversität")
                .replace("Egyetem", "Universität")
                .replace("egyetem", "Universität")
                .replace("Főiskola", "Hochschule")
                .replace("főiskola", "Hochschule")
                .replace("Akadémia", "Akademie")
                .replace("akadémia", "Akademie")
        }
        return result
    }

    // =========================================================================
    // ACADEMIC DEGREE PROGRAMS / MAJORS (KÉPZÉSEK)
    // =========================================================================

    private data class ProgramTranslation(
        val en: String,
        val de: String
    )

    private val programTranslations = mapOf(
        // Informatics / Computer Science
        "mernokinformatikus" to ProgramTranslation(
            en = "Computer Science and Engineering",
            de = "Technische Informatik"
        ),
        "mernokinformatika" to ProgramTranslation(
            en = "Computer Science and Engineering",
            de = "Technische Informatik"
        ),
        "programtervezo informatikus" to ProgramTranslation(
            en = "Computer Science / Software Engineering",
            de = "Informatik / Softwaretechnik"
        ),
        "programtervezo informatika" to ProgramTranslation(
            en = "Computer Science / Software Engineering",
            de = "Informatik / Softwaretechnik"
        ),
        "gazdasaginformatikus" to ProgramTranslation(
            en = "Business Information Technology",
            de = "Wirtschaftsinformatik"
        ),
        "gazdasaginformatika" to ProgramTranslation(
            en = "Business Information Technology",
            de = "Wirtschaftsinformatik"
        ),
        "uzemmernok-informatikus" to ProgramTranslation(
            en = "Computer Operational Engineering (BProf)",
            de = "Betriebsinformatik (BProf)"
        ),
        "adattudomany" to ProgramTranslation(
            en = "Data Science",
            de = "Data Science / Datenwissenschaft"
        ),

        // Engineering
        "villamosmernoki" to ProgramTranslation(
            en = "Electrical Engineering",
            de = "Elektrotechnik"
        ),
        "villamosmernok" to ProgramTranslation(
            en = "Electrical Engineering",
            de = "Elektrotechnik"
        ),
        "gepeszmernoki" to ProgramTranslation(
            en = "Mechanical Engineering",
            de = "Maschinenbau"
        ),
        "gepeszmernok" to ProgramTranslation(
            en = "Mechanical Engineering",
            de = "Maschinenbau"
        ),
        "epitomernoki" to ProgramTranslation(
            en = "Civil Engineering",
            de = "Bauingenieurwesen"
        ),
        "epitomernok" to ProgramTranslation(
            en = "Civil Engineering",
            de = "Bauingenieurwesen"
        ),
        "epiteszmernoki" to ProgramTranslation(
            en = "Architecture",
            de = "Architektur"
        ),
        "epiteszmernok" to ProgramTranslation(
            en = "Architecture",
            de = "Architektur"
        ),
        "mechatronikai mernoki" to ProgramTranslation(
            en = "Mechatronics Engineering",
            de = "Mechatronik"
        ),
        "mechatronikai mernok" to ProgramTranslation(
            en = "Mechatronics Engineering",
            de = "Mechatronik"
        ),
        "jarmumernoki" to ProgramTranslation(
            en = "Vehicle Engineering",
            de = "Fahrzeugtechnik"
        ),
        "jarmumernok" to ProgramTranslation(
            en = "Vehicle Engineering",
            de = "Fahrzeugtechnik"
        ),
        "kornyezetmernoki" to ProgramTranslation(
            en = "Environmental Engineering",
            de = "Umweltingenieurwesen"
        ),
        "vegyeszmernoki" to ProgramTranslation(
            en = "Chemical Engineering",
            de = "Chemieingenieurwesen"
        ),
        "biomernoki" to ProgramTranslation(
            en = "Bioengineering",
            de = "Bioingenieurwesen"
        ),
        "energetikai mernoki" to ProgramTranslation(
            en = "Energy Engineering",
            de = "Energietechnik"
        ),
        "logisztikai mernoki" to ProgramTranslation(
            en = "Logistics Engineering",
            de = "Logistikmanagement / Logistikingenieurwesen"
        ),
        "muszaki menedzser" to ProgramTranslation(
            en = "Engineering Management",
            de = "Wirtschaftsingenieurwesen"
        ),
        "ipari termek- es formatervezo mernoki" to ProgramTranslation(
            en = "Industrial Design Engineering",
            de = "Industriedesign"
        ),
        "molekularis bionika mernoki" to ProgramTranslation(
            en = "Molecular Bionics Engineering",
            de = "Molekulare Bionik"
        ),

        // Economics & Business
        "gazdalkodasi es menedzsment" to ProgramTranslation(
            en = "Business Administration and Management",
            de = "Betriebswirtschaftslehre"
        ),
        "penzugy es szamvitel" to ProgramTranslation(
            en = "Finance and Accounting",
            de = "Finanz- und Rechnungswesen"
        ),
        "kereskedelem es marketing" to ProgramTranslation(
            en = "Commerce and Marketing",
            de = "Handel und Marketing"
        ),
        "nemzetkozi gazdalkodas" to ProgramTranslation(
            en = "International Business",
            de = "Internationale Wirtschaft"
        ),
        "emberi eroforrasok" to ProgramTranslation(
            en = "Human Resources",
            de = "Personalmanagement"
        ),
        "turizmus-vendeglatas" to ProgramTranslation(
            en = "Tourism and Catering",
            de = "Tourismus und Gastgewerbe"
        ),
        "alkalmazott kozgazdasagtan" to ProgramTranslation(
            en = "Applied Economics",
            de = "Angewandte Volkswirtschaftslehre"
        ),
        "gazdasagelemzes" to ProgramTranslation(
            en = "Economic Analysis",
            de = "Wirtschaftsanalyse"
        ),

        // Medicine & Health Sciences
        "altalanos orvos" to ProgramTranslation(
            en = "General Medicine (MD)",
            de = "Humanmedizin"
        ),
        "altalanos orvostudomany" to ProgramTranslation(
            en = "General Medicine (MD)",
            de = "Humanmedizin"
        ),
        "fogorvos" to ProgramTranslation(
            en = "Dentistry (DMD)",
            de = "Zahnmedizin"
        ),
        "fogorvostudomany" to ProgramTranslation(
            en = "Dentistry (DMD)",
            de = "Zahnmedizin"
        ),
        "gyogyszeresz" to ProgramTranslation(
            en = "Pharmacy (PharmD)",
            de = "Pharmazie"
        ),
        "gyogyszeresztudomany" to ProgramTranslation(
            en = "Pharmacy (PharmD)",
            de = "Pharmazie"
        ),
        "apolas es betegellatas" to ProgramTranslation(
            en = "Nursing and Patient Care",
            de = "Krankenpflege und Patientenbetreuung"
        ),
        "egeszsegugyi gondozas es prevencio" to ProgramTranslation(
            en = "Health Care and Prevention",
            de = "Gesundheitsvorsorge und Prävention"
        ),
        "orvosi diagnosztikai analitikus" to ProgramTranslation(
            en = "Medical Diagnostic Analyst",
            de = "Medizinisch-diagnostische Analytik"
        ),

        // Law & Social Sciences
        "jogasz" to ProgramTranslation(
            en = "Law (LL.B / LL.M)",
            de = "Rechtswissenschaften"
        ),
        "igazsagugyi igazgatasi" to ProgramTranslation(
            en = "Judicial Administration",
            de = "Justizverwaltung"
        ),
        "pszichologia" to ProgramTranslation(
            en = "Psychology",
            de = "Psychologie"
        ),
        "szociologia" to ProgramTranslation(
            en = "Sociology",
            de = "Soziologie"
        ),
        "politologia" to ProgramTranslation(
            en = "Political Science",
            de = "Politikwissenschaft"
        ),
        "nemzetkozi tanulmanyok" to ProgramTranslation(
            en = "International Relations",
            de = "Internationale Beziehungen"
        ),
        "kommunikacio- es mediatudomany" to ProgramTranslation(
            en = "Communication and Media Science",
            de = "Kommunikations- und Medienwissenschaft"
        ),
        "kommunikacio es mediatudomany" to ProgramTranslation(
            en = "Communication and Media Science",
            de = "Kommunikations- und Medienwissenschaft"
        ),

        // Humanities
        "anglisztika" to ProgramTranslation(
            en = "English Studies",
            de = "Anglistik"
        ),
        "germanisztika" to ProgramTranslation(
            en = "German Studies",
            de = "Germanistik"
        ),
        "magyar" to ProgramTranslation(
            en = "Hungarian Studies / Linguistics",
            de = "Hungarologie / Linguistik"
        ),
        "tortenelem" to ProgramTranslation(
            en = "History",
            de = "Geschichte"
        ),
        "filozofia" to ProgramTranslation(
            en = "Philosophy",
            de = "Philosophie"
        ),
        "szabad bolcseszet" to ProgramTranslation(
            en = "Liberal Arts",
            de = "Freie Geisteswissenschaften"
        ),

        // Natural Sciences
        "biologia" to ProgramTranslation(
            en = "Biology",
            de = "Biologie"
        ),
        "kemia" to ProgramTranslation(
            en = "Chemistry",
            de = "Chemie"
        ),
        "fizika" to ProgramTranslation(
            en = "Physics",
            de = "Physik"
        ),
        "matematika" to ProgramTranslation(
            en = "Mathematics",
            de = "Mathematik"
        ),
        "foldrajz" to ProgramTranslation(
            en = "Geography",
            de = "Geographie"
        ),
        "foldtudomanyi" to ProgramTranslation(
            en = "Earth Sciences",
            de = "Geowissenschaften"
        ),
        "kornyezettan" to ProgramTranslation(
            en = "Environmental Science",
            de = "Umweltwissenschaften"
        ),

        // Generic / Fallbacks
        "egyetemi kepzes" to ProgramTranslation(
            en = "University Program",
            de = "Universitätsstudium"
        ),
        "egyetemi hallgato" to ProgramTranslation(
            en = "University Student",
            de = "Student"
        ),
        "alapkepzes" to ProgramTranslation(
            en = "Bachelor's Program",
            de = "Bachelorstudium"
        ),
        "mesterkepzes" to ProgramTranslation(
            en = "Master's Program",
            de = "Masterstudium"
        ),
        "osztatlan kepzes" to ProgramTranslation(
            en = "One-tier Master's Program",
            de = "Diplomstudium (ungeteilt)"
        ),
        "doktori kepzes" to ProgramTranslation(
            en = "Doctoral Program (PhD)",
            de = "Doktoratsstudium (PhD)"
        )
    )

    fun localizeProgramName(rawProgram: String?, languageCode: String): String {
        if (rawProgram.isNullOrBlank()) return ""
        val trimmed = rawProgram.trim()
        if (languageCode == "hu") return trimmed

        // Extract degree suffix (e.g., " BSc", " MSc", " BA", " MA", " PhD", " osztatlan")
        val (baseName, suffix) = extractDegreeSuffix(trimmed)
        val normalizedBase = normalizeKey(baseName)

        // 1. Direct match on normalized base
        programTranslations[normalizedBase]?.let { translation ->
            val transText = if (languageCode == "de") translation.de else translation.en
            return combineWithSuffix(transText, suffix)
        }

        // 2. Contains match
        for ((key, translation) in programTranslations) {
            if (key.length >= 6 && normalizedBase.contains(key)) {
                val transText = if (languageCode == "de") translation.de else translation.en
                return combineWithSuffix(transText, suffix)
            }
        }

        // 3. Fallback translation of generic parts
        return fallbackTranslateProgram(trimmed, suffix, languageCode)
    }

    private fun extractDegreeSuffix(raw: String): Pair<String, String> {
        val patterns = listOf(" BSc", " MSc", " BA", " MA", " PhD", " BProf", " FSZ", " FOSZK")
        for (pat in patterns) {
            if (raw.endsWith(pat, ignoreCase = true)) {
                return Pair(raw.dropLast(pat.length).trim(), pat.trim())
            }
        }
        return Pair(raw, "")
    }

    private fun combineWithSuffix(translatedBase: String, suffix: String): String {
        return if (suffix.isNotBlank() && !translatedBase.contains(suffix, ignoreCase = true)) {
            "$translatedBase $suffix"
        } else {
            translatedBase
        }
    }

    private fun fallbackTranslateProgram(raw: String, suffix: String, languageCode: String): String {
        var text = raw
        if (languageCode == "en") {
            text = text
                .replace("mérnöki", "Engineering", ignoreCase = true)
                .replace("mérnök", "Engineering", ignoreCase = true)
                .replace("informatikus", "Computer Science", ignoreCase = true)
                .replace("informatika", "Computer Science", ignoreCase = true)
                .replace("alapképzés", "Bachelor's Program", ignoreCase = true)
                .replace("mesterképzés", "Master's Program", ignoreCase = true)
                .replace("egyetemi képzés", "University Program", ignoreCase = true)
        } else if (languageCode == "de") {
            text = text
                .replace("mérnöki", "Ingenieurwesen", ignoreCase = true)
                .replace("mérnök", "Ingenieurwesen", ignoreCase = true)
                .replace("informatikus", "Informatik", ignoreCase = true)
                .replace("informatika", "Informatik", ignoreCase = true)
                .replace("alapképzés", "Bachelorstudium", ignoreCase = true)
                .replace("mesterképzés", "Masterstudium", ignoreCase = true)
                .replace("egyetemi képzés", "Universitätsstudium", ignoreCase = true)
        }
        return combineWithSuffix(text, suffix)
    }

    // =========================================================================
    // UTILS
    // =========================================================================

    private fun normalizeKey(str: String): String {
        var s = str.lowercase().trim()
        val diacritics = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ö' to 'o', 'ő' to 'o',
            'ú' to 'u', 'ü' to 'u', 'ű' to 'u', 'ä' to 'a', 'ß' to "ss"
        )
        val sb = StringBuilder()
        for (c in s) {
            val replacement = diacritics[c]
            if (replacement != null) {
                sb.append(replacement)
            } else {
                sb.append(c)
            }
        }
        return sb.toString().replace(Regex("[^a-z0-9\\s-]"), " ").replace(Regex("\\s+"), " ").trim()
    }
}
