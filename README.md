# MinimalLauncher

*MinimalLauncher* is a hyper-minimalist, text-based home screen replacement for Android. It was built to declutter your digital life and look good on OLED screens.

> 🧪 *Experiment:* This project is *vibe coded*. It was built purely on vibes to see how to build Android interface. It is not enterprise-grade software—it's a personal experiment in aesthetics and utility.

## ✨ Features

* *OLED Friendly:* Pure black background (#000000) for maximum contrast and battery savings.
* *Vertical Home Screen:* Pinned apps are displayed in a clean, left-aligned vertical list.
* *Gesture Controls:*
    * *Swipe Left:* Open the full App Drawer.
    * *Swipe Down:* Open the Notification Panel.
    * *Double Tap:* Lock the screen.
* *Smart App Drawer:* Includes a fast search bar to find apps instantly.
* *Essential Actions:* Long-press any app for a context menu:
    * Add/Remove from Home.
    * App Info.
    * *Popup View* (Supports Android Freeform windows).

## 📥 Installation

1.  Go to the [*Releases*](../../releases) page of this repository.
2.  Download the latest MinimalLauncher.apk.
3.  Install the file on your Android device.
4.  Go to *Settings > Apps > Default Apps > Home App* and select *MinimalLauncher*.

## ⚙ Setup & Permissions

To keep the interface clean, this launcher relies on *Accessibility Services* for gestures. It does *not* track you or access the internet.

1.  *Lock Screen & Notifications:*
    * When you first *Double Tap* or *Swipe Down*, the app will ask to enable the Accessibility Service.
    * Find *"Launcher Lock (Fingerprint Safe)"* in the list and turn it *ON*.
    * Note: We use this specific method so that your biometrics (Fingerprint/Face Unlock) continue to work after locking.

## 🛠 Build It Yourself

This project is built with Kotlin and XML.

1.  Clone the repository.
2.  Open in *Android Studio*.
3.  Build > *Build APK(s)*.

## 📄 License

This project is a personal experiment. Feel free to fork it, mod it, or use the code to build your own vibe-coded launcher.
