package com.example.clinexusapp.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatChatTime(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return ""
            
            val now = Calendar.getInstance()
            val chatDate = Calendar.getInstance()
            chatDate.time = date
            
            if (now.get(Calendar.YEAR) == chatDate.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == chatDate.get(Calendar.DAY_OF_YEAR)) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDisplayDate(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            // Handle ISO formats like 2024-08-18T00:00:00.000Z
            val cleanString = if (dateString.contains("T")) {
                dateString.substringBefore("T")
            } else {
                dateString
            }
            
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputSdf.parse(cleanString) ?: return cleanString
            
            val outputSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            outputSdf.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDisplayTime(timeString: String?): String {
        if (timeString == null) return ""
        return try {
            // Handle formats like 09:00:00 or 09:00
            val inputSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputSdf.parse(timeString) ?: return timeString
            
            val outputSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputSdf.format(date)
        } catch (e: Exception) {
            timeString
        }
    }
}
