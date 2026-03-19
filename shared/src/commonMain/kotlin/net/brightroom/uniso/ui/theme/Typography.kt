package net.brightroom.uniso.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography =
    Typography(
        // Dialog title — 16px / 500
        titleMedium =
            TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
        // Section header — 13px / 500
        titleSmall =
            TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        // Body / Menu items — 14px / 400
        bodyLarge =
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            ),
        // Body / Menu items — 13px / 400
        bodyMedium =
            TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
            ),
        // Description / Supplementary — 12px / 400
        bodySmall =
            TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            ),
        // Small labels — 12px / 400
        labelSmall =
            TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            ),
        // Avatar initials (14px avatar) — 7px / 500
        labelMedium =
            TextStyle(
                fontSize = 7.sp,
                fontWeight = FontWeight.Medium,
            ),
    )
