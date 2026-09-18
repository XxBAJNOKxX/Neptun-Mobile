package com.example.data.network

import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade

object MockNeptunDataSource {

    fun getMockCalendarEvents(): List<CalendarEvent> = listOf(
        // Monday (1)
        CalendarEvent(
            id = "cal_1",
            subjectName = "Mesterséges intelligencia",
            subjectCode = "BMEVIIIM01",
            courseCode = "EA01",
            location = "I épület",
            room = "IB025",
            teacherName = "Dr. Szabó Péter",
            startHour = 8,
            startMinute = 15,
            endHour = 10,
            endMinute = 0,
            dayOfWeek = 1,
            courseType = CourseType.LECTURE
        ),
        CalendarEvent(
            id = "cal_2",
            subjectName = "Szoftvertervezés és -architektúrák",
            subjectCode = "BMESZITM02",
            courseCode = "GY02",
            location = "I épület",
            room = "IL102",
            teacherName = "Kovács Tamás",
            startHour = 10,
            startMinute = 15,
            endHour = 12,
            endMinute = 0,
            dayOfWeek = 1,
            courseType = CourseType.PRACTICE
        ),
        CalendarEvent(
            id = "cal_3",
            subjectName = "Mobil- és felhőalkalmazások",
            subjectCode = "BMEVIHIM03",
            courseCode = "LAB01",
            location = "Q épület",
            room = "QBF11",
            teacherName = "Dr. Varga Balázs",
            startHour = 14,
            startMinute = 15,
            endHour = 16,
            endMinute = 0,
            dayOfWeek = 1,
            courseType = CourseType.LAB
        ),
        // Tuesday (2)
        CalendarEvent(
            id = "cal_4",
            subjectName = "Algoritmusok és adatszerkezetek",
            subjectCode = "BMETMTM04",
            courseCode = "EA01",
            location = "I épület",
            room = "IB028",
            teacherName = "Dr. Németh Zoltán",
            startHour = 8,
            startMinute = 15,
            endHour = 10,
            endMinute = 0,
            dayOfWeek = 2,
            courseType = CourseType.LECTURE
        ),
        CalendarEvent(
            id = "cal_5",
            subjectName = "Adatbázisok elmélete",
            subjectCode = "BMESZITM05",
            courseCode = "EA01",
            location = "Q épület",
            room = "Q-II nagyelőadó",
            teacherName = "Tóth Gábor",
            startHour = 12,
            startMinute = 15,
            endHour = 14,
            endMinute = 0,
            dayOfWeek = 2,
            courseType = CourseType.LECTURE
        ),
        // Wednesday (3)
        CalendarEvent(
            id = "cal_6",
            subjectName = "Számítógépes hálózatok",
            subjectCode = "BMEHITM06",
            courseCode = "GY03",
            location = "I épület",
            room = "IB027",
            teacherName = "Kiss Norbert",
            startHour = 10,
            startMinute = 15,
            endHour = 12,
            endMinute = 0,
            dayOfWeek = 3,
            courseType = CourseType.PRACTICE
        ),
        CalendarEvent(
            id = "cal_7",
            subjectName = "Elosztott rendszerek",
            subjectCode = "BMEAUTM07",
            courseCode = "LAB02",
            location = "I épület",
            room = "IL401",
            teacherName = "Molnár Dániel",
            startHour = 12,
            startMinute = 15,
            endHour = 14,
            endMinute = 0,
            dayOfWeek = 3,
            courseType = CourseType.LAB
        ),
        CalendarEvent(
            id = "cal_8",
            subjectName = "Rendszermodellezés és verifikáció",
            subjectCode = "BMEMITM08",
            courseCode = "EA01",
            location = "CH épület",
            room = "CH.MAX",
            teacherName = "Dr. Horváth Ákos",
            startHour = 16,
            startMinute = 15,
            endHour = 18,
            endMinute = 0,
            dayOfWeek = 3,
            courseType = CourseType.LECTURE
        ),
        // Thursday (4)
        CalendarEvent(
            id = "cal_9",
            subjectName = "Mesterséges intelligencia labor",
            subjectCode = "BMEVIIIM01L",
            courseCode = "LAB01",
            location = "I épület",
            room = "IL203",
            teacherName = "Dr. Szabó Péter",
            startHour = 8,
            startMinute = 15,
            endHour = 10,
            endMinute = 0,
            dayOfWeek = 4,
            courseType = CourseType.LAB
        ),
        CalendarEvent(
            id = "cal_10",
            subjectName = "Statisztika és valószínűségszámítás",
            subjectCode = "BMEMATM09",
            courseCode = "GY01",
            location = "K épület",
            room = "K134",
            teacherName = "Dr. Fekete András",
            startHour = 10,
            startMinute = 15,
            endHour = 12,
            endMinute = 0,
            dayOfWeek = 4,
            courseType = CourseType.PRACTICE
        ),
        // Friday (5)
        CalendarEvent(
            id = "cal_11",
            subjectName = "Kiberbiztonság alapjai",
            subjectCode = "BMECYBM10",
            courseCode = "EA01",
            location = "Q épület",
            room = "Q-I nagyelőadó",
            teacherName = "Dr. Balogh Zsolt",
            startHour = 10,
            startMinute = 15,
            endHour = 12,
            endMinute = 0,
            dayOfWeek = 5,
            courseType = CourseType.LECTURE
        )
    )

