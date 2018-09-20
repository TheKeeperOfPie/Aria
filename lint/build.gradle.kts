plugins {
    `java-library`
    id("kotlin")
}

val lintVersion = "26.3.0-alpha11"

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")

    testCompile("junit:junit:4.12")
    testCompile("com.android.tools.lint:lint:$lintVersion")
    testCompile("com.android.tools.lint:lint-tests:$lintVersion")
    testCompile("com.android.tools:testutils:$lintVersion")
}
