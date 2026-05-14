// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.ksp) apply false  // version = 2.0.21-1.0.27
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false}