    fun getMockGrades(): List<SubjectGrade> = listOf(
        // 2025/26/1 (Aktuális félév)
        SubjectGrade(
            id = "grade_1",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Mesterséges intelligencia",
            subjectCode = "BMEVIIIM01",
            credit = 5,
            grade = null,
            gradeText = "Folyamatban",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_2",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Szoftvertervezés és -architektúrák",
            subjectCode = "BMESZITM02",
            credit = 4,
            grade = null,
            gradeText = "Folyamatban",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_3",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Mobil- és felhőalkalmazások",
            subjectCode = "BMEVIHIM03",
            credit = 4,
            grade = 5,
            gradeText = "Jeles (5)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_4",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Algoritmusok és adatszerkezetek",
            subjectCode = "BMETMTM04",
            credit = 5,
            grade = 4,
            gradeText = "Jó (4)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_5",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Számítógépes hálózatok",
            subjectCode = "BMEHITM06",
            credit = 4,
            grade = null,
            gradeText = "Még nincs jegy",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_6",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Kiberbiztonság alapjai",
            subjectCode = "BMECYBM10",
            credit = 3,
            grade = 3,
            gradeText = "Közepes (3)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_7",
            termId = "2025/26/1",
            termName = "2025/26/1 félév",
            subjectName = "Szakdolgozati konzultáció I.",
            subjectCode = "BMEDIPL01",
            credit = 5,
            grade = null,
            gradeText = "Aláírásra vár",
            isSigned = false
        ),
        // 2024/25/2 (Előző félév)
        SubjectGrade(
            id = "grade_8",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Adatbázisok",
            subjectCode = "BMESZITM05",
            credit = 5,
            grade = 5,
            gradeText = "Jeles (5)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_9",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Operációs rendszerek",
            subjectCode = "BMEOPR02",
            credit = 5,
            grade = 4,
            gradeText = "Jó (4)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_10",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Kalkulus II.",
            subjectCode = "BMEMAT02",
            credit = 6,
            grade = 3,
            gradeText = "Közepes (3)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_11",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Digitális technika II.",
            subjectCode = "BMEDIG02",
            credit = 4,
            grade = 5,
            gradeText = "Jeles (5)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_12",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Fizika II.",
            subjectCode = "BMEFIZ02",
            credit = 4,
            grade = 4,
            gradeText = "Jó (4)",
            isSigned = true
        ),
        SubjectGrade(
            id = "grade_13",
            termId = "2024/25/2",
            termName = "2024/25/2 félév",
            subjectName = "Szakmai idegen nyelv",
            subjectCode = "BMEIDEG01",
            credit = 2,
            grade = 5,
            gradeText = "Jeles (5)",
            isSigned = true
        )
    )

