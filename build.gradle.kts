android {
    namespace = "com.example.animeplayer" // ESSENCIAL: deve ser igual ao seu pacote
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.animeplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        viewBinding = true
    }
    // ... restante do código
}
