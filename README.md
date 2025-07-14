# 💸 PocketSafe – Personal Finance Tracker

PocketSafe is a mobile budgeting app built in Kotlin that helps users take control of their spending habits by tracking expenses, setting goals, and visualizing progress. The app is designed with a pixel-art aesthetic and intuitive user experience in mind, making finance tracking less stressful and more enjoyable.

## 📱 How It Works

PocketSafe allows users to register and log in securely. Once logged in, users can:

- Set a monthly savings goal
- Track individual expenses across various categories
- Visually monitor progress using an animated progress bar and percentage tracker
- Get notified if they exceed category-specific budgets
- View how much they’ve spent per category over time
- Export a summary of their financial activity as a downloadable PDF

The app uses Firebase Realtime Database for online data storage and retrieval, ensuring that user data is safely stored and synced across devices.

## ✨ Features

### 🔐 User Authentication
- Secure user registration and login using Firebase Authentication.
- Email and password-based login.

### 🧾 Expense Tracking
- Add expenses with details like amount, category, and optional notes.
- Categories include: Entertainment, Necessities, Sport, and Other.
- Users can also create their own custom categories.

### 🎯 Goal Setting
- Set monthly savings goals.
- View progress using a pixel-art style animated bar.
- Real-time percentage tracker showing how close the user is to meeting their goal.

### 📊 Spending Graph (Planned)
- View bar charts for expenses per category over a selected time range.
- Charts will include visual indicators for minimum and maximum spending goals.

### 🧠 Subscription Tracker (Custom Feature 1)
- Add and view recurring subscriptions (like Netflix, Spotify, etc).
- Notifications or warnings when subscriptions are due or exceed budget.

### ⏰ Bill Reminders (Custom Feature 2)
- Set and manage upcoming bill reminders.
- Keeps track of due dates and expected payment amounts.

### 📄 PDF Export
- Users can generate a PDF report summarizing their expenses, categories, and savings progress.
- The PDF includes a breakdown of spending by category and can be shared or saved for personal records.

### 🎨 Pixel Aesthetic UI
- All interface elements use custom pixel-art sprites and fonts (`pixel_game.otf`) to maintain a consistent retro look.
- Animated goal celebration plays when users reach their monthly goal.

## 🚀 Tech Stack

- **Language**: Kotlin
- **Framework**: Android SDK (Jetpack Compose & ViewBinding)
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Auth
- **PDF**: iText / PDFBox or native Kotlin PDF generation
- **GitHub Actions**: For automated builds and continuous integration

## 📁 Project Structure Overview

PocketSafe/
├── app/
│ ├── src/main/java/com/example/pocketsafe/
│ │ ├── ui/
│ │ ├── data/
│ │ ├── auth/
│ │ └── expense/
│ ├── res/
│ │ ├── layout/
│ │ ├── drawable/
│ │ ├── font/
│ │ └── values/
└── README.md
