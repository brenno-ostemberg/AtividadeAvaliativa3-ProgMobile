plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.atividadeavaliativa2_progmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.atividadeavaliativa2_progmobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Adicionado para Java 8+ features (necessário para algumas bibliotecas e APIs modernas)
        // e para garantir compatibilidade com o Room.
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true" // Habilita processamento incremental
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Se você estiver usando Kotlin no seu projeto (mesmo que apenas para buildSrc ou testes),
    // esta seção seria necessária. Se seu código de app é 100% Java, ela pode não ser
    // estritamente necessária, mas não prejudica.
    // kotlinOptions {
    //     jvmTarget = "11"
    // }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Dependências do Room
    // Você já tinha estas, o que é bom:
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)

    // ---- ADIÇÃO CRÍTICA AQUI ----
    // Esta é a linha que faltava para o processador de anotações do Room.
    // Ele é responsável por gerar AppDatabase_Impl.
    // Assumindo que você tem uma entrada como 'room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }'
    // no seu arquivo libs.versions.toml.
    // Se 'libs.room.compiler' não estiver definido no seu libs.versions.toml, veja a nota abaixo.
    annotationProcessor(libs.room.compiler) // Geralmente androidx.room:room-compiler

    // Dependências de Teste
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}