# Smart Pantry Manager — Reduce Food Waste

A focused Android utility application built with Java, XML, and SQLite. The Smart Pantry Manager helps users track their at-home ingredients and suggests meals based *strictly* on what they already have — eliminating food waste and unnecessary grocery runs. 

## Features

* **Strict-Matching Recipe Engine** — Suggests recipes only if the user has *every single* required ingredient in their pantry, functioning as the core business logic.
* **Pantry Inventory Management** — Full CRUD (Create, Read, Update, Delete) functionality to track ingredients, quantities, units, and categories.
* **Interactive UI & UX** — Features swipe-to-delete with a unified "Undo" snackbar, category dropdown filtering, and dynamic low-stock warnings (quantities under 2 turn red).
* **Detailed Recipe Views** — Displays perfectly formatted, step-by-step cooking instructions and bulleted ingredient lists in a dedicated Fragment.
* **Offline First** — Pre-loaded with 100 diverse recipes and entirely powered by an on-device database, requiring zero internet connection.

## Database Justification

**Database Chosen: SQLite**
SQLite was chosen as the persistence layer for this application because the Smart Pantry Manager handles structured, relational data (Pantry inventory vs. Recipe requirements). Using `SQLiteOpenHelper` ensures the app functions completely offline, providing lightning-fast, on-device querying. This is essential for the strict-matching algorithm, which needs to iterate through recipes and check them against current inventory without network latency.

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java |
| **IDE** | Android Studio |
| **Database** | SQLite (`SQLiteOpenHelper`) |
| **UI/Layout** | XML (ConstraintLayout, LinearLayout, CardView) |
| **Navigation** | Jetpack Navigation Component (`NavController`) |
| **Lists** | `RecyclerView` with Custom Adapters |

## Project Structure

```text
app/src/main/
├── java/com/DeandreNaiker/smartpantrymanager/
│   ├── MainActivity.java           # Shared page shell (Bottom Nav host)
│   ├── DatabaseHelper.java         # SQLite config, CRUD operations, & recipe seeding
│   ├── PantryFragment.java         # Inventory view, filtering, and swipe-to-delete
│   ├── RecipesFragment.java        # Core strict-matching algorithm & search
│   ├── RecipeDetailFragment.java   # Dynamic text formatting for recipe steps
│   ├── SettingsFragment.java       # Clear-all functionality
│   ├── PantryAdapter.java          # Binds database cursor to inventory UI
│   └── RecipeAdapter.java          # Binds matching recipes to suggestion UI
└── res/
    ├── layout/                     # XML UI layouts (fragments, items, dialogs)
    ├── navigation/                 # nav_graph.xml (Fragment routing)
    └── values/                     # colors.xml, themes.xml (Light/Dark mode)

```
Getting Started
Prerequisites
Android Studio (latest stable version recommended)

Android SDK (Minimum API level 24 recommended)

Git installed on your local machine

Installation & Setup
1. Clone the repository:

git clone [https://github.com/code-by-dre/Smart-Pantry-Manager.git](https://github.com/code-by-dre/Smart-Pantry-Manager.git)

2. Open the project:

Launch Android Studio.

Select File > Open and navigate to the cloned Smart-Pantry-Manager folder.

3. Sync Gradle:

Allow Android Studio to download required dependencies and sync the Gradle files. (Click "Sync Now" if prompted).

Run the Application
1. Set up an Android Virtual Device (AVD) via the Device Manager, or plug in a physical Android device via USB with USB Debugging enabled.

2. Click the green Run 'app' button (Shift + F10) in the top toolbar.

3. The app will install and launch on your device. The database will automatically seed with 100 recipes on the first run.

Customization
Recipes: Add, edit, or remove pre-loaded recipes by modifying the seedDefaultRecipes() method inside DatabaseHelper.java. (Note: Increment the DATABASE_VERSION to force an update).

Categories: Modify the dropdown categories arrays located in PantryFragment.java.

Theme: Adjust the primary brand colors (like the app's signature yellow) by editing res/values/colors.xml.

Disclaimer
This project was developed as a final-year practical assignment for the Mobile App Development 700 (MAD700) module. It represents original work and adheres to the strict-matching logic and minimum screen requirements outlined in the course rubric.

Built with
Android Studio

Java

SQLite

Material Design Components

