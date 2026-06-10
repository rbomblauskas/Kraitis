# Kraitis

Kraitis *(Lithuanian for trosseau, wardrobe)* is a local-first Android app for wardrobe auditing, written in Kotlin and Jetpack Compose.

## Table of Contents
- [How to build](#how-to-build)
- [Features](#features)
- [How recommendations work](#how-recommendations-work)
- [Roadmap](#roadmap)
- [Showcase and screenshots](#showcase-and-screenshots)
- [Reflection](#reflection)

## How to build
- Open this folder in Android Studio
- Let Gradle sync (it may take a bit on first open, as usual)
- Run the app on an emulator or physical Android device
- Min SDK is 24

The app stores everything locally with Room, so right now there is no backend link.

## Reflection
This is a learning project, so the goal was not to make the biggest possible app. I wanted something small that actually works and has a clear point.

I made it in the span of about 5 days, so some parts are definitely still a bit rough around the edges.

For most of the code I tried not using LLMs, however I resorted to them for reminding myself how a few Compose APIs are usually wired, and getting suggestions when a function was becoming too messy. I still tried to keep the final code simple enough that I understood what was going on.

I like that the recommendation part is straightforward. It doesn't try to use AI image recognition for fashion taste or look up the market price of clothes, and relies just on simple signals.

However, there is still room for improvement. I should add UI and Unit tests, customize it more to not look like a default android app, and improve item editing.