    fun getMockMessages(): List<NeptunMessage> = listOf(
        NeptunMessage(
            id = "msg_0",
            subject = "Kurzus órarendi változás",
            sender = "Rendszerüzenet",
            sendDate = "2026.09.08 15:49",
            previewText = "Tisztelt Hallgató! Tájékoztatjuk, hogy 'mérnökinformatikus (IANI-MI)' képzésén 2026/27/1 félévben a 'P-ITMAT-0043A-Alkalmazott analízis I.' tárgyhoz tartozó kurzus órarendi időpontjai megváltoztak...",
            bodyHtml = "<p>Tisztelt Hallgató!</p><p>Tájékoztatjuk, hogy 'mérnökinformatikus (IANI-MI)' képzésén 2026/27/1 félévben a 'P-ITMAT-0043A-Alkalmazott analízis I.' tárgyhoz tartozó '00' kódú kurzus órarendi időpontjai megváltoztak, a változások félkövérrel jelölve:</p><p><b>Aktuális órarendi információ:</b></p><table border=\"1\"><thead><tr><th>Óra eleje</th><th>Óra vége</th><th>Terem</th><th>Oktató</th></tr></thead><tbody><tr><td>2026. 09. 08. 13:15:00</td><td>2026. 09. 08. 15:00:00</td><td>ITK Simonyi ea. 006.</td><td><b>Dr. Fogarasi Norbert, Molnár Gyula</b></td></tr><tr><td>2026. 09. 09. 14:15:00</td><td>2026. 09. 09. 16:00:00</td><td>ITK Simonyi ea. 006.</td><td><b>Dr. Fogarasi Norbert, Molnár Gyula</b></td></tr><tr><td>2026. 09. 10. 10:15:00</td><td>2026. 09. 10. 12:00:00</td><td>ITK Simonyi ea. 006.</td><td><b>Dr. Fogarasi Norbert, Molnár Gyula</b></td></tr></tbody></table>",
            isRead = false,
            isOfficial = true
        ),
        NeptunMessage(
            id = "msg_1",
            subject = "Órarendi változás - Mesterséges intelligencia labor",
            sender = "Dr. Szabó Péter (Oktató)",
            sendDate = "2026.10.02 08:30",
            previewText = "Tájékoztatom a hallgatókat, hogy a csütörtöki labor az IL203 terembe lett áthelyezve...",
            bodyHtml = "<p>Tisztelt Hallgatók!</p><p>Tájékoztatom Önöket, hogy a <b>Mesterséges intelligencia laboratórium</b> (BMEVIIIM01L) csütörtöki időpontja változatlanul 08:15-kor kezdődik, azonban karbantartási munkálatok miatt a helyszín az <b>IL203</b> számú számítógépes laborba kerül áthelyezésre.</p><p>Kérem, hogy a gépekre való belépéshez készítsék elő a kari címtáras azonosítójukat!</p><p>Üdvözlettel,<br><b>Dr. Szabó Péter</b><br>egyetemi docens</p>",
            isRead = false,
            isOfficial = false
        ),
        NeptunMessage(
            id = "msg_2",
            subject = "Vizsgaidőpontok közzététele a 2025/26/1 félévre",
            sender = "Központi Tanulmányi Hivatal",
            sendDate = "2026.09.28 14:10",
            previewText = "Értesítjük a Tisztelt Hallgatókat, hogy a 2025/26/1 félév vizsgaidőpontjai rögzítésre kerültek...",
            bodyHtml = "<p>Tisztelt Hallgatók!</p><p>Értesítjük Önöket, hogy a 2025/26/1 tanév őszi félévének vizsgaidőszakára vonatkozó vizsgaalkalmakat a tanszékek meghirdették a Neptun Tanulmányi Rendszerben.</p><p>A vizsgajelentkezési időszak kezdete: <b>2026. november 25. 18:00</b>.</p><p>Kérjük, ellenőrizzék, hogy rendelkeznek-e érvényes kurzusteljesítési követelményekkel és nincs-e lejárt tartozásuk a Pénzügyek menüpontban!</p><p>KTH Igazgatóság</p>",
            isRead = false,
            isOfficial = true
        ),
        NeptunMessage(
            id = "msg_3",
            subject = "Szoftvertervezés házifeladat 2. mérföldkő határidő",
            sender = "Kovács Tamás (Gyakorlatvezető)",
            sendDate = "2026.09.25 11:45",
            previewText = "A 2. mérföldkő (Architektúra diagram és API specifikáció) leadási határideje vasárnap éjfél...",
            bodyHtml = "<p>Kedves Csapatok!</p><p>Emlékeztetőül jelzem, hogy a féléves projektfeladat <b>2. mérföldkövének</b> (C4 modell architektúra diagram és OpenAPI specifikáció) beküldési határideje <b>vasárnap 23:59</b> a GitLab repókba.</p><p>A késedelmes leadás pontlevonással jár!</p><p>Jó munkát kívánok,<br>Kovács Tamás</p>",
            isRead = true,
            isOfficial = false
        ),
        NeptunMessage(
            id = "msg_4",
            subject = "Rendszerkarbantartási értesítő (Neptun Mobile)",
            sender = "Neptun Rendszergazda",
            sendDate = "2026.09.20 09:00",
            previewText = "Tervezett karbantartás miatt a Neptun szolgáltatásai vasárnap 02:00 és 06:00 között szünetelnek...",
            bodyHtml = "<p>Értesítjük tisztelt Felhasználóinkat, hogy biztonsági frissítések és adatbázis-optimalizálás miatt <b>vasárnap hajnalban 02:00 és 06:00 között</b> a Neptun webes és mobil felületei nem lesznek elérhetők.</p><p>Köszönjük megértésüket!</p>",
            isRead = true,
            isOfficial = true
        ),
        NeptunMessage(
            id = "msg_5",
            subject = "Kollégiumi térítési díj kiírása - Október",
            sender = "Hallgatói Szolgáltató Központ",
            sendDate = "2026.09.15 16:30",
            previewText = "A 2026. október havi kollégiumi díjtétel kiírásra került a Neptun Pénzügyek modulban...",
            bodyHtml = "<p>Tisztelt Kollégista Hallgató!</p><p>Tájékoztatjuk, hogy az októberi kollégiumi lakhatási díj kiírásra került a Neptunban. Összeg: <b>18 500 Ft</b>. Befizetési határidő: <b>2026. október 15.</b></p><p>A befizetést SimplePay bankkártyás fizetéssel vagy gyűjtőszámlás átutalással rendezheti.</p>",
            isRead = true,
            isOfficial = true
        )
    )

