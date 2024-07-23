val groupSuffix = name

subprojects {
    group = "${rootProject.group}.$groupSuffix"
}