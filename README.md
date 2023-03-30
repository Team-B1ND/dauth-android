![Frame 1](https://user-images.githubusercontent.com/100567149/228731926-4f3d285f-d876-461c-a988-ea91a8d86d89.svg)

![version](https://img.shields.io/badge/version-1.0.5-blue)

## How to use?
in your project
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

module
```gradle
dependencies {
  implementation 'com.github.Team-B1ND:dauth-android:{version}'
}