    fun getMockFinances(): List<FinanceItem> = listOf(
        FinanceItem(
            id = "fin_1",
            title = "Kollégiumi térítési díj (Szeptember)",
            termName = "2025/26/1",
            amountHuf = 18500,
            status = FinanceStatus.COMPLETED,
            dueDate = "2026.09.15",
            paymentDate = "2026.09.12",
            transactionId = "TRX-948271"
        ),
        FinanceItem(
            id = "fin_2",
            title = "Kollégiumi térítési díj (Október)",
            termName = "2025/26/1",
            amountHuf = 18500,
            status = FinanceStatus.COMPLETED,
            dueDate = "2026.10.15",
            paymentDate = "2026.10.01",
            transactionId = "TRX-953812"
        ),
        FinanceItem(
            id = "fin_3",
            title = "Ismételt vizsgadíj - Kalkulus II.",
            termName = "2025/26/1",
            amountHuf = 4500,
            status = FinanceStatus.PENDING,
            dueDate = "2026.10.20",
            paymentDate = null,
            transactionId = "TRX-960241"
        ),
        FinanceItem(
            id = "fin_4",
            title = "Késedelmes kurzusleadási díj",
            termName = "2025/26/1",
            amountHuf = 3000,
            status = FinanceStatus.PENDING,
            dueDate = "2026.10.28",
            paymentDate = null,
            transactionId = "TRX-961190"
        ),
        FinanceItem(
            id = "fin_5",
            title = "Önköltségi képzés díja (I. részlet)",
            termName = "2025/26/1",
            amountHuf = 350000,
            status = FinanceStatus.COMPLETED,
            dueDate = "2026.09.10",
            paymentDate = "2026.09.05",
            transactionId = "TRX-941002"
        )
    )

