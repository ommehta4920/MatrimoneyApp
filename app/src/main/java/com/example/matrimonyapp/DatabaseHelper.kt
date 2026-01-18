package com.example.matrimonyapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "matrimony.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE Users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                email TEXT UNIQUE,
                phone_number TEXT,
                gender TEXT CHECK(gender IN ('Male','Female','Other')),
                date_of_birth TEXT,
                profile_picture TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_verified INTEGER DEFAULT 0,
                is_blocked INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Profiles (
                profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                height INTEGER,
                weight INTEGER,
                religion TEXT,
                caste TEXT,
                location TEXT,
                education TEXT,
                occupation TEXT,
                bio TEXT,
                FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Preferences (
                preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                preferred_gender TEXT CHECK(preferred_gender IN ('Male','Female','Other')),
                min_age INTEGER,
                max_age INTEGER,
                min_height INTEGER,
                max_height INTEGER,
                religion TEXT,
                caste TEXT,
                FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Matches (
                match_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id_1 INTEGER,
                user_id_2 INTEGER,
                match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status TEXT CHECK(status IN ('Pending','Accepted','Rejected')) DEFAULT 'Pending',
                UNIQUE(user_id_1, user_id_2),
                FOREIGN KEY(user_id_1) REFERENCES Users(user_id) ON DELETE CASCADE,
                FOREIGN KEY(user_id_2) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Messages (
                message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                match_id INTEGER,
                sender_id INTEGER,
                receiver_id INTEGER,
                message_content TEXT,
                sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_read INTEGER DEFAULT 0,
                FOREIGN KEY(match_id) REFERENCES Matches(match_id) ON DELETE CASCADE,
                FOREIGN KEY(sender_id) REFERENCES Users(user_id),
                FOREIGN KEY(receiver_id) REFERENCES Users(user_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Interests (
                interest_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                interest TEXT,
                UNIQUE(user_id, interest),
                FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE UserPhotos (
                photo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                photo_path TEXT,
                uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UserPhotos")
        db.execSQL("DROP TABLE IF EXISTS Interests")
        db.execSQL("DROP TABLE IF EXISTS Messages")
        db.execSQL("DROP TABLE IF EXISTS Matches")
        db.execSQL("DROP TABLE IF EXISTS Preferences")
        db.execSQL("DROP TABLE IF EXISTS Profiles")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)
    }

    // helper function to format the date
    private fun formatDateToYyyyMmDd(dateString: String): String {
        // List of possible date formats the user might enter
        val possibleFormats = listOf(
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("d-M-yyyy", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("d/M/yyyy", Locale.US),
            SimpleDateFormat("MM-dd-yyyy", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US) // Already in correct format
        )

        // The target format we want to store in the database
        val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (format in possibleFormats) {
            try {
                val date = format.parse(dateString)
                if (date != null) {
                    return targetFormat.format(date) // Success! Return the correctly formatted string.
                }
            } catch (e: Exception) {
                // Ignore and try the next format
            }
        }

        return dateString // If no format matches, return the original string as a fallback
    }

    // Check the email or phone number already exist at the time of registration
    fun isEmailOrPhoneExists(email: String, phone: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT user_id FROM Users WHERE email=? OR phone_number=?",
            arrayOf(email, phone)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getUsername(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT username FROM Users WHERE user_id=?", arrayOf(userId.toString()))
        val name = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return name
    }

    // ---------- Users ----------
    fun insertUser(username: String, password: String, email: String, phone: String, gender: String, dob: String): Long {

        // Call the new helper function to standardize the date format
        val formattedDob = formatDateToYyyyMmDd(dob)

        val cv = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("email", email)
            put("phone_number", phone)
            put("gender", gender)
            put("date_of_birth", formattedDob) // formatted date
        }
        return writableDatabase.insert("Users", null, cv)
    }

    fun updateUserProfilePic(userId: Int, path: String): Int {
        val cv = ContentValues().apply { put("profile_picture", path) }
        return writableDatabase.update("Users", cv, "user_id=?", arrayOf(userId.toString()))
    }

    fun verifyUser(username: String): Int {
        val cv = ContentValues().apply { put("is_verified", 1) }
        return writableDatabase.update("Users", cv, "username=?", arrayOf(username))
    }

    fun blockUser(userId: Int, block: Boolean): Int {
        val cv = ContentValues().apply { put("is_blocked", if (block) 1 else 0) }
        return writableDatabase.update("Users", cv, "user_id=?", arrayOf(userId.toString()))
    }

    fun checkUserLogin(emailOrPhone: String, password: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("""
        SELECT user_id FROM Users
        WHERE (email=? OR phone_number=?)
          AND password=? AND is_verified=1 AND is_blocked=0
    """.trimIndent(), arrayOf(emailOrPhone, emailOrPhone, password))
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return id
    }

    fun getAllUsers(): Cursor =
        readableDatabase.rawQuery("SELECT user_id, username, email, is_verified, is_blocked FROM Users ORDER BY created_at DESC", null)

    // ---------- Profiles ----------
    fun upsertProfile(userId: Int, height: Int, weight: Int, religion: String, caste: String, location: String, education: String, occupation: String, bio: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("height", height)
            put("weight", weight)
            put("religion", religion)
            put("caste", caste)
            put("location", location)
            put("education", education)
            put("occupation", occupation)
            put("bio", bio)
        }
        val exists = readableDatabase.rawQuery("SELECT profile_id FROM Profiles WHERE user_id=?", arrayOf(userId.toString()))
        val res = if (exists.moveToFirst()) {
            db.update("Profiles", cv, "user_id=?", arrayOf(userId.toString())).toLong()
        } else {
            db.insert("Profiles", null, cv)
        }
        exists.close()
        return res
    }

    fun getProfile(userId: Int): ProfileDetails? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT u.user_id, u.username, u.profile_picture, u.date_of_birth, 
               u.email, u.phone_number,
               p.height, p.weight, p.religion, p.caste, p.location, 
               p.education, p.occupation, p.bio
        FROM Users u
        LEFT JOIN Profiles p ON u.user_id = p.user_id
        WHERE u.user_id = ?
        """.trimIndent(),
            arrayOf(userId.toString())
        )

        var profile: ProfileDetails? = null
        if (cursor.moveToFirst()) {
            profile = ProfileDetails(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                profilePicture = cursor.getString(cursor.getColumnIndexOrThrow("profile_picture")),
                dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                // use the extension that accepts column name
                height = cursor.getIntOrNull("height"),
                weight = cursor.getIntOrNull("weight"),
                religion = cursor.getString(cursor.getColumnIndexOrThrow("religion")),
                caste = cursor.getString(cursor.getColumnIndexOrThrow("caste")),
                location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                education = cursor.getString(cursor.getColumnIndexOrThrow("education")),
                occupation = cursor.getString(cursor.getColumnIndexOrThrow("occupation")),
                bio = cursor.getString(cursor.getColumnIndexOrThrow("bio"))
            )
        }
        cursor.close()
        return profile
    }

    fun hasUserProfile(userId: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT profile_id FROM Profiles WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE user_id = ?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("username")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                cursor.getString(cursor.getColumnIndexOrThrow("profile_picture"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun updateUser(userId: Int, username: String, email: String, phone: String, profilePic: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("email", email)
            put("phone_number", phone)
            put("profile_picture", profilePic)
        }
        val rows = db.update("users", values, "user_id=?", arrayOf(userId.toString()))
        return rows > 0
    }

    fun insertUserPhoto(userId: Int, photoPath: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("photo_path", photoPath)
            put("uploaded_at", System.currentTimeMillis())
        }
        return db.insert("UserPhotos", null, values)
    }



    // ---------- Preferences ----------
    fun upsertPreferences(userId: Int, gender: String, minAge: Int, maxAge: Int, minH: Int, maxH: Int, religion: String, caste: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("preferred_gender", gender)
            put("min_age", minAge)
            put("max_age", maxAge)
            put("min_height", minH)
            put("max_height", maxH)
            put("religion", religion)
            put("caste", caste)
        }
        val cur = readableDatabase.rawQuery("SELECT preference_id FROM Preferences WHERE user_id=?", arrayOf(userId.toString()))
        val res = if (cur.moveToFirst()) {
            db.update("Preferences", cv, "user_id=?", arrayOf(userId.toString())).toLong()
        } else {
            db.insert("Preferences", null, cv)
        }
        cur.close()
        return res
    }

    fun getUser(userId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM Users WHERE user_id = ?", arrayOf(userId.toString()))
    }

    // ---------- Photos ----------
    fun addPhoto(userId: Int, path: String): Long {
        val cv = ContentValues().apply { put("user_id", userId); put("photo_path", path) }
        return writableDatabase.insert("UserPhotos", null, cv)
    }
    fun updateUserProfilePicture(userId: Int, path: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("profile_picture", path)
        }
        db.update("Users", values, "user_id=?", arrayOf(userId.toString()))
    }

    fun addUserPhoto(userId: Int, photoPath: String) {
        val db = this.writableDatabase

        // Insert into UserPhotos table
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("photo_path", photoPath)
        }
        db.insert("UserPhotos", null, cv)

        // If profile_picture in Users is empty, set this as the main profile picture
        val cursor = db.rawQuery("SELECT profile_picture FROM Users WHERE user_id=?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            val existingPic = cursor.getString(0)
            if (existingPic.isNullOrBlank()) {
                val cv2 = ContentValues().apply {
                    put("profile_picture", photoPath)
                }
                db.update("Users", cv2, "user_id=?", arrayOf(userId.toString()))
            }
        }
        cursor.close()
    }


    fun getUserPhotos(userId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT photo_path FROM UserPhotos WHERE user_id=?",
            arrayOf(userId.toString())
        )
    }

    fun deleteUserPhoto(photoId: Int) {
        val db = this.writableDatabase
        db.delete("UserPhotos", "photo_id=?", arrayOf(photoId.toString()))
    }

    // ---------- Interests ----------
    fun addInterest(userId: Int, interest: String): Long {
        val cv = ContentValues().apply { put("user_id", userId); put("interest", interest.trim()) }
        return writableDatabase.insertWithOnConflict("Interests", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun removeInterest(userId: Int, interest: String): Int =
        writableDatabase.delete("Interests", "user_id=? AND interest=?", arrayOf(userId.toString(), interest))

    fun getInterests(userId: Int): Cursor =
        readableDatabase.rawQuery("SELECT interest_id, interest FROM Interests WHERE user_id=? ORDER BY interest", arrayOf(userId.toString()))

    // ---------- Matching ----------
    /** Call when userId likes targetId. If reverse already exists, keep 'Pending'. */
    fun likeUser(userId: Int, targetId: Int) {
        val db = writableDatabase
        // Normalize pair to keep single direction uniqueness logic
        val cv = ContentValues().apply {
            put("user_id_1", userId)
            put("user_id_2", targetId)
            put("status", "Pending")
        }
        db.insertWithOnConflict("Matches", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun setMatchStatus(matchId: Int, status: String): Int {
        val cv = ContentValues().apply { put("status", status) }
        return writableDatabase.update("Matches", cv, "match_id=?", arrayOf(matchId.toString()))
    }

        fun getUserMatches(userId: Int): Cursor =
            readableDatabase.rawQuery("""
                SELECT m.match_id, m.user_id_1, m.user_id_2, m.status,
                       u1.username AS u1name, u2.username AS u2name,
                       u1.profile_picture AS u1pic, u2.profile_picture AS u2pic
                FROM Matches m
                LEFT JOIN Users u1 ON m.user_id_1 = u1.user_id
                LEFT JOIN Users u2 ON m.user_id_2 = u2.user_id
                WHERE m.user_id_1=? OR m.user_id_2=?
                ORDER BY m.match_date DESC
            """.trimIndent(), arrayOf(userId.toString(), userId.toString()))

    // ---- Get Users according to Preferences ----
    fun getMatchesByPreferences(userId: Int): Cursor {
        val db = readableDatabase

        // Fetch preferences of this user
        val prefCursor = db.rawQuery(
            "SELECT preferred_gender, min_age, max_age, min_height, max_height, religion, caste FROM Preferences WHERE user_id=?",
            arrayOf(userId.toString())
        )

        if (!prefCursor.moveToFirst()) {
            prefCursor.close()
            // No preferences set → return empty
            return db.rawQuery("SELECT 1 WHERE 0", null)
        }

        val gender = prefCursor.getString(0)
        val minAge = prefCursor.getInt(1)
        val maxAge = prefCursor.getInt(2)
        val minHeight = prefCursor.getInt(3)
        val maxHeight = prefCursor.getInt(4)
        val religion = prefCursor.getString(5)
        val caste = prefCursor.getString(6)
        prefCursor.close()

        val args = ArrayList<String>()
        val where = StringBuilder(" WHERE u.is_verified=1 AND u.is_blocked=0 AND u.user_id != ? ")
        args.add(userId.toString())

        // Gender
        if (!gender.isNullOrBlank()) {
            where.append(" AND u.gender = ? ")
            args.add(gender)
        }

        // Age range → convert to DOB
        if (minAge > 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -minAge)
            val maxDob = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            where.append(" AND u.date_of_birth <= ? ")
            args.add(maxDob)
        }
        if (maxAge > 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -(maxAge + 1))
            val minDob = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            where.append(" AND u.date_of_birth > ? ")
            args.add(minDob)
        }

        if (minHeight > 0) {
            where.append(" AND p.height >= ? ")
            args.add(minHeight.toString())
        }
        if (maxHeight > 0) {
            where.append(" AND p.height <= ? ")
            args.add(maxHeight.toString())
        }
        if (!religion.isNullOrBlank()) {
            where.append(" AND p.religion LIKE ? ")
            args.add("%$religion%")
        }
        if (!caste.isNullOrBlank()) {
            where.append(" AND p.caste LIKE ? ")
            args.add("%$caste%")
        }

        val sql = """
        SELECT u.user_id, u.username, u.profile_picture,
               p.height, p.religion, p.caste
        FROM Users u
        LEFT JOIN Profiles p ON u.user_id = p.user_id
        $where
        ORDER BY u.created_at DESC
    """

        return db.rawQuery(sql, args.toTypedArray())
    }


    // ---------- Messaging ----------
    fun sendMessage(matchId: Int, senderId: Int, receiverId: Int, content: String): Long {
        val cv = ContentValues().apply {
            put("match_id", matchId)
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("message_content", content)
        }
        return writableDatabase.insert("Messages", null, cv)
    }

    fun getMessages(matchId: Int): Cursor =
        readableDatabase.rawQuery("""
            SELECT message_id, sender_id, receiver_id, message_content, sent_at, is_read
            FROM Messages WHERE match_id=? ORDER BY message_id ASC
        """.trimIndent(), arrayOf(matchId.toString()))

    fun markMessageAsRead(messageId: Int): Int {
        val cv = ContentValues().apply { put("is_read", 1) }
        return writableDatabase.update("Messages", cv, "message_id=?", arrayOf(messageId.toString()))
    }

    // ---------- Search ----------
    /** Manual search using filters; empty strings mean no filter. */
    fun searchProfiles(
        currentUserId: Int,
        minAge: Int?, maxAge: Int?,
        minHeight: Int?, maxHeight: Int?,
        gender: String?, religion: String?, caste: String?
    ): Cursor {
        val args = ArrayList<String>()
        val where = StringBuilder(" WHERE Users.is_verified=1 AND Users.is_blocked=0 ")

        // Exclude the logged-in user
        where.append(" AND Users.user_id != ? ")
        args.add(currentUserId.toString())

        // existing filters...
        if (minAge != null && minAge > 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -minAge)
            val maxDob = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
            where.append(" AND Users.date_of_birth <= ? ")
            args.add(maxDob)
        }
        if (maxAge != null && maxAge > 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -(maxAge + 1))
            val minDob = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
            where.append(" AND Users.date_of_birth > ? ")
            args.add(minDob)
        }
        if (minHeight != null) {
            where.append(" AND Profiles.height >= ? ")
            args.add(minHeight.toString())
        }
        if (maxHeight != null) {
            where.append(" AND Profiles.height <= ? ")
            args.add(maxHeight.toString())
        }
        if (!gender.isNullOrBlank()) {
            where.append(" AND Users.gender = ? ")
            args.add(gender)
        }
        if (!religion.isNullOrBlank()) {
            where.append(" AND Profiles.religion LIKE ? ")
            args.add("%$religion%")
        }
        if (!caste.isNullOrBlank()) {
            where.append(" AND Profiles.caste LIKE ? ")
            args.add("%$caste%")
        }

        val sql = """
    SELECT Users.user_id, Users.username, Users.profile_picture,
           Profiles.height, Profiles.religion, Profiles.caste
    FROM Users
    LEFT JOIN Profiles ON Profiles.user_id = Users.user_id
""" + where.toString() + " ORDER BY Users.created_at DESC"


        return readableDatabase.rawQuery(sql, args.toTypedArray())
    }

    fun getMatchBetweenUsers(user1: Int, user2: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM Matches WHERE " +
                    "((user_id_1 = ? AND user_id_2 = ?) OR (user_id_1 = ? AND user_id_2 = ?))",
            arrayOf(user1.toString(), user2.toString(), user2.toString(), user1.toString())
        )
    }

    // ----- Requests Page -----

    fun sendMatchRequest(senderId: Int, receiverId: Int): Boolean {
        val db = this.writableDatabase

        // First check if a record already exists
        val cursor = db.rawQuery(
            "SELECT match_id, status FROM matches WHERE " +
                    "(user_id_1 = ? AND user_id_2 = ?) OR (user_id_1 = ? AND user_id_2 = ?)",
            arrayOf(senderId.toString(), receiverId.toString(), receiverId.toString(), senderId.toString())
        )

        return if (cursor.moveToFirst()) {
            val matchId = cursor.getInt(cursor.getColumnIndexOrThrow("match_id"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            cursor.close()

            // If status is Rejected (or Accepted/Pending), update it
            val values = ContentValues().apply {
                put("status", "Pending")
                put("user_id_1", senderId)   // new sender
                put("user_id_2", receiverId)
                put("match_date", System.currentTimeMillis())
            }

            db.update("matches", values, "match_id = ?", arrayOf(matchId.toString())) > 0
        } else {
            cursor.close()

            // Insert new request if no record exists
            val values = ContentValues().apply {
                put("user_id_1", senderId)
                put("user_id_2", receiverId)
                put("status", "Pending")
                put("match_date", System.currentTimeMillis())
            }

            db.insert("matches", null, values) > 0
        }
    }

    fun Cursor.getIntOrNull(columnName: String): Int? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getInt(index)
    }
}
