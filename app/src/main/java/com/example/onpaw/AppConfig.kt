package com.example.onpaw

/**
 * App configuration flags.
 * Set USE_FIREBASE to true to enable Firebase backend.
 * When false, the app uses fake/mock data for all operations.
 */
object AppConfig {
    /**
     * Toggle between Firebase (true) and fake data mode (false).
     * Set to false for demo/prototype with fake data.
     * Set to true to use real Firebase backend.
     */
    const val USE_FIREBASE = false
}