    fun getMockExams(): List<ExamItem> {
        val today = java.time.LocalDate.now()
        val d1 = today.plusDays(2).toString()
        val d2 = today.plusDays(6).toString()
        val d3 = today.plusDays(14).toString()

        return listOf(
            ExamItem(
                id = "exam_1",
                subjectName = "Mesterséges intelligencia",
                subjectCode = "BMEVIIIM01",
                courseCode = "V1",
                examDate = d1,
                startTime = "08:30",
                room = "IB028",
                location = "I épület",
                examType = "Írásbeli kollokvium",
                isSignedUp = true,
                teacherName = "Dr. Szabó Péter",
                applicationDeadline = today.plusDays(1).toString()
            ),
            ExamItem(
                id = "exam_2",
                subjectName = "Algoritmusok és adatszerkezetek",
                subjectCode = "BMETMTM04",
                courseCode = "V2",
                examDate = d2,
                startTime = "10:00",
                room = "Q-II nagyelőadó",
                location = "Q épület",
                examType = "Írásbeli vizsga",
                isSignedUp = true,
                teacherName = "Dr. Németh Zoltán",
                applicationDeadline = today.plusDays(5).toString()
            ),
            ExamItem(
                id = "exam_3",
                subjectName = "Adatbázisok elmélete",
                subjectCode = "BMESZITM05",
                courseCode = "V1",
                examDate = d3,
                startTime = "13:00",
                room = "IL102",
                location = "I épület",
                examType = "Szóbeli kollokvium",
                isSignedUp = false,
                teacherName = "Tóth Gábor",
                applicationDeadline = today.plusDays(13).toString()
            )
        )
    }

    fun getMockDegreeProgress(): com.example.domain.model.DegreeProgress = com.example.domain.model.DegreeProgress(
        completedCredits = 138,
        totalRequiredCredits = 210,
        completedCurriculums = 2,
        totalCurriculums = 5,
        compulsoryCompleted = 96,
        compulsoryTotal = 120,
        compulsoryElectiveCompleted = 24,
        compulsoryElectiveTotal = 30,
        freeElectiveCompleted = 10,
        freeElectiveTotal = 10,
        thesisCompleted = 0,
        thesisTotal = 15,
        criteriaPassedCount = 2,
        criteriaTotalCount = 2,
        cumulativeWeightedAverage = 4.38,
        cumulativeCreditIndex = 4.22,
        templates = listOf(
            com.example.domain.model.CurriculumTemplateItem(
                id = "tmpl_1",
                name = "MI-BSc 2026",
                code = "IANI-MI-26",
                completedSubjects = 28,
                totalSubjects = 38,
                completedCredits = 96,
                totalCredits = 120,
                isCompleted = false
            ),
            com.example.domain.model.CurriculumTemplateItem(
                id = "tmpl_2",
                name = "BSc szabadon választható tárgyak 2026",
                code = "IANI-MI-26-SZABVAL",
                completedSubjects = 3,
                totalSubjects = 3,
                completedCredits = 10,
                totalCredits = 10,
                status = "Teljesített",
                isCompleted = true
            ),
            com.example.domain.model.CurriculumTemplateItem(
                id = "tmpl_3",
                name = "Egyéb szabadon választható tárgyak",
                code = "IANI-MI-EGYEB",
                completedSubjects = 2,
                totalSubjects = 2,
                completedCredits = 6,
                totalCredits = 6,
                status = "Teljesített",
                isCompleted = true
            )
        )
    )

    fun getMockAcademicPeriods(): List<com.example.domain.model.AcademicPeriod> {
        val today = java.time.LocalDate.now()
        return listOf(
            com.example.domain.model.AcademicPeriod(
                id = "per_1",
                name = "Végleges kurzusjelentkezési időszak",
                startDate = today.minusDays(5).toString(),
                endDate = today.plusDays(3).toString(),
                type = "COURSE_REG",
                isActive = true
            ),
            com.example.domain.model.AcademicPeriod(
                id = "per_2",
                name = "Féléves bejelentkezési időszak",
                startDate = today.minusDays(14).toString(),
                endDate = today.plusDays(7).toString(),
                type = "REGISTRATION",
                isActive = true
            ),
            com.example.domain.model.AcademicPeriod(
                id = "per_3",
                name = "Térítési díjak befizetési határideje",
                startDate = today.minusDays(10).toString(),
                endDate = today.plusDays(12).toString(),
                type = "FINANCE",
                isActive = true
            ),
            com.example.domain.model.AcademicPeriod(
                id = "per_4",
                name = "Vizsgaidőszak",
                startDate = today.plusDays(45).toString(),
                endDate = today.plusDays(85).toString(),
                type = "EXAM",
                isActive = false
            )
        )
    }
}
